package com.hcs.services

import org.junit.Assert.assertEquals
import org.junit.Test

class ServicesUnitTest {

    @Test
    fun testLocationBinderDescriptors() {
        assertEquals("com.google.android.gms.location.internal.IGoogleLocationManagerService", LocationBinder.DESCRIPTOR)
    }

    @Test
    fun testPushBinderDescriptors() {
        assertEquals("com.google.android.gms.gcm.INetworkTaskCallback", PushBinder.DESCRIPTOR)
    }

    @Test
    fun testAuthBinderDescriptors() {
        assertEquals("com.google.android.gms.auth.firstparty.dataservice.IGoogleAuthService", AuthBinder.DESCRIPTOR)
    }
}
