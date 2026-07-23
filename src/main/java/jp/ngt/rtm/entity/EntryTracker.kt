package jp.ngt.rtm.entity

class EntryTracker<T> {
    private var active: Set<T> = emptySet()

    fun update(currentValues: Collection<T>): Set<T> {
        val current = LinkedHashSet(currentValues)
        val entered = current.filterTo(LinkedHashSet()) { it !in active }
        active = current
        return entered
    }

    fun clear() {
        active = emptySet()
    }
}
