package org.hcs.shizuku

import android.content.pm.PackageManager

enum class ShizukuState {
    NOT_INSTALLED_OR_RUNNING,
    RUNNING_NOT_AUTHORIZED,
    RUNNING_AND_AUTHORIZED
}

object ShizukuAvailability {

    fun getShizukuState(): ShizukuState {
        return try {
            val pingBinderMethod = Class.forName("rikka.shizuku.Shizuku").getMethod("pingBinder")
            val isBinderAlive = pingBinderMethod.invoke(null) as Boolean

            if (!isBinderAlive) {
                ShizukuState.NOT_INSTALLED_OR_RUNNING
            } else {
                val checkPermMethod = Class.forName("rikka.shizuku.Shizuku").getMethod("checkSelfPermission")
                val permResult = checkPermMethod.invoke(null) as Int
                if (permResult == PackageManager.PERMISSION_GRANTED) {
                    ShizukuState.RUNNING_AND_AUTHORIZED
                } else {
                    ShizukuState.RUNNING_NOT_AUTHORIZED
                }
            }
        } catch (e: Throwable) {
            ShizukuState.NOT_INSTALLED_OR_RUNNING
        }
    }
}
