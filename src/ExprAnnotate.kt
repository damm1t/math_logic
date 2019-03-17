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


val listAxioms = listOf(
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

private fun checkAxiom(expr: Expression): AnnotationInfo? {
    for (i in 0 until listAxioms.size) {
        varToNode.clear()
        if (isomorphismCheck(expr, listAxioms[i]))
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
                return MP(realIndex[indexByImplication[impl]!!], realIndex[trueIndex])
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
