/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package jp.kaiz.kaizpatch.fixrtm.caching

import jp.kaiz.kaizpatch.fixrtm.fixRTMCommonExecutor
import jp.kaiz.kaizpatch.fixrtm.modelpack.FIXFileLoader
import jp.kaiz.kaizpatch.fixrtm.modelpack.FIXModelPack
import java.io.File

class ModelPackBasedCache(
    baseDir: File,
    vararg serializers: Pair<Int, TaggedFileManager.Serializer<*>>,
) {
    private val caches: Map<FIXModelPack, FileCache<Any>>
    private val taggedFileManager = TaggedFileManager()

    init {
        for ((id, serializer) in serializers) {
            taggedFileManager.register(id, serializer)
        }
    }

    init {
        val modelName = FIXFileLoader.allModelPacks.mapTo(mutableSetOf()) { it.file.name }

        for (removedNames in (baseDir.list()?.toSet().orEmpty() - modelName)) {
            baseDir.resolve(removedNames).deleteRecursively()
        }

        val caches = mutableMapOf<FIXModelPack, FileCache<Any>>()

        for (modelPack in FIXFileLoader.allModelPacks) {
            val cache = FileCache(
                baseDir = baseDir.resolve(modelPack.file.name),
                baseDigest = modelPack.sha1Hash,
                executor = fixRTMCommonExecutor,
                serialize = taggedFileManager::serialize,
                deserialize = taggedFileManager::deserialize,
                withTwoCharDir = false
            )
            cache.loadAll()
            caches[modelPack] = cache
        }

        this.caches = caches
    }

    fun <T : Any> get(pack: FIXModelPack, sha1: String, serializer: TaggedFileManager.Serializer<T>): T? {
        return serializer.type.cast(caches[pack]?.getCachedValue(sha1))
    }

    fun put(pack: FIXModelPack, sha1: String, model: Any) {
        caches[pack]?.putCachedValue(sha1, model)
    }

    fun discord(pack: FIXModelPack, sha1: String) {
        caches[pack]?.discordCachedValue(sha1)
    }
}
