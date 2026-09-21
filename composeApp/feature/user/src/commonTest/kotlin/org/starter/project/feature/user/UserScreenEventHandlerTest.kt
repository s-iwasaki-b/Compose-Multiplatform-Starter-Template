@file:OptIn(ExperimentalCoroutinesApi::class)

package org.starter.project.feature.user

import dev.mokkery.MockMode
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.starter.project.domain.service.ZennService
import org.starter.project.ui.route.AppRouter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * [UserScreenEventHandler] が各 [UserScreenEvent] を正しい宛先
 * (ViewModel / AppRouter / articles refresh コールバック) に振り分けることを検証する。
 *
 * EventHandler は LazyPagingItems に依存しないため、リフレッシュ要求は
 * 呼び出し回数を記録する単純なラムダで代替する。
 * ZennService / AppRouter は Mokkery の mock で生成する。
 */
class UserScreenEventHandlerTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): UserScreenViewModel {
        return UserScreenViewModel(zennService = mock<ZennService>())
    }

    private fun dispatch(
        event: UserScreenEvent,
        viewModel: UserScreenViewModel,
        appRouter: AppRouter = mock<AppRouter>(MockMode.autoUnit),
        onRefreshArticles: () -> Unit = {}
    ) {
        UserScreenEventHandler(
            event = event,
            appRouter = appRouter,
            viewModel = viewModel,
            onRefreshArticles = onRefreshArticles
        )
    }

    @Test
    fun onClickBack_popsBackStackOnce() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        val mockAppRouter = mock<AppRouter>(MockMode.autoUnit)

        dispatch(
            event = UserScreenEvent.OnClickBack,
            viewModel = viewModel,
            appRouter = mockAppRouter
        )

        verify { mockAppRouter.popBackStack() }
        verify(VerifyMode.not) { mockAppRouter.navigate(any()) }
    }

    @Test
    fun onClickErrorScreenAction_refreshesOnce() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var refreshCount = 0

        dispatch(
            event = UserScreenEvent.OnClickErrorScreenAction,
            viewModel = viewModel,
            onRefreshArticles = { refreshCount++ }
        )

        assertEquals(1, refreshCount)
    }

    @Test
    fun onSnackBarShown_clearsSnackBarState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.updateSnackBarState("error message")
        advanceUntilIdle()
        assertEquals("error message", viewModel.state.value.screenState.snackBarState?.message)

        dispatch(event = UserScreenEvent.OnSnackBarShown, viewModel = viewModel)
        advanceUntilIdle()

        assertNull(viewModel.state.value.screenState.snackBarState)
    }
}
