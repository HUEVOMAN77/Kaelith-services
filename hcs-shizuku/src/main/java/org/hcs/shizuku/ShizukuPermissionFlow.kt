package org.hcs.shizuku

object ShizukuPermissionFlow {

    const val EXPLANATION_TEXT_ES = "Shizuku te permite consultar el estado real de la batería de EMUI y gestionar la lista blanca sin necesidad de root ni modificar el sistema."
    const val EXPLANATION_TEXT_EN = "Shizuku allows checking real EMUI battery optimization state and managing whitelist without requiring root or system modifications."

    fun requestPermission(requestCode: Int) {
        try {
            val pingMethod = Class.forName("rikka.shizuku.Shizuku").getMethod("pingBinder")
            val isAlive = pingMethod.invoke(null) as Boolean
            if (isAlive) {
                val reqMethod = Class.forName("rikka.shizuku.Shizuku").getMethod("requestPermission", Int::class.javaPrimitiveType)
                reqMethod.invoke(null, requestCode)
            }
        } catch (ignored: Throwable) { }
    }
}
