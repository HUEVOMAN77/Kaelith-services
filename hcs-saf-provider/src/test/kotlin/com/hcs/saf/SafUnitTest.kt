package com.hcs.saf

import org.junit.Assert.assertNotNull
import org.junit.Test

class SafUnitTest {

    @Test
    fun testProviderInstantiation() {
        val provider = HcsDriveDocumentsProvider()
        assertNotNull(provider)
    }
}
