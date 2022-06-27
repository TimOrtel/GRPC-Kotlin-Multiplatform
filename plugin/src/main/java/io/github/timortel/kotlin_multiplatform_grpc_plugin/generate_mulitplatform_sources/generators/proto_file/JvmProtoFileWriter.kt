package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.JvmMapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.MapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.JvmOneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.OneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.JvmRepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.RepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.JvmScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ScalarMessageMethodGenerator

class JvmProtoFileWriter(private val protoFile: ProtoFile) : ProtoFileWriter(protoFile, true), DefaultChildClassName {

    override val scalarMessageMethodGenerator: ScalarMessageMethodGenerator = JvmScalarMessageMethodGenerator

    override val repeatedMessageMethodGenerator: RepeatedMessageMethodGenerator = JvmRepeatedMessageMethodGenerator

    override val oneOfMethodAndClassGenerator: OneOfMethodAndClassGenerator = JvmOneOfMethodAndClassGenerator

    override val mapMessageMethodGenerator: MapMessageMethodGenerator = JvmMapMessageMethodGenerator

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage) {
        builder.addProperty(
            PropertySpec
                .builder(Const.Message.Constructor.JVM.PARAM_IMPL, message.jvmType)
                .initializer(Const.Message.Constructor.JVM.PARAM_IMPL)
                .build()
        )

        builder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(Const.Message.Constructor.JVM.PARAM_IMPL, message.jvmType)
                .build()
        )
    }

    override fun applyToEqualsFunction(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        thisClassName: ClassName
    ) {
        with(builder) {
            //Simply delegate equals to the implementation provided by the JVM
            //First check if the param is of the same type
            addStatement("if (%N == null) return false", Const.Message.BasicFunctions.EqualsFunction.OTHER_PARAM)

            beginControlFlow(
                "return if (%N is %T)",
                Const.Message.BasicFunctions.EqualsFunction.OTHER_PARAM,
                thisClassName
            )

            //Delegate
            addStatement(
                "%N.equals(%N.%N)",
                Const.Message.Constructor.JVM.PARAM_IMPL,
                Const.Message.BasicFunctions.EqualsFunction.OTHER_PARAM,
                Const.Message.Constructor.JVM.PARAM_IMPL
            )

            endControlFlow()
            addCode("else false")
        }
    }

    override fun applyToHashCodeFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        //Delegate hash code generation to jvm impl
        builder.addStatement("return %N.hashCode()", Const.Message.Constructor.JVM.PARAM_IMPL)
    }

    override fun getChildClassName(parentClass: ClassName?, childName: String): ClassName =
        getChildClassName(parentClass, childName, protoFile.pkg)
}