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
var realIndex = IntArray(0)
var endImprove = -1

fun main(args: Array<String>) {
    val file = "1"
    BufferedReader(InputStreamReader(System.`in`)).use { fin ->
        //Files.newBufferedReader(Paths.get("$file.in")).use { fin ->
        //Files.newBufferedWriter(Paths.get("$file.out")).use { fout ->
        BufferedWriter(OutputStreamWriter(System.out)).use { fout ->
            val lines = fin.readLines()

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
                fout.write("Proof is incorrect")
                return
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
                fout.write(list[i].toStringImpl())
                if (i != list.size - 1)
                    fout.write(",")
            }
            fout.write("|-")
            fout.write(head.toStringImpl())
            fout.newLine()


            for (index in 1 until endImprove + 1) {
                val line = lines[index]
                if (usedLines[index]) {
                    val annotation = annotateLine(line, index)
                    if (annotation is MP) {
                        val left = realIndex[tree[index].first]
                        val right = realIndex[tree[index].second]
                        fout.append("[${realIndex[index]}. ${MP(left, right)}] ${outLines[index]}")
                    } else fout.append("[${realIndex[index]}. ${annotateLine(line, index)}] ${outLines[index]}")
                    fout.newLine()
                }
            }
        }
    }
//println("time : ${deltaTime2()}")
}