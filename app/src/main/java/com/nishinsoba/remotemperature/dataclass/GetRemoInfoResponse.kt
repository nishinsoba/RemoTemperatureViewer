package com.nishinsoba.remotemperature.dataclass

import com.squareup.moshi.Json

data class GetRemoInfoResponse(
    @Json(name = "result_message")
    val resultMessage: String,
    @Json(name = "remo_data")
    val remoData: List<RemoData>,
    @Json(name = "start_date_time")
    val startDateTime: String,
    @Json(name = "end_date_time")
    val endDateTime: String,
    @Json(name = "average_room_temperature")
    val averageRoomTemperature: Float,
    @Json(name = "average_outdoor_temperature")
    val averageOutdoorTemperature: Float

)

data class RemoData(
    @Json(name = "datetime")
    val dateTime: String,
    @Json(name = "is_using_aircon")
    val isUsingAircon: Int,
    @Json(name = "outdoor_temperature")
    val outdoorTemperature: Float,
    @Json(name = "room_temperature")
    val roomTemperature: Float,
    @Json(name = "aircon_mode")
    val airconMode: String,
    @Json(name = "aircon_temperature")
    val airconTemperature: Float
)