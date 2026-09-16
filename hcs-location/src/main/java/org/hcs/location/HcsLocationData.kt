package org.hcs.location

data class HcsLocation(
    val provider: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val time: Long = System.currentTimeMillis()
)

data class HcsLocationResult(
    val locations: List<HcsLocation>
) {
    val lastLocation: HcsLocation?
        get() = locations.lastOrNull()
}
