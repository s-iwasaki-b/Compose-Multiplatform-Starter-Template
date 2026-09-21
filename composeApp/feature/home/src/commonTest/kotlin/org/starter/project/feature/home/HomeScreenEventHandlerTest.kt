@file:OptIn(ExperimentalCoroutinesApi::class)

package org.starter.project.feature.home

import dev.mokkery.MockMode
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
import org.starter.project.ui.route.AppRoute
import org.starter.project.ui.route.AppRouter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [HomeScreenEventHandler] が各 [HomeScreenEvent] を正しい宛先
 * (ViewModel / AppRouter / articles refresh コールバック) に振り分けることを検証する。
 *
 * EventHandler は LazyPagingItems に依存しないため、リフレッシュ要求は
 * 呼び出し回数を記録する単純なラムダで代替する。
 * ZennService / AppRouter は Mokkery の mock で生成する。
 */
class HomeScreenEventHandlerTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeScreenViewModel {
        return HomeScreenViewModel(zennService = mock<ZennService>())
    }

    private fun dispatch(
        event: HomeScreenEvent,
        viewModel: HomeScreenViewModel,
        appRouter: AppRouter = mock<AppRouter>(MockMode.autoUnit),
        onRefreshArticles: () -> Unit = {}
    ) {
        HomeScreenEventHandler(
            event = event,
            appRouter = appRouter,
            viewModel = viewModel,
            onRefreshArticles = onRefreshArticles
        )
    }

    @Test
    fun onChangeSearchKeyword_updatesKeywordWithoutRefresh() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var refreshCount = 0

        dispatch(
            event = HomeScreenEvent.OnChangeSearchKeyword("abc"),
            viewModel = viewModel,
            onRefreshArticles = { refreshCount++ }
        )
        advanceUntilIdle()

        assertEquals("abc", viewModel.state.value.searchKeyword)
        assertEquals(0, refreshCount)
    }

    @Test
    fun onClickClearSearchKeyword_clearsKeywordAndRefreshesOnce() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.updateSearchKeyword("kotlin")
        advanceUntilIdle()
        var refreshCount = 0

        dispatch(
            event = HomeScreenEvent.OnClickClearSearchKeyword,
            viewModel = viewModel,
            onRefreshArticles = { refreshCount++ }
        )
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.searchKeyword)
        assertEquals(1, refreshCount)
    }

    @Test
    fun onClickUser_navigatesToUserRoute() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        val mockAppRouter = mock<AppRouter>(MockMode.autoUnit)

        dispatch(
            event = HomeScreenEvent.OnClickUser("username"),
            viewModel = viewModel,
            appRouter = mockAppRouter
        )

        verify { mockAppRouter.navigate(AppRoute.User("username")) }
        verify(VerifyMode.not) { mockAppRouter.popBackStack() }
    }

    @Test
    fun onPullToRefresh_startsRefreshingAndRefreshesOnce() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var refreshCount = 0

        dispatch(
            event = HomeScreenEvent.OnPullToRefresh,
            viewModel = viewModel,
            onRefreshArticles = { refreshCount++ }
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isPullToRefreshing)
        assertEquals(1, refreshCount)
    }

    @Test
    fun onPullToRefreshComplete_stopsRefreshing() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        dispatch(event = HomeScreenEvent.OnPullToRefresh, viewModel = viewModel)
        advanceUntilIdle()

        dispatch(event = HomeScreenEvent.OnPullToRefreshComplete, viewModel = viewModel)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isPullToRefreshing)
    }

    @Test
    fun onClickErrorScreenAction_refreshesOnce() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var refreshCount = 0

        dispatch(
            event = HomeScreenEvent.OnClickErrorScreenAction,
            viewModel = viewModel,
            onRefreshArticles = { refreshCount++ }
        )

        assertEquals(1, refreshCount)
    }

    @Test
    fun onClickActionSearchKeyword_refreshesOnce() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var refreshCount = 0

        dispatch(
            event = HomeScreenEvent.OnClickActionSearchKeyword,
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

        dispatch(event = HomeScreenEvent.OnSnackBarShown, viewModel = viewModel)
        advanceUntilIdle()

        assertNull(viewModel.state.value.screenState.snackBarState)
    }
}
