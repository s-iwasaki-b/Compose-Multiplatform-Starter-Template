package org.starter.project.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.aakira.napier.Napier
import org.koin.compose.viewmodel.koinViewModel
import org.starter.project.ui.design.system.scaffold.DesignSystemScaffold
import org.starter.project.ui.shared.event.ScreenEvent

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    // This is an example of lifecycle event listener
    // cf. https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-lifecycle.html#mapping-android-lifecycle-to-other-platforms
    LifecycleEventEffect(Lifecycle.Event.ON_START) { Napier.d { "HomeScreen.onStart" } }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { Napier.d { "HomeScreen.onResume" } }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { Napier.d { "HomeScreen.onPause" } }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { Napier.d { "HomeScreen.onStop" } }

    HomeScreenContent(
        state = state,
        handler = { event ->
            HomeScreenEventHandler(
                event = event,
                viewModel = viewModel
            )
        }
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeScreenState,
    handler: (event: ScreenEvent) -> Unit
) {
    DesignSystemScaffold(
        modifier = Modifier.fillMaxSize(),
        screenState = state.screenState,
        onTapErrorActionButton = {
            handler(HomeScreenEvent.OnTapErrorScreenAction)
        }
    ) { paddingValues ->

    }
}
