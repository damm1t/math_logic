import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Paths

val time1 = { System.currentTimeMillis().toDouble() }
val st1 = time1()
val deltaTime1 = { (time1() - st1) / 1000.0 }

fun main(args: Array<String>) {
    val file = "test"
    BufferedReader(InputStreamReader(System.`in`)).use { fin ->
        //Files.newBufferedReader(Paths.get("$file.in")).use { fin ->
        //Files.newBufferedWriter(Paths.get("$file.out")).use { fout ->
        BufferedWriter(OutputStreamWriter(System.out)).use { fout ->
            val line = fin.readLine()
            val expr = line.parse()
            fout.write(expr.toTree())
        }
    }
    println("time : ${deltaTime1()}")
}