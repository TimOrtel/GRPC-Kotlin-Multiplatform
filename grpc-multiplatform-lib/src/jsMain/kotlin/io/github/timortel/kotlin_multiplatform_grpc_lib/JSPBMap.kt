package io.github.timortel.kotlin_multiplatform_grpc_lib

class JSPBMap<K, V>(private val impl: dynamic) : AbstractMap<K, V>() {

    override val size: Int
        get() = impl.getLength() as Int

    override fun containsKey(key: K): Boolean = impl.has(key) as Boolean

    override fun containsValue(value: V): Boolean = values.any { it == value }

    override fun get(key: K): V? {
        val result = impl.get(key)
        if (result == undefined) return null
        return result as V?
    }

    override fun isEmpty(): Boolean = size == 0

    override val entries: Set<Map.Entry<K, V>> by lazy {
        val internal = impl.entries()

        val set = mutableSetOf<ArrayEntry>()
        while (true) {
            val next = internal.next()
            if (next.done as Boolean) break

            set += ArrayEntry(next.value as Array<*>)
        }

        set
    }

    override val keys: Set<K>
        get() = (impl.keys() as Iterable<K>).toSet()

    override val values: Collection<V>
        get() = (impl.values() as Iterable<V>).toSet()

    @Suppress("UNCHECKED_CAST")
    private inner class ArrayEntry(entry: Array<*>) : Map.Entry<K, V> {
        override val key: K = entry[0] as K
        override val value: V = entry[1] as V
    }
}