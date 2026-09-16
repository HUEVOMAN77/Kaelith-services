package org.hcs.push

import android.content.Context
import android.content.Intent
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

enum class PushTransportType {
    UNIFIED_PUSH,
    FCM_COMPAT,
    HUAWEI_PUSH_KIT
}

data class PushMessage(
    val messageId: String,
    val fromApp: String,
    val payloadData: Map<String, String>,
    val transportUsed: PushTransportType
)

interface PushTransportConnector {
    val transportType: PushTransportType
    val isAvailable: Boolean
    fun registerPushToken(appPackageName: String): Task<String>
    fun unregisterPushToken(appPackageName: String): Task<Void?>
}

class UnifiedPushConnector(private val context: Context) : PushTransportConnector {
    override val transportType: PushTransportType = PushTransportType.UNIFIED_PUSH
    override val isAvailable: Boolean = true

    override fun registerPushToken(appPackageName: String): Task<String> {
        val tcs = TaskCompletionSource<String>()
        // Generates open UnifiedPush endpoint token for target application
        val token = "up_token_${appPackageName}_${System.currentTimeMillis()}"
        tcs.setResult(token)
        return tcs.task
    }

    override fun unregisterPushToken(appPackageName: String): Task<Void?> {
        val tcs = TaskCompletionSource<Void?>()
        tcs.setResult(null)
        return tcs.task
    }
}

class FcmCompatAdapter(private val context: Context) : PushTransportConnector {
    override val transportType: PushTransportType = PushTransportType.FCM_COMPAT
    override val isAvailable: Boolean = true

    override fun registerPushToken(appPackageName: String): Task<String> {
        val tcs = TaskCompletionSource<String>()
        val token = "fcm_compat_token_${appPackageName}_${System.currentTimeMillis()}"
        tcs.setResult(token)
        return tcs.task
    }

    override fun unregisterPushToken(appPackageName: String): Task<Void?> {
        val tcs = TaskCompletionSource<Void?>()
        tcs.setResult(null)
        return tcs.task
    }

    /**
     * Dispatches intercepted FCM/C2DM push intent to third-party app expecting com.google.android.c2dm.intent.RECEIVE.
     */
    fun dispatchPushMessageToApp(targetPackage: String, data: Map<String, String>): Boolean {
        return try {
            val intent = Intent("com.google.android.c2dm.intent.RECEIVE").apply {
                setPackage(targetPackage)
                for ((k, v) in data) {
                    putExtra(k, v)
                }
            }
            context.sendBroadcast(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}

class HuaweiPushKitAdapter(private val context: Context) : PushTransportConnector {
    override val transportType: PushTransportType = PushTransportType.HUAWEI_PUSH_KIT

    override val isAvailable: Boolean
        get() {
            return try {
                context.packageManager.getPackageInfo("com.huawei.android.pushagent", 0)
                true
            } catch (e: Exception) {
                false
            }
        }

    override fun registerPushToken(appPackageName: String): Task<String> {
        val tcs = TaskCompletionSource<String>()
        if (isAvailable) {
            tcs.setResult("hms_push_token_${appPackageName}_${System.currentTimeMillis()}")
        } else {
            tcs.setException(IllegalStateException("Huawei Push Agent is not available on this device"))
        }
        return tcs.task
    }

    override fun unregisterPushToken(appPackageName: String): Task<Void?> {
        val tcs = TaskCompletionSource<Void?>()
        tcs.setResult(null)
        return tcs.task
    }
}

class PushEngineManager(private val context: Context) {
    val unifiedPush = UnifiedPushConnector(context)
    val fcmCompat = FcmCompatAdapter(context)
    val huaweiPush = HuaweiPushKitAdapter(context)

    fun getPreferredTransport(): PushTransportConnector {
        if (unifiedPush.isAvailable) return unifiedPush
        if (huaweiPush.isAvailable) return huaweiPush
        return fcmCompat
    }

    fun registerApp(packageName: String): Task<Pair<PushTransportType, String>> {
        val tcs = TaskCompletionSource<Pair<PushTransportType, String>>()
        val transport = getPreferredTransport()
        transport.registerPushToken(packageName).addOnSuccessListener { token ->
            tcs.setResult(transport.transportType to token)
        }.addOnFailureListener { e ->
            tcs.setException(e)
        }
        return tcs.task
    }
}
