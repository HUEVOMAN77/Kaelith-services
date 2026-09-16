package com.hcs.services

import android.os.Binder
import android.os.Parcel
import org.hcs.push.PushEngineManager

class PushBinder(
    private val pushManager: PushEngineManager
) : Binder() {

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        return when (code) {
            FIRST_CALL_TRANSACTION -> {
                data.enforceInterface(DESCRIPTOR)
                val appPackage = data.readString() ?: ""
                reply?.writeNoException()
                reply?.writeInt(1)
                reply?.writeString("up_token_${appPackage}")
                true
            }
            FIRST_CALL_TRANSACTION + 1 -> {
                data.enforceInterface(DESCRIPTOR)
                val appPackage = data.readString() ?: ""
                val tokenTask = pushManager.registerApp(appPackage)
                reply?.writeNoException()
                reply?.writeString(tokenTask.result?.second ?: "")
                true
            }
            else -> super.onTransact(code, data, reply, flags)
        }
    }

    companion object {
        const val DESCRIPTOR = "com.google.android.c2dm.intent.REGISTER"
    }
}
