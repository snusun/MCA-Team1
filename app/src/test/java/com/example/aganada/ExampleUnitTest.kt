package com.example.aganada

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val fileName = "/sdcard/Android/media/com.example.aganada/wordbook/LABEL_2021-11-15-22-15-48-770.jpeg"
        val label = PhotoFiles.getLabel(fileName)
        assertEquals("LABEL", label)

        val tmpFileName = "/sdcard/Android/media/com.example.aganada/tmp/LABEL_2021-11-15-22-15-48-770.jpeg"
        val tmpLabel = PhotoFiles.getLabel(tmpFileName)
        assertEquals("LABEL", tmpLabel)
    }
}