package com.raulcatalinas.shopping.core.invitations.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.core.invitations.repositories.InvitationsRepository
import com.raulcatalinas.shopping.core.invitations.types.InvitationException
import com.raulcatalinas.shopping.shared.enums.InviteDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvitationsViewModel @Inject constructor(
    private val invitationsRepository: InvitationsRepository
) : ViewModel() {

    private val _isWorking = MutableStateFlow(false)

    val isWorking: StateFlow<Boolean> = _isWorking.asStateFlow()

    // onResult always gets called exactly once: with the link, or with an error
    // string resource. (null, null) means "ignored", so the caller can stop its spinner.
    fun createInvitation(
        listId: String,
        inviteDuration: InviteDuration,
        onResult: (link: String?, errorRes: Int?) -> Unit
    ) {
        if (_isWorking.value) {
            onResult(null, null)

            return
        }

        _isWorking.value = true

        viewModelScope.launch {
            try {
                invitationsRepository.createInvitationLink(listId, inviteDuration.hours)
                    .onSuccess { link -> onResult(link, null) }
                    .onFailure { error ->
                        onResult(
                            null,
                            if (error is InvitationException.NotAllowed) {
                                R.string.invite_not_allowed_error
                            } else {
                                R.string.invite_create_error
                            }
                        )
                    }
            } finally {
                _isWorking.value = false
            }
        }
    }
}