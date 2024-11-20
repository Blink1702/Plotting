package me.bernyoyervides.plotting

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlotModel : ViewModel() {
    var plots = mutableStateListOf<PlotInfo>()
    var lines = mutableListOf<Line>()

    fun drawPlot(equation : String, colorId : Int){
        plots.add(PlotInfo(equation,colorId))
        lines.add(Line(equation,colorId))
    }
    fun resetPlot(){
        plots.clear()
        lines.clear()
    }
    fun convertCoords(x: Float, y: Float, drawScope: DrawScope){
        val xorigin = drawScope.center.x
        val yOrigin = drawScope.center.y
        Offset(xorigin.plus(x),yOrigin.plus(y))
    }


}

