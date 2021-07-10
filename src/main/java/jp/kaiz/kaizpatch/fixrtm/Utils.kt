//Copyright © 2021 anatawa12.

package jp.kaiz.kaizpatch.fixrtm

import com.google.common.collect.Iterators
import cpw.mods.fml.common.Loader
import jp.kaiz.kaizpatch.fixrtm.util.ArrayPool
import jp.kaiz.kaizpatch.fixrtm.util.closeScope
import jp.kaiz.kaizpatch.fixrtm.util.sortedWalk
import java.io.*
import java.nio.charset.Charset
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger


fun getThreadGroup() = System.getSecurityManager()?.threadGroup ?: Thread.currentThread().threadGroup!!

fun threadFactoryWithPrefix(prefix: String, group: ThreadGroup = getThreadGroup()) = object : ThreadFactory {
    private val threadNumber = AtomicInteger(1)

    override fun newThread(r: Runnable?): Thread {
        val t = Thread(
            group, r,
            "$prefix-" + threadNumber.getAndIncrement(),
            0
        )
        if (t.isDaemon) t.isDaemon = false
        if (t.priority != Thread.NORM_PRIORITY) t.priority = Thread.NORM_PRIORITY
        return t
    }
}

fun <E> List<E?>.isAllNotNull(): Boolean = all { it != null }
fun <E> Array<E?>.isAllNotNull(): Boolean = all { it != null }

val minecraftDir = Loader.instance().configDir.parentFile!!
val fixCacheDir = minecraftDir.resolve("fixrtm-cache")
val MS932 = Charset.forName("MS932")
val fixRTMCommonExecutor = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors(),
    threadFactoryWithPrefix("fixrtm-common-executor")
)

fun File.directoryDigestBaseStream() = SequenceInputStream(Iterators.asEnumeration(
    this.sortedWalk()
        .flatMap {
            sequenceOf(
                it.toRelativeString(this).byteInputStream(),
                it.inputStream().buffered()
            )
        }
        .iterator()
))

fun DataOutput.writeUTFNullable(string: String?) = closeScope {
    if (string == null) return writeShort(0xFFFF)

    var utflen = 0

    for (c in string) {
        @Suppress("NAME_SHADOWING")
        val c = c.toInt()
        if (c in 0x0001..0x007F) {
            utflen++
        } else if (c <= 0x07FF) {
            utflen += 2
        } else {
            utflen += 3
        }
    }

    val bytes = ArrayPool.bytePool.request(utflen).closer().array

    if (utflen >= 0xFFFF)
        throw UTFDataFormatException("encoded string too long: $utflen bytes")

    var count = 0
    for (c in string) {
        @Suppress("NAME_SHADOWING")
        val c = c.toInt()
        if (c >= 0x0001 && c <= 0x007F) {
            bytes[count++] = c.toByte()
        } else if (c > 0x07FF) {
            bytes[count++] = (0xE0 or (c shr 12 and 0x0F)).toByte()
            bytes[count++] = (0x80 or (c shr 6 and 0x3F)).toByte()
            bytes[count++] = (0x80 or (c shr 0 and 0x3F)).toByte()
        } else {
            bytes[count++] = (0xC0 or (c shr 6 and 0x1F)).toByte()
            bytes[count++] = (0x80 or (c shr 0 and 0x3F)).toByte()
        }
    }

    write(bytes)
}

fun DataInput.readUTFNullable(): String? = closeScope {
    val length = readUnsignedShort()
    if (length == 0xFFFF) return null
    val bytes = ArrayPool.bytePool.request(length).closer().array
    val chars = ArrayPool.charPool.request(length).closer().array

    readFully(bytes)
    var byteI = 0
    var charI = 0

    while (byteI < length) {
        val c = bytes.get(byteI).toInt() and 0xff
        when (c shr 4) {
            0, 1, 2, 3, 4, 5, 6, 7 -> {
                /* 0xxxxxxx*/byteI++
                chars[charI++] = c.toChar()
            }
            12, 13 -> {
                /* 110x xxxx   10xx xxxx*/
                byteI += 2
                if (byteI > length) throw UTFDataFormatException("malformed input: partial character at end")
                val char2 = bytes[byteI - 1].toInt()
                if (char2 and 0xC0 != 0x80) throw UTFDataFormatException("malformed input around byte $byteI")
                chars[charI++] = (c and 0x1F shl 6)
                    .or(char2 and 0x3F)
                    .toChar()
            }
            14 -> {
                /* 1110 xxxx  10xx xxxx  10xx xxxx */
                byteI += 3
                if (byteI > length) throw UTFDataFormatException("malformed input: partial character at end")
                val char2 = bytes[byteI - 2].toInt()
                val char3 = bytes[byteI - 1].toInt()
                if (char2 and 0xC0 != 0x80 || char3 and 0xC0 != 0x80) throw UTFDataFormatException("malformed input around byte " + (byteI - 1))
                chars[charI++] = (c and 0x0F shl 12)
                    .or(char2 and 0x3F shl 6)
                    .or(char3 and 0x3F shl 0)
                    .toChar()
            }
            else -> throw UTFDataFormatException(
                "malformed input around byte $byteI"
            )
        }
    }

    return String(chars, 0, charI)
}

