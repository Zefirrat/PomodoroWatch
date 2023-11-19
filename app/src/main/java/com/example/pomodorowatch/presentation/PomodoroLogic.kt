package com.example.pomodorowatch.presentation

import com.tinder.StateMachine

class PomodoroLogic(workSegments: Int) {
    private var _workSegments: Int = workSegments
    private var _currentSegments: Int = _workSegments

    fun getNextState(): PState {
        if (_currentSegments > 1) {
            stateMachine.transition(PEvent.Next)
        } else {
            stateMachine.transition(PEvent.ToLongBreak)
        }

        return stateMachine.state
    }

    fun reset(){
        stateMachine.transition(PEvent.Reset)
    }

    sealed class PState {
        object Work : PState()
        object ShortBreak : PState()
        object LongBreak : PState()
    }

    private sealed class PEvent {
        object Next : PEvent()
        object Reset : PEvent()
        object ToLongBreak : PEvent()
    }

    private sealed class PSideEffect {
        object ReduceSegments : PSideEffect()
        object ResetSegments : PSideEffect()
    }

    private val stateMachine = StateMachine.create<PState, PEvent, PSideEffect> {
        initialState(PState.Work)
        state<PState.Work> {
            on<PEvent.Next> {
                transitionTo(PState.ShortBreak)
            }
            on<PEvent.ToLongBreak> {
                transitionTo(PState.LongBreak, PSideEffect.ResetSegments)
            }
            on<PEvent.Reset> {
                transitionTo(PState.Work, PSideEffect.ResetSegments)
            }
        }
        state<PState.ShortBreak> {
            on<PEvent.Next> {
                transitionTo(PState.Work, PSideEffect.ReduceSegments)
            }
            on<PEvent.Reset> {
                transitionTo(PState.Work, PSideEffect.ResetSegments)
            }
        }
        state<PState.LongBreak> {
            on<PEvent.Next> {
                transitionTo(PState.Work, PSideEffect.ResetSegments)
            }
            on<PEvent.Reset> {
                transitionTo(PState.Work, PSideEffect.ResetSegments)
            }
        }

        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
            when (validTransition.sideEffect) {
                PSideEffect.ReduceSegments -> reduceSegments()
                PSideEffect.ResetSegments -> resetSegments()
                null -> null
            }
        }
    }

    private fun resetSegments() {
        _currentSegments = _workSegments
    }

    private fun reduceSegments() {
        _currentSegments--
    }
}