package com.example.myapplication.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.components.CustomSegmentedTabRow
import com.example.myapplication.screens.dialogs.CustomDateRangeDialog
import com.example.myapplication.ui.theme.COMBINED_COLORS
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.White
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.CategoryViewModel
import com.example.myapplication.viewmodel.TransactionViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import java.text.DecimalFormat
import java.util.Calendar

@Composable
fun AnalyticsTab(
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    authViewModel: AuthViewModel
) {
    val timeTab = listOf("All", "Today", "Week", "Month", "Custom")
    var selectedTimeTab by remember { mutableIntStateOf(0) }

    val typeTab = listOf("Comparison", "Spending", "Earning")
    var selectedTypeTab by remember { mutableIntStateOf(0) }

    var customDateDialogExpanded by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    var selectedType by remember { mutableStateOf<String>("") }

    var centerText by remember { mutableStateOf<String>("") }
    var total by remember { mutableDoubleStateOf(0.0) }
    var entries by remember { mutableStateOf<List<PieEntry>>(mutableListOf()) }

    LaunchedEffect(selectedTimeTab) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        when (selectedTimeTab) {
            0 -> {
                startDate = null
                endDate = null
            }

            1 -> {
                // Today
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis
                endDate = now
            }

            2 -> {
                // Week
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis
                endDate = now
            }

            3 -> {
                // Month
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis
                endDate = now
            }

            4 -> {
                customDateDialogExpanded = true
            }
        }
    }

    LaunchedEffect(selectedTypeTab, startDate, endDate) {
        when (selectedTypeTab) {
            0 -> {
                selectedType = "Comparison"
                centerText = "Balance"
                total = transactionViewModel.getBalance(startDate, endDate)
                entries = transactionViewModel.getTotalSpendingAndEarning(startDate, endDate)
                    .toMutableList()
            }

            1 -> {
                selectedType = "Spending"
                centerText = "Total"
                total = transactionViewModel.getTotalSpend(startDate, endDate)
                entries =
                    transactionViewModel.getSpendingByCategory(startDate, endDate).toMutableList()
            }

            2 -> {
                selectedType = "Earning"
                centerText = "Total"
                total = transactionViewModel.getTotalEarn(startDate, endDate)
                entries =
                    transactionViewModel.getEarningByCategory(startDate, endDate).toMutableList()
            }
        }
    }

    if (customDateDialogExpanded) {
        CustomDateRangeDialog(
            onDismiss = { customDateDialogExpanded = false },
            onConfirm = { start, end ->
                startDate = start
                endDate = end
                customDateDialogExpanded = false
            }
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            Text(
                text = "Analytics",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 18.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))

            CustomSegmentedTabRow(
                tabTexts = timeTab,
                selectedTab = selectedTimeTab,
                onTabSelected = { selectedTimeTab = it }
            )
            Spacer(modifier = Modifier.height(18.dp))

            CustomSegmentedTabRow(
                tabTexts = typeTab,
                selectedTab = selectedTypeTab,
                onTabSelected = { selectedTypeTab = it }
            )
            Spacer(modifier = Modifier.height(18.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                factory = { context ->
                    val pieChart = com.github.mikephil.charting.charts.PieChart(context)

                    //slice thickness
//                    pieChart.holeRadius = 38f
//                    pieChart.transparentCircleRadius = 40f // optional

                    pieChart.setUsePercentValues(false)

                    //labels inside the slice
                    pieChart.setDrawEntryLabels(true)
                    pieChart.setEntryLabelTextSize(14f)
                    pieChart.setEntryLabelColor(android.graphics.Color.rgb(24, 49, 83))
                    pieChart.setEntryLabelTypeface(android.graphics.Typeface.DEFAULT_BOLD)

                    pieChart.legend.isEnabled = false
                    pieChart.description.isEnabled = false

                    //center text
                    pieChart.setDrawCenterText(true)
                    pieChart.setCenterTextSize(18f)
                    pieChart.setCenterTextColor(android.graphics.Color.rgb(24, 49, 83))
                    pieChart.setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)

                    pieChart
                },
                update = { pieChart ->
                    val dataSet = PieDataSet(entries, "")
                    dataSet.setColors(COMBINED_COLORS)
                    dataSet.sliceSpace = 3f
                    dataSet.selectionShift = 5f
                    dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                    dataSet.valueLinePart1Length = 0.3f
                    dataSet.valueLinePart2Length = 0.3f
                    dataSet.valueLineColor = android.graphics.Color.DKGRAY
                    dataSet.valueFormatter = object : IValueFormatter {
                        private val decimalFormat = DecimalFormat("#,###.##")

                        override fun getFormattedValue(
                            value: Float,
                            entry: Entry?,
                            dataSetIndex: Int,
                            viewPortHandler: ViewPortHandler?
                        ): String {
                            return "${decimalFormat.format(value)} $"
                        }
                    }

                    val data = PieData(dataSet)
                    data.setValueTextSize(16f)

                    pieChart.data = data
                    pieChart.centerText = "${centerText}\n$${total}"

                    pieChart.invalidate()

                }
            )
        }
    }
}
