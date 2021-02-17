package com.zp4rker.whatsthetime

import com.zp4rker.whatsthetime.http.request
import org.json.JSONObject

/**
 * @author zp4rker
 */
data class IPLocation(val city: String, val country: String, val timezone: String) {
    companion object {
        fun search(): IPLocation {
            val data = JSONObject(request("GET", "https://get.geojs.io/v1/ip/geo.json"))
            return IPLocation(data.getString("city"), data.getString("country"), data.getString("timezone").replace("\\", ""))
        }
    }
}
