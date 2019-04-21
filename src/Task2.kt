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
    val file = "trip"
    //BufferedReader(InputStreamReader(System.`in`)).use { fin ->
    Files.newBufferedReader(Paths.get("$file.in")).use { fin ->
        Files.newBufferedWriter(Paths.get("$file.out")).use { fout ->
            //BufferedWriter(OutputStreamWriter(System.out)).use { fout ->
            val lines = fin.readLines()
            fout.write(optimize(lines).toString())
        }
    }
//println("time : ${deltaTime2()}")
}