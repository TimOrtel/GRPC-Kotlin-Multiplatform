package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

/**
 * Parent interface of [ProtoMessage] and [io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf].
 *
 * Used to resolve the property name of a proto property in the generated code and to avoid name clashes.
 */
interface ProtoChildPropertyNameResolver {

    val reservedAttributeNames: Set<String>

    val childProperties: List<ProtoChildProperty>

    fun resolveMessagePropertyName(field: ProtoChildProperty): String {
        val reservedNames = reservedAttributeNames.toMutableSet()
        val nameMap: MutableMap<ProtoChildProperty, String> = mutableMapOf()

        childProperties
            .sortedBy { it.priority }
            .forEach { currentField ->
                var attributeName = currentField.desiredAttributeName

                while (attributeName in reservedNames) {
                    attributeName = "${attributeName}_"
                }

                if (currentField == field) return attributeName

                reservedNames += attributeName
                nameMap[currentField] = attributeName
            }

        throw IllegalArgumentException("field=$field not child of resolver=$this. Known children=$childProperties.")
    }
}
