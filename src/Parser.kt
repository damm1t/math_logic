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
            '!', '&', '|', '(', ')' -> {
                cancelBuildVariable()
                tmp_tokens.add(s[i].toString())
            }
            '-' -> {
                cancelBuildVariable()
                tmp_tokens.add("->")
            }
            '>', ' ', '\n', '\t', '\r' -> skip()
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
        else -> {
            val exp = Variable(curToken())
            skipToken()
            exp
        }
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


fun gliv(lines: List<String>): StringBuilder {
    val res = StringBuilder()

    val list: MutableList<Expression> = parseHead(lines[0])
    val head = list[0]

    for (i in 1 until list.size) {
        res.append(list[i].toStringImpl())
        if (i != list.size - 1)
            res.append(",")
    }
    res.append("|-")
    res.append("!!" + head.toStringImpl())
    res.append(System.lineSeparator())

    for (index in 1 until lines.size) {
        val line = lines[index]
        val annotation = annotate(line, index)
        val impl = line.parse()

        if (annotation is Axiom || annotation is Assumption) { // b
            res.append(addAx(impl.toStringImpl(), "ax"))
        }
        if (annotation is None) { // actually ax10
            res.append(addAx(getAnyName(impl), "ax10"))
        }
        if (annotation is MP) { // d
            val implication = lines[annotation.number].parse()
            if (implication is Implication) {
                res.append(addMP(implication.left.toStringImpl(), implication.right.toStringImpl()))
            }
        }
    }
    return res
}


fun optimize(lines: List<String>): StringBuilder {
    val res = StringBuilder()

    realIndex = IntArray(lines.size) { i -> i }
    val list: MutableList<Expression> = parseHead(lines[0])
    val usedLines = BooleanArray(lines.size) { false }
    val head = list[0]
    val outLines = Array(lines.size) { "" }
    var last = -1
    // Correct check
    val tree = Array(lines.size) { Pair(-1, -1) }
    for (index in 1 until lines.size) {
        val line = lines[index]
        val annotation = annotateLine(line, index)
        if (annotation is None) {
            endImprove = -1
            System.err.println(line)
            break
        }
        if (annotation is MP) {
            tree[index] = Pair(annotation.number, annotation.number2)
        }
        val impl = line.parse()
        outLines[index] = impl.toStringImpl()
        if (head.hashCode() == impl.hashCode()) {
            last = index
            if (endImprove == -1)
                endImprove = index
        }
    }
    if (endImprove == -1 || last != lines.size - 1) {
        res.append("Proof is incorrect")
        return res
    }
    usedLines[endImprove] = true
    // Optimize
    for (index in endImprove downTo 1) {
        if (tree[index].first != -1 && usedLines[index]) {
            usedLines[tree[index].first] = true
            usedLines[tree[index].second] = true
        }
    }
    var d = 0
    for (index in 1 until endImprove + 1) {
        if (!usedLines[index]) d++
        else realIndex[index] = index - d
    }

    for (i in 1 until list.size) {
        res.append(list[i].toStringImpl())
        if (i != list.size - 1)
            res.append(",")
    }
    res.append("|-")
    res.append(head.toStringImpl())
    res.append(System.lineSeparator())

    for (index in 1 until endImprove + 1) {
        val line = lines[index]
        if (usedLines[index]) {
            val annotation = annotateLine(line, index)
            if (annotation is MP) {
                val left = realIndex[tree[index].first]
                val right = realIndex[tree[index].second]
                res.append(outLines[index]) //res.append("[${realIndex[index]}. ${MP(left, right)}] ${outLines[index]}")
            } else res.append(outLines[index]) //res.append("[${realIndex[index]}. ${annotateLine(line, index)}] ${outLines[index]}")
            res.append(System.lineSeparator())
        }
    }
    return res
}