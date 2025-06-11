package com.example.myapplication.screens.tabs


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.R
import com.example.myapplication.components.AdBanner
import com.example.myapplication.components.CustomSegmentedTabRow
import com.example.myapplication.data.datastore.UserPreferences
import com.example.myapplication.helpers.askHuggingFace
import com.example.myapplication.screens.dialogs.CustomDateRangeDialog
import com.example.myapplication.ui.theme.COMBINED_COLORS
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.White
import com.example.myapplication.viewmodel.CurrencyViewModel
import com.example.myapplication.viewmodel.transaction.TransactionViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun AnalyticsTab(
    transactionViewModel: TransactionViewModel,
    currencyViewModel: CurrencyViewModel,
    shortFormCurrency: String
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

    // --- AI Assistant State ---
    var aiDialogVisible by remember { mutableStateOf(false) }
    var aiResponse by remember { mutableStateOf<String?>(null) }
    var aiLoading by remember { mutableStateOf(false) }



    val context = LocalContext.current

    var shortFormCurrency by remember { mutableStateOf("CAD") }
    LaunchedEffect(Unit) {
        val defaultCurrency = UserPreferences.getCurrency(context)
        shortFormCurrency = currencyViewModel.getCurrencyShortForm(defaultCurrency)
    }
// --- Date range logic ---
    LaunchedEffect(selectedTimeTab) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        when (selectedTimeTab) {
            0 -> { startDate = null; endDate = null }
            1 -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis
                endDate = now
            }
            2 -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis
                endDate = now
            }
            3 -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis
                endDate = now
            }
            4 -> { customDateDialogExpanded = true }
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
                entries = transactionViewModel.getSpendingByCategory(startDate, endDate).toMutableList()
            }
            2 -> {
                selectedType = "Earning"
                centerText = "Total"
                total = transactionViewModel.getTotalEarn(startDate, endDate)
                entries = transactionViewModel.getEarningByCategory(startDate, endDate).toMutableList()
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
            AdBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Analytics",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                modifier = Modifier.align(Alignment.CenterHorizontally)
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

            // Pie chart
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                factory = { context ->
                    val pieChart = PieChart(context)
                    pieChart.setUsePercentValues(false)
                    pieChart.setDrawEntryLabels(true)
                    pieChart.setEntryLabelTextSize(14f)
                    pieChart.setEntryLabelColor(android.graphics.Color.rgb(24, 49, 83))
                    pieChart.setEntryLabelTypeface(android.graphics.Typeface.DEFAULT_BOLD)
                    pieChart.legend.isEnabled = false
                    pieChart.description.isEnabled = false
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
                    dataSet.valueLinePart1Length = 0.4f
                    dataSet.valueLinePart2Length = 0.3f
                    dataSet.valueLineColor = android.graphics.Color.DKGRAY
                    dataSet.valueFormatter = object : IValueFormatter {
                        override fun getFormattedValue(
                            value: Float,
                            entry: Entry?,
                            dataSetIndex: Int,
                            viewPortHandler: ViewPortHandler?
                        ): String {
                            return "${value.roundToInt()} $shortFormCurrency"
                        }
                    }

                    val data = PieData(dataSet)
                    data.setValueTextSize(16f)
                    pieChart.data = data
                    pieChart.centerText = "${centerText}\n${total.roundToInt()} $shortFormCurrency"
                    pieChart.invalidate()
                }
            )
            Spacer(modifier = Modifier.height(18.dp))

            // --- Category List with Proper Formatting and Spacing ---
            if (selectedType == "Spending" || selectedType == "Earning") {
                val list = if (selectedType == "Spending")
                    transactionViewModel.getSpendingWithLimitByCategory(startDate, endDate)
                else
                    transactionViewModel.getEarningWithTotalByCategory(startDate, endDate)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp), // More space between items
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp) // Padding around content
                ) {
                    items(list.size) { index ->
                        val (labelWithLimit, amount) = list[index]
                        val parts = labelWithLimit.split("|")
                        val category = parts.getOrNull(0) ?: "Unknown"
                        val limit = parts.getOrNull(1)?.toDoubleOrNull()
                        val effectiveLimit = if (limit != null && limit > 0) limit else total

                        Column(
                            modifier = Modifier
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = category,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f) // Ensures category takes available space
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = buildString {
                                        append("$${amount.roundToInt()} of $${effectiveLimit.roundToInt()} $shortFormCurrency")
                                        if (limit == null || limit == 0.0) append(" (total)")
                                    },
                                    fontSize = 14.sp,
                                    color = PrimaryBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1.2f), // Adjust weight as needed for layout balance
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val denominator = limit?.takeIf { it > 0 } ?: total.takeIf { it > 0 } ?: 1.0
                            val progress = (amount / denominator).coerceIn(0.0, 1.0)
                            LinearProgressIndicator(
                                progress = { progress.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp) // Shorter bar
                                    .clip(RoundedCornerShape(8.dp)),
                                color = Color(COMBINED_COLORS[index % COMBINED_COLORS.size]),
                                trackColor = Color(COMBINED_COLORS[index % COMBINED_COLORS.size]).copy(alpha = 0.3f),
                            )
                        }
                    }
                }

            }

            if (selectedType == "Comparison") {

            }
        }

        // --- FAB ---
        FloatingActionButton(
            onClick = { aiDialogVisible = true },
            containerColor = PrimaryBlue,
            contentColor = White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding() // ensures above system nav bar
                .padding(end = 24.dp, bottom = 96.dp), // 96.dp = height of
            shape = RoundedCornerShape(16.dp),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.sparkle),
                contentDescription = "Ask AI",
                modifier = Modifier.size(22.dp)
            )
        }

        // --- AI Dialog (scrollable, formatted answer!) ---
        if (aiDialogVisible) {
            Dialog(
                onDismissRequest = {
                    aiDialogVisible = false
                    aiResponse = null
                    aiLoading = false
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(0.95f)
                        .wrapContentHeight()
                        .heightIn(max = 400.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(White)
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth()
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    aiDialogVisible = false
                                    aiResponse = null
                                    aiLoading = false
                                },
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Close",
                                    tint = PrimaryBlue
                                )
                            }
                            Text(
                                "Ask AI for Advice",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center
                            )
                        }
                        var userInput by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            label = { Text("Your question") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            textStyle = TextStyle(fontSize = 18.sp)
                        )
                        if (aiLoading) {
                            CircularProgressIndicator(Modifier.padding(top = 18.dp))
                        } else if (aiResponse != null) {
                            // --- Improved AI Answer Formatting ---
                            val lines = aiResponse!!.lines()
                            Column(
                                modifier = Modifier
                                    .padding(top = 18.dp)
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFFF7F9FA),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "AI Advice:",
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                lines.forEach { line ->
                                    val trimmed = line.trim()
                                    if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                                        Row(Modifier.padding(bottom = 4.dp)) {
                                            Text(
                                                "â€¢ ",
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlue
                                            )
                                            Text(
                                                trimmed.removePrefix("-").removePrefix("*").trim(),
                                                color = Color.DarkGray
                                            )
                                        }
                                    } else if (trimmed.isNotEmpty()) {
                                        Text(
                                            trimmed,
                                            color = Color.DarkGray,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    aiDialogVisible = false
                                    aiResponse = null
                                    aiLoading = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryRed,
                                    contentColor = White
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    aiLoading = true
                                    // Build the context summary, add currency if you want
                                    val contextSummary = transactionViewModel.getFinancialSummary(startDate, endDate)
                                        .replace("spent (limit:", "spent (limit: $shortFormCurrency")
                                        .replace("Total spent:", "Total spent: $shortFormCurrency")
                                        .replace("Total earned:", "Total earned: $shortFormCurrency")
                                        .replace("Net balance:", "Net balance: $shortFormCurrency")
                                    val fullPrompt = """
            Here is my recent financial data:
            $contextSummary

            Now answer this question: $userInput
        """.trimIndent()
                                    askHuggingFace(fullPrompt) { reply ->
                                        aiResponse = reply
                                        aiLoading = false
                                    }
                                },
                                enabled = userInput.isNotBlank() && !aiLoading,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Ask", color = White, fontWeight = FontWeight.Bold)
                            }

                        }
                    }
                }
            }
        }
    }
}