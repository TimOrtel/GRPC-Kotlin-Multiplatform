package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

import com.squareup.kotlinpoet.TypeName

/**
 * Base interface of a proto property of a generated class.
 */
interface ProtoChildProperty {

    val resolvingParent: ProtoChildPropertyNameResolver

    /**
     * The name of the property as defined in the proto source code
     */
    val name: String

    /**
     * The name of the field the property would like to have if there were not any clashes
     */
    val desiredAttributeName: String

    /**
     * The name of the field in the generated message class
     */
    val attributeName: String
        get() = resolvingParent.resolveMessagePropertyName(this)

    /**
     * The priority of this child property in the context of name clash resolution.
     */
    val priority: Int

    /**
     * The type of the property in the generated code
     */
    val propertyType: TypeName
}
