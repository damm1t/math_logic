import java.nio.file.Files
import java.nio.file.Paths

sealed class AnnotationInfo {
    abstract val number: Int
    abstract val number2: Int

}

class Assumption(override val number: Int) : AnnotationInfo() {
    override val number2 = -1
    override fun toString() = "Hypothesis $number"
}

class Axiom(override val number: Int) : AnnotationInfo() {
    override val number2 = -1
    override fun toString() = "Ax. sch. $number"
}

class MP(override val number: Int, override val number2: Int) : AnnotationInfo() {
    override fun toString() = "M.P. $number, $number2"
}

class None : AnnotationInfo() {
    override val number = -1
    override val number2 = -1
    override fun toString() = "Proof is incorrect"
}

val indexByNode = hashMapOf<Expression, Int>()
val indexByImplication = hashMapOf<Expression, Int>()
val indexToAssumption = hashMapOf<Expression, Int>()

val implications = hashMapOf<Expression, HashSet<Implication>>()


val listAxiomsKIV = listOf(
    Implication(Variable("A"), Implication(Variable("B"), Variable("A"))),
    Implication(
        Implication(Variable("A"), Variable("B")), Implication(
            Implication(Variable("A"), Implication(Variable("B"), Variable("C"))),
            Implication(Variable("A"), Variable("C"))
        )
    ),
    Implication(Variable("A"), Implication(Variable("B"), Conjunction(Variable("A"), Variable("B")))),
    Implication(Conjunction(Variable("A"), Variable("B")), Variable("A")),
    Implication(Conjunction(Variable("A"), Variable("B")), Variable("B")),
    Implication(Variable("A"), Disjunction(Variable("A"), Variable("B"))),
    Implication(Variable("B"), Disjunction(Variable("A"), Variable("B"))),
    Implication(
        Implication(Variable("A"), Variable("C")),
        Implication(
            Implication(Variable("B"), Variable("C")),
            Implication(Disjunction(Variable("A"), Variable("B")), Variable("C"))
        )
    ),
    Implication(
        Implication(Variable("A"), Variable("B")),
        Implication(Implication(Variable("A"), Negation(Variable("B"))), Negation(Variable("A")))
    ),
    Implication(Negation(Negation(Variable("A"))), Variable("A"))
)

val listAxiomsIIV = listOf(
    Implication(Variable("A"), Implication(Variable("B"), Variable("A"))),
    Implication(
        Implication(Variable("A"), Variable("B")), Implication(
            Implication(Variable("A"), Implication(Variable("B"), Variable("C"))),
            Implication(Variable("A"), Variable("C"))
        )
    ),
    Implication(Variable("A"), Implication(Variable("B"), Conjunction(Variable("A"), Variable("B")))),
    Implication(Conjunction(Variable("A"), Variable("B")), Variable("A")),
    Implication(Conjunction(Variable("A"), Variable("B")), Variable("B")),
    Implication(Variable("A"), Disjunction(Variable("A"), Variable("B"))),
    Implication(Variable("B"), Disjunction(Variable("A"), Variable("B"))),
    Implication(
        Implication(Variable("A"), Variable("C")),
        Implication(
            Implication(Variable("B"), Variable("C")),
            Implication(Disjunction(Variable("A"), Variable("B")), Variable("C"))
        )
    ),
    Implication(
        Implication(Variable("A"), Variable("B")),
        Implication(Implication(Variable("A"), Negation(Variable("B"))), Negation(Variable("A")))
    ),
    Implication(Variable("A"), Implication(Negation(Variable("A")), Variable("B")))
)

val listAxiom9 = listOf(
    Implication(Variable("A"), Implication(Variable("B"), Variable("A"))),
    Implication(
        Implication(Variable("A"), Variable("B")), Implication(
            Implication(Variable("A"), Implication(Variable("B"), Variable("C"))),
            Implication(Variable("A"), Variable("C"))
        )
    ),
    Implication(Variable("A"), Implication(Variable("B"), Conjunction(Variable("A"), Variable("B")))),
    Implication(Conjunction(Variable("A"), Variable("B")), Variable("A")),
    Implication(Conjunction(Variable("A"), Variable("B")), Variable("B")),
    Implication(Variable("A"), Disjunction(Variable("A"), Variable("B"))),
    Implication(Variable("B"), Disjunction(Variable("A"), Variable("B"))),
    Implication(
        Implication(Variable("A"), Variable("C")),
        Implication(
            Implication(Variable("B"), Variable("C")),
            Implication(Disjunction(Variable("A"), Variable("B")), Variable("C"))
        )
    ),
    Implication(
        Implication(Variable("A"), Variable("B")),
        Implication(Implication(Variable("A"), Negation(Variable("B"))), Negation(Variable("A")))
    )
)

