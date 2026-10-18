package com.raulcatalinas.shopping.core.lists.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.core.lists.repositories.ListsRepository
import com.raulcatalinas.shopping.core.lists.viewmodels.constants.LISTS_VIEW_MODEL_TAG
import com.raulcatalinas.shopping.core.mappers.toDomain
import com.raulcatalinas.shopping.shared.types.ShoppingElement
import com.raulcatalinas.shopping.shared.types.ShoppingList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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

sealed interface ListDetailUiState {
    data object Loading : ListDetailUiState

    data class Success(
        val list: ShoppingList,
        val elements: List<ShoppingElement>
    ) : ListDetailUiState

    data object NotFound : ListDetailUiState
    data object Error : ListDetailUiState
}

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val listsRepository: ListsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListsUiState>(ListsUiState.Loading)
    private val _detailState = MutableStateFlow<ListDetailUiState>(ListDetailUiState.Loading)
    private val _isSaving = MutableStateFlow(false)

    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    val detailState: StateFlow<ListDetailUiState> = _detailState.asStateFlow()
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()

    private var currentListId: String? = null

    fun loadLists() {
        viewModelScope.launch {
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
            val isSameList = id == currentListId

            if (!isSameList || _detailState.value !is ListDetailUiState.Success) {
                _detailState.value = ListDetailUiState.Loading
            }

            currentListId = id

            val list = listsRepository.getListById(id).getOrElse {
                _detailState.value = ListDetailUiState.Error
                return@launch
            }

            if (list == null) {
                _detailState.value = ListDetailUiState.NotFound
                return@launch
            }

            val elements = listsRepository.getElementsByListId(id).getOrElse {
                _detailState.value = ListDetailUiState.Error
                return@launch
            }

            _detailState.value = ListDetailUiState.Success(list.toDomain(), elements.toDomain())
        }
    }

    fun createList(
        listName: String,
        onCreated: (ShoppingList) -> Unit,
        onError: (errorRes: Int) -> Unit
    ) {
        // Ignore a second tap while the first one is still saving
        if (_isSaving.value) return

        _isSaving.value = true

        viewModelScope.launch {
            try {
                val createdList = listsRepository.createList(listName.trim())

                if (createdList == null) {
                    onError(R.string.create_lists_error)

                    return@launch
                }

                onCreated(createdList)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(LISTS_VIEW_MODEL_TAG, "Error creating list: ${e.message}", e)
                onError(R.string.create_lists_error)
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun createShoppingElement(
        elementName: String,
        quantity: Int,
        listId: String,
        onResult: (Int?) -> Unit
    ) {
        // Ignore a second tap while the first one is still saving
        if (_isSaving.value) return

        _isSaving.value = true

        viewModelScope.launch {
            try {
                val createdElement = listsRepository.createShoppingElement(
                    elementName = elementName,
                    quantity = quantity,
                    listId = listId
                )

                onResult(
                    if (createdElement == null) R.string.create_shopping_element_error
                    else null
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(
                    LISTS_VIEW_MODEL_TAG,
                    "Error creating shopping element: ${e.message}",
                    e
                )

                onResult(R.string.create_shopping_element_error)
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteList(listId: String, onError: (errorRes: Int) -> Unit) {
        viewModelScope.launch {
            listsRepository.deleteList(listId)
                .onSuccess {
                    val current = _uiState.value as? ListsUiState.Success ?: return@onSuccess

                    _uiState.value = ListsUiState.Success(
                        current.lists.filterNot { it.id == listId }
                    )
                }
                .onFailure { onError(R.string.delete_list_error) }
        }
    }

    fun deleteShoppingElement(elementId: String, onError: (errorRes: Int) -> Unit) {
        viewModelScope.launch {
            listsRepository.deleteShoppingElement(elementId)
                .onSuccess {
                    val current =
                        _detailState.value as? ListDetailUiState.Success
                            ?: return@onSuccess

                    _detailState.value = current.copy(
                        elements = current.elements.filterNot { it.id == elementId }
                    )
                }
                .onFailure { onError(R.string.delete_element_error) }
        }
    }
}