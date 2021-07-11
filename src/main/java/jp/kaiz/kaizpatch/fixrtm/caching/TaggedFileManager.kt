/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package jp.kaiz.kaizpatch.fixrtm.caching

import com.google.common.collect.HashBiMap
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

class TaggedFileManager {
    private val map = HashBiMap.create<Int, Serializer<*>>()
    private val classMap = ConcurrentHashMap<Class<*>, Serializer<*>>()

    fun deserialize(stream: InputStream): Any {
        val serializer = map[readVInt(stream)]
            ?: throw IOException("invalid stream: invalid id")
        return serializer.deserialize(stream)
    }

    fun serialize(stream: OutputStream, value: Any) {
        val serializer = getSerializerFor(value)
        val id = map.inverse()[serializer]
            ?: throw IOException("serializer for ${value.javaClass} is not register")
        writeVInt(stream, id)
        serializer.serialize(stream, value)
    }

    fun register(id: Int, serializer: Serializer<*>) {
        if (id in map) throw IllegalStateException("id duplicate: $id")
        map[id] = serializer
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getSerializerFor(value: T): Serializer<T> {
        val clazz = value.javaClass
        return classMap.computeIfAbsent(clazz) { computeSerializerFor(it) } as Serializer<T>
    }

    private fun <T : Any> computeSerializerFor(clazz: Class<T>): Serializer<T> {
        for (serializer in map.values) {
            if (serializer.type.isAssignableFrom(clazz)) {
                @Suppress("UNCHECKED_CAST")
                return serializer as Serializer<T>
            }
        }
        throw IOException("invalid value: no serializer found.")
    }

    private fun writeVInt(stream: OutputStream, v: Int) {
        if (v < 0) {
            throw IOException("too small: $v")
        } else if (v < 0x80) {
            // 00000000 0xxxxxxx -> 0xxxxxxx

            stream.write(v)
        } else if (v < 0x4000) {
            // 0xxxxxxx xxxxxxxx -> 1xxxxxxx xxxxxxxx
            stream.write(v ushr 8 or 0x80)
            stream.write(v and 0xFF)
        } else {
            throw IOException("too big: $v")
        }
    }

    private fun readVInt(stream: InputStream): Int {
        val first = stream.read().takeUnless { it == -1 } ?: throw EOFException()
        if (first and 0x80 == 0) {
            // 0xxxxxxx
            return first
        } else {
            // 1xxxxxxx xxxxxxxx
            val second = stream.read().takeUnless { it == -1 } ?: throw EOFException()
            return first and 0x7F shl 8 or second
        }
    }

    interface Serializer<T : Any> {
        val type: Class<T>
        fun serialize(stream: OutputStream, value: T)
        fun deserialize(stream: InputStream): T
    }
}
