package jp.ngt.rtm.world

/**
 * ForgeやMinecraftに依存しない、編成チャンク保持範囲の計算値。
 */
data class FormationChunkCoordinate(
    val x: Int,
    val z: Int,
) : Comparable<FormationChunkCoordinate> {
    override fun compareTo(other: FormationChunkCoordinate): Int {
        return compareValuesBy(this, other, FormationChunkCoordinate::x, FormationChunkCoordinate::z)
    }
}

data class FormationChunkDelta(
    val toForce: Set<FormationChunkCoordinate>,
    val toUnforce: Set<FormationChunkCoordinate>,
)

/**
 * チャンク保持範囲とTicketへの割り当てを決定する純粋な計算処理。
 */
object FormationChunkPlanner {
    @JvmStatic
    fun expand(
        occupied: Collection<FormationChunkCoordinate>,
        radius: Int = 1,
    ): Set<FormationChunkCoordinate> {
        require(radius >= 0) { "radius must not be negative" }

        val result = linkedSetOf<FormationChunkCoordinate>()
        occupied.sorted().forEach { center ->
            for (x in center.x - radius..center.x + radius) {
                for (z in center.z - radius..center.z + radius) {
                    result += FormationChunkCoordinate(x, z)
                }
            }
        }
        return result
    }

    /**
     * [capacity] が0以下の場合はForgeの「無制限」として1 Ticketへまとめる。
     */
    @JvmStatic
    fun partition(
        chunks: Collection<FormationChunkCoordinate>,
        capacity: Int,
    ): List<Set<FormationChunkCoordinate>> {
        if (chunks.isEmpty()) {
            return emptyList()
        }

        val sorted = chunks.toSortedSet().toList()
        if (capacity <= 0) {
            return listOf(LinkedHashSet(sorted))
        }

        return sorted.chunked(capacity).map(::LinkedHashSet)
    }

    @JvmStatic
    fun difference(
        current: Collection<FormationChunkCoordinate>,
        desired: Collection<FormationChunkCoordinate>,
    ): FormationChunkDelta {
        val currentSet = current.toSet()
        val desiredSet = desired.toSet()
        return FormationChunkDelta(
            toForce = (desiredSet - currentSet).toSortedSet(),
            toUnforce = (currentSet - desiredSet).toSortedSet(),
        )
    }
}