fun File.mkParent(): File = apply { parentFile.mkdirs() }

//val EntityPlayerMP.modList get() = NetworkDispatcher.get(this.connection.netManager)?.modList ?: emptyMap()
//val EntityPlayerMP.hasFixRTM get() = server.isSinglePlayer || modList.containsKey(FixRtm.MODID)
//
//@Suppress("unused")
//fun Entity.rayTraceBothSide(blockReachDistance: Double, partialTicks: Float): RayTraceResult? {
//    val eyePosition = getPositionEyes(partialTicks)
//    val lookDir = getLook(partialTicks)
//    val endPosition = eyePosition
//        .add(lookDir.x * blockReachDistance, lookDir.y * blockReachDistance, lookDir.z * blockReachDistance)
//    return world.rayTraceBlocks(
//        eyePosition, endPosition,
//        false, false, true
//    )
//}
//
//@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused")
//fun <T> T.addEntityCrashInfoAboutModelSet(
//    category: CrashReportCategory,
//    configGetter: T.() -> ResourceConfig?,
//) = try {
//    val cfg = configGetter()
//    if (cfg == null) {
//        category.addCrashSection("Parent Train ModelSet Name", "no modelpack detected")
//    } else {
//        category.addCrashSection("Parent Train ModelSet Name", cfg.name)
//        category.addCrashSection("Parent Train ModelSet Source JSON Path", cfg.file ?: "no source")
//    }
//} catch (t: Throwable) {
//    category.addCrashSectionThrowable("Error Getting ModelSet", t)
//}
//
//fun arrayOfItemStack(size: Int) = Array(size) { ItemStack.EMPTY }

private const val START = 0
private const val KEY = 1
private const val STR_BS = 2
private const val STR_BS_U0 = 3 // \u|0000
private const val STR_BS_U1 = 4 // \u0|000
private const val STR_BS_U2 = 5 // \u00|00
private const val STR_BS_U3 = 6 // \u000|0
private const val STR = 7

fun joinLinesForJsonReading(lines: List<String>): String = buildString {
    var stat = START
    var shouldAddNewLine = false
    for (line in lines) {
        for (c in line) {
            val preStat = stat
            when (stat) {
                START -> {
                    if (c in '0'..'9' || c in 'a'..'z' || c in 'A'..'Z' || c == '+' || c == '-' || c == '.')
                        stat = KEY
                    else if (c == '"')
                        stat = STR
                }
                KEY -> {
                    if (c in '0'..'9' || c in 'a'..'z' || c in 'A'..'Z' || c == '+' || c == '-' || c == '.')
                        stat = KEY
                    else
                        stat = START
                }
                STR -> {
                    when (c) {
                        '\\' -> stat = STR_BS
                        '"' -> stat = START
                    }
                }
                STR_BS -> {
                    when (c) {
                        'u' -> stat = STR_BS_U0
                        else -> stat = STR
                    }
                }
                STR_BS_U0, STR_BS_U1, STR_BS_U2, STR_BS_U3 -> {
                    stat++
                }
            }
            if (shouldAddNewLine && (preStat == START || stat == START)) append('\n')
            shouldAddNewLine = false
            append(c)
        }
        shouldAddNewLine = true
    }
}
//
//fun ItemStack.isItemOf(machine: TileEntityMachineBase): Boolean {
//    if (this.item !== RTMItem.installedObject) return false
//    val type = ItemInstalledObject.IstlObjType.getType(this.itemDamage)
//    return type.type == machine.subType
//}
//
//fun EntityPlayer.openGui(fixGuiId: GuiId, world: World, x: Int, y: Int, z: Int) {
//    openGui(FixRtm, fixGuiId.ordinal, world, x, y, z)
//}
