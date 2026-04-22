/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package jp.kaiz.kaizpatch.fixrtm.caching

import jp.kaiz.kaizpatch.fixrtm.mkParent
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService

class FileCache<TValue>(
    private val baseDir: File,
    private val baseDigest: String,
    private val executor: ExecutorService,
    private val serialize: (OutputStream, TValue) -> Unit,
    private val deserialize: (InputStream) -> TValue,
    private val withTwoCharDir: Boolean = true,
) {
    private var writings = Collections.newSetFromMap<String>(ConcurrentHashMap())
    private val baseDigestFile = baseDir.resolve("base-digest")
    val cacheDiscarded: Boolean

    init {
        if (!baseDir.exists() || !baseDigestFile.exists()) {
            cacheDiscarded = false
        } else if (baseDigest != baseDigestFile.readText()) {
            cacheDiscarded = true
            baseDir.deleteRecursively()
            baseDir.mkdirs()
            baseDir.resolve("base-digest").writeText(baseDigest)
        } else {
            cacheDiscarded = false
        }
    }

    fun getCachedValue(sha1: String): TValue? {
        require(isHex40IgnoreCase(sha1)) { "invalid sha hash" }
        if (sha1 in writings) return null
        val file = getFile(sha1)
        if (!file.exists()) return null
        return readCache(file, sha1)
    }

    private fun readCache(file: File, sha1: String): TValue? {
        try {
            return file.inputStream().buffered().use(deserialize)
        } catch (e: IOException) {
            file.delete()
            return null
        } finally {
        }
    }

    fun putCachedValue(sha1: String, value: TValue) {
        require(isHex40IgnoreCase(sha1)) { "invalid sha hash" }
        executor.submit { writeCache(sha1, value) }
    }

    fun putCachedValueSync(sha1: String, value: TValue) {
        require(isHex40IgnoreCase(sha1)) { "invalid sha hash" }
        writeCache(sha1, value)
    }

    fun discordCachedValue(sha1: String) {
        require(isHex40IgnoreCase(sha1)) { "invalid sha hash" }
        getFile(sha1).delete()
    }

    fun getCachedFile(sha1: String): File {
        require(isHex40IgnoreCase(sha1)) { "invalid sha hash" }
        return getFile(sha1)
    }

    private fun writeCache(sha1: String, value: TValue) {
        val file = getFile(sha1).prepare()
        writings.add(sha1)
        try {
            val bas = ByteArrayOutputStream()
            serialize(bas, value)
            file.outputStream().buffered().use { bas.writeTo(it) }
        } catch (_: IOException) {
            file.delete()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            writings.remove(sha1)
        }
    }

    private fun getFile(sha1In: String): File {
        val sha1 = sha1In.lowercase()
        if (withTwoCharDir) {
            return baseDir.resolve(sha1.substring(0, 2))
                .resolve(sha1)
        } else {
            return baseDir.resolve(sha1)
        }
    }

    private fun File.prepare(): File = apply {
        if (!baseDir.exists()) {
            baseDir.mkdirs()
            baseDigestFile.writeText(baseDigest)
        }
        mkParent()
    }

    companion object {
        private fun hexDigestIgnoreCase(c: Char) = c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F'
        private fun isHex40IgnoreCase(v: String) = v.length == 40 && v.all { hexDigestIgnoreCase(it) }
    }
}
