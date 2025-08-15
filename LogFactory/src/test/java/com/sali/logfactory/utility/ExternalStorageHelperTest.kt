package com.sali.logfactory.utility

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.isNull
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class ExternalStorageHelperTest {

    @Test
    fun `findFileUri calls onFindUri with found URI`() {
        val mockResolver = mock<ContentResolver>()
        val mockCursor = mock<Cursor>()
        val contentUri = Uri.parse("content://media/external/file")
        val expectedId = 42L
        val expectedUri = ContentUris.withAppendedId(contentUri, expectedId)

        `when`(
            mockResolver.query(eq(contentUri), isNull(), eq("sel"), eq(arrayOf("arg1")), isNull())
        ).thenReturn(mockCursor)
        `when`(mockCursor.moveToFirst()).thenReturn(true)
        `when`(mockCursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)).thenReturn(0)
        `when`(mockCursor.getLong(0)).thenReturn(expectedId)

        var resultUri: Uri? = null

        ExternalStorageHelper.findFileUri(
            resolver = mockResolver,
            contentUri = contentUri,
            selection = "sel",
            selectionArgs = arrayOf("arg1"),
            onFindUri = { resultUri = it }
        )

        assertEquals(expectedUri, resultUri)
        verify(mockCursor).close()
    }

    @Test
    fun `createFileUri inserts content values and calls onCreateUri`() {
        val mockResolver = mock<ContentResolver>()
        val contentUri = Uri.parse("content://media/external/file")
        val returnedUri = Uri.parse("content://media/external/file/123")

        `when`(mockResolver.insert(eq(contentUri), any())).thenReturn(returnedUri)

        var resultUri: Uri? = null

        ExternalStorageHelper.createFileUri(
            fileName = "log.txt",
            mediaStoreRelativePath = "Logs/",
            resolver = mockResolver,
            contentUri = contentUri,
            onCreateUri = { resultUri = it }
        )

        assertEquals(returnedUri, resultUri)

        argumentCaptor<ContentValues>().apply {
            verify(mockResolver).insert(eq(contentUri), capture())
            assertEquals("log.txt", firstValue.getAsString(MediaStore.MediaColumns.DISPLAY_NAME))
            assertEquals("text/plain", firstValue.getAsString(MediaStore.MediaColumns.MIME_TYPE))
            assertEquals("Logs/", firstValue.getAsString(MediaStore.MediaColumns.RELATIVE_PATH))
        }
    }

    @Test
    fun `deleteFileFromMediaStore deletes file when found`() {
        val mockResolver = mock<ContentResolver>()
        val mockCursor = mock<Cursor>()
        val contentUri = Uri.parse("content://media/external/file")
        val expectedId = 123L

        `when`(mockResolver.query(eq(contentUri), any(), eq("sel"), eq(arrayOf("arg1")), isNull()))
            .thenReturn(mockCursor)
        `when`(mockCursor.moveToFirst()).thenReturn(true)
        `when`(mockCursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)).thenReturn(0)
        `when`(mockCursor.getLong(0)).thenReturn(expectedId)
        `when`(mockResolver.delete(any(), isNull(), isNull())).thenReturn(1)

        val logMock: MockedStatic<Log> = mockStatic(Log::class.java)
        try {
            val result = ExternalStorageHelper.deleteFileFromMediaStore(
                resolver = mockResolver,
                selection = "sel",
                selectionArgs = arrayOf("arg1"),
                contentUri = contentUri,
                relativePath = "DCIM/",
                fileName = "photo.jpg"
            )

            assertTrue(result)
            verify(mockResolver).delete(
                eq(ContentUris.withAppendedId(contentUri, expectedId)),
                isNull(),
                isNull()
            )
            logMock.verify { Log.d(Mockito.anyString(), Mockito.anyString()) }
        } finally {
            logMock.close()
        }
    }

    @Test
    fun `deleteFileFromMediaStore returns false when file not found`() {
        val mockResolver = mock<ContentResolver>()
        val mockCursor = mock<Cursor>()
        val contentUri = Uri.parse("content://media/external/file")

        `when`(mockResolver.query(any(), any(), any(), any(), any())).thenReturn(mockCursor)
        `when`(mockCursor.moveToFirst()).thenReturn(false)

        val result = ExternalStorageHelper.deleteFileFromMediaStore(
            resolver = mockResolver,
            selection = "sel",
            selectionArgs = arrayOf("arg1"),
            contentUri = contentUri,
            relativePath = "DCIM/",
            fileName = "missing.jpg"
        )

        assertFalse(result)
        verify(mockResolver, never()).delete(any(), any(), any())
        verify(mockCursor).close()
    }

    @Test
    fun `deleteFileFromMediaStore returns false and logs error on exception`() {
        val mockResolver = mock<ContentResolver>()
        val contentUri = Uri.parse("content://media/external/file")

        `when`(mockResolver.query(any(), any(), any(), any(), any()))
            .thenThrow(RuntimeException("DB error"))

        val logMock: MockedStatic<Log> = mockStatic(Log::class.java)
        try {
            val result = ExternalStorageHelper.deleteFileFromMediaStore(
                resolver = mockResolver,
                selection = "sel",
                selectionArgs = arrayOf("arg1"),
                contentUri = contentUri,
                relativePath = "DCIM/",
                fileName = "error.jpg"
            )

            assertFalse(result)
            logMock.verify {
                Log.e(Mockito.anyString(), Mockito.anyString())
            }
        } finally {
            logMock.close()
        }
    }
}