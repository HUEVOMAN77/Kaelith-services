package com.hcs.services

import android.os.Binder
import android.os.Bundle
import android.os.Parcel
import com.hcs.games.HcsGameServicesClient

class GameBinder(
    private val gameClient: HcsGameServicesClient
) : Binder() {

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (reply == null) {
            return super.onTransact(code, data, reply, flags)
        }
        return when (code) {
            FIRST_CALL_TRANSACTION -> {
                // getPlayerId / getGameSession
                reply.writeNoException()
                reply.writeInt(1)
                reply.writeString("hcs_player_local_1001")
                true
            }
            FIRST_CALL_TRANSACTION + 1 -> {
                // unlockAchievement
                val achId = if (data.dataAvail() > 0) data.readString() ?: "ach_1" else "ach_1"
                gameClient.unlockAchievement(achId)
                reply.writeNoException()
                reply.writeInt(1)
                true
            }
            FIRST_CALL_TRANSACTION + 2 -> {
                // saveGameSnapshot
                val title = if (data.dataAvail() > 0) data.readString() ?: "Game Save" else "Game Save"
                val payload = if (data.dataAvail() > 0) data.readString() ?: "{}" else "{}"
                gameClient.saveGameSnapshot(title, payload)
                reply.writeNoException()
                reply.writeInt(1)
                true
            }
            else -> super.onTransact(code, data, reply, flags)
        }
    }

    companion object {
        const val DESCRIPTOR = "com.google.android.gms.games.internal.IGamesService"
    }
}
