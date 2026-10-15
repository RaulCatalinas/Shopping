package com.raulcatalinas.shopping.core.lists.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcatalinas.shopping.core.lists.repositories.ListsRepository
import com.raulcatalinas.shopping.core.mappers.toDomain
import com.raulcatalinas.shopping.shared.types.ShoppingList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// What the home screen can be showing at any moment
sealed interface ListsUiState {
    data object Loading : ListsUiState
    data class Success(val lists: List<ShoppingList>) : ListsUiState

    // Distinct from Success(emptyList()): a failed request must never look
    // like "you have no lists"
    data object Error : ListsUiState
}

// What the detail screen of a single list can be showing
sealed interface ListDetailUiState {
    data object Loading : ListDetailUiState
    data class Success(val list: ShoppingList) : ListDetailUiState

    // The request worked but returned nothing: the list was deleted, or the
    // user is no longer a member (the database hides it)
    data object NotFound : ListDetailUiState

    // The request itself failed (network, server...)
    data object Error : ListDetailUiState
}

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val listsRepository: ListsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListsUiState>(ListsUiState.Loading)
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<ListDetailUiState>(ListDetailUiState.Loading)
    val detailState: StateFlow<ListDetailUiState> = _detailState.asStateFlow()

    // Id of the list currently held in detailState
    private var currentListId: String? = null

    fun loadLists() {
        viewModelScope.launch {
            // Only show the spinner on the first load. When coming back to the
            // screen, keep the lists visible while they refresh.
            if (_uiState.value !is ListsUiState.Success) {
                _uiState.value = ListsUiState.Loading
            }

            listsRepository
                .getAllList()
                .onSuccess { _uiState.value = ListsUiState.Success(it.toDomain()) }
                .onFailure { _uiState.value = ListsUiState.Error }
        }
    }

    fun loadListById(id: String) {
        viewModelScope.launch {
            // A different list than the one we hold: drop the old data right away,
            // so the previous list is never shown under the new one's title.
            // The same list (a refresh) keeps its data visible while reloading.
            val isSameList = id == currentListId

            if (!isSameList || _detailState.value !is ListDetailUiState.Success) {
                _detailState.value = ListDetailUiState.Loading
            }

            currentListId = id

            listsRepository
                .getListById(id)
                .onSuccess { dto ->
                    _detailState.value =
                        if (dto == null) ListDetailUiState.NotFound
                        else ListDetailUiState.Success(dto.toDomain())
                }
                .onFailure { _detailState.value = ListDetailUiState.Error }
        }
    }
}