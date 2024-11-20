package me.bernyoyervides.plotting

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.pow

val colorList = listOf<Color>(Color.Blue,Color.Red,Color.Green, Color.Yellow, Color.Magenta,Color.Cyan)

class Line(val str: String, val colorId: Int) {
    val p = Parser(str=str)
    val root = p.parse()
    var ptList = mutableListOf<Pair<Float, Float>>()

    fun plotting(scope: DrawScope) {
        var x = (scope.drawContext.size.width/2)*-1
        while(x<=(scope.drawContext.size.width/2)){
            ptList.add(Pair(x, root?.evalAt(x)!!))
            x += 0.1f
        }
    }

    fun drawIn(scope: DrawScope,scale : Float,offsetX : Float,offsetY : Float){
        plotting(scope)
        val path = Path()
        path.moveTo(scope.center.x.plus(ptList.first().first.times(scale)).plus(offsetX),scope.center.y.minus(ptList.first().second.times(scale)).plus(offsetY))
        for(n in 1 until ptList.count()) {
            path.lineTo(scope.center.x.plus(ptList[n].first.times(scale)).plus(offsetX), scope.center.y.minus(ptList[n].second.times(scale)).plus(offsetY))
        }
        path.close()
        scope.drawPath(path, color = colorList[this.colorId], style = Stroke(width = 3f))
    }

}

class PlotInfo(var eq : String, val colorId: Int){
    @Composable
    fun CreateField(){
        TextField(value = eq, onValueChange = {eq = it}, readOnly = eq.isNotEmpty(), colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = colorList[colorId]) ,modifier = Modifier.fillMaxWidth())
    }
}

class Token(val value : Float?,val oper : Char? = null) {
    fun isNumber() : Boolean { return value != null }
    fun isX() : Boolean { return value == null && oper == null }
}

class Tokenizer(val str : String) {
    var n = 0

    fun getNextToken() : Token? {
        while (n < str.length) {
            val ch = str[n]
            n += 1
            if (ch == ' ') {
                continue
            }
            if (ch in '0'..'9' || ch == '.') {
                var sawDot = false
                var numStr = ch.toString()
                if(ch == '.') {
                    sawDot = true
                }
                while(n < str.length) {
                    val nextCh = str[n]
                    if(sawDot && nextCh >= '0' && nextCh <= '9') {
                        numStr += nextCh
                        n += 1
                    } else if (!sawDot && nextCh == '.') {
                        sawDot = true
                        numStr += nextCh
                        n += 1
                    } else if (nextCh >= '0' && nextCh <= '9') {
                        numStr += nextCh
                        n += 1
                    } else {
                        break
                    }
                }
                return Token(value = numStr.toFloat())
            } else if (ch == 'x') {
                return Token(value = null)
            } else if (ch == '-' || ch == '+' || ch == '*' || ch == '^') {
                return Token(value = null,oper = ch)
            }
        }
        return null
    }

    fun getAllTokens() : List<Token> {
        val ts = mutableListOf<Token>()
        while(true) {
            val t = getNextToken()

            if (t != null) {
                ts.add(t)
            } else {
                break
            }
        }
        return ts.toList()
    }
}

open class Node()  {
    open fun evalAt(x:Float) : Float {
        return 0.0f
    }
}

class OperNode(val oper : Char,val left : Node,val right: Node) : Node() {
    override fun evalAt(x: Float) : Float {
        return when(oper) {
            '+' ->
                left.evalAt(x= x) + right.evalAt(x= x)

            '-' ->
                left.evalAt(x= x) - right.evalAt(x= x)

            '*' ->
                left.evalAt(x= x) * right.evalAt(x= x)

            '^' ->
                left.evalAt(x= x).pow(right.evalAt(x= x))

            else ->
                0.0f
        }
    }
}

class UnaryMinusNode(val arg : Node) : Node() {
    override fun evalAt(x: Float) : Float {
        return -1.0f*arg.evalAt(x= x)
    }
}

class NumNode(val value : Float) : Node() {
    override fun evalAt(x: Float) : Float {
        return value
    }
}

class VarNode() : Node() {
    override fun evalAt(x: Float) : Float {
        return x
    }
}

class Parser {
    var tokens = listOf<Token>()
    var n : Int = 0

    constructor(str: String) {
        val t : Tokenizer = Tokenizer(str=str)
        tokens = t.getAllTokens()
    }

    fun parse() : Node? {
        return sum()
    }

    private fun sum() : Node? {
        if (n == tokens.size) {
            return null
        }
        val start = n
        val left = product()
        if(left != null) {
            var result : Node = left
            while(n < tokens.size) {
                val o = tokens[n].oper
                if(o != null) {
                    n += 1
                    if(o == '+') {
                        val right = product()
                        if(right != null) {
                            result = OperNode(oper= '+', left= result, right= right)
                        } else {
                            return null
                        }
                    } else if(o == '-') {
                        val right = product()
                        if(right != null) {
                            result = OperNode(oper= '-', left= result, right= right)
                        } else {
                            return null
                        }
                    } else {
                        n -= 1
                        return null
                    }
                } else {
                    return null
                }
            }
            return result
        }
        n = start
        return null
    }

    private fun product() : Node? {
        if(n == tokens.size) {
            return null
        }
        val start = n
        val left = term()
        if (left != null) {
            if(n < tokens.size) {
                val o = tokens[n].oper
                if(o != null) {
                    if(o == '*') {
                        n += 1
                        val right = term()
                        if(right != null) {
                            return OperNode(oper= '*', left= left, right= right)
                        }
                    }
                }
            }
            return left
        }
        n = start
        return null
    }

    private fun term() : Node? {
        if(n == tokens.size) {
            return null
        }
        val t = pow()
        if(t != null) {
            return t
        }
        val nm = number()
        if(nm != null) {
            return nm
        }
        val v = variable()
        if(v != null) {
            return v
        }
        val o = tokens[n].oper
        if(o != null)  {
            if(o == '-') {
                n += 1
                val right = term()
                if(right != null) {
                    return UnaryMinusNode(arg=right)
                } else {
                    n -= 1
                }
                return null
            }
        }
        return null
    }

    private fun pow() : Node? {
        if(n == tokens.size) {
            return null
        }
        val start = n
        var left = variable()
        if(left != null) {
            if(n < tokens.size) {
                val o = tokens[n].oper
                if(o != null){
                    if(o == '^') {
                        n += 1
                        val right = number()
                        if(right != null) {
                            return OperNode(oper='^', left=left, right=right)
                        }
                    }
                }
            }
        }
        n = start
        return null
    }

    private fun number() : Node? {
        if(n == tokens.size) {
            return null
        }
        if(tokens[n].isNumber()) {
            val v = tokens[n].value
            n += 1
            return NumNode(value=v!!)
        }
        return null
    }

    private fun variable() : Node? {
        if(n == tokens.size) {
            return null
        }
        if(tokens[n].isX()) {
            n += 1
            return VarNode()
        }
        return null
    }
}
