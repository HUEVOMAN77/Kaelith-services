package org.hcs.maps

data class LatLng(
    val latitude: Double,
    val longitude: Double
)

data class LatLngBounds(
    val southwest: LatLng,
    val northeast: LatLng
)

data class CameraPosition(
    val target: LatLng,
    val zoom: Float,
    val tilt: Float = 0f,
    val bearing: Float = 0f
)

data class MarkerOptions(
    val position: LatLng,
    val title: String? = null,
    val snippet: String? = null,
    val draggable: Boolean = false,
    val visible: Boolean = true
)

data class PolylineOptions(
    val points: List<LatLng> = emptyList(),
    val color: Int = 0xFF0000FF.toInt(),
    val width: Float = 10f,
    val visible: Boolean = true
)

data class PolygonOptions(
    val points: List<LatLng> = emptyList(),
    val fillColor: Int = 0x7F00FF00.toInt(),
    val strokeColor: Int = 0xFF00FF00.toInt(),
    val strokeWidth: Float = 5f
)

data class CircleOptions(
    val center: LatLng,
    val radiusMeters: Double,
    val fillColor: Int = 0x7F0000FF.toInt(),
    val strokeColor: Int = 0xFF0000FF.toInt(),
    val strokeWidth: Float = 5f
)

enum class MapEngineType {
    MAPLIBRE,
    OPEN_STREET_MAP,
    MAPBOX,
    VTM
}

data class MapOptions(
    val initialCameraPosition: CameraPosition = CameraPosition(LatLng(0.0, 0.0), 10f),
    val preferredEngine: MapEngineType = MapEngineType.OPEN_STREET_MAP
)

interface HcsMapProvider {
    val engineType: MapEngineType
    val isAvailable: Boolean
    fun renderMapTile(latLng: LatLng, zoom: Float): String
    fun addMarker(options: MarkerOptions): String
    fun addPolyline(options: PolylineOptions): String
}

class OpenStreetMapProvider : HcsMapProvider {
    override val engineType: MapEngineType = MapEngineType.OPEN_STREET_MAP
    override val isAvailable: Boolean = true

    override fun renderMapTile(latLng: LatLng, zoom: Float): String {
        return "https://tile.openstreetmap.org/${zoom.toInt()}/${latLng.latitude}/${latLng.longitude}.png"
    }

    override fun addMarker(options: MarkerOptions): String {
        return "osm_marker_${options.position.latitude}_${options.position.longitude}"
    }

    override fun addPolyline(options: PolylineOptions): String {
        return "osm_polyline_${options.points.size}_pts"
    }
}

class MapLibreProvider : HcsMapProvider {
    override val engineType: MapEngineType = MapEngineType.MAPLIBRE
    override val isAvailable: Boolean = true

    override fun renderMapTile(latLng: LatLng, zoom: Float): String {
        return "https://demotiles.maplibre.org/tiles/${zoom.toInt()}/${latLng.latitude}/${latLng.longitude}.pbf"
    }

    override fun addMarker(options: MarkerOptions): String {
        return "maplibre_marker_${options.position.latitude}_${options.position.longitude}"
    }

    override fun addPolyline(options: PolylineOptions): String {
        return "maplibre_polyline_${options.points.size}_pts"
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
