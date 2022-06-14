package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.mapper

import com.squareup.kotlinpoet.CodeBlock
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.Types
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.MapType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute

/**
 * Can map a map from common to native and backwards
 */
abstract class MapMapper {

    fun mapMap(
        builderVariable: String,
        mapVariable: CodeBlock,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        mapType: MapType
    ): CodeBlock {
        val builder = CodeBlock.builder()

        //Here we have to handle the case where we can directly put the map in or where we have to modify the map
        if (!mapType.keyTypes.doDiffer && !mapType.valueTypes.doDiffer) {
            //Case where nothing differs, and we can directly put in the map
            builder.add(
                handleCreatedMap(
                    builderVariable,
                    mapVariable,
                    message,
                    attribute,
                    mapType
                )
            )
        } else {
            builder.beginControlFlow("run")
            //Something differs, we have to copy and map the map
            builder.add("val newMap = ")
            builder.add(mapVariable)
            builder.add(".entries.associate·{·(k, v) ->\n")

            if (mapType.keyTypes.doDiffer) {
                builder.add(mapVariable("k", message, attribute, mapType.keyTypes))
            } else {
                builder.add("k")
            }

            builder.add(" to ")

            if (mapType.valueTypes.doDiffer) {
                builder.add(mapVariable("v", message, attribute, mapType.valueTypes))
            } else {
                builder.add("v")
            }

            builder.add("\n")

            builder.add("}\n")

            builder.add(handleCreatedMap(builderVariable, CodeBlock.of("newMap"), message, attribute, mapType))
            builder.endControlFlow()
        }

        return builder.build()
    }

    /**
     * Generate the code that puts the map into the builder map
     *
     * @param mapToPutVariable the variable of type map that is put int
     */
    protected abstract fun handleCreatedMap(
        builderVariable: String,
        mapToPutVariable: CodeBlock,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        mapType: MapType
    ): CodeBlock

    /**
     * @return a code block that transforms the variable into the correct type
     */
    protected abstract fun mapVariable(
        variableName: String,
        message: ProtoMessage,
        attribute: ProtoMessageAttribute,
        types: Types
    ): CodeBlock

}