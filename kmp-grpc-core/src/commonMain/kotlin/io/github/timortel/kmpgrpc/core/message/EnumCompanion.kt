package io.github.timortel.kmpgrpc.core.message

/**
 * Base interface for all companion objects of [Enum]
 * @param T the enum
 */
interface EnumCompanion<T : Enum> {

    /**
     * Retrieves the enumeration value corresponding to the given numeric value.
     *
     * @param num The numeric value for which the corresponding enumeration value is to be retrieved.
     * @return The enumeration value of type T corresponding to the provided numeric value,
     * or `UNRECOGNIZED` if no corresponding value is found
     */
    fun getEnumForNumber(num: Int): T

    /**
     * Retrieves the enumeration value corresponding to the given numeric value.
     *
     * @param num The numeric value for which the corresponding enumeration value is to be retrieved.
     * @return The enumeration value of type T corresponding to the provided numeric value,
     * or null if no corresponding value is found
     */
    fun getEnumForNumberOrNull(num: Int): T?
}
