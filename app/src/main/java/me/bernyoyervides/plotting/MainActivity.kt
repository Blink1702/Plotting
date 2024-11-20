package me.bernyoyervides.plotting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.bernyoyervides.plotting.ui.theme.PlottingTheme
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Constraints

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val plot : PlotModel = PlotModel()
        enableEdgeToEdge()
        setContent {
            PlottingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Graph(plot = plot, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Graph(plot : PlotModel, modifier: Modifier = Modifier) {
    val equ = remember { mutableStateOf("") }
    var slider = remember { mutableFloatStateOf(500f) }
    var currColorId = remember { mutableIntStateOf(0) }
    var offsetX = remember { mutableStateOf(0f) }
    var offsetY = remember { mutableStateOf(0f) }

    Row{
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX.value += dragAmount.x
                        offsetY.value += dragAmount.y
                    }
                }
            )
                {
                drawLine(
                    brush = Brush.linearGradient(listOf(Color.Black, Color.Black)),
                    start = Offset(center.x.times(size.width*-1).plus(offsetX.value),center.y.plus(offsetY.value)),
                    end = Offset(center.x.times(size.width).plus(offsetX.value),center.y.plus(offsetY.value)),
                    strokeWidth = 5f
                )
                drawLine(
                    brush = Brush.linearGradient(listOf(Color.Black, Color.Black)),
                    start = Offset(center.x.plus(offsetX.value),center.y.times(size.height*-1).plus(offsetY.value)),
                    end = Offset(center.x.plus(offsetX.value),center.y.times(size.height).plus(offsetY.value)),
                    strokeWidth = 5f
                )
                plot.lines.forEach{ l ->
                    l.drawIn(this,slider.floatValue,offsetX.value,offsetY.value)
                }
            }
        }
        Column(verticalArrangement = Arrangement.Top, modifier = modifier.weight(1f).background(Color.White)
            .fillMaxSize()
            .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(100.dp))
            plot.plots.forEach { p ->
                p.CreateField()
            }
            TextField(value = equ.value, onValueChange = {equ.value = it},label = {Text("Enter equation")},modifier = Modifier.fillMaxWidth())
            TextButton(onClick = {plot.drawPlot(equ.value,currColorId.intValue);equ.value="";currColorId.intValue++}, modifier = Modifier.padding(8.dp)) { Text("Add to Plot")}
            Slider(value = slider.floatValue, valueRange = 10f..1000f, onValueChange = {slider.floatValue = it})
            TextButton(onClick =  {plot.resetPlot();offsetX.value=0f;offsetY.value=0f}, modifier = Modifier.padding(8.dp)) { Text("Start Over")}
        }

    }
    if(currColorId.intValue>=6){
        currColorId.intValue=0
    }

}

