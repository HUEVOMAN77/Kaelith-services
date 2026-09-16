package org.hcs.maps

enum class MapEngineType {
    MAPLIBRE,
    OPEN_STREET_MAP,
    MAPBOX,
    VTM
}

data class MapOptions(
    val initialLatitude: Double = 0.0,
    val initialLongitude: Double = 0.0,
    val initialZoom: Float = 10f,
    val preferredEngine: MapEngineType = MapEngineType.OPEN_STREET_MAP
)

interface HcsMapProvider {
    val engineType: MapEngineType
    val isAvailable: Boolean
    fun renderMapTile(lat: Double, lon: Double, zoom: Float): String
}

class OpenStreetMapProvider : HcsMapProvider {
    override val engineType: MapEngineType = MapEngineType.OPEN_STREET_MAP
    override val isAvailable: Boolean = true

    override fun renderMapTile(lat: Double, lon: Double, zoom: Float): String {
        return "https://tile.openstreetmap.org/${zoom.toInt()}/$lat/$lon.png"
    }
}

class MapLibreProvider : HcsMapProvider {
    override val engineType: MapEngineType = MapEngineType.MAPLIBRE
    override val isAvailable: Boolean = true

    override fun renderMapTile(lat: Double, lon: Double, zoom: Float): String {
        return "https://demotiles.maplibre.org/tiles/$zoom/$lat/$lon.pbf"
    }
}

class MapManager {
    fun getProvider(engineType: MapEngineType): HcsMapProvider {
        return when (engineType) {
            MapEngineType.MAPLIBRE -> MapLibreProvider()
            else -> OpenStreetMapProvider()
        }
    }
}
