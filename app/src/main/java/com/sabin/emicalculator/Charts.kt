package com.sabin.emicalculator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun DonutChart(shares: Shares, modifier: Modifier = Modifier) {
    val principalColor = MaterialTheme.colorScheme.primary
    val interestColor = MaterialTheme.colorScheme.tertiary
    Column(modifier.semantics { contentDescription = ChartData.donutDescription(shares) }) {
        Canvas(Modifier.size(160.dp)) {
            val stroke = 28.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val sweep = (shares.principalPercent / 100 * 360).toFloat()
            drawArc(principalColor, -90f, sweep, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(interestColor, -90f + sweep, 360f - sweep, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
        }
        Text("Principal %.0f%%  •  Interest %.0f%%".format(shares.principalPercent, shares.interestPercent),
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun BalanceLineChart(points: List<BalancePoint>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outline
    Column(modifier.semantics { contentDescription = ChartData.lineDescription(points) }) {
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
            val max = points.maxOf { it.balance }.coerceAtLeast(1.0)
            val lastYear = points.last().year.coerceAtLeast(1)
            drawLine(axisColor, Offset(0f, size.height), Offset(size.width, size.height), 2f)
            val path = Path()
            points.forEachIndexed { i, pt ->
                val x = pt.year.toFloat() / lastYear * size.width
                val y = size.height - (pt.balance / max).toFloat() * size.height
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, lineColor, style = Stroke(4.dp.toPx()))
        }
        Text("Outstanding balance by year (0–${points.last().year})", style = MaterialTheme.typography.bodySmall)
    }
}
