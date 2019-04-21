private val hashes = hashMapOf<ArrayList<Int>, Int>()

abstract class Expression(val hash: Int) {
    override fun hashCode() = hash
    abstract fun toTree(): String
    abstract fun toStringImpl(): String
    open val getSymbol: String = ""
    override fun equals(other: Any?) = other is Expression && hash == other.hash

}

fun calcHash(arr: ArrayList<Int>): Int {
    hashes.putIfAbsent(arr, hashes.size)
    return hashes[arr]!!
}

open class Binary(val opcode: Int, val left: Expression, val right: Expression) : Expression(
        hash = calcHash(arrayListOf(opcode, left.hash, right.hash))) {
    override fun toTree(): String {
        return "($getSymbol,${left.toTree()},${right.toTree()})"
    }

    override fun toStringImpl(): String {
        return "(${left.toStringImpl()}$getSymbol${right.toStringImpl()})"
    }
}

class Disjunction(left: Expression, right: Expression) : Binary(1, left, right) {
    override val getSymbol get() = "|"
}

class Conjunction(left: Expression, right: Expression) : Binary(2, left, right) {
    override val getSymbol get() = "&"
}

class Implication(left: Expression, right: Expression) : Binary(3, left, right) {
    override val getSymbol get() = "->"
}

class Negation(val negated: Expression) : Expression(hash = calcHash(arrayListOf(4, negated.hash))) {
    override val getSymbol get() = "!"

    override fun toTree(): String {
        return "($getSymbol${negated.toTree()})"
    }

    override fun toStringImpl(): String {
        return "$getSymbol${negated.toStringImpl()}"
    }
}

class Variable(var name: String) : Expression(hash = calcHash(arrayListOf(name.hashCode()))) {
    override val getSymbol get() = "Variable"

    override fun toTree(): String {
        return name
    }

    override fun toStringImpl(): String {
        return name
    }
}
