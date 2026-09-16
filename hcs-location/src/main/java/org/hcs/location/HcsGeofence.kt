package org.hcs.location

import android.content.Context
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

data class HcsGeofence(
    val requestId: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val expirationDurationMs: Long = 3600000L,
    val transitionTypes: Int = GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_EXIT
) {
    companion object {
        const val GEOFENCE_TRANSITION_ENTER = 1
        const val GEOFENCE_TRANSITION_EXIT = 2
        const val GEOFENCE_TRANSITION_DWELL = 4
    }
}

class HcsGeofenceManager(private val context: Context) {

    private val activeGeofences = mutableMapOf<String, HcsGeofence>()

    fun addGeofences(geofences: List<HcsGeofence>): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        for (g in geofences) {
            activeGeofences[g.requestId] = g
        }
        tcs.setResult(true)
        return tcs.task
    }

    fun removeGeofences(requestIds: List<String>): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        for (id in requestIds) {
            activeGeofences.remove(id)
        }
        tcs.setResult(true)
        return tcs.task
    }

    fun getActiveGeofences(): List<HcsGeofence> = activeGeofences.values.toList()
}
