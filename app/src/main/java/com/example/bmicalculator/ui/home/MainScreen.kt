package com.example.bmicalculator.ui.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.ui.RecentActivity
import com.example.bmicalculator.ui.ResultActivity
import com.example.bmicalculator.ui.theme.Black
import com.example.bmicalculator.ui.theme.White
import com.example.bmicalculator.viewmodel.BmiFragmentViewModel
import com.example.bmicalculator.viewmodel.InputFragmentViewModel
import com.example.bmicalculator.viewmodel.StatisticsFragmentViewModel
import kotlinx.coroutines.launch


data class TabItem(
    val iconInt: Int,
    val title: String,
)

@Composable
fun MainScreen() {

    val context = LocalContext.current
    val tabItems = listOf(
        TabItem(R.drawable.tab_calculator, stringResource(R.string.title_calculate)),
        TabItem(R.drawable.tab_bmi, "BMI"),
        TabItem(R.drawable.tab_discover, stringResource(R.string.title_calculate))
    )
    val pageState = rememberPagerState(pageCount = { tabItems.size }, initialPage = 1)
    val scope = rememberCoroutineScope()


    val db = BmiDatabase.getDatabase(context)
    val bmiViewmodel: BmiFragmentViewModel = viewModel(
        factory = BmiFragmentViewModel.provideFactory(
            BmiRepository(db.bmiDao())
        )
    )
    val inputViewmodel: InputFragmentViewModel = viewModel(
        factory = InputFragmentViewModel.provideFactory(
            BmiRepository(db.bmiDao())
        )
    )
    val statisticsViewmodel: StatisticsFragmentViewModel = viewModel(
        factory = StatisticsFragmentViewModel.provideFactory(
            BmiRepository(db.bmiDao())
        )
    )

    val bmiEffect = bmiViewmodel.effect
    val inputEffect = inputViewmodel.effect
    val statisticsEffect = statisticsViewmodel.effect
    LaunchedEffect(Unit) {
        launch {
            bmiEffect.collect { effect ->
                when (effect) {
                    is BmiFragmentViewModel.BmiEffect.NavToInput -> pageState.animateScrollToPage(0)

                    is BmiFragmentViewModel.BmiEffect.NavToRecent -> {
                        val intent = Intent(context, RecentActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            }
        }
        launch {
            inputEffect.collect { effect ->
                when (effect) {
                    is InputFragmentViewModel.InputEffect.NavToResult -> {
                        val intent = Intent(context, ResultActivity::class.java)
                        intent.putExtra("BMI", effect.bmiEntity)
                        intent.putExtra("FATHER", effect.isFirst)
                        context.startActivity(intent)
                    }

                    is InputFragmentViewModel.InputEffect.ShowToast -> {
                        effect.msgResId?.let { id ->
                            val str = context.getString(id)
                            Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }
        }
        launch {
            statisticsEffect.collect { effect ->
                when (effect) {
                    is StatisticsFragmentViewModel.StatisticsEffect.InputPageEffect -> pageState.animateScrollToPage(
                        0
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .navigationBarsPadding()
    ) {
        HorizontalPager(
            state = pageState,
            modifier = Modifier.weight(1f),
            beyondViewportPageCount = 2
        ) { pageIndex ->
            when (pageIndex) {
                0 -> InputScreen(inputViewmodel)
                1 -> BmiScreen(bmiViewmodel)
                2 -> StatisticsScreen(statisticsViewmodel)
            }


        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp) // 阴影的高度
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.White
                        )
                    )
                )
        )
        PrimaryTabRow(
            selectedTabIndex = pageState.currentPage,
            modifier = Modifier
                .fillMaxWidth(),
            indicator = {}
        ) {
            tabItems.forEach { item ->
                val isSelected = pageState.currentPage == tabItems.indexOf(item)
                Tab(
                    selected = isSelected,
                    onClick = {
                        scope.launch {
                            pageState.animateScrollToPage(tabItems.indexOf(item))
                        }
                    },
                    modifier = Modifier
                        .background(White)
                        .padding(vertical = 10.dp),
                    content = {
                        Column(
                            modifier = Modifier
                                .background(White)
                                .alpha(if (isSelected) 1f else 0.5f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = item.iconInt),
                                contentDescription = item.title
                            )
                            Text(
                                text = item.title,
                                fontFamily = FontFamily(Font(R.font.font_regular)),
                                fontSize = 14.sp,
                                color = Black
                            )
                        }
                    }
                )
            }
        }
    }
}