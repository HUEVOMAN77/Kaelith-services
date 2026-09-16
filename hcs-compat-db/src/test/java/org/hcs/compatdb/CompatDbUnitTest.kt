package org.hcs.compatdb

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class CompatDbUnitTest {

    @Test
    fun testGetReportForApp() {
        val client = HcsCompatDbClient()
        val task = client.getReportForApp("com.aurorastore.targetapp")
        val report = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertNotNull(report)
        assertEquals("A", report!!.compatibilityLevel)
    }

    @Test
    fun testSubmitAnonymousReport() {
        val client = HcsCompatDbClient()
        val newReport = CommunityReport(
            packageName = "org.example.newapp",
            appVersionCode = 1,
            appVersionName = "1.0",
            deviceModel = "NCO-LX3",
            emuiVersion = "13.0",
            androidSdk = 31,
            compatibilityLevel = "B",
            failingApis = emptyList(),
            summaryNotes = "Works with minor notes."
        )

        val submitTask = client.submitAnonymousReport(newReport)
        val success = Tasks.await(submitTask, 1, TimeUnit.SECONDS)
        assertTrue(success)

        val queryTask = client.getReportForApp("org.example.newapp")
        val retrieved = Tasks.await(queryTask, 1, TimeUnit.SECONDS)
        assertEquals("B", retrieved!!.compatibilityLevel)
    }
}
