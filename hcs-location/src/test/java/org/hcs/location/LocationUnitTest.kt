package org.hcs.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LocationUnitTest {

    @Test
    fun testLocationRequestDefaults() {
        val request = LocationRequest()
        assertEquals(LocationRequest.PRIORITY_HIGH_ACCURACY, request.priority)
        assertEquals(5000L, request.intervalMs)
    }

    @Test
    fun testLocationResultLastLocation() {
        val loc1 = HcsLocation("gps", 10.0, 20.0)
        val loc2 = HcsLocation("gps", 10.5, 20.5)
        val result = HcsLocationResult(listOf(loc1, loc2))

        assertNotNull(result.lastLocation)
        assertEquals(10.5, result.lastLocation!!.latitude, 0.001)
    }
}
