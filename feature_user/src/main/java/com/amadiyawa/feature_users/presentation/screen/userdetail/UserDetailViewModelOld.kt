package com.amadiyawa.feature_users.presentation.screen.userdetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_base.presentation.screen.viewmodel.OldBaseAction
import com.amadiyawa.feature_base.presentation.screen.viewmodel.BaseState
import com.amadiyawa.feature_base.presentation.screen.viewmodel.OldBaseViewModel
import com.amadiyawa.feature_users.domain.model.OldUser
import com.amadiyawa.feature_users.domain.usecase.GetUserUseCase
import com.amadiyawa.feature_users.presentation.screen.userdetail.UserDetailViewModelOld.ActionOld
import com.amadiyawa.feature_users.presentation.screen.userdetail.UserDetailViewModelOld.UiState
import com.amadiyawa.feature_users.presentation.screen.userdetail.UserDetailViewModelOld.UiState.Content
import kotlinx.coroutines.launch

internal class UserDetailViewModelOld(
    private val getUserDetailUseCase: GetUserUseCase,
) : OldBaseViewModel<UiState, ActionOld>(UiState.Loading) {

    fun onEnter(uuid: String) {
        getUserByUuid(uuid)
    }

    private fun getUserByUuid(uuid: String) {
        viewModelScope.launch {
            getUserDetailUseCase(uuid).also { result ->
                val action = when (result) {
                    is OperationResult.Success -> {
                        ActionOld.UserDetailSuccess(result.data)
                    }
                    is OperationResult.Failure -> ActionOld.UserDetailFailure
                    is OperationResult.Error -> TODO()
                }
                sendAction(action)
            }
        }
    }

    internal sealed interface ActionOld : OldBaseAction<UiState> {
        class UserDetailSuccess(private val oldUser: OldUser) : ActionOld {
            override fun reduce(state: UiState) = Content(oldUser)
        }

        data object UserDetailFailure : ActionOld {
            override fun reduce(state: UiState) = UiState.Error
        }
    }

    @Immutable
    internal sealed interface UiState : BaseState {
        data class Content(val oldUser: OldUser) : UiState
        data object Loading : UiState
        data object Error : UiState
    }
}