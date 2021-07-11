/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package jp.kaiz.kaizpatch.fixrtm.util

import java.io.Closeable
import java.util.*

class ArrayPool<TArray>(
    private val factory: (Int) -> TArray,
    private val size: TArray.() -> Int,
) {
    private val using = BitSet()
    private val arrays = ArrayList<CloseableArray<TArray>>(0x10)

    fun request(size: Int): CloseableArray<TArray> {
        for ((i, ary) in arrays.withIndex()) {
            if (ary.array.size() < size) continue
            if (!using[i]) {
                using[i] = true
                return ary
            }
        }
        val ary = CloseableArrayImpl(factory(arraySize(size)), arrays.size)
        using[arrays.size] = true
        arrays.add(ary)
        return ary
    }

    fun get(size: Int): TArray = request(size).array

    fun release(ary: TArray) {
        val closeable = arrays.firstOrNull { it.array === ary }
            ?: throw IllegalArgumentException("the array is not created from this")
        closeable.close()
    }

    private fun arraySize(size: Int): Int {
        var s = 16
        while (s < size)
            s *= 2
        return s
    }

    interface CloseableArray<TArray> : Closeable {
        val array: TArray
    }

    private inner class CloseableArrayImpl(override val array: TArray, private val index: Int) :
        CloseableArray<TArray> {
        override fun close() {
            using[index] = false
        }
    }

    companion object {
        private val pByte: ThreadLocal<ArrayPool<ByteArray>> = ThreadLocal.withInitial {
            ArrayPool(::ByteArray) { size }
        }
        private val pChar: ThreadLocal<ArrayPool<CharArray>> = ThreadLocal.withInitial {
            ArrayPool(::CharArray) { size }
        }

        val bytePool get() = pByte.get()
        val charPool get() = pChar.get()
    }
}
