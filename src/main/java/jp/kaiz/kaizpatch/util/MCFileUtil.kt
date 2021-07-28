package jp.kaiz.kaizpatch.util

import jp.ngt.ngtlib.io.NGTFileLoader
import net.minecraft.util.ResourceLocation
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

class MCFileUtil {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun readText(file: File, indention: Boolean = false): String {
            return readText(NGTFileLoader.getInputStreamFromFile(file), indention)
        }

        @JvmStatic
        @JvmOverloads
        fun readText(resource: ResourceLocation, indention: Boolean = false): String {
            return readText(NGTFileLoader.getInputStream(resource), indention)
        }

        private fun readText(inputStream: InputStream, indention: Boolean = false): String {
            val byteArray = inputStream.readBytes()
            return String(byteArray)
                .let { if ("�" in it) String(byteArray, Charset.forName("MS932")) else it }
                .let { if (!indention) it.replace(System.lineSeparator(), "") else it }
        }

        @JvmStatic
        fun readTextList(file: File): MutableList<String> {
            return readTextList(NGTFileLoader.getInputStreamFromFile(file))
        }

        @JvmStatic
        fun readTextList(resourceLocation: ResourceLocation): MutableList<String> {
            return readTextList(NGTFileLoader.getInputStream(resourceLocation))
        }

        private fun readTextList(inputStream: InputStream): MutableList<String> {
            return readText(inputStream, true).split(System.lineSeparator()).toMutableList()
        }
    }
}