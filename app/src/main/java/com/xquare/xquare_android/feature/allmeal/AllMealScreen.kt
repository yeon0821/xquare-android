package com.xquare.xquare_android.feature.allmeal

import MealDetail
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.semicolon.design.color.primary.white.white
import com.xquare.domain.entity.meal.AllMealEntity
import com.xquare.xquare_android.R
import com.xquare.xquare_android.component.Header
import com.xquare.xquare_android.util.DevicePaddings
import com.xquare.xquare_android.util.makeToast
import org.threeten.bp.LocalDate

@Composable
fun AllMealScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val viewModel: AllMealViewModel = hiltViewModel()
    var allMeal: AllMealEntity? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        viewModel.fetchAllMeal()
        viewModel.eventFlow.collect {
            when (it) {
                is AllMealViewModel.Event.Success -> {
                    allMeal = it.data
                }
                is AllMealViewModel.Event.Failure -> {
                    makeToast(context, "급식을 불러오는 데 실패했습니다")
                }
            }
        }
    }
    AllMeal(
        allMeal = allMeal,
        onBackPress = { navController.popBackStack() }
    )
}

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@Composable
private fun AllMeal(
    allMeal: AllMealEntity?,
    onBackPress: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    Column(
        modifier = Modifier
            .background(white)
            .padding(
                top = DevicePaddings.statusBarHeightDp.dp,
                bottom = DevicePaddings.navigationBarHeightDp.dp
            ),
    ) {
        Header(
            painter = painterResource(R.drawable.ic_back),
            title = "전체 급식",
            onIconClick = onBackPress
        )
        allMeal?.run {
            LaunchedEffect(Unit) { lazyListState.scrollToItem(calculateScrollPosition(allMeal)) }
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(allMeal.meals.count()) {
                    if (it == 0) Spacer(Modifier.size(20.dp))
                    MealDetail(
                        mealWithDateEntity = allMeal.meals[it],
                        borderState = it == calculateScrollPosition(allMeal)
                    )
                    if (it == allMeal.meals.lastIndex)
                        Spacer(Modifier.size(20.dp))
                    else Spacer(Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun calculateScrollPosition(allMeal: AllMealEntity): Int {
    val today = LocalDate.now()
    val meals = allMeal.meals
    return meals
        .firstOrNull { it.date >= today }
        ?.let { meals.indexOf(it) } ?: meals.lastIndex
}