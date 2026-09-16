package com.hcs.services

import android.os.Binder
import android.os.Parcel
import org.hcs.auth.HcsAuthClient

class AuthBinder(
    private val authClient: HcsAuthClient
) : Binder() {

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        return when (code) {
            FIRST_CALL_TRANSACTION -> {
                data.enforceInterface(DESCRIPTOR)
                reply?.writeNoException()
                reply?.writeInt(1)
                reply?.writeString("user@hcs.local")
                true
            }
            else -> super.onTransact(code, data, reply, flags)
        }
    }

    companion object {
        const val DESCRIPTOR = "com.google.android.gms.auth.service.START"
    }
}
