package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmChannel
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmMetadata
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoRpc
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoService
import kotlinx.coroutines.flow.Flow

abstract class ProtoServiceWriter(private val isActual: Boolean) {

    protected abstract val primaryConstructorModifiers: List<KModifier>
    protected abstract val channelConstructorModifiers: List<KModifier>

    fun generateServiceStub(service: ProtoService): TypeSpec {
        val classAndFunctionModifiers = when {
            isActual -> listOf(KModifier.ACTUAL)
            !service.isNested -> listOf(KModifier.EXPECT)
            else -> emptyList()
        }

        return TypeSpec
            .classBuilder(service.className)
            .addModifiers(classAndFunctionModifiers)
            .addFunction(
                FunSpec
                    .constructorBuilder()
                    .addParameter(Const.Service.Constructor.CHANNEL_PARAMETER_NAME, kmChannel)
                    .addModifiers(channelConstructorModifiers)
                    .apply { applyToChannelConstructor(this, service) }
                    .build()
            )
            .apply {
                specifyInheritance(this, service)
                applyToClass(this, service)

                service.rpcs.forEach { rpc ->
                    val returnType = if (rpc.isReceivingStream) {
                        Flow::class.asTypeName().parameterizedBy(rpc.returnType.resolve())
                    } else {
                        rpc.returnType.resolve()
                    }

                    addFunction(
                        FunSpec
                            .builder(rpc.name)
                            .apply {
                                if (!rpc.isReceivingStream) {
                                    this.addModifiers(KModifier.SUSPEND)
                                }

                                if (rpc.isSendingStream) {
                                    addParameter(
                                        Const.Service.RpcCall.PARAM_REQUESTS,
                                        Flow::class.asTypeName().parameterizedBy(rpc.sendType.resolve())
                                    )
                                } else {
                                    addParameter(
                                        Const.Service.RpcCall.PARAM_REQUEST,
                                        rpc.sendType.resolve()
                                    )
                                }
                            }
                            .addModifiers(classAndFunctionModifiers)
                            .addParameter(
                                ParameterSpec
                                    .builder(Const.Service.RpcCall.PARAM_METADATA, kmMetadata)
                                    .apply { applyToMetadataParameter(this, service) }
                                    .build()
                            )
                            .returns(returnType)
                            .apply {
                                applyToRpcFunction(this, rpc)
                            }
                            .build()
                    )
                }
            }
            .build()
    }

    protected abstract fun applyToClass(
        builder: TypeSpec.Builder,
        service: ProtoService
    )

    /**
     * Apply to the constructor that receives the channel argument. That is not the primary constructor.
     */
    protected abstract fun applyToChannelConstructor(
        builder: FunSpec.Builder,
        service: ProtoService
    )

    protected abstract fun applyToRpcFunction(
        builder: FunSpec.Builder,
        rpc: ProtoRpc
    )

    protected abstract fun applyToMetadataParameter(builder: ParameterSpec.Builder, service: ProtoService)

    protected abstract fun specifyInheritance(
        builder: TypeSpec.Builder,
        service: ProtoService
    )
}
