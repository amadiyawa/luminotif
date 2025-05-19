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
                val titleEn = "Low Balance Alert"
                val titleFr = "Alerte Solde Faible"
                val messageEn = "Your remaining balance is $balance FCFA"
                val messageFr = "Votre solde restant est de $balance FCFA"
                val detailsEn = "Consider topping up your account to avoid service interruption. You can recharge through our mobile app, authorized agents, or online platform."
                val detailsFr = "Pensez à recharger votre compte pour éviter une interruption de service. Vous pouvez recharger via notre application mobile, nos agents agréés ou notre plateforme en ligne."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("balance" to balance.toString(), "action" to "top_up")
                )
            }

            NotificationType.PLANNED_OUTAGE -> {
                val hours = Random.nextInt(2, 8)
                val titleEn = "Scheduled Maintenance"
                val titleFr = "Maintenance Programmée"
                val messageEn = "Power outage planned for ${hours}h tomorrow"
                val messageFr = "Coupure de courant prévue pour ${hours}h demain"
                val detailsEn = "We will be performing maintenance work in your area from 08:00 to ${8 + hours}:00. We apologize for any inconvenience."
                val detailsFr = "Nous effectuerons des travaux de maintenance dans votre zone de 08h00 à ${8 + hours}h00. Nous nous excusons pour tout désagrément."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("duration" to hours.toString(), "date" to "tomorrow")
                )
            }

            NotificationType.SERVICE_RESTORED -> {
                val titleEn = "Service Restored"
                val titleFr = "Service Rétabli"
                val messageEn = "Power has been restored in your area"
                val messageFr = "Le courant a été rétabli dans votre zone"
                val detailsEn = "The scheduled maintenance has been completed successfully. Your electricity service is now fully operational."
                val detailsFr = "La maintenance programmée a été terminée avec succès. Votre service électrique est maintenant pleinement opérationnel."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.NORMAL,
                    actionData = null
                )
            }

            NotificationType.CONSUMPTION_TIPS -> {
                val tips = listOf(
                    "Turn off appliances when not in use / Éteignez les appareils non utilisés",
                    "Use energy-efficient LED bulbs / Utilisez des ampoules LED économes",
                    "Unplug chargers when not charging / Débranchez les chargeurs non utilisés",
                    "Use natural light during the day / Utilisez la lumière naturelle le jour"
                )
                val tip = tips.random()
                val titleEn = "Energy Saving Tip"
                val titleFr = "Conseil Économie d'Énergie"
                val detailsEn = "Small changes in your daily habits can lead to significant savings on your electricity bill."
                val detailsFr = "De petits changements dans vos habitudes quotidiennes peuvent entraîner des économies importantes sur votre facture d'électricité."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = tip,
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.LOW,
                    actionData = mapOf("tip" to tip)
                )
            }

            NotificationType.OVERCONSUMPTION_ALERT -> {
                val percentage = Random.nextInt(20, 50)
                val titleEn = "High Consumption Alert"
                val titleFr = "Alerte Surconsommation"
                val messageEn = "Usage is ${percentage}% above average"
                val messageFr = "Consommation ${percentage}% au-dessus de la moyenne"
                val detailsEn = "Your current month's consumption is significantly higher than usual. Check for any appliances that might be consuming more energy than expected."
                val detailsFr = "Votre consommation du mois actuel est significativement plus élevée que d'habitude. Vérifiez les appareils qui pourraient consommer plus d'énergie que prévu."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("increase" to "$percentage%")
                )
            }

            NotificationType.BILL_GENERATED -> {
                val amount = Random.nextDouble(5000.0, 25000.0)
                val titleEn = "New Bill Available"
                val titleFr = "Nouvelle Facture Disponible"
                val messageEn = "Your bill of ${amount.toInt()} FCFA is ready"
                val messageFr = "Votre facture de ${amount.toInt()} FCFA est prête"
                val detailsEn = "Your electricity bill for this month has been generated. You can view and pay it through the app or at any authorized payment center."
                val detailsFr = "Votre facture d'électricité pour ce mois a été générée. Vous pouvez la consulter et la payer via l'application ou dans un centre de paiement agréé."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("amount" to amount.toString(), "action" to "view_bill")
                )
            }

            NotificationType.PAYMENT_RECEIVED -> {
                val amount = Random.nextDouble(5000.0, 25000.0)
                val titleEn = "Payment Confirmed"
                val titleFr = "Paiement Confirmé"
                val messageEn = "Payment of ${amount.toInt()} FCFA received"
                val messageFr = "Paiement de ${amount.toInt()} FCFA reçu"
                val detailsEn = "Thank you for your payment. Your account has been credited and your service will continue uninterrupted."
                val detailsFr = "Merci pour votre paiement. Votre compte a été crédité et votre service continuera sans interruption."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("amount" to amount.toString())
                )
            }

            NotificationType.SERVICE_REQUEST_UPDATE -> {
                val statuses = listOf(
                    "assigned / assignée",
                    "in progress / en cours",
                    "resolved / résolue"
                )
                val status = statuses.random()
                val requestId = Random.nextInt(1000, 9999)
                val titleEn = "Request Update"
                val titleFr = "Mise à jour Demande"
                val messageEn = "Your service request is now $status"
                val messageFr = "Votre demande de service est maintenant $status"
                val detailsEn = "Your service request #SR-$requestId has been updated. You will receive further updates as work progresses."
                val detailsFr = "Votre demande de service #SR-$requestId a été mise à jour. Vous recevrez d'autres mises à jour au fur et à mesure de l'avancement des travaux."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("status" to status, "action" to "view_request")
                )
            }

            // AGENT notifications
            NotificationType.NEW_SERVICE_REQUEST -> {
                val requestId = "SR-${Random.nextInt(1000, 9999)}"
                val titleEn = "New Assignment"
                val titleFr = "Nouvelle Affectation"
                val messageEn = "Service request $requestId assigned to you"
                val messageFr = "Demande de service $requestId qui vous est assignée"
                val detailsEn = "A new service request has been assigned to you in your territory. Priority: High. Please review and take appropriate action."
                val detailsFr = "Une nouvelle demande de service vous a été assignée dans votre territoire. Priorité : Élevée. Veuillez examiner et prendre les mesures appropriées."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("request_id" to requestId, "action" to "view_request")
                )
            }

            NotificationType.URGENT_REQUEST -> {
                val sector = Random.nextInt(1, 10)
                val titleEn = "URGENT: Power Outage"
                val titleFr = "URGENT : Panne de Courant"
                val messageEn = "Multiple customers reporting outage"
                val messageFr = "Plusieurs clients signalent une panne"
                val detailsEn = "Several customers in sector $sector are reporting power outages. Immediate investigation required."
                val detailsFr = "Plusieurs clients du secteur $sector signalent des pannes de courant. Enquête immédiate requise."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.URGENT,
                    actionData = mapOf("sector" to sector.toString())
                )
            }

            NotificationType.WORK_SCHEDULE_UPDATE -> {
                val titleEn = "Schedule Change"
                val titleFr = "Changement d'Horaire"
                val messageEn = "Your work schedule has been updated"
                val messageFr = "Votre horaire de travail a été mis à jour"
                val detailsEn = "Your schedule for next week has been modified. Please check the updated assignments and plan accordingly."
                val detailsFr = "Votre horaire pour la semaine prochaine a été modifié. Veuillez vérifier les affectations mises à jour et planifier en conséquence."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("action" to "view_schedule")
                )
            }

            NotificationType.METER_READING_REMINDER -> {
                val count = Random.nextInt(5, 15)
                val titleEn = "Meter Reading Due"
                val titleFr = "Relevé de Compteur Dû"
                val messageEn = "$count meters pending in your area"
                val messageFr = "$count compteurs en attente dans votre zone"
                val detailsEn = "You have $count meter readings scheduled for today. Please complete them before end of business hours."
                val detailsFr = "Vous avez $count relevés de compteurs programmés pour aujourd'hui. Veuillez les terminer avant la fin des heures de bureau."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("count" to count.toString(), "action" to "view_meters")
                )
            }

            NotificationType.CLIENT_FEEDBACK -> {
                val rating = Random.nextInt(1, 6)
                val titleEn = "Client Feedback"
                val titleFr = "Commentaire Client"
                val messageEn = "New feedback received: $rating stars"
                val messageFr = "Nouveau commentaire reçu : $rating étoiles"
                val detailsEn = "A client has left feedback about your recent service. Check the details to understand their experience and improve your service."
                val detailsFr = "Un client a laissé un commentaire sur votre service récent. Consultez les détails pour comprendre leur expérience et améliorer votre service."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("rating" to rating.toString(), "action" to "view_feedback")
                )
            }

            NotificationType.PERFORMANCE_REPORT -> {
                val titleEn = "Performance Report"
                val titleFr = "Rapport de Performance"
                val messageEn = "Your monthly performance report is ready"
                val messageFr = "Votre rapport de performance mensuel est prêt"
                val detailsEn = "Your performance metrics for this month are now available. Review your statistics and see areas for improvement."
                val detailsFr = "Vos métriques de performance pour ce mois sont maintenant disponibles. Consultez vos statistiques et voyez les domaines d'amélioration."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("action" to "view_performance")
                )
            }

            NotificationType.TERRITORY_CHANGE -> {
                val newTerritory = listOf("North District", "South District", "East District", "West District").random()
                val titleEn = "Territory Assignment"
                val titleFr = "Affectation Territoire"
                val messageEn = "You've been assigned to $newTerritory"
                val messageFr = "Vous avez été assigné à $newTerritory"
                val detailsEn = "Your territory assignment has been updated. Please review the new area boundaries and client list."
                val detailsFr = "Votre affectation de territoire a été mise à jour. Veuillez consulter les nouvelles limites de zone et la liste des clients."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("territory" to newTerritory, "action" to "view_territory")
                )
            }

            // ADMIN notifications
            NotificationType.SYSTEM_ALERT -> {
                val systemNames = listOf(
                    "Billing System / Système de Facturation",
                    "Payment Gateway / Passerelle de Paiement",
                    "Meter Management / Gestion des Compteurs",
                    "Customer Portal / Portail Client"
                )
                val system = systemNames.random()
                val titleEn = "System Alert"
                val titleFr = "Alerte Système"
                val messageEn = "$system experiencing issues"
                val messageFr = "$system rencontre des problèmes"
                val detailsEn = "The system is currently experiencing performance issues. Technical team has been notified and is investigating."
                val detailsFr = "Le système rencontre actuellement des problèmes de performance. L'équipe technique a été notifiée et enquête."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("system" to system)
                )
            }

            NotificationType.UNRESOLVED_REQUESTS -> {
                val count = Random.nextInt(10, 50)
                val titleEn = "Pending Requests"
                val titleFr = "Demandes en Attente"
                val messageEn = "$count unresolved service requests"
                val messageFr = "$count demandes de service non résolues"
                val detailsEn = "There are $count service requests that have been pending for more than 48 hours. Please review and assign to available agents."
                val detailsFr = "Il y a $count demandes de service en attente depuis plus de 48 heures. Veuillez examiner et assigner aux agents disponibles."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("count" to count.toString(), "action" to "view_requests")
                )
            }

            NotificationType.AGENT_PERFORMANCE -> {
                val agentName = listOf("Agent Smith", "Agent Johnson", "Agent Williams", "Agent Brown").random()
                val performance = Random.nextInt(70, 100)
                val titleEn = "Agent Performance Report"
                val titleFr = "Rapport Performance Agent"
                val messageEn = "$agentName: $performance% efficiency"
                val messageFr = "$agentName : $performance% d'efficacité"
                val detailsEn = "Monthly performance report for $agentName is available. Review metrics and provide feedback."
                val detailsFr = "Le rapport de performance mensuel pour $agentName est disponible. Consultez les métriques et donnez vos commentaires."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("agent" to agentName, "performance" to performance.toString(), "action" to "view_agent")
                )
            }

            NotificationType.REVENUE_REPORT -> {
                val amount = Random.nextDouble(1000000.0, 5000000.0)
                val titleEn = "Monthly Report Ready"
                val titleFr = "Rapport Mensuel Prêt"
                val messageEn = "Revenue report: ${(amount/1000000).format(2)}M FCFA"
                val messageFr = "Rapport de revenus : ${(amount/1000000).format(2)}M FCFA"
                val detailsEn = "The monthly revenue report is now available. Total revenue: ${amount.toInt()} FCFA. Detailed breakdown and analytics included."
                val detailsFr = "Le rapport de revenus mensuel est maintenant disponible. Revenus totaux : ${amount.toInt()} FCFA. Ventilation détaillée et analyses incluses."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("revenue" to amount.toString(), "action" to "view_report")
                )
            }

            NotificationType.MAINTENANCE_SCHEDULED -> {
                val date = java.time.LocalDate.now().plusDays(Random.nextLong(1, 7))
                val titleEn = "System Maintenance"
                val titleFr = "Maintenance Système"
                val messageEn = "Scheduled maintenance on $date"
                val messageFr = "Maintenance programmée le $date"
                val detailsEn = "System maintenance is scheduled for $date. Some services may be temporarily unavailable."
                val detailsFr = "La maintenance du système est programmée pour le $date. Certains services peuvent être temporairement indisponibles."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("date" to date.toString(), "action" to "view_maintenance")
                )
            }

            NotificationType.NEW_USER_REGISTERED -> {
                val userType = listOf("Client", "Agent").random()
                val titleEn = "New User Registration"
                val titleFr = "Nouvel Utilisateur Enregistré"
                val messageEn = "New $userType registered in the system"
                val messageFr = "Nouveau $userType enregistré dans le système"
                val detailsEn = "A new $userType has registered and is awaiting approval. Please review their information and activate their account."
                val detailsFr = "Un nouveau $userType s'est inscrit et attend l'approbation. Veuillez examiner ses informations et activer son compte."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.NORMAL,
                    actionData = mapOf("user_type" to userType, "action" to "view_user")
                )
            }

            NotificationType.PAYMENT_ISSUES -> {
                val issueCount = Random.nextInt(5, 20)
                val titleEn = "Payment Processing Issues"
                val titleFr = "Problèmes Traitement Paiements"
                val messageEn = "$issueCount payment failures detected"
                val messageFr = "$issueCount échecs de paiement détectés"
                val detailsEn = "Multiple payment processing issues have been detected. Please investigate and resolve these issues promptly."
                val detailsFr = "Plusieurs problèmes de traitement des paiements ont été détectés. Veuillez enquêter et résoudre ces problèmes rapidement."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.HIGH,
                    actionData = mapOf("issue_count" to issueCount.toString(), "action" to "view_payments")
                )
            }

            NotificationType.CAPACITY_WARNING -> {
                val percentage = Random.nextInt(80, 95)
                val titleEn = "System Capacity Warning"
                val titleFr = "Avertissement Capacité Système"
                val messageEn = "System capacity at $percentage%"
                val messageFr = "Capacité du système à $percentage%"
                val detailsEn = "System capacity is approaching critical levels. Consider upgrading infrastructure or optimizing performance."
                val detailsFr = "La capacité du système approche des niveaux critiques. Envisagez de mettre à niveau l'infrastructure ou d'optimiser les performances."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
                    priority = NotificationPriority.URGENT,
                    actionData = mapOf("capacity" to percentage.toString(), "action" to "view_system")
                )
            }

            NotificationType.EMERGENCY_ALERT, NotificationType.SYSTEM_MAINTENANCE -> {
                val titleEn = "Emergency Alert"
                val titleFr = "Alerte d'Urgence"
                val messageEn = "Emergency situation requires attention"
                val messageFr = "Situation d'urgence nécessite attention"
                val detailsEn = "An emergency situation has been detected that requires immediate attention from all personnel."
                val detailsFr = "Une situation d'urgence a été détectée qui nécessite une attention immédiate de tout le personnel."

                NotificationData(
                    title = "$titleEn / $titleFr",
                    message = "$messageEn / $messageFr",
                    details = "$detailsEn\n\n$detailsFr",
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