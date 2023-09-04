package example.tools

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileReader
import java.io.FileWriter

fun main(args: Array<String>) {
    doRC("stats-g3-solve-m-5walls-v3")
    doRC("stats-g3-random-5walls-v3")
}

fun doRC(fileName: String) {
    println("Processing $fileName")
    val byRC = load("../data/v1.5/$fileName.csv")

    BufferedWriter(FileWriter("../data/v1.5/$fileName-to.csv")).use {out ->
        CSVPrinter(out, CSVFormat.DEFAULT.builder().setHeader(
            "rc", "p", "ontime", "timeout"
        ).build()).use { csvPrinter ->
            byRC.forEach { (rc, le) ->
                val ot = le.count { it.success.equals("ontime") }
                val to = le.count { it.success.equals("timeout") }
                println("$rc: $ot $to ${le.size}")

                byP(le).forEach {(p, le) ->
                    val ot = le.count { it.success.equals("ontime") }
                    val to = le.count { it.success.equals("timeout") }
                    //println("   $p: $ot $to ${le.size}")

                    csvPrinter.printRecord(rc, p, ot, to)
                }
            }
        }
    }
}

fun byP(le: MutableList<Execution>) : Map<Double,MutableList<Execution>> {
    val mByP = mutableMapOf<Double,MutableList<Execution>>()
    le.forEach {
        if (!(it.pChange in mByP))
            mByP[it.pChange] = mutableListOf<Execution>()
        mByP[it.pChange]?.add(it)
    }
    return mByP
}

fun load(fileName: String) : Map<Double,MutableList<Execution>> {
    val bufferedReader = BufferedReader(FileReader(fileName))
    val csvParser = CSVParser(bufferedReader, CSVFormat.DEFAULT.builder().setTrim(true).build())
    val byRC = mutableMapOf<Double,MutableList<Execution>>()
    for (csvRecord in csvParser) {
        if (!csvRecord.get(0).toString().equals("scenario")) {
            //print(csvRecord)
            val e = Execution(
                csvRecord.get(0),
                csvRecord.get(1).toDouble(),
                csvRecord.get(2).toDouble(),
                csvRecord.get(3),
                csvRecord.get(4).toDouble(),
                csvRecord.get(5).toInt(),
                csvRecord.get(6).toInt(),
                csvRecord.get(7).toInt(),
                csvRecord.get(8).toInt(),
                csvRecord.get(9).toLong(),
                csvRecord.get(10)
            )
            if (!(e.rc in byRC))
                byRC[e.rc] = mutableListOf<Execution>()
            byRC[e.rc]?.add(e)
            //println(e)
        }
    }
    return byRC
}


data class Execution (
    val scenario: String,
    val pChange: Double,
    val gamma: Double,
    val strategy: String,
    val rc: Double,
    val plans: Int,
    val matrices: Int,
    val states: Int,
    val actions: Int,
    val time: Long,
    val success: String
)