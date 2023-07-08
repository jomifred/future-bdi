package example.tools

import java.io.FileWriter
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

            //val `in` = BufferedReader(InputStreamReader(process.getInputStream()))
            /*val `in` = BufferedReader(InputStreamReader(process.errorStream))
            var line : String? = `in`.readLine()
            while (line != null) {
                System.out.println(line)
                line = `in`.readLine()
            }
            `in`.close()*/
            var failure = "ok"
            process.waitFor(20, TimeUnit.SECONDS)
            if (process.isAlive) {
                process.destroyForcibly()
                failure = "timeout"
            }

            println("${args[0]}: ${pChange}, ${rCert}, $failure")

            //rCert += 0.1
        //}

        pChange += 0.05
    }

}

