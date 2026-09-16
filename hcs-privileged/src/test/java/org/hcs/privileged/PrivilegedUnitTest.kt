package org.hcs.privileged

import org.hcs.tasks.Tasks
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

class PrivilegedUnitTest {

    @Test
    fun testSimulationPatchApplication() {
        val patcher = PrivilegedPatcher(DummyContext())
        val task = patcher.applySystemPatch("/tmp/mock_target.conf", "mock_patch_content", isSimulation = true)
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertTrue(result.isSuccess)
        assertTrue(result.isSimulation)
        assertNotNull(result.appliedHash)
    }

    @Test
    fun testRollbackWhenNoBackupExists() {
        val patcher = PrivilegedPatcher(DummyContext())
        val task = patcher.performRollback("/tmp/non_existent_file.conf")
        val result = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertFalse(result)
    }
}

private class DummyContext : android.content.ContextWrapper(null) {
    override fun getFilesDir(): File {
        val f = File(System.getProperty("java.io.tmpdir"), "hcs_test_privileged")
        if (!f.exists()) f.mkdirs()
        return f
    }
}
