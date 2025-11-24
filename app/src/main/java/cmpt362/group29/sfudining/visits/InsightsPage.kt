package cmpt362.group29.sfudining.visits

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cmpt362.group29.sfudining.ui.theme.SFURed
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsPage(
    viewModel: VisitViewModel,
    modifier: Modifier = Modifier
) {

    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedMetric by remember { mutableStateOf(Metric.SPENDING) }
    var selectedMode by remember { mutableStateOf(Mode.DAILY) }
    val visitsThisMonth = remember(viewModel.visits, currentMonth) {
        val cal = Calendar.getInstance()
        viewModel.visits.filter { v ->
            v.datetime.let {
                cal.time = it
                cal.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                        cal.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
            }
        }
    }

    val aggregatedValues = remember(visitsThisMonth, selectedMetric, selectedMode) {
        when (selectedMode) {
            Mode.DAILY, Mode.CUMULATIVE -> {
                val dayMap = mutableMapOf<Int, Double>()
                val cal = Calendar.getInstance()

                visitsThisMonth.forEach { visit ->
                    cal.time = visit.datetime
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    val value = when (selectedMetric) {
                        Metric.SPENDING -> visit.totalCost ?: 0.0
                        Metric.CALORIES -> visit.totalCal?.toDouble() ?: 0.0
                        Metric.VISITS -> 1.0
                    }
                    dayMap[day] = (dayMap[day] ?: 0.0) + value
                }

                val maxDay = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                val dailyValues = (1..maxDay).map { day -> dayMap[day] ?: 0.0 }

                if (selectedMode == Mode.CUMULATIVE) {
                    dailyValues.runningReduce { acc, v -> acc + v }
                } else dailyValues
            }

            Mode.PER_VISIT -> {
                visitsThisMonth.map { visit ->
                    when (selectedMetric) {
                        Metric.SPENDING -> visit.totalCost ?: 0.0
                        Metric.CALORIES -> visit.totalCal?.toDouble() ?: 0.0
                        Metric.VISITS -> 1.0
                    }
                }
            }
        }
    }


    val monthFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val monthLabel = monthFormatter.format(currentMonth.time)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Month Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = {
                currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }

            Text(
                text = monthLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }

            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (visitsThisMonth.isEmpty()) {
                Text(
                    text = "No data for this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                InsightsGraph(
                    dataPoints = aggregatedValues,
                    selectedMetric = selectedMetric,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Metric Tabs (y axis)
        val metricOptions = Metric.entries.toTypedArray()
        TabRow(
            selectedTabIndex = metricOptions.indexOf(selectedMetric),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[metricOptions.indexOf(selectedMetric)])
                )
            }
        ) {
            metricOptions.forEachIndexed { _, metric ->
                Tab(
                    selected = metric == selectedMetric,
                    onClick = { selectedMetric = metric },
                    text = { Text(metric.label) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Mode Segmented Buttons (x axis)
        val visibleModes = if (selectedMetric == Metric.VISITS) {
            Mode.entries.filter { it != Mode.PER_VISIT }
        } else {
            Mode.entries.toList()
        }

        SingleChoiceSegmentedButtonRow {
            visibleModes.forEachIndexed { index, mode ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index, visibleModes.size),
                    onClick = { selectedMode = mode },
                    selected = selectedMode == mode,
                    label = { Text(mode.label) }
                )
            }
        }

        // Ensure selectedMode is valid if metric changes
        LaunchedEffect(selectedMetric) {
            if (selectedMetric == Metric.VISITS && selectedMode == Mode.PER_VISIT) {
                selectedMode = Mode.DAILY
            }
        }

        Spacer(Modifier.height(20.dp))

        StatsSummaryCard(
            metric = selectedMetric,
            aggregatedValues = aggregatedValues,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun InsightsGraph(
    dataPoints: List<Double>,
    selectedMetric: Metric,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        LineChart(
            modifier = Modifier.fillMaxSize(),
            data = remember(dataPoints, selectedMetric) {
                listOf(
                    Line(
                        label = selectedMetric.label,
                        values = dataPoints,
                        color = SolidColor(SFURed),
                        firstGradientFillColor = SFURed.copy(alpha = 0.3f),
                        secondGradientFillColor = Color.Transparent,
                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                        curvedEdges = true,
                        dotProperties = DotProperties(
                            enabled = true,
                            color = SolidColor(SFURed),
                            strokeColor = SolidColor(Color.White),
                            strokeWidth = 2.dp,
                            radius = 6.dp
                        ),
                        strokeAnimationSpec = tween(1000),
                        gradientAnimationDelay = 500
                    )
                )
            },
            animationMode = AnimationMode.Together { it * 100L },
            zeroLineProperties = ZeroLineProperties(
                enabled = true,
                color = SolidColor(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            ),

            minValue = 0.0,
            maxValue = (dataPoints.maxOrNull() ?: 0.0) * 1.2
        )
    }
}

@Composable
fun StatsSummaryCard(
    metric: Metric,
    aggregatedValues: List<Double>,
    modifier: Modifier = Modifier
) {
    if (aggregatedValues.isEmpty()) return

    val sortedValues = aggregatedValues.sorted()
    val total = aggregatedValues.sum()
    val average = total / aggregatedValues.size
    val median = if (aggregatedValues.size % 2 == 0) {
        val mid = aggregatedValues.size / 2
        (sortedValues[mid - 1] + sortedValues[mid]) / 2
    } else sortedValues[aggregatedValues.size / 2]
    val min = sortedValues.first()
    val max = sortedValues.last()

    val valueFormatter: (Double) -> String = { v ->
        when (metric) {
            Metric.SPENDING -> "$${"%.2f".format(v)}"
            Metric.CALORIES -> if (v % 1.0 == 0.0) "${v.toInt()} kcal" else "${"%.2f".format(v)} kcal"
            Metric.VISITS -> "%.2f".format(v)
        }
    }

    val avgDisplay = if (metric == Metric.VISITS) {
        // average visits per day
        "%.2f".format(total / aggregatedValues.size)
    } else {
        valueFormatter(average)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text("Total ${metric.label.lowercase()}: ${valueFormatter(total)}")
            Text("Average: $avgDisplay")
            Text("Median: ${valueFormatter(median)}")
            Text("Max: ${valueFormatter(max)}")
            Text("Min: ${valueFormatter(min)}")
        }
    }
}


// Enums for Metric and Mode selections
enum class Metric(val label: String) {
    SPENDING("Spending"),
    CALORIES("Calories"),
    VISITS("Visits")
}

enum class Mode(val label: String) {
    DAILY("Daily"),
    CUMULATIVE("Cumulative"),
    PER_VISIT("Per Visit")
}
