package com.hcs.navigation

import android.content.Context
import org.hcs.maps.LatLng
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource
import kotlin.concurrent.thread

data class RouteInstruction(
    val instruction: String,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val turnType: String
)

data class NavigationRoute(
    val waypoints: List<LatLng>,
    val instructions: List<RouteInstruction>,
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Double
)

class HcsNavigationEngine(private val context: Context) {

    fun calculateRoute(origin: LatLng, destination: LatLng): Task<NavigationRoute> {
        val tcs = TaskCompletionSource<NavigationRoute>()
        thread {
            val waypoints = listOf(
                origin,
                LatLng((origin.latitude + destination.latitude) / 2, (origin.longitude + destination.longitude) / 2),
                destination
            )
            val instructions = listOf(
                RouteInstruction("Avanza hacia el norte", 500.0, 60.0, "STRAIGHT"),
                RouteInstruction("Gira a la derecha en 200m", 300.0, 40.0, "RIGHT"),
                RouteInstruction("Has llegado a tu destino", 0.0, 0.0, "ARRIVED")
            )
            val route = NavigationRoute(
                waypoints = waypoints,
                instructions = instructions,
                totalDistanceMeters = 800.0,
                totalDurationSeconds = 100.0
            )
            tcs.setResult(route)
        }
        return tcs.task
    }
}
