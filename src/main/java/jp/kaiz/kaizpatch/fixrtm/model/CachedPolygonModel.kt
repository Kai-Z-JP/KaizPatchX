package jp.kaiz.kaizpatch.fixrtm.model

import jp.kaiz.kaizpatch.fixrtm.caching.ModelPackBasedCache
import jp.kaiz.kaizpatch.fixrtm.caching.TaggedFileManager
import jp.kaiz.kaizpatch.fixrtm.fixCacheDir
import jp.kaiz.kaizpatch.fixrtm.modelpack.FIXModelPack
import jp.kaiz.kaizpatch.fixrtm.readUTFNullable
import jp.kaiz.kaizpatch.fixrtm.util.DigestUtils
import jp.kaiz.kaizpatch.fixrtm.writeUTFNullable
import jp.ngt.ngtlib.io.FileType
import jp.ngt.ngtlib.renderer.model.*
import net.minecraft.util.ResourceLocation
import java.io.*

object CachedPolygonModel {
    private val cache = ModelPackBasedCache(
        fixCacheDir.resolve("polygon-model"),
        0x0000 to Serializer
    )

    val type = FileType("fixrtm-cached-polygon-model-file", "fixrtm cached polygon model file.")

    fun getCachedModel(pack: FIXModelPack, resource: ResourceLocation, accuracy: VecAccuracy): PolygonModel? {
        val sha1 = DigestUtils.sha1Hex("cached-model:$accuracy:$resource")
        return cache.get(pack, sha1, Serializer)
    }

    fun putCachedModel(pack: FIXModelPack, resource: ResourceLocation, accuracy: VecAccuracy, model: PolygonModel) {
        val sha1 = DigestUtils.sha1Hex("cached-model:$accuracy:$resource")
        cache.put(pack, sha1, model)
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