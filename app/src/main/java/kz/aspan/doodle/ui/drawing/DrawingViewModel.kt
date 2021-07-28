package kz.aspan.doodle.ui.drawing

import androidx.core.graphics.rotationMatrix
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kz.aspan.data.models.RoundDrawInfo
import kz.aspan.doodle.R
import kz.aspan.doodle.data.remote.ws.DrawingApi
import kz.aspan.doodle.data.remote.ws.Room
import kz.aspan.doodle.data.remote.ws.models.*
import kz.aspan.doodle.data.remote.ws.models.DrawAction.Companion.ACTION_UNDO
import kz.aspan.doodle.ui.views.DrawingView
import kz.aspan.doodle.util.CoroutineTimer
import kz.aspan.doodle.util.DispatcherProvider
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val drawingApi: DrawingApi,
    private val dispatchers: DispatcherProvider,
    private val gson: Gson
) : ViewModel() {

    sealed class SocketEvent {
        data class ChatMessageEvent(val data: ChatMessage) : SocketEvent()
        data class AnnouncementEvent(val data: Announcement) : SocketEvent()
        data class GameStateEvent(val data: GameState) : SocketEvent()
        data class DrawDataEvent(val data: DrawData) : SocketEvent()
        data class NewWordsEvent(val data: NewWords) : SocketEvent()
        data class ChosenWordEvent(val data: ChosenWord) : SocketEvent()
        data class GameErrorEvent(val data: GameError) : SocketEvent()
        data class RoundDrawInfoEvent(val data: RoundDrawInfo) : SocketEvent()
        object UndoEvent : SocketEvent()
    }

    private val _pathData = MutableStateFlow(Stack<DrawingView.PathData>())
    val pathData = _pathData

    private val _newWords = MutableStateFlow(NewWords(listOf()))
    val newWords: StateFlow<NewWords> = _newWords

    private val _phase = MutableStateFlow(PhaseChange(null, 0L, null))
    val phase = _phase

    private val _phaseTime = MutableStateFlow(0L)
    val phaseTime = _phaseTime

    private val _gameState = MutableStateFlow(GameState("", ""))
    val gameState = _gameState

    private val _chat = MutableStateFlow<List<BaseModel>>(listOf())
    val chat: StateFlow<List<BaseModel>> = _chat

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

    private val timer = CoroutineTimer()
    private var timerJob: Job? = null

    init {
        observeBaseModels()
        observerEvents()
    }

    private fun setTimer(duration: Long) {
        timerJob?.cancel()
        timerJob = timer.timeAndEmit(duration, viewModelScope) {
            _phaseTime.value = it
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
    }

    fun setPathData(stack: Stack<DrawingView.PathData>) {
        _pathData.value = stack
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
                    is ChatMessage -> {
                        socketEventChannel.send(SocketEvent.ChatMessageEvent(data))
                    }
                    is ChosenWord -> {
                        socketEventChannel.send(SocketEvent.ChosenWordEvent(data))
                    }
                    is Announcement -> {
                        socketEventChannel.send(SocketEvent.AnnouncementEvent(data))
                    }
                    is GameState -> {
                        _gameState.value = data
                        socketEventChannel.send(SocketEvent.GameStateEvent(data))
                    }
                    is NewWords -> {
                        _newWords.value = data
                        socketEventChannel.send(SocketEvent.NewWordsEvent(data))
                    }
                    is DrawAction -> {
                        when (data.action) {
                            ACTION_UNDO -> socketEventChannel.send(SocketEvent.UndoEvent)
                        }
                    }
                    is PhaseChange -> {
                        data.phase?.let {
                            _phase.value = data
                        }
                        _phaseTime.value = data.time
                        if (data.phase != Room.Phase.WAITING_FOR_PLAYERS) {
                            setTimer(data.time)
                        }
                    }
                    is GameError -> socketEventChannel.send(SocketEvent.GameErrorEvent(data))
                    is Ping -> sendBaseModel(Ping())
                }
            }
        }
    }

    fun chooseWord(word: String, roomName: String) {
        val chosenWord = ChosenWord(word, roomName)
        sendBaseModel(chosenWord)
    }

    fun sendChatMessage(message: ChatMessage) {
        if (message.message.trim().isEmpty()) {
            return
        }
        viewModelScope.launch(dispatchers.io) {
            drawingApi.sendBaseModel(message)
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