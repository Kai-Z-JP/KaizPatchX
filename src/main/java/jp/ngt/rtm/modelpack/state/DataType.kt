package jp.ngt.rtm.modelpack.state

import jp.ngt.ngtlib.math.Vec3
import kotlin.reflect.KClass

enum class DataType(
    @JvmField val key: String,
    val klass: KClass<*>?,
    val isValidListElement: Boolean = true
) {
    BOOLEAN("Boolean", Boolean::class),
    DOUBLE("Double", Double::class),
    INT("Int", Int::class),
    STRING("String", String::class),
    VEC("Vec", Vec3::class),
    HEX("Hex", null),
    LIST("List", List::class, isValidListElement = false);

    companion object {
        @JvmStatic
        fun getType(s: String?) = entries.firstOrNull { type: DataType? -> type!!.key == s }

        @JvmStatic
        inline fun <reified T> inferElementType(): DataType =
            entries.firstOrNull { type -> type.klass == T::class && type.isValidListElement }
                ?: throw IllegalArgumentException("Unsupported list element type: ${T::class}")
    }
}
