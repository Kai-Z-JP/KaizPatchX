/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package jp.kaiz.kaizpatch.fixrtm.modelpack

import net.minecraft.util.ResourceLocation
import java.io.File

interface FIXModelPack {
    val sha1Hash: String
    val file: File
    val domains: Set<String>
    fun getFile(location: ResourceLocation): FIXResource?
}
