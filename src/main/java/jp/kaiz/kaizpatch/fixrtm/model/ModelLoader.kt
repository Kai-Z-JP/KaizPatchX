/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

@file:JvmName("ModelLoaderKt")

package jp.kaiz.kaizpatch.fixrtm.model

import jp.kaiz.kaizpatch.fixrtm.modelpack.FIXFileLoader
import jp.ngt.ngtlib.io.FileType
import jp.ngt.ngtlib.renderer.model.ModelLoader
import jp.ngt.ngtlib.renderer.model.PolygonModel
import jp.ngt.ngtlib.renderer.model.VecAccuracy
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ModelFormatException
import java.io.IOException
import java.io.InputStream


fun loadModel(resource: ResourceLocation, par1: VecAccuracy, vararg args: Any?): PolygonModel? {
    val fileName = resource.toString()
    try {
        val pack = FIXFileLoader.getPack(resource)
        CachedPolygonModel.getCachedModel(pack, resource, par1)?.let {
            return CachedPolygonModel.createCachedModel(pack, resource, par1, it)
        }

        val streams = inputStreams(resource)
        val model = ModelLoader.loadModel(streams, fileName, par1, *args) ?: return null
        CachedPolygonModel.putCachedModelSync(pack, resource, par1, model)
        return CachedPolygonModel.createCachedModel(pack, resource, par1, model)
    } catch (var10: IOException) {
        throw ModelFormatException("Failed to load model : $fileName", var10)
    }
}

private fun inputStreams(resource: ResourceLocation): Array<InputStream?> {
    val mainStream: InputStream = FIXFileLoader.getInputStream(resource)
    if (FileType.OBJ.match(resource.resourcePath)) {
        val mtlFileName = resource.resourcePath.replace(".obj", ".mtl")
        val mtlFile = ResourceLocation(resource.resourceDomain, mtlFileName)
        var is2: InputStream? = null
        try {
            is2 = FIXFileLoader.getInputStream(mtlFile)
        } catch (var9: IOException) {
        }
        return arrayOf(mainStream, is2)
    } else {
        return arrayOf(mainStream)
    }
}
