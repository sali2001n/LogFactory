package com.sali.logfactory.utility

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.sali.logfactory.utility.FileHelper.writeToLogFile
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(RobolectricTestRunner::class)
class FileHelperTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `writeLogsToInternalStorageLogFile appends text to file in internal storage`() {
        val file = File(context.filesDir, "Test.txt")
        file.writeText("")

        FileHelper.writeLogToFile(file, "hello")
        FileHelper.writeLogToFile(file, " world")

        val content = file.readText()
        assertEquals("hello world", content)
    }

    @Test
    fun `writeToLogFile writes message when uri is not null`() {
        val mockResolver = Mockito.mock(ContentResolver::class.java)
        val fileUri = Mockito.mock(Uri::class.java)

        val outputStream = ByteArrayOutputStream()

        Mockito.`when`(mockResolver.openOutputStream(fileUri, "wa"))
            .thenReturn(outputStream)

        val message = "Hello Logs!"

        // Act
        writeToLogFile(fileUri, mockResolver, message)

        // Assert
        val writtenData = outputStream.toByteArray().decodeToString()
        assertEquals(message, writtenData)
    }

    @Test
    fun `writeToLogFile logs error when uri is null`() {
        // Arrange
        val mockResolver = Mockito.mock(ContentResolver::class.java)

        // Spy on Log.e to check it's called
        val logSpy = Mockito.mockStatic(Log::class.java)
        logSpy.`when`<Int> {
            Log.e(
                Mockito.anyString(),
                Mockito.anyString()
            )
        }.thenReturn(0)

        // Act
        writeToLogFile(null, mockResolver, "Some message")

        // Assert
        logSpy.verify {
            Log.e(
                Mockito.anyString(),
                Mockito.anyString()
            )
        }

        logSpy.close()
    }


}