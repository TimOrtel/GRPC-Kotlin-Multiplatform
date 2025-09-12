package io.github.timortel.kmpgrpc.core.message.extensions

import io.github.timortel.kmpgrpc.core.message.FieldType
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi
import kotlin.reflect.KClass


/**
 * Represents a protobuf extension.
 *
 * @param M The type of the message the extension is associated with.
 * @param T The type of the value held by the extension.
 * @property messageClass The class type of the protocol buffer message this extension is associated with.
 * @property fieldNumber The field number in the protocol buffer schema for the extension.
 */
sealed class Extension<M : Message, T : Any>(
    val fullName: String,
    val fieldNumber: Int,
    internal val messageClass: KClass<M>
) {

    internal abstract val fieldType: FieldType<T>

    /**
     * Class for [Extension]s that are defined non-repeated.
     */
    class ScalarValueExtension<M : Message, T : Any>
    @InternalKmpGrpcApi
    constructor(
        fullName: String,
        fieldNumber: Int,
        messageClass: KClass<M>,
        override val fieldType: FieldType<T>
    ) : Extension<M, T>(fullName = fullName, messageClass = messageClass, fieldNumber = fieldNumber)

    /**
     * Class for [Extension]s that are defined as repeated.
     */
    sealed class RepeatedValueExtension<M : Message, T : Any>
    @InternalKmpGrpcApi
    constructor(
        fullName: String,
        fieldNumber: Int,
        messageClass: KClass<M>
    ) : Extension<M, T>(fullName = fullName, messageClass = messageClass, fieldNumber = fieldNumber) {
        abstract override val fieldType: FieldType.RepeatableFieldType<T>
    }

    /**
     * Class for [Extension]s that are defined as repeated on types that are packable.
     */
    @InternalKmpGrpcApi
    class PackableRepeatedValueExtension<M : Message, T : Any>(
        fullName: String,
        fieldNumber: Int,
        messageClass: KClass<M>,
        override val fieldType: FieldType.PackableFieldType<T>,
        internal val isPacked: Boolean,
        internal val tag: UInt
    ) : RepeatedValueExtension<M, T>(fullName = fullName, fieldNumber = fieldNumber, messageClass = messageClass)

    /**
     * Class for [Extension]s that are defined as repeated on types that are not packable.
     */
    @InternalKmpGrpcApi
    class NonPackableRepeatedValueExtension<M : Message, T : Any>(
        fullName: String,
        fieldNumber: Int,
        messageClass: KClass<M>,
        override val fieldType: FieldType.NonPackableFieldType<T>
    ) : RepeatedValueExtension<M, T>(fullName = fullName, fieldNumber = fieldNumber, messageClass = messageClass)

    override fun toString(): String {
        return fullName
    }
}
