package com.hcs.games

import android.content.Context
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val currentSteps: Int,
    val totalSteps: Int,
    val isUnlocked: Boolean = currentSteps >= totalSteps
)

data class SavedGameSnapshot(
    val snapshotId: String,
    val title: String,
    val playedTimeMs: Long,
    val lastModifiedTimestamp: Long,
    val payloadData: String
)

class HcsGameServicesClient(private val context: Context) {

    private val achievements = mutableListOf<Achievement>()
    private val snapshots = mutableListOf<SavedGameSnapshot>()

    fun unlockAchievement(achievementId: String, name: String = "Logro Local"): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        val index = achievements.indexOfFirst { it.id == achievementId }
        val updated = Achievement(
            id = achievementId,
            name = name,
            description = "Desbloqueado localmente vía HCS Games",
            currentSteps = 100,
            totalSteps = 100,
            isUnlocked = true
        )
        if (index >= 0) {
            achievements[index] = updated
        } else {
            achievements.add(updated)
        }
        tcs.setResult(true)
        return tcs.task
    }

    fun getAchievements(): Task<List<Achievement>> {
        val tcs = TaskCompletionSource<List<Achievement>>()
        tcs.setResult(achievements.toList())
        return tcs.task
    }

    fun saveGameSnapshot(title: String, payloadData: String): Task<SavedGameSnapshot> {
        val tcs = TaskCompletionSource<SavedGameSnapshot>()
        val snapshotId = "snapshot_${System.currentTimeMillis()}"
        val snapshot = SavedGameSnapshot(
            snapshotId = snapshotId,
            title = title,
            playedTimeMs = System.currentTimeMillis(),
            lastModifiedTimestamp = System.currentTimeMillis(),
            payloadData = payloadData
        )
        snapshots.add(snapshot)
        tcs.setResult(snapshot)
        return tcs.task
    }

    fun getSavedGameSnapshots(): Task<List<SavedGameSnapshot>> {
        val tcs = TaskCompletionSource<List<SavedGameSnapshot>>()
        tcs.setResult(snapshots.toList())
        return tcs.task
    }
}
