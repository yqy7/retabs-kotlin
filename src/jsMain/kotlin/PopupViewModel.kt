import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PopupViewModel: ViewModel() {
    val searchText = MutableStateFlow("")
    val historyList = MutableStateFlow(emptyArray<dynamic>())
    val sessions = MutableStateFlow(emptyArray< dynamic>())
    val showSession = MutableStateFlow(true)
    val showHistory = MutableStateFlow(true)

    val showMoreSession = MutableStateFlow(false)

    fun setSearchText(text: String) {
        searchText.value = text
        viewModelScope.launch { loadHistory() }
    }

    suspend fun loadChromeSessions() {
        sessions.value = getChromeSessions()
    }

    suspend fun loadHistory() {
        val maxResult = if (searchText.value.isNotBlank()) 50 else 20
        historyList.value = getChromeHistory(searchText.value, maxResult)
    }

    fun toggleShowSession() {
        showSession.value = !showSession.value
    }

    fun toggleShowHistory() {
        showHistory.value = !showHistory.value
    }

    fun toggleShowMoreSession() {
        showMoreSession.value = !showMoreSession.value
    }
}