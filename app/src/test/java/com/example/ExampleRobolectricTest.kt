package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AssistantId
import com.example.data.model.DTFGAssistants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DTFG World Hub AI", appName)
    }

    @Test
    fun `verify all seven DTFG assistants exist and are configured`() {
        val assistants = DTFGAssistants.all
        assertEquals(7, assistants.size)

        val ids = assistants.map { it.id }.toSet()
        assertEquals(
            setOf(
                AssistantId.GENERAL,
                AssistantId.TECH,
                AssistantId.BUSINESS,
                AssistantId.PROJECT,
                AssistantId.RESEARCH,
                AssistantId.DATA,
                AssistantId.LEARNING
            ),
            ids
        )

        assistants.forEach { assistant ->
            assertNotNull(assistant.name)
            assertNotNull(assistant.domain)
            assertNotNull(assistant.systemPrompt)
            assertEquals(true, assistant.samplePrompts.isNotEmpty())
            assertEquals(true, assistant.capabilities.isNotEmpty())
        }
    }

    @Test
    fun `verify downloadJsonFile executes successfully`() {
        val activity = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val repository = com.example.data.repository.DTFGRepository(activity.applicationContext)
        val webAppInterface = com.example.web.WebAppInterface(activity, repository) { null }

        val testJson = """{"platform":"DTFG World Hub AI","version":"1.0","messages":[]}"""
        val result = webAppInterface.downloadJsonFile("dtfg-chat-test.json", testJson)
        assertEquals(true, result)
    }
}
