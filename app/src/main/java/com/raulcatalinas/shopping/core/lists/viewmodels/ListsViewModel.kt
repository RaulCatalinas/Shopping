package com.raulcatalinas.shopping.core.lists.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.core.lists.repositories.ListsRepository
import com.raulcatalinas.shopping.core.lists.types.ElementChange
import com.raulcatalinas.shopping.core.lists.types.ListChange
import com.raulcatalinas.shopping.core.lists.viewmodels.constants.LISTS_VIEW_MODEL_TAG
import com.raulcatalinas.shopping.core.mappers.toDomain
import com.raulcatalinas.shopping.shared.types.ShoppingElement
import com.raulcatalinas.shopping.shared.types.ShoppingList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    private var _elementsJob: Job? = null
    private var _listsJob: Job? = null

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

    fun updateListName(
        listId: String,
        name: String,
        onResult: (errorRes: Int?) -> Unit
    ) {
        _isSaving.value = true

        viewModelScope.launch {
            try {
                val updated = listsRepository.updateList(listId, name)

                if (updated == null) {
                    onResult(R.string.update_list_error)

                    return@launch
                }

                (_uiState.value as? ListsUiState.Success)?.let { current ->
                    _uiState.value = ListsUiState.Success(
                        current.lists.map { if (it.id == listId) updated else it }
                    )
                }

                (_detailState.value as? ListDetailUiState.Success)?.let { current ->
                    if (current.list.id == listId) {
                        _detailState.value = current.copy(list = updated)
                    }
                }

                onResult(null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(LISTS_VIEW_MODEL_TAG, "Error renaming list: ${e.message}", e)
                onResult(R.string.update_list_error)
            } finally {
                _isSaving.value = false
            }
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

    fun updateShoppingElement(
        elementId: String,
        elementName: String,
        quantity: Int,
        onResult: (Int?) -> Unit
    ) {
        _isSaving.value = true

        viewModelScope.launch {
            try {
                val updated =
                    listsRepository.updateShoppingElement(elementId, elementName, quantity)

                if (updated == null) {
                    onResult(R.string.update_shopping_element_error)

                    return@launch
                }

                val current = _detailState.value as? ListDetailUiState.Success

                if (current != null) {
                    _detailState.value = current.copy(
                        elements = current.elements.map {
                            if (it.id == updated.id) updated
                            else it
                        }
                    )
                }

                onResult(null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(LISTS_VIEW_MODEL_TAG, "Error updating shopping element: ${e.message}", e)
                onResult(R.string.update_shopping_element_error)
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun setElementChecked(
        elementId: String,
        checked: Boolean,
        onError: (errorRes: Int) -> Unit
    ) {
        val current = _detailState.value as? ListDetailUiState.Success ?: return
        val previous = current.elements.firstOrNull { it.id == elementId } ?: return

        if (previous.isChecked == checked) return

        updateElementInState(elementId) { it.copy(isChecked = checked) }

        viewModelScope.launch {
            listsRepository.setElementChecked(elementId, checked)
                .onFailure {
                    updateElementInState(elementId) {
                        it.copy(isChecked = previous.isChecked)
                    }
                    onError(R.string.update_shopping_element_error)
                }
        }
    }

    private fun updateElementInState(
        elementId: String,
        change: (ShoppingElement) -> ShoppingElement
    ) {
        val current = _detailState.value as? ListDetailUiState.Success ?: return

        _detailState.value = current.copy(
            elements = current.elements.map {
                if (it.id == elementId) change(it)
                else it
            }
        )
    }

    fun startObservingElements(listId: String) {
        _elementsJob?.cancel()

        _elementsJob = viewModelScope.launch {
            listsRepository.observeElementChanges(listId).collect { change ->
                if (change is ElementChange.Connected) loadListById(listId)
                else applyElementChange(change)
            }
        }
    }

    fun stopObservingElements() {
        _elementsJob?.cancel()
        _elementsJob = null
    }

    private fun applyElementChange(change: ElementChange) {
        val current = _detailState.value as? ListDetailUiState.Success ?: return

        val elements = when (change) {
            is ElementChange.Connected -> return
            is ElementChange.Deleted -> current.elements.filterNot { it.id == change.id }

            is ElementChange.Upsert -> {
                val element = change.element.toDomain()

                if (current.elements.any { it.id == element.id }) {
                    current.elements.map {
                        if (it.id == element.id) element
                        else it
                    }
                } else {
                    current.elements + element
                }
            }
        }

        _detailState.value = current.copy(elements = elements)
    }

    fun startObservingLists() {
        _listsJob?.cancel()

        _listsJob = viewModelScope.launch {
            listsRepository.observeListChanges().collect { change ->
                if (change is ListChange.Connected) loadLists()
                else applyListChange(change)
            }
        }
    }

    fun stopObservingLists() {
        _listsJob?.cancel()
        _listsJob = null
    }

    private fun applyListChange(change: ListChange) {
        val current = _uiState.value as? ListsUiState.Success ?: return

        val lists = when (change) {
            ListChange.Connected -> return
            is ListChange.Deleted -> current.lists.filterNot { it.id == change.id }

            is ListChange.Upsert -> {
                val list = change.list.toDomain()

                if (current.lists.any { it.id == list.id }) {
                    current.lists.map {
                        if (it.id == list.id) list
                        else it
                    }
                } else {
                    current.lists + list
                }
            }
        }

        _uiState.value = ListsUiState.Success(lists)
    }
}