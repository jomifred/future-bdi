package example.tools

import java.io.BufferedReader
import java.io.FileWriter
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {
    var pChange = 0.0
    while (pChange <= 1.01) {

        var rCert = args[1].toDouble()
        //while (rCert < 1.01) {
            val conf = Properties()
            conf.setProperty("pChange", pChange.toString())
            conf.setProperty("requiredCertainty", rCert.toString())
            //conf.setProperty("maxTime", "20000")
            conf.setProperty("recover_strategy", args[0])

            conf.store(FileWriter("examples/params.properties"), "conf")

            val process = Runtime.getRuntime()
                .exec("./gradlew -q :examples:run --args=run-all-grid-3/grid3.mas2j")

            // I do not know way, but it is necessary to read the process stream until it ends, and then uses waitFor (!)
            val `in` = BufferedReader(InputStreamReader(process.getInputStream()))
            //val `in` = BufferedReader(InputStreamReader(process.errorStream))
            var line : String? = `in`.readLine()
            while (line != null) {
                //System.out.println(line)
                line = `in`.readLine()
            }
            `in`.close()

            var failure = "ok"
            process.waitFor(10, TimeUnit.SECONDS)
            if (process.isAlive) {
                process.destroyForcibly()
                failure = "timeout"
            }

            println("${args[0]}: ${pChange}, ${rCert}, $failure")

            //rCert += 0.1
        //}

        if (pChange < 0.2)
            pChange += 0.02
        else
            pChange += 0.05
    }

}

