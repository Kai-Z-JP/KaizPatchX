package jp.kaiz.kaizpatch.rtm.modelpack

import jp.kaiz.kaizpatch.fixrtm.modelpack.FIXModelPack
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.LogManager
import java.util.concurrent.ConcurrentHashMap

internal class ModelPackResourceCollisionDetector(
    private val packs: Map<String, Set<FIXModelPack>>,
    private val onCollision: (ResourceLocation, FIXModelPack, List<FIXModelPack>) -> Unit =
        ::warnModelPackResourceCollision,
) {
    private val inspectedLocations = ConcurrentHashMap.newKeySet<ResourceLocation>()

    fun inspect(location: ResourceLocation, selectedPack: FIXModelPack) {
        if (!inspectedLocations.add(location)) return

        val ignoredPacks = mutableListOf<FIXModelPack>()
        for (pack in packs[location.resourceDomain].orEmpty()) {
            if (pack === selectedPack) continue
            val hasFile = try {
                pack.hasFile(location)
            } catch (_: Exception) {
                false
            }
            if (hasFile) ignoredPacks += pack
        }

        if (ignoredPacks.isEmpty()) return
        try {
            onCollision(location, selectedPack, ignoredPacks)
        } catch (_: Exception) {
        }
    }
}

private val logger = LogManager.getLogger("KaizPatch/ModelPackResourceCollision")

private fun warnModelPackResourceCollision(
    location: ResourceLocation,
    selectedPack: FIXModelPack,
    ignoredPacks: List<FIXModelPack>,
) {
    logger.warn(
        "Model-pack resource collision: resource={}, selected={}, ignored={}, providerCount={}. " +
                "Selection order is not guaranteed and may change between launches.",
        location,
        describePack(selectedPack),
        ignoredPacks.joinToString(prefix = "[", postfix = "]", transform = ::describePack),
        ignoredPacks.size + 1,
    )
}

private fun describePack(pack: FIXModelPack): String {
    val safeFileName = pack.file.name
        .take(160)
        .map { character -> if (character.isISOControl() || character == '"') '?' else character }
        .joinToString(separator = "")
    return "\"$safeFileName\" (sha1=${pack.sha1Hash.take(12)})"
}
