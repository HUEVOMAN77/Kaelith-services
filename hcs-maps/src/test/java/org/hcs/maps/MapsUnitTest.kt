package org.hcs.maps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapsUnitTest {

    @Test
    fun testOpenStreetMapTileUrl() {
        val provider = OpenStreetMapProvider()
        val url = provider.renderMapTile(10.0, 20.0, 12f)
        assertTrue(url.contains("tile.openstreetmap.org"))
        assertEquals(MapEngineType.OPEN_STREET_MAP, provider.engineType)
    }

    @Test
    fun testMapManagerSelection() {
        val manager = MapManager()
        val provider = manager.getProvider(MapEngineType.MAPLIBRE)
        assertEquals(MapEngineType.MAPLIBRE, provider.engineType)
    }
}
