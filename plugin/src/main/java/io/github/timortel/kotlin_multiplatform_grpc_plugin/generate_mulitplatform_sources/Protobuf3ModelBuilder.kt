package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.anltr.Protobuf3BaseListener
import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser
import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser.EnumTypeContext
import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser.MessageTypeContext
import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser.Type_Context
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree.EnumNode
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree.MessageNode
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree.Node
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree.PackageNode
import java.util.*
import java.util.ArrayDeque

class Protobuf3ModelBuilder(
    private val fileNameWithoutExtensions: String,
    private val fileName: String,
    private val packageTree: PackageNode,
    private val javaUseMultipleFiles: Boolean
) : Protobuf3BaseListener() {

    private var packageName: String? = null

    //By default, we are in the root package node
    private var ownPackageNode: PackageNode = packageTree
    private var ownPackageNodeParents: List<PackageNode> = emptyList()

    private val topLevelMessages = mutableListOf<ProtoMessage>()

    private val messageReadingStack: Deque<MessageReader> = ArrayDeque()

    /**
     * not null if currently in a one of node
     */
    private var currentOneOfAttributes: MutableList<ProtoMessageField>? = null

    private val services = mutableListOf<ProtoService>()
    private val currentServiceRpcs = mutableListOf<ProtoRpc>()

    private val topLevelEnums = mutableListOf<ProtoEnum>()
    private val currentEnumFields = mutableListOf<ProtoEnumField>()

    /**
     * The resulting representation of the proto file.
     */
    var protoFile: ProtoFile? = null

    override fun enterPackageStatement(ctx: Protobuf3Parser.PackageStatementContext) {
        val newPackageName = ctx.fullIdent().text
        packageName = newPackageName

        val ownPackageNodeParents = mutableListOf<PackageNode>()
        ownPackageNode = packageTree
        var remainingPackageName = newPackageName
        while (remainingPackageName.isNotEmpty()) {
            val currentPackage = remainingPackageName.substringBefore('.')
            remainingPackageName = remainingPackageName.substringAfter('.', "")

            ownPackageNodeParents += ownPackageNode
            ownPackageNode = ownPackageNode.children.firstOrNull { it.packageName == currentPackage }
                ?: throw RuntimeException("Could not find package $newPackageName")
        }

        this.ownPackageNodeParents = ownPackageNodeParents
    }

    override fun exitProto(ctx: Protobuf3Parser.ProtoContext) {
        protoFile =
            ProtoFile(
                packageName ?: "",
                fileNameWithoutExtensions,
                fileName,
                topLevelMessages.toList(),
                services.toList(),
                topLevelEnums.toList()
            )
    }

    override fun enterMessageDef(ctx: Protobuf3Parser.MessageDefContext) {
        val messageName = ctx.messageName().text
        val newMessageNode = if (messageReadingStack.isNotEmpty()) {
            //Just look for the child in the current message
            messageReadingStack
                .peekFirst()
                .messageNode
                .children
                .first { it.name == messageName } as MessageNode
        } else {
            //we are a top level message
            ownPackageNode.nodes.first { it.name == messageName } as MessageNode
        }

        messageReadingStack.push(
            MessageReader(
                name = messageName,
                messageNode = newMessageNode
            )
        )
    }

    override fun exitMessageDef(ctx: Protobuf3Parser.MessageDefContext?) {
        val messageReader = messageReadingStack.pop()

        if (messageReadingStack.isNotEmpty()) {
            messageReadingStack.peekFirst().childMessages += messageReader
        } else {
            //this is a top level message, build the message tree now
            topLevelMessages += buildMessageTree(messageReader, null)
        }
    }

    private fun buildMessageTree(messageReader: MessageReader, parent: ProtoMessage?): ProtoMessage {
        val children = mutableListOf<ProtoMessage>()

        val protoMessage = ProtoMessage(
            packageName ?: "",
            messageReader.name,
            messageReader.attributes,
            messageReader.oneOfs,
            messageReader.enums,
            parent,
            children,
            fileNameWithoutExtensions,
            javaUseMultipleFiles
        )

        children += messageReader.childMessages.map { childMessageReader ->
            buildMessageTree(childMessageReader, protoMessage)
        }

        return protoMessage
    }

    override fun enterField(ctx: Protobuf3Parser.FieldContext) {
        val label = ctx.fieldLabel()?.text
        val name = ctx.fieldName().text
        val fieldNumber =
            ctx.fieldNumber().text.toIntOrNull() ?: throw IllegalStateException("Could not parse field number for $ctx")

        val fieldType = ctx.type_().toFieldType()

        val fieldCardinality = when (label) {
            "optional" -> FieldCardinality.OPTIONAL
            "repeated" -> FieldCardinality.REPEATED
            else -> FieldCardinality.IMPLICIT
        }

        enterField(
            fieldName = name,
            fieldNumber = fieldNumber,
            fieldType = fieldType,
            fieldCardinality = fieldCardinality
        )
    }

    override fun enterOneofField(ctx: Protobuf3Parser.OneofFieldContext) {
        val name = ctx.fieldName().text
        val type = ctx.type_().toFieldType()
        val fieldNumber =
            ctx.fieldNumber().text.toIntOrNull() ?: throw IllegalStateException("Could not parse field number for $ctx")

        enterField(
            fieldName = name,
            fieldNumber = fieldNumber,
            fieldType = type,
            fieldCardinality = FieldCardinality.ONE_OF
        )
    }

    private fun enterField(
        fieldName: String,
        fieldNumber: Int,
        fieldType: FieldType,
        fieldCardinality: FieldCardinality
    ) {
        val currentReadingStack = messageReadingStack.peekFirst()

        val types = resolveType(fieldType)

        val type = when (fieldCardinality) {
            FieldCardinality.IMPLICIT -> Scalar(false, Scalar.Type.IMPLICIT, types.isEnum)
            FieldCardinality.OPTIONAL -> Scalar(false, Scalar.Type.OPTIONAL, types.isEnum)
            FieldCardinality.REPEATED -> Repeated(types.isEnum)
            FieldCardinality.ONE_OF -> Scalar(true, Scalar.Type.IMPLICIT, types.isEnum)
        }

        val attr = ProtoMessageField(
            name = fieldName,
            commonType = types.commonType,
            types = types,
            fieldCardinality = type,
            protoId = fieldNumber,
            isOneOfAttribute = currentOneOfAttributes != null
        )
        currentReadingStack.attributes += attr

        currentOneOfAttributes?.add(attr)
    }

    // One-of handling
    override fun enterOneof(ctx: Protobuf3Parser.OneofContext) {
        super.enterOneof(ctx)

        currentOneOfAttributes = mutableListOf()
    }

    override fun exitOneof(ctx: Protobuf3Parser.OneofContext) {
        super.exitOneof(ctx)

        messageReadingStack.peekFirst().oneOfs += ProtoOneOf(
            ctx.oneofName().text,
            currentOneOfAttributes!!,
            messageReadingStack.peekFirst().oneOfs.size
        )

        currentOneOfAttributes = null
    }

    // Enum handling

    override fun enterEnumDef(ctx: Protobuf3Parser.EnumDefContext) {
        currentEnumFields.clear()
    }

    override fun exitEnumField(ctx: Protobuf3Parser.EnumFieldContext) {
        val enumField = ProtoEnumField(ctx.ident().text, ctx.intLit().text.toInt())
        currentEnumFields += enumField
    }

    override fun exitEnumDef(ctx: Protobuf3Parser.EnumDefContext) {
        val protoEnum = ProtoEnum(ctx.enumName().text, currentEnumFields.toList())

        if (messageReadingStack.isNotEmpty()) {
            //enum is inside a message
            messageReadingStack.peekFirst().enums += protoEnum
        } else {
            topLevelEnums += protoEnum
        }
    }

    // Map handling

    override fun enterMapField(ctx: Protobuf3Parser.MapFieldContext) {
        val currentReadingStack = messageReadingStack.peekFirst()

        val name = ctx.mapName().text
        val fieldNumber = ctx.fieldNumber().text.toInt()

        val keyType = FieldType.Scalar(ctx.keyType().text)
        val valuesType = ctx.type_().toFieldType()

        val keyTypes = resolveType(keyType)
        val valueTypes = resolveType(valuesType)

        val type = MapType(keyTypes, valueTypes)

        val attr = ProtoMessageField(
            name = name,
            commonType = MAP,
            types = Types(
                commonType = MAP,
                jvmType = MAP,
                jsType = MAP,
                iosType = MAP,
                doDiffer = false,
                isEnum = false,
                isNullable = false,
                protoType = ProtoType.MAP
            ),
            fieldCardinality = type,
            protoId = fieldNumber,
            isOneOfAttribute = false
        )

        currentReadingStack.attributes += attr
    }

    // Service handling

    override fun enterServiceDef(ctx: Protobuf3Parser.ServiceDefContext?) {
        currentServiceRpcs.clear()
    }

    override fun exitRpc(ctx: Protobuf3Parser.RpcContext) {
        val rpcName = ctx.rpcName().text

        val requestType = FieldType.Message(ctx.messageType(0).text)
        val responseType = FieldType.Message(ctx.messageType(1).text)

        val requestTypes = resolveType(requestType)
        val responseTypes = resolveType(responseType)

        if (ctx.clientStream != null) throw IllegalStateException("Client side stream is currently not supported")
        val isResponseStream = ctx.serverStream != null

        currentServiceRpcs += ProtoRpc(
            rpcName = rpcName,
            request = requestTypes,
            response = responseTypes,
            method = if (isResponseStream) ProtoRpc.Method.SERVER_STREAMING else ProtoRpc.Method.UNARY
        )
    }

    override fun exitServiceDef(ctx: Protobuf3Parser.ServiceDefContext) {
        val name = ctx.serviceName().text

        services += ProtoService(name, currentServiceRpcs.toList())
    }

    /**
     * Resolves the type with the given type text.
     * First checks if it's just a scalar.
     * Then looks in the current message node if one is supplied, then in the current file and then in the whole tree.
     */
    private fun resolveType(fieldType: FieldType): Types {
        return when (fieldType) {
            is FieldType.Scalar -> {
                val (type, protoType) = when (fieldType.text) {
                    "double" -> DOUBLE to ProtoType.DOUBLE
                    "float" -> FLOAT to ProtoType.FLOAT
                    "int32" -> INT to ProtoType.INT_32
                    "int64" -> LONG to ProtoType.INT_64
                    "bool" -> BOOLEAN to ProtoType.BOOL
                    "string" -> STRING to ProtoType.STRING
                    else -> throw IllegalStateException("Unknown proto type ${fieldType.text}")
                }

                return Types(
                    commonType = type,
                    jvmType = type,
                    jsType = type,
                    iosType = type,
                    doDiffer = false,
                    isEnum = false,
                    isNullable = false,
                    protoType = protoType
                )
            }

            is FieldType.Enum, is FieldType.Message -> {
                val messageNode = messageReadingStack.peekFirst()?.messageNode

                if (messageNode != null) {
                    //Priority 1: Look for the message in the current message node
                    val relativeInMessage =
                        resolveMessageOrEnumInMessageTree(packageName ?: "", messageNode, fieldType.text)
                    if (relativeInMessage != null) return relativeInMessage
                }

                //Priority 2: Look in the current proto file
                val relativeInFile = resolveTypeInTree(ownPackageNode, ownPackageNodeParents, fieldType.text)
                if (relativeInFile != null) return relativeInFile

                //Priority 3: Look in the whole package structure
                return resolveTypeInTree(packageTree, emptyList(), fieldType.text)
                    ?: throw RuntimeException("No message or enum ${fieldType.text} found.")
            }
        }
    }

    private fun resolveTypeInTree(node: PackageNode, parents: List<PackageNode>, typeText: String): Types? {
        val nodeText = typeText.substringBefore('.')
        val remainingTypeText = typeText.substringAfter('.', "")

        //Prioritize messages
        val matchingMessage = node.nodes.firstOrNull { it.name == nodeText }

        val pkg = (parents + node).joinToString(".") { it.packageName }

        return if (matchingMessage != null) {
            resolveMessageOrEnumInMessageTree(pkg, matchingMessage, remainingTypeText)
        } else {
            //Look in packages
            val packageNode = node.children.firstOrNull { it.packageName == nodeText }
                ?: return null

            resolveTypeInTree(packageNode, parents + node, remainingTypeText)
        }
    }

    /**
     * Looks for the matching message or enum
     */
    @Suppress("DefaultLocale")
    private fun resolveMessageOrEnumInMessageTree(
        pkg: String,
        node: Node,
        typeText: String
    ): Types? {
        if (typeText.isEmpty()) {
            //No more type text, so this node is the correct message

            return when (node) {
                is MessageNode -> {
                    val messageNodes = (node.path + node).map { it.name }

                    Types(
                        commonType = getCommonClassName(pkg, messageNodes),
                        jvmType = getJVMClassName(pkg, fileNameWithoutExtensions, javaUseMultipleFiles, messageNodes),
                        jsType = getJSClassName(pkg, messageNodes),
                        iosType = getCommonClassName(pkg, messageNodes),
                        doDiffer = true,
                        isEnum = false,
                        isNullable = true,
                        protoType = ProtoType.MESSAGE
                    )
                }

                is EnumNode -> {
                    val messageNodes = node.path.map { it.name }

                    val (commonType, jvmType) = if (messageNodes.isEmpty()) {
                        //This is a top level enum
                        val commonType = ClassName(pkg, Const.Enum.commonEnumName(node.name))
                        val jvmType = getJVMClassName(
                            pkg,
                            fileNameWithoutExtensions,
                            javaUseMultipleFiles,
                            listOf(node.name.capitalize())
                        )

                        commonType to jvmType
                    } else {
                        val commonType =
                            getCommonClassName(pkg, messageNodes).nestedClass(Const.Enum.commonEnumName(node.name))
                        val jvmType = getJVMClassName(
                            pkg,
                            fileNameWithoutExtensions,
                            javaUseMultipleFiles,
                            messageNodes
                        ).nestedClass(
                            node.name.capitalize()
                        )

                        commonType to jvmType
                    }

                    return Types(
                        commonType = commonType,
                        jvmType = jvmType,
                        jsType = Int::class.asTypeName(),
                        iosType = commonType,
                        doDiffer = true,
                        isEnum = true,
                        isNullable = false,
                        protoType = ProtoType.ENUM
                    )
                }
            }

        }

        val nodeText = typeText.substringBefore('.')
        val remainingTypeText = typeText.substringAfter('.', "")

        return when (node) {

            is EnumNode -> {
                //If this is an enum node but the remainingTypeText is not empty, this cannot be correct
                //because enum nodes have no children
                null
            }

            is MessageNode -> {
                val child = node.children.firstOrNull { it.name == nodeText }
                    ?: return null

                return resolveMessageOrEnumInMessageTree(pkg, child, remainingTypeText)
            }
        }
    }

    /**
     * @property messageNode the message node for this message reader in the package tree
     */
    private class MessageReader(
        val name: String,
        val attributes: MutableList<ProtoMessageField> = mutableListOf(),
        val oneOfs: MutableList<ProtoOneOf> = mutableListOf(),
        val enums: MutableList<ProtoEnum> = mutableListOf(),
        val childMessages: MutableList<MessageReader> = mutableListOf(),
        val messageNode: MessageNode
    )

    private enum class FieldCardinality {
        IMPLICIT,
        OPTIONAL,
        REPEATED,
        ONE_OF
    }

    private sealed class FieldType {
        abstract val text: String

        data class Scalar(override val text: String) : FieldType()
        data class Message(override val text: String) : FieldType()
        data class Enum(override val text: String) : FieldType()
    }

    private fun Type_Context.toFieldType(): FieldType = when (getChild(0)) {
        is MessageTypeContext -> FieldType.Message(text)
        is EnumTypeContext -> FieldType.Enum(text)
        else -> FieldType.Scalar(text)
    }
}
