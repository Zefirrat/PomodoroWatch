package com.example.pomodorowatch.presentation

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinder.StateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Duration
import kotlin.time.toKotlinDuration

class MainScreenViewModel : ViewModel() {
    private val _timerText = MutableStateFlow(Phrases.Ready)
    val timerText = _timerText.stateIn(viewModelScope, SharingStarted.Lazily, Phrases.Ready)

    private val _buttonState = MutableStateFlow(StateUI.Initial)
    val state = _buttonState.asStateFlow()

    private val _stateText = MutableStateFlow(Phrases.Work)
    val stateText = _stateText.stateIn(viewModelScope, SharingStarted.Lazily, Phrases.Work)

    private val _progressBar = MutableStateFlow(0f)
    val progressBar = _progressBar.stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    private val _pomodoroState = MutableStateFlow(PomodoroStateUI.Work)
    val pomodoroState = _pomodoroState.asStateFlow()


    private val _pomodoroLogic = PomodoroLogic(2)

    private fun timerFactory(milis: Long): CountDownTimer {
        return object :
            CountDownTimer(milis, Duration.ofSeconds(1).toMillis()) {
            override fun onTick(millisUntilFinished: Long) {
                elapsed = millisUntilFinished
                _timerText.update { s ->
                    Duration.ofMillis(millisUntilFinished).toKotlinDuration()
                        .toComponents { hours, minutes, seconds, _ -> "${hours}:${minutes}:${seconds}" }
                }
                _progressBar.update { p -> p + _tickWeight }
            }

            override fun onFinish() {
                timerFinished()
            }
        }
    }

    private var _tickWeight = 0f

    private fun timerFinished() {
        val nextState = _pomodoroLogic.getNextState()
        when (nextState) {
            PomodoroLogic.PState.LongBreak -> setLongBreak()
            PomodoroLogic.PState.ShortBreak -> setShortBreak()
            PomodoroLogic.PState.Work -> setWork()
        }
    }

    private fun setWork() {
//        elapsed = Duration.ofMinutes(25).toMillis()
        elapsed = Duration.ofSeconds(25).toMillis()
        _stateText.update { s -> Phrases.Work }
        _pomodoroState.update { s -> PomodoroStateUI.Work }
        setTickWeight()
        _progressBar.update { 0f }
        startOrContinueTimer()
    }

    private fun setShortBreak() {
//        elapsed = Duration.ofMinutes(5).toMillis()
        elapsed = Duration.ofSeconds(5).toMillis()
        _stateText.update { s -> Phrases.Break }
        _pomodoroState.update { s -> PomodoroStateUI.Break }
        setTickWeight()
        _progressBar.update { 0f }
        startOrContinueTimer()
    }

    private fun setLongBreak() {
//        elapsed = Duration.ofMinutes(20).toMillis()
        elapsed = Duration.ofSeconds(20).toMillis()
        _stateText.update { s -> Phrases.StrongBreak }
        _pomodoroState.update { s -> PomodoroStateUI.Break }
        setTickWeight()
        _progressBar.update { 0f }
        startOrContinueTimer()
    }

    private fun setTickWeight(){
        _tickWeight =  100f/(elapsed.toFloat()/1000f)/100f
    }

    private val stateMachine = StateMachine.create<State, Event, SideEffect> {
        initialState(State.Initial)
        state<State.Initial> {
            on<Event.OnStartPressed> {
                transitionTo(State.Running, SideEffect.ResumeTimer)
            }
        }
        state<State.Running> {
            on<Event.OnPausePressed> {
                transitionTo(State.Paused, SideEffect.PauseTimer)
            }
            on<Event.OnStopPressed> {
                transitionTo(State.Initial, SideEffect.ResetTimer)
            }
        }
        state<State.Paused> {
            on<Event.OnStopPressed> {
                transitionTo(State.Initial, SideEffect.ResetTimer)
            }
            on<Event.OnStartPressed> {
                transitionTo(State.Running, SideEffect.ResumeTimer)
            }
        }
        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
            when (validTransition.sideEffect) {
                SideEffect.ResumeTimer -> startOrContinueTimer()
                SideEffect.ResetTimer -> resetTimer()
                SideEffect.PauseTimer -> pauseTimer()
                null -> TODO()
            }
        }
    }

    private fun pauseTimer() {
        _timer.cancel()
        _buttonState.update { s -> StateUI.Paused }
    }

    private fun resetTimer() {
        elapsed = 0L
        _timerText.update { s -> Phrases.Ready }
        _timer.cancel()
        _buttonState.update { s -> StateUI.Initial }
        _pomodoroLogic.reset()
    }

    private var elapsed = 0L
    private var _timer: CountDownTimer = object : CountDownTimer(0, 0) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
        }
    }

    fun start() {
        stateMachine.transition(Event.OnStartPressed)
    }

    private fun startOrContinueTimer() {
        if (elapsed <= 0L) {
//            _timer = timerFactory(Duration.ofMinutes(25).toMillis())
            setWork()
            return
        } else {
            _timer = timerFactory(elapsed)
        }
        _timer.start()
        _buttonState.update { s -> StateUI.Running }
    }

    fun stop() {
        stateMachine.transition(Event.OnStopPressed)
    }

    fun pause() {
        stateMachine.transition(Event.OnPausePressed)
    }

    sealed class State {
        object Initial : State()
        object Running : State()
        object Paused : State()
    }

    sealed class Event {
        object OnStartPressed : Event()
        object OnPausePressed : Event()
        object OnStopPressed : Event()
        object OnTimerFinished : Event()
    }

    sealed class SideEffect {
        object ResetTimer : SideEffect()
        object PauseTimer : SideEffect()
        object ResumeTimer : SideEffect()
    }


}