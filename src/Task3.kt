import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashSet

/*val time2 = { System.currentTimeMillis().toDouble() }
val st2 = time2()
val deltaTime2 = { (time2() - st2) / 1000.0 }*/

fun main(args: Array<String>) {
    val file = "test"
    //BufferedReader(InputStreamReader(System.`in`)).use { fin ->
    Files.newBufferedReader(Paths.get("$file.in")).use { fin ->
        Files.newBufferedWriter(Paths.get("$file.out")).use { fout ->
            //BufferedWriter(OutputStreamWriter(System.out)).use { fout ->
            val lines = fin.readLines()
            val list: MutableList<Expression> = parseHead(lines[0])
            val head = list[0]

            for (i in 1 until list.size) {
                fout.write(list[i].toStringImpl())
                if (i != list.size - 1)
                    fout.write(",")
            }
            fout.write("|-")
            fout.write("!!" + head.toStringImpl())
            fout.write(System.lineSeparator())

            for (index in 1 until lines.size) {
                val line = lines[index]
                val annotation = annotate(line, index)
                val impl = line.parse()

                if (annotation is Axiom || annotation is Assumption) { // ax 1-9
                    fout.write(addAx(impl.toStringImpl(), "ax").toString())
                }
                else if (annotation is None) { // actually ax10
                    fout.write(addAx(getAnyName(impl), "ax10").toString())
                }
                else if (annotation is MP) { // d
                    val implication = lines[annotation.number].parse()
                    if (implication is Implication) {
                        fout.write(addMP(implication.left.toStringImpl(), implication.right.toStringImpl()).toString())
                    }
                }
            }
        }
    }
//println("time : ${deltaTime2()}")
}