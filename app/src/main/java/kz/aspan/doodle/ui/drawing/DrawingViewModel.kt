package kz.aspan.doodle.ui.drawing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kz.aspan.data.models.RoundDrawInfo
import kz.aspan.doodle.R
import kz.aspan.doodle.data.remote.ws.DrawingApi
import kz.aspan.doodle.data.remote.ws.models.*
import kz.aspan.doodle.data.remote.ws.models.DrawAction.Companion.ACTION_UNDO
import kz.aspan.doodle.util.DispatcherProvider
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val drawingApi: DrawingApi,
    private val dispatchers: DispatcherProvider,
    private val gson: Gson
) : ViewModel() {

    sealed class SocketEvent {
        data class CharMessageEvent(val data: ChatMessage) : SocketEvent()
        data class AnnouncementEvent(val data: Announcement) : SocketEvent()
        data class GameStateEvent(val data: GameState) : SocketEvent()
        data class DrawDataEvent(val data: DrawData) : SocketEvent()
        data class NewWordsEvent(val data: NewWords) : SocketEvent()
        data class ChosenWordEvent(val data: ChosenWord) : SocketEvent()
        data class GameErrorEvent(val data: GameError) : SocketEvent()
        data class RoundDrawInfoEvent(val data: RoundDrawInfo) : SocketEvent()
        object UndoEvent : SocketEvent()
    }

    private val _selectedColorButtonId = MutableStateFlow(R.id.rbBlack)
    val selectedColorButtonId: StateFlow<Int> = _selectedColorButtonId

    private val _connectionProgressBarVisible = MutableStateFlow(true)
    val connectionProgressBarVisible = _connectionProgressBarVisible

    private val _chooseWordOverlayVisible = MutableStateFlow(false)
    val chooseWordOverlayVisible = _chooseWordOverlayVisible

    private val connectionEventChannel = Channel<WebSocket.Event>()
    val connectionEvent = connectionEventChannel.receiveAsFlow().flowOn(dispatchers.io)

    private val socketEventChannel = Channel<SocketEvent>()
    val socketEvent = socketEventChannel.receiveAsFlow().flowOn(dispatchers.io)

    init {
        observeBaseModels()
        observerEvents()
    }

    fun setChooseWordOverlayVisibility(isVisible: Boolean) {
        _chooseWordOverlayVisible.value = isVisible
    }

    fun setConnectionProgressBarVisibility(isVisible: Boolean) {
        _connectionProgressBarVisible.value = isVisible
    }

    private fun observerEvents() {
        viewModelScope.launch(dispatchers.io) {
            drawingApi.observeEvents().collect { event ->
                connectionEventChannel.send(event)
            }
        }
    }

    private fun observeBaseModels() {
        viewModelScope.launch(dispatchers.io) {
            drawingApi.observeBaseModels().collect { data ->
                when (data) {
                    is DrawData -> {
                        socketEventChannel.send(SocketEvent.DrawDataEvent(data))
                    }
                    is DrawAction -> {
                        when (data.action) {
                            ACTION_UNDO -> socketEventChannel.send(SocketEvent.UndoEvent)
                        }
                    }
                    is GameError -> socketEventChannel.send(SocketEvent.GameErrorEvent(data))
                    is Ping -> sendBaseModel(Ping())
                }
            }
        }
    }

    fun sendBaseModel(data: BaseModel) {
        viewModelScope.launch(dispatchers.io) {
            drawingApi.sendBaseModel(data)
        }
    }


    fun checkRadioButton(id: Int) {
        _selectedColorButtonId.value = id
    }
}