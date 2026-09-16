package org.hcs.offlineprofiles

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

class OfflineProfilesUnitTest {

    @Test
    fun testExportAndImportProfile() {
        val serializer = HcsProfileSerializer()
        val tempFile = File.createTempFile("profile_", ".hcsjson")
        tempFile.deleteOnExit()

        val originalProfile = OfflineProfile(
            packageName = "org.aurorastore.target",
            appName = "Target App",
            compatibilityLevel = "A",
            preferredPushTransport = "UNIFIED_PUSH",
            preferredMapEngine = "MAPLIBRE",
            notes = "Exported offline profile"
        )

        val exportTask = serializer.exportProfileToFile(originalProfile, tempFile)
        val exportSuccess = Tasks.await(exportTask, 1, TimeUnit.SECONDS)
        assertTrue(exportSuccess)

        val importTask = serializer.importProfileFromFile(tempFile)
        val importedProfile = Tasks.await(importTask, 1, TimeUnit.SECONDS)

        assertNotNull(importedProfile)
        assertEquals("org.aurorastore.target", importedProfile!!.packageName)
        assertEquals("MAPLIBRE", importedProfile.preferredMapEngine)
    }
}
