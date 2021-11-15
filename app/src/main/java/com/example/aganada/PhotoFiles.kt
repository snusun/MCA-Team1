package com.example.aganada

import android.content.Context
import android.nfc.FormatException
import android.util.Log
import java.io.File
import java.io.FileFilter
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object PhotoFiles {
    const val WORDBOOK = "wordbook"

    fun moveTempToWordbook(tempFile: File) {
        val wordbook = File(tempFile.parentFile?.parent, WORDBOOK)
        if (!wordbook.exists()) {
            wordbook.mkdirs()
        }
        val wordBookFile = File(wordbook, tempFile.name)

        tempFile.renameTo(wordBookFile)
    }

    fun getLabel(fileName: String): String {
        val pattern: Pattern = Pattern.compile("^.+(tmp|${WORDBOOK})/(.+)_.+\\.jpeg$")
        val matches: Matcher = pattern.matcher(fileName)
        return if (matches.matches()) {
            matches.group(2)?: ""
        } else {
            ""
        }
    }

    fun getWordbook(context: Context): List<File> {
        val mediaDir = context.externalMediaDirs.firstOrNull()?: return listOf()
        val wordbookDir = File(mediaDir, WORDBOOK)

        val files = wordbookDir.listFiles(FileFilter {
            val label = getLabel(it.absolutePath)
            return@FileFilter label.isNotBlank()
        })?: return listOf()
        return files.toList()
    }
}