//Copyright © 2021 anatawa12.

package jp.kaiz.kaizpatch.fixrtm.modelpack

import jp.kaiz.kaizpatch.fixrtm.directoryDigestBaseStream
import jp.kaiz.kaizpatch.fixrtm.util.DigestUtils
import jp.ngt.ngtlib.io.NGTFileLoader
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.ZipFile

object FIXFileLoader {
    val allModelPacks: Set<FIXModelPack>
    private val packs: Map<String, Set<FIXModelPack>>

    private val logger = LogManager.getLogger("FIXFileLoader")

    init {
        val packs = HashMap<String, MutableSet<FIXModelPack>>()

        for (file in getFiles()) {
            val pack = loadModelPack(file) ?: continue

            for (domain in pack.domains) {
                packs.computeIfAbsent(domain) { HashSet() }.add(pack)
            }
        }

        FIXFileLoader.packs = packs

        allModelPacks = packs.flatMapTo(mutableSetOf()) { it.value }
        logger.info("FIXFileLoader loads model packs:")
        allModelPacks.forEach {
            logger.info("${it.file.name}: ${it.domains}")
        }
    }

    fun getFiles(): List<File> {
        val fileList: MutableList<File> = mutableListOf()
        NGTFileLoader.getModsDir().mapNotNull { it.listFiles() }.forEach { fileList += listOf(*it) }
        return fileList
    }

    fun getResource(location: ResourceLocation): FIXResource {
        packs[location.resourceDomain].orEmpty().forEach { fixModelPack ->
            fixModelPack.getFile(location)?.let { return it }
        }
        throw FileNotFoundException("$location")
    }

    fun getInputStream(location: ResourceLocation): InputStream = getResource(location).inputStream

    private fun loadModelPack(file: File): FIXModelPack? {
        try {
            try {
                if (file.isFile) {
                    if (file.extension != "jar" && file.extension != "zip") return null
                    return ZipModelPack(file)
                } else {
                    return DirectoryModelPack(file)
                }
            } catch (e: IllegalArgumentException) {
                if (file.isFile) {
                    return ZipModelPack(file, Charset.forName("MS932"))
                } else {
                    throw e
                }
            }
        } catch (e: Throwable) {
            logger.error("trying to construct model pack: ${file.name}", e)
            return null
        }
    }

    private class ZipModelPack(override val file: File, charset: Charset = Charsets.UTF_8) : FIXModelPack {
        private val zipFile = ZipFile(file, charset)

        override val sha1Hash: String = DigestUtils.sha1Hex(file.inputStream().buffered())

        override val domains: Set<String>
        private val ignoreCaseMap: Map<String, String>

        init {
            val domains = mutableSetOf<String>()
            val ignoreCaseMap = mutableMapOf<String, String>()
            zipFile.stream().forEach { entry ->
                val parts = entry.name.split("/")
                if (parts[0] == "assets" && parts.size >= 2 && parts[1].isNotEmpty())
                    domains.add(parts[1])
                ignoreCaseMap[entry.name.toLowerCase()] = entry.name
            }
            this.domains = domains
            this.ignoreCaseMap = ignoreCaseMap
        }

        override fun getFile(location: ResourceLocation): FIXResource? {
            var path = "assets/${location.resourceDomain}/${location.resourcePath}"
            path = ignoreCaseMap[path] ?: path
            val file = zipFile.getEntry(path) ?: return null
            return FIXResource(this, zipFile.getInputStream(file))
        }
    }

    private class DirectoryModelPack(override val file: File) : FIXModelPack {
        override val sha1Hash: String = DigestUtils.sha1Hex(file.directoryDigestBaseStream())

        override val domains: Set<String>

        init {
            val domains = mutableSetOf<String>()
            (if (file.name == "assets") file else file.resolve("assets")).listFiles()?.forEach {
                if (it.isDirectory) {
                    domains.add(it.name)
                }
            }
            this.domains = domains
        }

        override fun getFile(location: ResourceLocation): FIXResource? {
            val path = "${location.resourceDomain}/${location.resourcePath}"
            return try {
                FIXResource(this, file.resolve(path).inputStream())
            } catch (e: FileNotFoundException) {
                null
            }
        }
    }

    fun load() {}
}