fun parseHead(header: String): MutableList<Expression> {
    val end = header.indexOf("|-")
    val list = mutableListOf<Expression>()
    list.add(header.substring(end + 2).parse())
    if (end < 1)
        return list
    header.substring(0, end).split(',').forEachIndexed { id, s ->
        val expr = s.parse()
        list.add(expr)
        indexToAssumption[expr] = id + 1
    }
    return list
}

val varToNode = hashMapOf<String, Expression>()

fun isomorphismCheck(node: Expression, otherNode: Expression): Boolean = when (otherNode) {
    is Variable -> {
        val variable = otherNode.name
        if (varToNode.containsKey(variable))
            varToNode[variable] == node
        else {
            varToNode[variable] = node
            true
        }
    }
    is Binary -> node is Binary && node.opcode == otherNode.opcode
            && isomorphismCheck(node.left, otherNode.left) && isomorphismCheck(node.right, otherNode.right)
    is Negation -> node is Negation && isomorphismCheck(node.negated, otherNode.negated)
    else -> false
}

fun getAnyName(expr: Expression): String {
    return when (expr) {
        is Variable -> {
            expr.name
        }
        is Binary -> getAnyName(expr.left)
        is Negation -> getAnyName(expr.negated)
        else -> ""
    }
}


fun substitutionAx(expr: Expression, variable: String) {
    when (expr) {
        is Variable -> {
            expr.name = variable
        }
        is Binary -> {
            substitutionAx(expr.left, variable)
            substitutionAx(expr.right, variable)
        }
        is Negation -> substitutionAx(expr.negated, variable)
    }
}

fun addAx(variable: String, file: String): StringBuilder {
    val res = StringBuilder()
    Files.newBufferedReader(Paths.get("$file.txt")).use { fin ->
        val lines = fin.readLines()
        for (line in lines) {
            val expr = line.parse()
            substitutionAx(expr, variable)
            res.append(expr.toStringImpl())
            res.append(System.lineSeparator())
        }
    }
    return res
}

fun addMP(left: String, right: String): StringBuilder {
    val res = StringBuilder()
    val file = "mp"
    Files.newBufferedReader(Paths.get("$file.txt")).use { fin ->
        val lines = fin.readLines()
        for (line in lines) {
            val expr = line.parse()
            substitutionMP(expr, left, right)
            res.append(expr.toStringImpl())
            res.append(System.lineSeparator())
        }
    }
    return res
}

fun substitutionMP(expr: Expression, left: String, right: String) {
    when (expr) {
        is Variable -> {
            expr.name = if (expr.name == "A")
                left
            else
                right
        }
        is Binary -> {
            substitutionMP(expr.left, left, right)
            substitutionMP(expr.right, left, right)
        }
        is Negation -> substitutionMP(expr.negated, left, right)
    }
}

private fun checkAxiom(expr: Expression): AnnotationInfo? {
    for (i in 0 until listAxiomsIIV.size) {
        varToNode.clear()
        if (isomorphismCheck(expr, listAxiomsIIV[i]))
            return Axiom(i + 1)
    }
    return null
}

private fun checkAxiom9(expr: Expression): AnnotationInfo? {
    for (i in 0 until listAxiom9.size) {
        varToNode.clear()
        if (isomorphismCheck(expr, listAxiomsKIV[i]))
            return Axiom(i + 1)
    }
    return null
}


fun checkModusPonens(expr: Expression): AnnotationInfo? {
    if (implications.containsKey(expr)) {
        for (impl in implications[expr]!!) {
            val reason = impl.left
            val trueIndex = indexByNode[reason]
            if (trueIndex != null) {
                return MP(indexByImplication[impl]!!, trueIndex)
            }
        }
    }
    return null
}

fun checkAssumption(expr: Expression) = indexToAssumption[expr]?.let(::Assumption)

fun setTrue(expr: Expression, index: Int) {
    indexByNode.putIfAbsent(expr, index)
}

fun tryAddToImplications(expr: Expression, index: Int) {
    if (expr is Implication) {
        val consequence = expr.right
        implications.putIfAbsent(consequence, hashSetOf())
        implications[consequence]!!.add(expr)
        if (!indexByImplication.containsKey(expr)) indexByImplication[expr] = index
    }
}

fun annotateLine(line: String, index: Int): AnnotationInfo {
    val expr = line.parse()
    val result = checkAssumption(expr) ?: checkAxiom(expr) ?: checkModusPonens(expr)
    setTrue(expr, index)
    tryAddToImplications(expr, index)
    return result ?: None()
}

fun annotate(line: String, index: Int): AnnotationInfo {
    val expr = line.parse()
    val result = checkAssumption(expr) ?: checkAxiom9(expr) ?: checkModusPonens(expr)
    setTrue(expr, index)
    tryAddToImplications(expr, index)
    return result ?: None()
}
