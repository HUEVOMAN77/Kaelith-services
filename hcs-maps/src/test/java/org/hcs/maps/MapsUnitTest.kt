package org.hcs.maps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapsUnitTest {

    @Test
    fun testOpenStreetMapTileUrlAndMarker() {
        val provider = OpenStreetMapProvider()
        val pos = LatLng(10.0, 20.0)
        val url = provider.renderMapTile(pos, 12f)
        assertTrue(url.contains("tile.openstreetmap.org"))
        assertEquals(MapEngineType.OPEN_STREET_MAP, provider.engineType)

        val markerId = provider.addMarker(MarkerOptions(pos, "Test Marker"))
        assertTrue(markerId.startsWith("osm_marker_"))
    }

    @Test
    fun testMapManagerSelection() {
        val manager = MapManager()
        val provider = manager.getProvider(MapEngineType.MAPLIBRE)
        assertEquals(MapEngineType.MAPLIBRE, provider.engineType)
    }
}
