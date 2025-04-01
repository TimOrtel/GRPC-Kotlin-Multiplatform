package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.PACKAGE_IO
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.computeMessageSize
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.MessageWriterExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType

abstract class BaseSerializationExtension : MessageWriterExtension {

    protected fun getComputeMapValueRequiredSizeCode(type: ProtoType): CodeBlock {
        return when (type) {
            is ProtoType.DefType -> {
                when (type.declType) {
                    ProtoType.DefType.DeclarationType.MESSAGE -> {
                        CodeBlock.of(
                            "::%M",
                            getComputeDataTypeSizeMember(type, true)
                        )
                    }

                    ProtoType.DefType.DeclarationType.ENUM -> {
                        CodeBlock.of(
                            "{·fieldNumber,·it·-> %M(fieldNumber, it.%N)·}",
                            getComputeDataTypeSizeMember(type, true),
                            Const.Enum.NUMBER_PROPERTY_NAME
                        )
                    }
                }
            }

            is ProtoType.NonDeclType -> {
                CodeBlock.of(
                    "::%M",
                    getComputeDataTypeSizeMember(type, true)
                )
            }
        }
    }

    companion object {
        /**
         * @return the function that compute the size of the data type.
         */
        fun getComputeDataTypeSizeMember(
            protoType: ProtoType,
            withTag: Boolean
        ): MemberName {
            return when (protoType) {
                is ProtoType.DefType -> {
                    when (protoType.declType) {
                        ProtoType.DefType.DeclarationType.MESSAGE -> computeMessageSize
                        ProtoType.DefType.DeclarationType.ENUM -> {
                            MemberName(PACKAGE_IO, "computeEnumSize${if (!withTag) "NoTag" else ""}")
                        }
                    }
                }

                is ProtoType.NonDeclType -> {
                    val name = when (protoType) {
                        ProtoType.BytesType -> "Bytes"
                        ProtoType.DoubleType -> "Double"
                        ProtoType.FloatType -> "Float"
                        ProtoType.BoolType -> "Bool"
                        ProtoType.Fixed32Type -> "Fixed32"
                        ProtoType.Fixed64Type -> "Fixed64"
                        ProtoType.Int32Type -> "Int32"
                        ProtoType.Int64Type -> "Int64"
                        ProtoType.SFixed32Type -> "SFixed32"
                        ProtoType.SFixed64Type -> "SFixed64"
                        ProtoType.SInt32Type -> "SInt32"
                        ProtoType.SInt64Type -> "SInt64"
                        ProtoType.StringType -> "String"
                        ProtoType.UInt32Type -> "UInt32"
                        ProtoType.UInt64Type -> "UInt64"
                    }
                    MemberName(PACKAGE_IO, "compute${name}Size${if (!withTag) "NoTag" else ""}")
                }
            }
        }
    }
}
