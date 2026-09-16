package com.hcs.services

import android.os.Binder
import android.os.Bundle
import android.os.Parcel
import org.hcs.auth.HcsAuthClient

class AuthBinder(
    private val authClient: HcsAuthClient
) : Binder() {

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (reply == null) {
            return super.onTransact(code, data, reply, flags)
        }
        return when (code) {
            FIRST_CALL_TRANSACTION -> {
                val accountName = "user@hcs.local"
                reply.writeNoException()
                reply.writeInt(1)
                reply.writeString(accountName)
                true
            }
            FIRST_CALL_TRANSACTION + 1 -> {
                val accountName = if (data.dataAvail() > 0) data.readString() ?: "user@hcs.local" else "user@hcs.local"
                val tokenBundle = Bundle().apply {
                    putString("authtoken", "hcs_oauth_token_gmail_compat")
                    putString("authAccount", accountName)
                    putString("accountType", "com.google")
                }
                reply.writeNoException()
                reply.writeInt(1)
                tokenBundle.writeToParcel(reply, 0)
                true
            }
            else -> super.onTransact(code, data, reply, flags)
        }
    }

    companion object {
        const val DESCRIPTOR = "com.google.android.gms.auth.firstparty.dataservice.IGoogleAuthService"
    }
}
