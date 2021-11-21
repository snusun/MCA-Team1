package com.example.aganada

import org.junit.Test

import org.junit.Assert.*
import java.io.File

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

    @Test
    fun sorting() {
        val fileNames = arrayOf(
            "/sdcard/Android/media/com.example.aganada/wordbook/LABEL6_2021-11-15-22-15-48-770.jpeg",
            "/sdcard/Android/media/com.example.aganada/wordbook/LABEL1_2021-12-31-22-15-48-770.jpeg",
            "/sdcard/Android/media/com.example.aganada/wordbook/LABEL4_2021-05-17-22-15-48-770.jpeg",
            "/sdcard/Android/media/com.example.aganada/wordbook/LABEL3_2021-11-18-22-15-48-770.jpeg",
            "/sdcard/Android/media/com.example.aganada/wordbook/LABEL5_2021-09-20-22-15-48-770.jpeg",
            "/sdcard/Android/media/com.example.aganada/wordbook/LABEL3_2021-11-23-22-15-48-770.jpeg",
        )
        val files = List(fileNames.size) {
            File(fileNames[it])
        }
        val sorted = PhotoFiles.sortWordbook(files)
        print(sorted)
        assertEquals(sorted[0].absolutePath, fileNames[2])
    }
}