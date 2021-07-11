/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package jp.kaiz.kaizpatch.fixrtm.util

import com.google.common.collect.Iterators
import java.io.File
import java.util.*

/**
 * like [FileTreeWalk], but the result must be sorted
 */
class SortedFileTreeWalk internal constructor(private val start: File) : Sequence<File> {
    /** Returns an iterator walking through files. */
    override fun iterator(): Iterator<File> = when {
        start.isDirectory -> FileTreeWalkIterator()
        start.isFile -> Iterators.singletonIterator(start)
        else -> emptyList<File>().iterator()
    }

    private inner class FileTreeWalkIterator : AbstractIterator<File>() {
        private val state = ArrayDeque<WalkState>()

        init {
            state.push(WalkState(start))
        }

        override fun computeNext() {
            val nextFile = gotoNext()
            if (nextFile != null)
                setNext(nextFile)
            else
                done()
        }

        private tailrec fun gotoNext(): File? {
            // Take next file from the top of the stack or return if there's nothing left
            val topState = state.peek() ?: return null
            val file = topState.step()
            if (file == null) {
                // There is nothing more on the top of the stack, go back
                state.pop()
                return gotoNext()
            } else {
                // Check that file/directory matches the filter
                if (file == topState.root || !file.isDirectory) {
                    // Proceed to a root directory or a simple file
                    return file
                } else {
                    // Proceed to a sub-directory
                    state.push(WalkState(file))
                    return gotoNext()
                }
            }
        }

        /** Visiting in top-down order */
        private inner class WalkState(val root: File) {
            private var fileList: Array<File>? = null

            private var fileIndex = 0

            /** First root directory, then all children */
            fun step(): File? {
                if (fileList == null || fileIndex < fileList!!.size) {
                    if (fileList == null) {
                        // Then read an array of files, if any
                        fileList = root.listFiles()?.sortedArray()
                        if (fileList == null || fileList!!.isEmpty()) {
                            return null
                        }
                    }
                    // Then visit all files
                    return fileList!![fileIndex++]
                } else {
                    // That's all
                    return null
                }
            }
        }

    }
}

fun File.sortedWalk(): SortedFileTreeWalk = SortedFileTreeWalk(this)
