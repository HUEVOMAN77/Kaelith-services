package org.hcs.remoteconfig

import android.content.Context
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

class HcsRemoteConfigClient(private val context: Context) {

    private val localConfigValues = mutableMapOf<String, String>()

    init {
        // Set default local configuration values
        localConfigValues["hcs_welcome_message"] = "Welcome to HCS (Huawei Compatibility Services)"
        localConfigValues["hcs_enable_unified_push"] = "true"
        localConfigValues["hcs_preferred_map_engine"] = "OPEN_STREET_MAP"
    }

    fun fetchAndActivate(): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        // Simulates fetching latest remote JSON configuration and activating parameters
        tcs.setResult(true)
        return tcs.task
    }

    fun getString(key: String): String {
        return localConfigValues[key] ?: ""
    }

    fun getBoolean(key: String): Boolean {
        return localConfigValues[key]?.toBoolean() ?: false
    }

    fun setDefaults(defaults: Map<String, String>) {
        localConfigValues.putAll(defaults)
    }
}
