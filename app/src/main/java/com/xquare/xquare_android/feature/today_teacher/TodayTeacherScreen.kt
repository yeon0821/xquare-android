package com.xquare.xquare_android.feature.today_teacher


import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.semicolon.design.Body1
import com.semicolon.design.Body2
import com.semicolon.design.color.primary.gray.gray50
import com.semicolon.design.color.primary.gray.gray700
import com.semicolon.design.color.primary.gray.gray800
import com.semicolon.design.color.primary.gray.gray900
import com.semicolon.design.color.primary.white.white
import com.xquare.domain.entity.pick.TodaySelfStudyTeacherEntity
import com.xquare.xquare_android.R
import com.xquare.xquare_android.component.Header
import com.xquare.xquare_android.util.DevicePaddings
import com.xquare.xquare_android.util.makeToast
import com.xquare.xquare_android.util.toKorean
import org.threeten.bp.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodayTeacherScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: TodayTeacherViewModel = hiltViewModel()
    var todayTeacher: TodaySelfStudyTeacherEntity? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        viewModel.TodaySelfStudyTeacher()
        viewModel.eventFlow.collect() {
            when (it) {
                is TodayTeacherViewModel.Event.Success -> {
                    todayTeacher = it.data
                }
                is TodayTeacherViewModel.Event.Failure -> {
                    makeToast(context, "자습감독선생님을 불러오지 못했습니다.")
                }
            }
        }
    }
    TodayTeacher(
        todayTeacher = todayTeacher,
        onBack = { navController.popBackStack() }
    )
}


@SuppressLint("NewApi")
@Composable
fun TodayTeacher(
    todayTeacher: TodaySelfStudyTeacherEntity?,
    onBack: () -> Unit,
){
    val listStatus = rememberLazyListState()
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
            title = "오늘의 자습감독 선생님 확인",
            onIconClick = onBack
        )
        todayTeacher?.run {
            LaunchedEffect(Unit) { listStatus.scrollToItem(calculateScrollPosition(todayTeacher))}
            LazyColumn(
                state = listStatus,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ){
                items(todayTeacher.teacherList.count()){
                    if (it == 0) Spacer(Modifier.size(20.dp))
                    if (teacherList[it].teacher[0].isNotEmpty() || teacherList[it].teacher[1].isNotEmpty() || teacherList[it].teacher[2].isNotEmpty() || teacherList[it].teacher[3].isNotEmpty() || teacherList[it].teacher[4].isNotEmpty()){
                    TodayTeacherDetail(
                        teacherEntity = todayTeacher.teacherList[it],
                        borderState = it == calculateScrollPosition(todayTeacher)
                    )
                    if (it == todayTeacher.teacherList.lastIndex){
                        Spacer(Modifier.size(20.dp))
                    }
                    else Spacer(Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateScrollPosition(todayTeacher: TodaySelfStudyTeacherEntity): Int {
    val today = LocalDate.now()
    val teacher = todayTeacher.teacherList
    return teacher
        .firstOrNull { it.date >= today}
        ?.let { teacher.indexOf(it) } ?: teacher.lastIndex
}


