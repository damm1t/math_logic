private var tmp_tokens = arrayListOf<String>()
private val skip = {}
private val curVariable = StringBuilder()

private fun tokenize(s: String): List<String> {
    tmp_tokens.clear()
    val cancelBuildVariable = {
        if (curVariable.isNotEmpty()) {
            tmp_tokens.add(curVariable.toString())
            curVariable.setLength(0)
        }
    }
    for (i in 0 until s.length) {
        when (s[i]) {
            '!', '&', '|', '(', ')' -> cancelBuildVariable().also { tmp_tokens.add(s[i].toString()) }
            '-' -> if (i + 1 < s.length && s[i + 1] == '>') cancelBuildVariable().also { tmp_tokens.add("->") }
            else System.err.println("Unsupported - on position $i in $s")
            '>' -> if (i > 0 && s[i - 1] == '-') skip()
            else System.err.println("Unsupported > on position $i in $s")
            ' ', '\n', '\t', '\r' -> skip()
            else -> curVariable.append(s[i])
        }
    }
    cancelBuildVariable()
    return tmp_tokens
}

var left = 0
var tokens: List<String> = listOf()
fun skipToken() {
    left++
}

fun isEnd() = left == tokens.size
fun curToken() = tokens[left]

private fun parseNegation(): Expression {
    return when (curToken()) {
        "!" -> {
            skipToken()
            Negation(parseNegation())
        }
        "(" -> {
            skipToken()
            val exp = parseExpression()
            skipToken()
            exp
        }
        else -> Variable(curToken()).also { skipToken() }
    }
}

private fun parseConjuction(): Expression {
    var expression = parseNegation()
    while (!isEnd() && curToken() == "&") {
        skipToken()
        expression = Conjunction(expression, parseNegation())
    }
    return expression
}

private fun parseDisjunction(): Expression {
    var expression = parseConjuction()
    while (!isEnd() && curToken() == "|") {
        skipToken()
        expression = Disjunction(expression, parseConjuction())
    }
    return expression
}

private fun parseExpression(): Expression {
    val expression = parseDisjunction()
    if (!isEnd() && curToken() == "->") {
        skipToken()
        return Implication(expression, parseExpression())
    }
    return expression
}


fun String.parse(): Expression {
    tokens = tokenize(this)
    left = 0
    return parseExpression()
}
