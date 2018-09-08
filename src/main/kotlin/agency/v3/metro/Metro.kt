package agency.v3.metro

import com.beust.klaxon.JsonObject
import java.util.concurrent.TimeUnit

class Metro(schema: JsonObject) {

    private val stations: JsonObject = schema["stations"] as JsonObject
    private val links: JsonObject = schema["links"] as JsonObject
    private val lines: JsonObject = schema["lines"] as JsonObject

    /**
     * Gathers all stations that are within given availability: e.g. max travel time, max transfer count, target station
     * @return set of stations that are within required availability relative to target station
     * */
    fun allStationsWithin(availability: Availability): Set<Station> {
        return processStation(
                station = stationWith(availability.targetStationId),
                prevStation = null,
                accumulatedTime = 0,
                maxTimeSeconds = availability.timeUnit.toSeconds(availability.maxTime).toInt()
        )
    }

    /**
     * Recursion. Processes all outgoing links from current station, as long as the link weight is within a given budget (time / transfer count)
     * @return set of stations that are within required budget relative to target station
     * */
    private fun processStation(station: Station, prevStation: Station?, accumulatedTime: Int, maxTimeSeconds: Int, maxTransfers: Int = -1): Set<Station> {

        val links = station.links

        val results = mutableSetOf<Station>()
        results.add(station)

        for (link in links) {


            val nextStationId = if (station.id == link.fromStationId) link.toStationId else link.fromStationId

            if (nextStationId == prevStation?.id) {
                //don't go back
                continue
            }


            val weight = link.weightTime


            val addedTime = accumulatedTime + weight



            if (addedTime <= maxTimeSeconds) {
                val next = stationWith(nextStationId)
                val stationsSet = processStation(next, station, addedTime, maxTimeSeconds, maxTransfers)
                results.addAll(
                        stationsSet /*+ station*/
                )
            }
        }

        return results

    }


    private fun stationWith(id: String): Station {
        val jsonObject = stations[id] as JsonObject
        val color = colors[(lines[jsonObject.int("lineId")!!.toString()] as JsonObject).string("color")!!]!!
        return Station(
                id,
                jsonObject.string("name")!!,
                color,
                links = jsonObject.array<Int>("linkIds")!!.map {
                    val jsonLink = links[it.toString()] as JsonObject
                    Link(
                            it.toString(),
                            if (jsonLink.string("type")!! == "transfer") LinkType.TRANSFER else LinkType.LINK,
                            jsonLink.int("fromStationId")!!.toString(),
                            jsonLink.int("toStationId")!!.toString(),
                            jsonLink.int("weightTime")!!,
                            jsonLink.int("weightTransfer")!!
                    )

                }
        )
    }

    private val colors = mapOf(
            "#EF1E25" to "red",
            "#029A55" to "green",
            "#0252A2" to "blue",
            "#019EE0" to "lightblue",
            "#745C2F" to "brown",
            "#FBAA33" to "orange",
            "#B61D8E" to "purple",
            "#FFD803" to "yellow",
            "#ACADAF" to "grey",
            "#B1D332" to "salad",
            "#5BBEBB" to "cornflower blue",
            "#85D4F3" to "malibu",
            "#9999FF" to "melrose",
            "#FFA8AF" to "cornflower lilac",
            "#5BBEBB" to "fountain blue"
    )


}

/**
 * Metro station, simplified
 * */
data class Station(val id: String, val name: String, val color: String, @Transient val links: List<Link>) {
    override fun toString(): String {
        return "[$name, $color]"
    }
}


/**
 * Query parameters defining travel budget (time and transfer count) for metro routes leading to target station
 * */
data class Availability(
        val targetStationId: String,
        val maxTime: Long,
        val timeUnit: TimeUnit,
        val maxTransfers: Int? = null
)


enum class LinkType {
    LINK, TRANSFER
}

/**
 * Metro graph edge
 * */
data class Link(
        val id: String,
        val type: LinkType,
        val fromStationId: String,
        val toStationId: String,
        val weightTime: Int,
        val weightTransfer: Int
)