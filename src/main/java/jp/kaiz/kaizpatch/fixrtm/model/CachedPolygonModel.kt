/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package jp.kaiz.kaizpatch.fixrtm.model

import jp.kaiz.kaizpatch.KaizPatchX
import jp.kaiz.kaizpatch.fixrtm.cachedModelLoaderExecutor
import jp.kaiz.kaizpatch.fixrtm.caching.ModelPackBasedCache
import jp.kaiz.kaizpatch.fixrtm.caching.TaggedFileManager
import jp.kaiz.kaizpatch.fixrtm.fixCacheDir
import jp.kaiz.kaizpatch.fixrtm.modelpack.FIXModelPack
import jp.kaiz.kaizpatch.fixrtm.readUTFNullable
import jp.kaiz.kaizpatch.fixrtm.util.DigestUtils
import jp.kaiz.kaizpatch.fixrtm.writeUTFNullable
import jp.ngt.ngtlib.NGTCore
import jp.ngt.ngtlib.io.FileType
import jp.ngt.ngtlib.renderer.model.*
import jp.ngt.rtm.RTMConfig
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.LogManager
import java.io.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference

object CachedPolygonModel {
    private val cache = ModelPackBasedCache(
        fixCacheDir.resolve("polygon-model"),
        0x0000 to Serializer
    )
    private val logger = LogManager.getLogger("CachedPolygonModel")
    private val trackedModels =
        Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap<AsyncCachedModel, Boolean>()))

    val type = FileType("fixrtm-cached-polygon-model-file", "fixrtm cached polygon model file.")

    fun getCachedModel(pack: FIXModelPack, resource: ResourceLocation, accuracy: VecAccuracy): PolygonModel? {
        return getCachedModel(pack, cacheKey(resource, accuracy))
    }

    fun putCachedModel(pack: FIXModelPack, resource: ResourceLocation, accuracy: VecAccuracy, model: PolygonModel) {
        cache.put(pack, cacheKey(resource, accuracy), model)
    }

    fun putCachedModelSync(pack: FIXModelPack, resource: ResourceLocation, accuracy: VecAccuracy, model: PolygonModel) {
        cache.putSync(pack, cacheKey(resource, accuracy), model)
    }

    fun createCachedModel(
        pack: FIXModelPack,
        resource: ResourceLocation,
        accuracy: VecAccuracy,
        model: PolygonModel
    ): PolygonModel {
        val sha1 = cacheKey(resource, accuracy)
        val cacheFile = cache.getFile(pack, sha1)
            ?: error("cache file missing for $resource")
        return AsyncCachedModel(
            header = ModelHeader.from(resource.toString(), model, cacheFile.length()),
            initialModel = model,
            loader = { getCachedModel(pack, sha1) }
        ).also { trackedModels += it }
    }

    fun compact(model: IModelNGT?) {
        if (model is AsyncCachedModel) {
            model.compactLoadedModel()
        }
    }

    fun pin(model: IModelNGT?) {
        if (model is AsyncCachedModel) {
            model.pinLoadedModel()
        }
    }

    fun prepareSharedUse(model: IModelNGT?) {
        if (model is AsyncCachedModel) {
            model.ensureLoadedForSharedUse()
        }
    }

    fun getDebugLines(): List<String> {
        val modelSnapshot = snapshotModels()
        val lruSnapshot = LoadedModelLru.snapshot()
        return listOf(
            "",
            "KaizPatchX/${KaizPatchX.VERSION}",
            " cached-model",
            String.format(
                Locale.ROOT,
                "  mem: %.1f/%.1f MiB, loaded=%d, protected=%d, sec=%d",
                bytesToMiB(lruSnapshot.totalWeight),
                bytesToMiB(lruSnapshot.maxWeight),
                lruSnapshot.loadedCount,
                lruSnapshot.protectedCount,
                configuredProtectionMillis() / 1000,
            ),
            String.format(
                Locale.ROOT,
                "  state: A=%d, U=%d, Q=%d, L=%d, R=%d, F=%d",
                modelSnapshot.total,
                modelSnapshot.unloaded,
                modelSnapshot.queued,
                modelSnapshot.loading,
                modelSnapshot.ready,
                modelSnapshot.failed
            ),
            "",
        )
    }

    private fun cacheKey(resource: ResourceLocation, accuracy: VecAccuracy): String {
        return DigestUtils.sha1Hex("cached-model:$accuracy:$resource")
    }

    private fun getCachedModel(pack: FIXModelPack, sha1: String): PolygonModel? {
        return cache.get(pack, sha1, Serializer)
    }

    private fun snapshotModels(): ModelStateSnapshot {
        val models = synchronized(trackedModels) { trackedModels.toList() }
        var unloaded = 0
        var queued = 0
        var loading = 0
        var ready = 0
        var failed = 0

        for (model in models) {
            when (model.currentState()) {
                LoadState.UNLOADED -> unloaded++
                LoadState.QUEUED -> queued++
                LoadState.LOADING -> loading++
                LoadState.READY -> ready++
                LoadState.FAILED -> failed++
            }
        }

        return ModelStateSnapshot(
            total = models.size,
            unloaded = unloaded,
            queued = queued,
            loading = loading,
            ready = ready,
            failed = failed
        )
    }

    private fun bytesToMiB(bytes: Long): Double = bytes / (1024.0 * 1024.0)
    private fun configuredMaxWeight(): Long = RTMConfig.fixRTMCachedModelMemoryLimitMiB.toLong() * 1024L * 1024L
    private fun configuredProtectionMillis(): Long = RTMConfig.fixRTMCachedModelProtectSeconds.toLong() * 1000L
    private fun debugInfo(message: String, vararg args: Any?) {
        if (NGTCore.debugLog) {
            logger.info(message, *args)
        }
    }

    private fun debugWarn(message: String, throwable: Throwable) {
        if (NGTCore.debugLog) {
            logger.warn(message, throwable)
        }
    }

    private fun debugWarn(message: String, vararg args: Any?) {
        if (NGTCore.debugLog) {
            logger.warn(message, *args)
        }
    }

    private object Serializer : TaggedFileManager.Serializer<PolygonModel> {
        override val type: Class<PolygonModel> get() = PolygonModel::class.java

        override fun serialize(stream: OutputStream, value: PolygonModel) {
            CachedModelWriter.writeCachedModel(stream, value)
        }

        override fun deserialize(stream: InputStream): PolygonModel {
            return CachedModel(stream)
        }
    }

    private data class ModelHeader(
        val modelName: String,
        val drawMode: Int,
        val type: FileType,
        val materials: Map<String, Material>,
        val weight: Long,
    ) {
        companion object {
            fun from(modelName: String, model: PolygonModel, weight: Long): ModelHeader {
                return ModelHeader(
                    modelName = modelName,
                    drawMode = model.drawMode,
                    type = model.type,
                    materials = model.materials.mapValues { (_, material) ->
                        Material(material.id, material.texture)
                    },
                    weight = maxOf(weight, 1L)
                )
            }
        }
    }

    private enum class LoadState {
        UNLOADED,
        QUEUED,
        LOADING,
        READY,
        FAILED
    }

    private data class ModelStateSnapshot(
        val total: Int,
        val unloaded: Int,
        val queued: Int,
        val loading: Int,
        val ready: Int,
        val failed: Int,
    )

    private class AsyncCachedModel(
        private val header: ModelHeader,
        initialModel: PolygonModel,
        private val loader: () -> PolygonModel?,
    ) : PolygonModel() {
        private val state = AtomicReference(LoadState.READY)

        @Volatile
        private var pinned = false

        @Volatile
        private var loadedModel: PolygonModel? = initialModel

        init {
            syncModelView(initialModel)
        }

        fun compactLoadedModel() {
            if (pinned) {
                return
            }
            if (loadedModel != null) {
                debugInfo(
                    "Discarding cached model from memory: model={}, reason=post-init-compact, weight={} bytes",
                    header.modelName,
                    header.weight
                )
            }
            clearModelView()
            loadedModel = null
            if (state.get() == LoadState.READY) {
                state.set(LoadState.UNLOADED)
            }
            LoadedModelLru.remove(this)
        }

        fun pinLoadedModel() {
            pinned = true
            LoadedModelLru.remove(this)
        }

        @Synchronized
        fun ensureLoadedForSharedUse() {
            val current = loadedModel
            if (current != null) {
                syncModelView(current)
                if (state.get() != LoadState.READY) {
                    state.set(LoadState.READY)
                }
                touchLoadedModel()
                return
            }

            debugInfo(
                "Restoring cached model for shared reuse: model={}, type={}, weight={} bytes",
                header.modelName,
                header.type.extension,
                header.weight
            )
            val model = try {
                loader()
            } catch (t: Throwable) {
                debugWarn("Failed to restore cached model for shared reuse", t)
                null
            }

            if (model == null) {
                state.set(LoadState.FAILED)
                debugWarn(
                    "Cached model restore for shared reuse failed: model={}, type={}, weight={} bytes",
                    header.modelName,
                    header.type.extension,
                    header.weight
                )
                return
            }

            loadedModel = model
            syncModelView(model)
            state.set(LoadState.READY)
            touchLoadedModel()
        }

        override fun renderAll(smoothing: Boolean) {
            getLoadedModel()?.renderAll(smoothing)
        }

        override fun renderOnly(smoothing: Boolean, vararg groupNames: String) {
            getLoadedModel()?.renderOnly(smoothing, *groupNames)
        }

        override fun renderPart(smoothing: Boolean, partName: String) {
            getLoadedModel()?.renderPart(smoothing, partName)
        }

        override fun getDrawMode(): Int = header.drawMode

        override fun getGroupObjects(): List<GroupObject> {
            return getLoadedModel()?.groupObjects ?: Collections.emptyList()
        }

        override fun getMaterials(): Map<String, Material> {
            val model = loadedModel
            if (model != null && state.get() == LoadState.READY) {
                touchLoadedModel()
                return model.materials
            }
            return header.materials
        }

        override fun getType(): FileType = header.type

        override fun parseLine(currentLine: String?, lineCount: Int) {
            throw UnsupportedOperationException("AsyncCachedModel does not parse source data directly")
        }

        override fun postInit() {
            throw UnsupportedOperationException("AsyncCachedModel does not initialize source data directly")
        }

        private fun getLoadedModel(): PolygonModel? {
            val model = loadedModel
            if (model != null && state.get() == LoadState.READY) {
                touchLoadedModel()
                return model
            }

            queueLoad()
            return null
        }

        private fun queueLoad() {
            if (!state.compareAndSet(LoadState.UNLOADED, LoadState.QUEUED)) {
                return
            }

            debugInfo(
                "Queueing dynamic cached model load: model={}, type={}, weight={} bytes",
                header.modelName,
                header.type.extension,
                header.weight
            )
            cachedModelLoaderExecutor.submit {
                if (!state.compareAndSet(LoadState.QUEUED, LoadState.LOADING)) {
                    return@submit
                }
                val model = try {
                    debugInfo(
                        "Starting dynamic cached model load: model={}, type={}, weight={} bytes",
                        header.modelName,
                        header.type.extension,
                        header.weight
                    )
                    loader()
                } catch (t: Throwable) {
                    debugWarn("Failed to restore cached model asynchronously", t)
                    null
                }

                if (model == null) {
                    state.set(LoadState.FAILED)
                    debugWarn(
                        "Dynamic cached model load failed: model={}, type={}, weight={} bytes",
                        header.modelName,
                        header.type.extension,
                        header.weight
                    )
                    return@submit
                }

                loadedModel = model
                syncModelView(model)
                state.set(LoadState.READY)
                debugInfo(
                    "Completed dynamic cached model load: model={}, type={}, weight={} bytes",
                    header.modelName,
                    header.type.extension,
                    header.weight
                )
                touchLoadedModel()
            }
        }

        private fun touchLoadedModel() {
            if (pinned) {
                return
            }
            LoadedModelLru.touch(this, header.weight)
        }

        fun currentState(): LoadState = state.get()

        fun evictLoadedModel(reason: String) {
            if (pinned) {
                return
            }
            if (state.compareAndSet(LoadState.READY, LoadState.UNLOADED)) {
                debugInfo(
                    "Discarding cached model from memory: model={}, reason={}, weight={} bytes",
                    header.modelName,
                    reason,
                    header.weight
                )
                clearModelView()
                loadedModel = null
            }
        }

        private fun syncModelView(model: PolygonModel) {
            drawMode = model.drawMode
            accuracy = model.accuracy
            System.arraycopy(model.sizeBox, 0, sizeBox, 0, sizeBox.size)
            groupObjects.clear()
            groupObjects.addAll(model.groupObjects)
        }

        private fun clearModelView() {
            groupObjects.clear()
            Arrays.fill(sizeBox, 0.0F)
        }
    }

    private object LoadedModelLru {
        private val lru = LinkedHashMap<AsyncCachedModel, LruEntry>(16, 0.75f, true)
        private var totalWeight = 0L

        fun touch(model: AsyncCachedModel, weight: Long) {
            val evicted = mutableListOf<AsyncCachedModel>()
            val now = System.currentTimeMillis()
            val protectionMillis = configuredProtectionMillis()
            val maxWeight = configuredMaxWeight()
            synchronized(this) {
                lru.remove(model)?.let { totalWeight -= it.weight }
                lru[model] = LruEntry(weight, now)
                totalWeight += weight

                val iterator = lru.entries.iterator()
                while (totalWeight > maxWeight && lru.size > 1 && iterator.hasNext()) {
                    val entry = iterator.next()
                    if (now - entry.value.lastTouchedAt < protectionMillis) {
                        break
                    }
                    iterator.remove()
                    totalWeight -= entry.value.weight
                    evicted += entry.key
                }
            }

            evicted.forEach { it.evictLoadedModel("size-limit") }
        }

        fun remove(model: AsyncCachedModel) {
            synchronized(this) {
                lru.remove(model)?.let { totalWeight -= it.weight }
            }
        }

        fun snapshot(): LruSnapshot {
            synchronized(this) {
                val now = System.currentTimeMillis()
                val protectionMillis = configuredProtectionMillis()
                val protectedCount = lru.values.count { now - it.lastTouchedAt < protectionMillis }
                val maxWeight = configuredMaxWeight()
                return LruSnapshot(
                    loadedCount = lru.size,
                    protectedCount = protectedCount,
                    totalWeight = totalWeight,
                    maxWeight = maxWeight
                )
            }
        }

        private data class LruEntry(
            val weight: Long,
            val lastTouchedAt: Long,
        )
    }

    private data class LruSnapshot(
        val loadedCount: Int,
        val protectedCount: Int,
        val totalWeight: Long,
        val maxWeight: Long,
    )

    private class CachedModel(file: InputStream) : PolygonModel() {
        private val materials = mutableMapOf<String, Material>()

        init {
            val reader = DataInputStream(file) as DataInput
            drawMode = reader.readInt()
            accuracy = readVecAccuracy(reader)
            readSizeBox(reader)
            repeat(reader.readInt()) {
                groupObjects.add(readGroupObject(reader))
            }
            repeat(reader.readInt()) {
                materials[reader.readUTF()] = readMaterial(reader)
            }
        }

        private fun readVecAccuracy(reader: DataInput) = when (val value = reader.readUnsignedByte()) {
            0 -> VecAccuracy.LOW
            1 -> VecAccuracy.MEDIUM
            else -> error("invalid VecAccuracy: $value")
        }

        private fun readSizeBox(reader: DataInput) {
            sizeBox[0] = reader.readFloat()
            sizeBox[1] = reader.readFloat()
            sizeBox[2] = reader.readFloat()
            sizeBox[3] = reader.readFloat()
            sizeBox[4] = reader.readFloat()
            sizeBox[5] = reader.readFloat()
        }

        private fun readGroupObject(reader: DataInput): GroupObject {
            val obj = GroupObject(reader.readUTF(), reader.readByte().toInt())
            obj.smoothingAngle = reader.readFloat()
            repeat(reader.readInt()) {
                obj.faces.add(readFace(reader))
            }
            return obj
        }

        private fun readFace(reader: DataInput): Face {
            val material = reader.readByte().toInt()
            val size = reader.readInt()
            val face = Face(size, material)
            face.vertexNormals = arrayOfNulls(size)
            repeat(size) { i ->
                face.vertices[i] = readVertex(reader)
                face.textureCoordinates[i] = readTextureCoordinate(reader)
                face.vertexNormals[i] = readVertex(reader)
            }
            face.faceNormal = readVertex(reader)

            return face
        }

        private fun readVertex(reader: DataInput): Vertex {
            return Vertex.create(
                reader.readFloat(),
                reader.readFloat(),
                reader.readFloat(),
                accuracy
            )
        }

        private fun readTextureCoordinate(reader: DataInput): TextureCoordinate {
            return TextureCoordinate.create(
                reader.readFloat(),
                reader.readFloat(),
                accuracy
            )
        }

        private fun readMaterial(reader: DataInput): Material {
            return Material(reader.readByte(), reader.readUTFNullable()?.let { ResourceLocation(it) })
        }

        override fun getMaterials(): Map<String, Material> = materials
        override fun getType(): FileType = CachedPolygonModel.type

        override fun parseLine(p0: String?, p1: Int) = TODO("Not yet implemented")
        override fun postInit() = TODO("Not yet implemented")
    }

    private object CachedModelWriter {

        fun writeCachedModel(writer: OutputStream, value: PolygonModel) {
            writeCachedModel(DataOutputStream(writer) as DataOutput, value)
        }

        fun writeCachedModel(writer: DataOutput, value: PolygonModel) {
            writer.writeInt(value.drawMode)
            writeVecAccuracy(writer, value.accuracy)
            writeSizeBox(writer, value.sizeBox)
            writer.writeInt(value.groupObjects.size)
            for (groupObject in value.groupObjects) {
                writeGroupObject(writer, groupObject)
            }
            writer.writeInt(value.materials.size)
            for ((name, material) in value.materials) {
                writer.writeUTF(name)
                writeMaterial(writer, material)
            }
        }


        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        private fun writeVecAccuracy(writer: DataOutput, value: VecAccuracy) = when (value) {
            VecAccuracy.LOW -> writer.writeByte(0)
            VecAccuracy.MEDIUM -> writer.writeByte(1)
            else -> error("invalid VecAccuracy: $value")
        }

        private fun writeSizeBox(writer: DataOutput, value: FloatArray) {
            writer.writeFloat(value[0])
            writer.writeFloat(value[1])
            writer.writeFloat(value[2])
            writer.writeFloat(value[3])
            writer.writeFloat(value[4])
            writer.writeFloat(value[5])
        }

        private fun writeGroupObject(writer: DataOutput, value: GroupObject) {
            writer.writeUTF(value.name)
            writer.writeByte(value.drawMode.toInt())
            writer.writeFloat(value.smoothingAngle)
            writer.writeInt(value.faces.size)
            for (face in value.faces) {
                writeFace(writer, face)
            }
        }

        private fun writeFace(writer: DataOutput, value: Face) {
            writer.writeByte(value.materialId.toInt())
            writer.writeInt(value.vertices.size)
            for (i in value.vertices.indices) {
                writeVertex(writer, value.vertices[i])
                writeTextureCoordinate(writer, value.textureCoordinates[i])
                writeVertex(writer, value.vertexNormals[i])
            }

            writeVertex(writer, value.faceNormal)
        }

        private fun writeVertex(writer: DataOutput, value: Vertex) {
            writer.writeFloat(value.x)
            writer.writeFloat(value.y)
            writer.writeFloat(value.z)
        }

        private fun writeTextureCoordinate(writer: DataOutput, value: TextureCoordinate) {
            writer.writeFloat(value.u)
            writer.writeFloat(value.v)
        }

        private fun writeMaterial(writer: DataOutput, value: Material) {
            writer.writeByte(value.id.toInt())
            writer.writeUTFNullable(value.texture?.toString())
        }
    }

    fun load() {}
}
