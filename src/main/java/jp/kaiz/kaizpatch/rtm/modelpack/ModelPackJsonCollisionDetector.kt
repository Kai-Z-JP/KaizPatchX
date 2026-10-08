package jp.kaiz.kaizpatch.rtm.modelpack

import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ModelPackJsonCollisionDetector @JvmOverloads constructor(
    private val registry: String,
    private val onCollision: (String, String, File, File) -> Unit = ::warnModelPackJsonCollision,
) {
    private val sources = ConcurrentHashMap<String, File>()

    fun record(key: String, source: File, isReplacement: Boolean) {
        val replacedSource = sources.put(key, source) ?: return
        if (!isReplacement || replacedSource.absoluteFile == source.absoluteFile) return

        try {
            onCollision(registry, key, source, replacedSource)
        } catch (_: Exception) {
        }
    }
}

private val jsonCollisionLogger = LogManager.getLogger("KaizPatch/ModelPackJsonCollision")

private fun warnModelPackJsonCollision(
    registry: String,
    key: String,
    selectedSource: File,
    replacedSource: File,
) {
    jsonCollisionLogger.warn(
        "Model-pack JSON registration collision: registry={}, key=\"{}\", selected={}, replaced={}. " +
                "Registration order is not guaranteed and may change between launches.",
        safeLogText(registry),
        safeLogText(key),
        describeJsonSource(selectedSource),
        describeJsonSource(replacedSource),
    )
}

private fun describeJsonSource(file: File): String {
    val path = file.absolutePath.replace('\\', '/')
    val archiveMatch = ARCHIVE_PATH.findAll(path).lastOrNull()
    val displayPath = if (archiveMatch != null) {
        val archiveEnd = archiveMatch.range.first + 4
        val archiveName = path.substring(0, archiveEnd).substringAfterLast('/')
        "$archiveName!/${path.substring(archiveEnd + 1)}"
    } else {
        val assetsIndex = path.lowercase().lastIndexOf("/assets/")
        if (assetsIndex >= 0) {
            val packName = path.substring(0, assetsIndex).substringAfterLast('/')
            "$packName/${path.substring(assetsIndex + 1)}"
        } else {
            path.split('/').takeLast(3).joinToString("/")
        }
    }
    return "\"${safeLogText(displayPath)}\""
}

private fun safeLogText(value: String): String {
    return value.take(400).map { character ->
        if (character.isISOControl() || character == '"') '?' else character
    }.joinToString(separator = "")
}

private val ARCHIVE_PATH = Regex("(?i)\\.(?:jar|zip)/")
