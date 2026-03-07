package io.github.timortel.kmpgrpc.plugin

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class KmpGrpcExtension @Inject constructor(objects: ObjectFactory) {

    companion object {
        internal const val COMMON = "common"
        internal const val JVM = "jvm"
        internal const val JS = "js"
        internal const val WASMJS = "wasmjs"
        internal const val NATIVE = "native"

        internal val targets = listOf(COMMON, JVM, JS, WASMJS, NATIVE)
    }

    /**
     * Maps the output target to the source sets that require it.
     */
    @Suppress("UNCHECKED_CAST")
    internal val targetSourcesMap: MapProperty<String, List<String>> =
        (objects
            .mapProperty(String::class.java, List::class.java) as MapProperty<String, List<String>>)
            .convention(mutableMapOf<String, List<String>>())

    val protoSourceFolders: ConfigurableFileCollection = objects.fileCollection()

    /**
     * Instructs the plugin to download and include the well known proto-types: https://protobuf.dev/reference/protobuf/google.protobuf/
     *
     * Can only be used in combination with `namingStrategy` `KOTLIN_IDIOMATIC`.
     */
    val includeWellKnownTypes: Property<Boolean> = objects
        .property(Boolean::class.java)
        .convention(false)

    /**
     * If the generated source code should be generated with "internal" visibility by default. By default, "public" is used.
     */
    val internalVisibility: Property<Boolean> = objects
        .property(Boolean::class.java)
        .convention(false)

    /**
     * Configures how Protobuf messages and fields are mapped to Kotlin identifiers.
     *
     * This property allows you to choose between standard Protobuf naming, idiomatic
     * Kotlin style, or the legacy behavior. It affects the generated class names,
     * property names, and method names.
     *
     * Supported strategies:
     * - [NamingStrategy.KOTLIN_IDIOMATIC]: (Default) Transforms names to Kotlin conventions
     * (e.g., `user_id` -> `userId`, `User_Message` -> `UserMessage`).
     * - [NamingStrategy.PROTO_LITERAL]: Preserves the exact names defined in the .proto file.
     * - [NamingStrategy.LEGACY]: Preserves .proto names but appends "List" or "Map"
     * suffixes to collection types (Behavior from v1.x).
     */
    val namingStrategy: Property<NamingStrategy> = objects
        .property(NamingStrategy::class.java)
        .convention(NamingStrategy.KOTLIN_IDIOMATIC)

    fun common(targets: List<String> = listOf("commonMain")) {
        targetSourcesMap.put(COMMON, targets)
    }

    fun jvm(targets: List<String> = listOf("jvmMain")) {
        targetSourcesMap.put(JVM, targetSourcesMap.get()[JVM].orEmpty() + targets)
    }

    fun android(targets: List<String> = listOf("androidMain")) {
        targetSourcesMap.put(JVM, targetSourcesMap.get()[JVM].orEmpty() + targets)
    }

    fun js(targets: List<String> = listOf("jsMain")) {
        targetSourcesMap.put(JS, targets)
    }

    fun wasmjs(targets: List<String> = listOf("wasmJsMain")) {
        targetSourcesMap.put(WASMJS, targets)
    }

    fun native(targets: List<String> = listOf("nativeMain")) {
        targetSourcesMap.put(NATIVE, targets)
    }
}
