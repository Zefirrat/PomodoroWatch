/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.pomodorowatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.pomodorowatch.presentation.theme.PomodoroWatchTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.CircularProgressIndicator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    PomodoroWatchTheme {
        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val vm = viewModel<MainScreenViewModel>()
            val timerText by vm.timerText.collectAsStateWithLifecycle()
            val state by vm.state.collectAsStateWithLifecycle()
            val stateText by vm.stateText.collectAsStateWithLifecycle()
            val pomodoroState by vm.pomodoroState.collectAsStateWithLifecycle()
            val progress by vm.progressBar.collectAsStateWithLifecycle()
            MainScreen(
                timerText,
                state,
                stateText,
                pomodoroState,
                progress,
                vm::start,
                vm::stop,
                vm::pause
            )
        }
    }
}

@Composable
fun MainScreen(
    timerText: String,
    state: StateUI,
    stateText: String,
    pomodoroState: PomodoroStateUI,
    progress: Float,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onPause: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state != StateUI.Initial) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stateText)
                Spacer(modifier = Modifier.width(10.dp))
                CircularProgressIndicator(
                    progress,
                    Modifier
                        .width(25.dp)
                        .height(25.dp),
                    indicatorColor = if (pomodoroState == PomodoroStateUI.Work) {
                        Color.Red
                    } else {
                        Color.Green
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Text(text = timerText)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Button(onClick = {
                if (state == StateUI.Initial || state == StateUI.Paused) {
                    onStart()
                } else {
                    onPause()
                }
            }) {
                Icon(
                    imageVector = if (state == StateUI.Initial || state == StateUI.Paused) {
                        Icons.Default.PlayArrow
                    } else if (state == StateUI.Running) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(
                onClick = { onStop() },
                enabled = state != StateUI.Initial
            )
            {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                )
            }
        }
    }
}


@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}