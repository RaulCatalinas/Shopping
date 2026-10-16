package com.raulcatalinas.shopping.core.lists.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcatalinas.shopping.core.lists.repositories.ListsRepository
import com.raulcatalinas.shopping.core.mappers.toDomain
import com.raulcatalinas.shopping.shared.types.ShoppingElement
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
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow<ListDetailUiState>(ListDetailUiState.Loading)
    val detailState: StateFlow<ListDetailUiState> = _detailState.asStateFlow()

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
}