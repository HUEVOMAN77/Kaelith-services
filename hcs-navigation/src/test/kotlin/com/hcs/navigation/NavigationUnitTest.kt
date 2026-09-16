package com.hcs.navigation

import org.hcs.maps.LatLng
import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.concurrent.TimeUnit

class NavigationUnitTest {

    @Test
    fun testCalculateRoute() {
        val engine = HcsNavigationEngine(DummyContext())
        val origin = LatLng(10.0, 20.0)
        val destination = LatLng(10.1, 20.1)
        val task = engine.calculateRoute(origin, destination)
        val route = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertNotNull(route)
        assertEquals(3, route.waypoints.size)
        assertTrue(route.totalDistanceMeters > 0)
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}

private class DummyContext : android.content.ContextWrapper(null)
