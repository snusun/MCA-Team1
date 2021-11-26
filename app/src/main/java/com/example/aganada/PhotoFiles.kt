package com.example.aganada

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileFilter
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object PhotoFiles {
    private const val WORDBOOK = "wordbook"
    private val pattern: Pattern = Pattern.compile("^.+(tmp|${WORDBOOK})/(.+)_(.+)\\.jpeg$")

    fun moveTempToWordbook(tempFile: File) {
        val wordbook = File(tempFile.parentFile?.parent, WORDBOOK)
        if (!wordbook.exists()) {
            wordbook.mkdirs()
        }
        val wordBookFile = File(wordbook, tempFile.name)

        tempFile.renameTo(wordBookFile)
    }

    fun getLabel(fileName: String): String {
        val matches: Matcher = pattern.matcher(fileName)
        return if (matches.matches()) {
            matches.group(2)?: ""
        } else {
            ""
        }
    }

    fun getWordbook(context: Context): List<File> {
        val mediaDir = File(context.dataDir.canonicalPath, "files")
        val wordbookDir = File(mediaDir, WORDBOOK)

        val files = wordbookDir.listFiles(FileFilter {
            val label = getLabel(it.absolutePath)
            return@FileFilter label.isNotBlank()
        })?: return listOf()
        return sortWordbook(files.asIterable())
    }

    fun sortWordbook(files: Iterable<File>): List<File> {
        return files.sortedBy{
            val matches: Matcher = pattern.matcher(it.absolutePath)
            if (matches.matches()) {
                matches.group(3)?: ""
            } else {
                ""
            }
        }
    }

}