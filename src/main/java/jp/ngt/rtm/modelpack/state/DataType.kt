package jp.ngt.rtm.modelpack.state

enum class DataType(@JvmField val key: String) {
    BOOLEAN("Boolean"),
    DOUBLE("Double"),
    INT("Int"),
    STRING("String"),
    VEC("Vec"),
    HEX("Hex"),
    LIST("List");

    companion object {
        @JvmStatic
        fun getType(s: String?) = entries.firstOrNull { type: DataType? -> type!!.key == s }
    }
}
