package com.amadiyawa.feature_notification.data.repository

import com.amadiyawa.feature_base.domain.model.UserSessionManager
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_notification.domain.model.Notification
import com.amadiyawa.feature_notification.domain.model.NotificationFilter
import com.amadiyawa.feature_notification.domain.model.NotificationPriority
import com.amadiyawa.feature_notification.domain.model.NotificationType
import com.amadiyawa.feature_notification.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

class FakeNotificationRepository(
    private val userSessionManager: UserSessionManager
) : NotificationRepository {

    private val allNotifications = MutableStateFlow<List<Notification>>(emptyList())

    init {
        // Initialize notifications based on the current user
        initializeNotifications()
    }

    private fun initializeNotifications() {
        val currentUserId = userSessionManager.currentUserId.value
        val currentRole = userSessionManager.currentRole.value

        allNotifications.value = generateFakeNotifications(currentUserId, currentRole)
    }

    override suspend fun createNotification(notification: Notification): Notification {
        val newNotification = notification.copy(
            id = UUID.randomUUID().toString(),
            createdAt = LocalDateTime.now()
        )
        allNotifications.value = listOf(newNotification) + allNotifications.value
        return newNotification
    }

    override suspend fun getNotificationById(id: String): Notification? {
        val currentUserId = userSessionManager.currentUserId.value
        return allNotifications.value.find {
            it.id == id && it.userId == currentUserId
        }
    }

    override suspend fun updateNotification(notification: Notification): Notification {
        allNotifications.value = allNotifications.value.map { n ->
            if (n.id == notification.id) notification else n
        }
        return notification
    }

    override suspend fun deleteNotification(id: String) {
        val currentUserId = userSessionManager.currentUserId.value
        allNotifications.value = allNotifications.value.filterNot {
            it.id == id && it.userId == currentUserId
        }
    }

    override fun getNotificationsByUser(userId: String): Flow<List<Notification>> {
        return allNotifications.map { notifications ->
            val currentUserId = userSessionManager.currentUserId.value
            val currentRole = userSessionManager.currentRole.value

            // If user changed or no notifications for current user, regenerate notifications
            if (notifications.isEmpty() || notifications.none { it.userId == currentUserId }) {
                initializeNotifications()
                // Return notifications for the current user after regeneration
                allNotifications.value.filter { it.userId == currentUserId }
            } else {
                // Filter notifications for the requested user and ensure they match the user's role
                notifications.filter { notification ->
                    notification.userId == userId && currentRole in notification.type.targetRoles
                }.sortedByDescending { it.createdAt }
            }
        }
    }

    override fun getUnreadNotificationsByUser(userId: String): Flow<List<Notification>> {
        return getNotificationsByUser(userId).map { notifications ->
            notifications.filter { !it.isRead }
        }
    }

    override fun getNotificationsByUser(
        userId: String,
        filter: NotificationFilter
    ): Flow<List<Notification>> {
        return getNotificationsByUser(userId).map { notifications ->
            notifications.filter { notification ->
                (filter.isRead == null || notification.isRead == filter.isRead) &&
                        (filter.type == null || notification.type == filter.type) &&
                        (filter.priority == null || notification.priority == filter.priority) &&
                        (filter.fromDate == null || !notification.createdAt.isBefore(filter.fromDate)) &&
                        (filter.toDate == null || !notification.createdAt.isAfter(filter.toDate))
            }
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        allNotifications.value = allNotifications.value.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(
                    isRead = true,
                    readAt = LocalDateTime.now()
                )
            } else {
                notification
            }
        }
    }

    override suspend fun markAllAsRead(userId: String) {
        val now = LocalDateTime.now()
        allNotifications.value = allNotifications.value.map { notification ->
            if (notification.userId == userId && !notification.isRead) {
                notification.copy(
                    isRead = true,
                    readAt = now
                )
            } else {
                notification
            }
        }
    }

    override suspend fun deleteAllForUser(userId: String) {
        allNotifications.value = allNotifications.value.filterNot { it.userId == userId }
    }

    override suspend fun getUnreadCount(userId: String): Int {
        val currentRole = userSessionManager.currentRole.value
        return allNotifications.value.count { notification ->
            notification.userId == userId &&
                    !notification.isRead &&
                    currentRole in notification.type.targetRoles
        }
    }

    override suspend fun deleteExpiredNotifications() {
        val now = LocalDateTime.now()
        allNotifications.value = allNotifications.value.filterNot { notification ->
            notification.expiresAt?.isBefore(now) == true
        }
    }

    private fun generateFakeNotifications(currentUserId: String?, currentRole: UserRole?): List<Notification> {
        if (currentUserId == null || currentRole == null) return emptyList()

        // Filter notification types based on current user role
        val notificationTypes = NotificationType.entries.filter { type ->
            currentRole in type.targetRoles
        }

        // Generate notifications with the current user ID and appropriate types
        return List(30) { index ->
            val type = notificationTypes.random()
            val createdAt = LocalDateTime.now().minusDays(Random.nextLong(0, 30))
                .minusHours(Random.nextLong(0, 23))
                .minusMinutes(Random.nextLong(0, 59))

            val isRead = Random.nextBoolean()

            createNotificationByType(
                index = index,
                userId = currentUserId, // Use the current user ID
                type = type,
                createdAt = createdAt,
                isRead = isRead
            )
        }.sortedByDescending { it.createdAt }
    }

    private fun createNotificationByType(
        index: Int,
        userId: String,
        type: NotificationType,
        createdAt: LocalDateTime,
        isRead: Boolean
    ): Notification {
        // Use a data class for better structure
        data class NotificationData(
            val title: String,
            val message: String,
            val details: String,
            val priority: NotificationPriority,
            val actionData: Map<String, String>?
        )

        val notificationData = when (type) {
            // CLIENT notifications
            NotificationType.REMAINING_BALANCE -> {
                val balance = Random.nextInt(500, 5000)
                NotificationData(
                    title = "Alerte Solde Faible",
                    message = "Votre solde restant est de $balance FCFA",
                    details = "Pensez à recharger votre compte pour éviter une interruption de service. Vous pouvez recharger via notre application mobile, nos agents agréés ou notre plateforme en ligne.",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("balance" to balance.toString(), "action" to "top_up")
                )
            }

            NotificationType.PLANNED_OUTAGE -> {
                val hours = Random.nextInt(2, 8)
                NotificationData(
                    title = "Maintenance Programmée",
                    message = "Coupure de courant prévue pour ${hours}h demain",
                    details = "Nous effectuerons des travaux de maintenance dans votre zone de 08h00 à ${8 + hours}h00. Nous nous excusons pour tout désagrément.",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("duration" to hours.toString(), "date" to "tomorrow")
                )
            }

            NotificationType.SERVICE_RESTORED -> {
                NotificationData(
                    title = "Service Rétabli",
                    message = "Le courant a été rétabli dans votre zone",
                    details = "La maintenance programmée a été terminée avec succès. Votre service électrique est maintenant pleinement opérationnel.",
                    priority = NotificationPriority.NORMAL,
                    actionData = null
                )
            }

            NotificationType.CONSUMPTION_TIPS -> {
                val tips = listOf(
                    "Éteignez les appareils non utilisés",
                    "Utilisez des ampoules LED économes",
                    "Débranchez les chargeurs non utilisés",
                    "Utilisez la lumière naturelle le jour"
                )
                val tip = tips.random()
                NotificationData(
                    title = "Conseil Économie d'Énergie",
                    message = tip,
                    details = "De petits changements dans vos habitudes quotidiennes peuvent entraîner des économies importantes sur votre facture d'électricité.",
                    priority = NotificationPriority.LOW,
                    actionData = mapOf("tip" to tip)
                )
            }

            NotificationType.OVERCONSUMPTION_ALERT -> {
                val percentage = Random.nextInt(20, 50)
                NotificationData(
                    title = "Alerte Surconsommation",
                    message = "Consommation ${percentage}% au-dessus de la moyenne",
                    details = "Votre consommation du mois actuel est significativement plus élevée que d'habitude. Vérifiez les appareils qui pourraient consommer plus d'énergie que prévu.",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("increase" to "$percentage%")
                )
            }

            NotificationType.BILL_GENERATED -> {
                val amount = Random.nextDouble(5000.0, 25000.0)
                NotificationData(
                    title = "Nouvelle Facture Disponible",
                    message = "Votre facture de ${amount.toInt()} FCFA est prête",
                    details = "Votre facture d'électricité pour ce mois a été générée. Vous pouvez la consulter et la payer via l'application ou dans un centre de paiement agréé.",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("amount" to amount.toString(), "action" to "view_bill")
                )
            }

            NotificationType.PAYMENT_RECEIVED -> {
                val amount = Random.nextDouble(5000.0, 25000.0)
                NotificationData(
                    title = "Paiement Confirmé",
                    message = "Paiement de ${amount.toInt()} FCFA reçu",
                    details = "Merci pour votre paiement. Votre compte a été crédité et votre service continuera sans interruption.",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("amount" to amount.toString())
                )
            }

            NotificationType.SERVICE_REQUEST_UPDATE -> {
                val statuses = listOf("assignée", "en cours", "résolue")
                val status = statuses.random()
                val requestId = Random.nextInt(1000, 9999)
                NotificationData(
                    title = "Mise à jour Demande",
                    message = "Votre demande de service est maintenant $status",
                    details = "Votre demande de service #SR-$requestId a été mise à jour. Vous recevrez d'autres mises à jour au fur et à mesure de l'avancement des travaux.",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("status" to status, "action" to "view_request")
                )
            }

            // AGENT notifications
            NotificationType.NEW_SERVICE_REQUEST -> {
                val requestId = "SR-${Random.nextInt(1000, 9999)}"
                NotificationData(
                    title = "Nouvelle Affectation",
                    message = "Demande de service $requestId qui vous est assignée",
                    details = "Une nouvelle demande de service vous a été assignée dans votre territoire. Priorité : Élevée. Veuillez examiner et prendre les mesures appropriées.",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("request_id" to requestId, "action" to "view_request")
                )
            }

            NotificationType.URGENT_REQUEST -> {
                val sector = Random.nextInt(1, 10)
                NotificationData(
                    title = "URGENT : Panne de Courant",
                    message = "Plusieurs clients signalent une panne",
                    details = "Plusieurs clients du secteur $sector signalent des pannes de courant. Enquête immédiate requise.",
                    priority = NotificationPriority.URGENT,
                    actionData = mapOf("sector" to sector.toString())
                )
            }

            NotificationType.WORK_SCHEDULE_UPDATE -> {
                NotificationData(
                    title = "Changement d'Horaire",
                    message = "Votre horaire de travail a été mis à jour",
                    details = "Votre horaire pour la semaine prochaine a été modifié. Veuillez vérifier les affectations mises à jour et planifier en conséquence.",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("action" to "view_schedule")
                )
            }

            NotificationType.METER_READING_REMINDER -> {
                val count = Random.nextInt(5, 15)
                NotificationData(
                    title = "Relevé de Compteur Dû",
                    message = "$count compteurs en attente dans votre zone",
                    details = "Vous avez $count relevés de compteurs programmés pour aujourd'hui. Veuillez les terminer avant la fin des heures de bureau.",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("count" to count.toString(), "action" to "view_meters")
                )
            }

            NotificationType.CLIENT_FEEDBACK -> {
                val rating = Random.nextInt(1, 6)
                NotificationData(
                    title = "Commentaire Client",
                    message = "Nouveau commentaire reçu : $rating étoiles",
                    details = "Un client a laissé un commentaire sur votre service récent. Consultez les détails pour comprendre leur expérience et améliorer votre service.",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("rating" to rating.toString(), "action" to "view_feedback")
                )
            }

            NotificationType.PERFORMANCE_REPORT -> {
                NotificationData(
                    title = "Rapport de Performance",
                    message = "Votre rapport de performance mensuel est prêt",
                    details = "Vos métriques de performance pour ce mois sont maintenant disponibles. Consultez vos statistiques et voyez les domaines d'amélioration.",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("action" to "view_performance")
                )
            }

            NotificationType.TERRITORY_CHANGE -> {
                val territories = listOf("District Nord", "District Sud", "District Est", "District Ouest")
                val newTerritory = territories.random()
                NotificationData(
                    title = "Affectation Territoire",
                    message = "Vous avez été assigné à $newTerritory",
                    details = "Votre affectation de territoire a été mise à jour. Veuillez consulter les nouvelles limites de zone et la liste des clients.",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("territory" to newTerritory, "action" to "view_territory")
                )
            }

            // ADMIN notifications
            NotificationType.SYSTEM_ALERT -> {
                val systemNames = listOf(
                    "Système de Facturation",
                    "Passerelle de Paiement",
                    "Gestion des Compteurs",
                    "Portail Client"
                )
                val system = systemNames.random()
                NotificationData(
                    title = "Alerte Système",
                    message = "$system rencontre des problèmes",
                    details = "Le système rencontre actuellement des problèmes de performance. L'équipe technique a été notifiée et enquête.",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("system" to system)
                )
            }

            NotificationType.UNRESOLVED_REQUESTS -> {
                val count = Random.nextInt(10, 50)
                NotificationData(
                    title = "Demandes en Attente",
                    message = "$count demandes de service non résolues",
                    details = "Il y a $count demandes de service en attente depuis plus de 48 heures. Veuillez examiner et assigner aux agents disponibles.",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("count" to count.toString(), "action" to "view_requests")
                )
            }

            NotificationType.AGENT_PERFORMANCE -> {
                val agentNames = listOf("Agent Dubois", "Agent Martin", "Agent Larsson", "Agent Bénard")
                val agentName = agentNames.random()
                val performance = Random.nextInt(70, 100)
                NotificationData(
                    title = "Rapport Performance Agent",
                    message = "$agentName : $performance% d'efficacité",
                    details = "Le rapport de performance mensuel pour $agentName est disponible. Consultez les métriques et donnez vos commentaires.",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("agent" to agentName, "performance" to performance.toString(), "action" to "view_agent")
                )
            }

            NotificationType.REVENUE_REPORT -> {
                val amount = Random.nextDouble(1000000.0, 5000000.0)
                NotificationData(
                    title = "Rapport Mensuel Prêt",
                    message = "Rapport de revenus : ${(amount/1000000).format(2)}M FCFA",
                    details = "Le rapport de revenus mensuel est maintenant disponible. Revenus totaux : ${amount.toInt()} FCFA. Ventilation détaillée et analyses incluses.",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("revenue" to amount.toString(), "action" to "view_report")
                )
            }

            NotificationType.MAINTENANCE_SCHEDULED -> {
                val date = java.time.LocalDate.now().plusDays(Random.nextLong(1, 7))
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                NotificationData(
                    title = "Maintenance Système",
                    message = "Maintenance programmée le ${date.format(formatter)}",
                    details = "La maintenance du système est programmée pour le ${date.format(formatter)}. Certains services peuvent être temporairement indisponibles.",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("date" to date.toString(), "action" to "view_maintenance")
                )
            }

            NotificationType.NEW_USER_REGISTERED -> {
                val userType = listOf("Client", "Agent").random()
                NotificationData(
                    title = "Nouvel Utilisateur Enregistré",
                    message = "Nouveau $userType enregistré dans le système",
                    details = "Un nouveau $userType s'est inscrit et attend l'approbation. Veuillez examiner ses informations et activer son compte.",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("user_type" to userType, "action" to "view_user")
                )
            }

            NotificationType.PAYMENT_ISSUES -> {
                val issueCount = Random.nextInt(5, 20)
                NotificationData(
                    title = "Problèmes Traitement Paiements",
                    message = "$issueCount échecs de paiement détectés",
                    details = "Plusieurs problèmes de traitement des paiements ont été détectés. Veuillez enquêter et résoudre ces problèmes rapidement.",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("issue_count" to issueCount.toString(), "action" to "view_payments")
                )
            }

            NotificationType.CAPACITY_WARNING -> {
                val percentage = Random.nextInt(80, 95)
                NotificationData(
                    title = "Avertissement Capacité Système",
                    message = "Capacité du système à $percentage%",
                    details = "La capacité du système approche des niveaux critiques. Envisagez de mettre à niveau l'infrastructure ou d'optimiser les performances.",
                    priority = NotificationPriority.URGENT,
                    actionData = mapOf("capacity" to percentage.toString(), "action" to "view_system")
                )
            }

            NotificationType.EMERGENCY_ALERT, NotificationType.SYSTEM_MAINTENANCE -> {
                NotificationData(
                    title = "Alerte d'Urgence",
                    message = "Situation d'urgence nécessite attention",
                    details = "Une situation d'urgence a été détectée qui nécessite une attention immédiate de tout le personnel.",
                    priority = NotificationPriority.URGENT,
                    actionData = mapOf("action" to "emergency_protocol")
                )
            }
        }

        return Notification(
            id = "NOTIF-${1000 + index}",
            userId = userId,
            type = type,
            title = notificationData.title,
            message = notificationData.message,
            details = notificationData.details,
            priority = notificationData.priority,
            isRead = isRead,
            createdAt = createdAt,
            readAt = if (isRead) createdAt.plusMinutes(Random.nextLong(1, 120)) else null,
            expiresAt = if (Random.nextBoolean()) createdAt.plusDays(Random.nextLong(7, 30)) else null,
            actionData = notificationData.actionData
        )
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
}