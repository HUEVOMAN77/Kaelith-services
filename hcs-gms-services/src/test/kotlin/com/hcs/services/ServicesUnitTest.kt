package com.hcs.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ServicesUnitTest {

    @Test
    fun testLocationBinderDescriptors() {
        assertEquals("com.google.android.gms.location.internal.IGoogleLocationManagerService", LocationBinder.DESCRIPTOR)
    }

    @Test
    fun testPushBinderDescriptors() {
        assertEquals("com.google.android.c2dm.intent.REGISTER", PushBinder.DESCRIPTOR)
    }

    @Test
    fun testAuthBinderDescriptors() {
        assertEquals("com.google.android.gms.auth.service.START", AuthBinder.DESCRIPTOR)
    }
}
