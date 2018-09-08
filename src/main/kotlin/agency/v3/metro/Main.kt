package agency.v3.metro

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit


fun main(args: Array<String>){

    //TODO: download scheme json from Yandex
    val schema = Parser().parse(
            StringBuilder(
                    Unit.javaClass.getResourceAsStream("/get-scheme-metadata.json").bufferedReader().use { it.readText() }
            )
    ) as JsonObject

    val result = Metro(schema).allStationsWithin(
            //93 = Kitay-Gorod, Orange
            Availability(targetStationId = "93", maxTime = 300, timeUnit = TimeUnit.SECONDS)
    )


    result.forEach{
        println(it)
    }

}
