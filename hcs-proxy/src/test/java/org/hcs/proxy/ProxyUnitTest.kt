package org.hcs.proxy

import org.hcs.tasks.Tasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class ProxyUnitTest {

    @Test
    fun testGoogleUrlPingInterception() {
        val server = HcsLocalProxyServer()
        Tasks.await(server.start(), 1, TimeUnit.SECONDS)

        val req = ProxyRequest("https://android.googleapis.com/checkin")
        val task = server.handleRequest(req)
        val res = Tasks.await(task, 1, TimeUnit.SECONDS)

        assertTrue(res.isIntercepted)
        assertEquals(200, res.statusCode)
    }
}
