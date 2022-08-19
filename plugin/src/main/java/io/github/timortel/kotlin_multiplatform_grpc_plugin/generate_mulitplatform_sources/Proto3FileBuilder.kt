package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.anltr.Proto3BaseListener
import io.github.timortel.kotlin_multiplatform_grpc_plugin.anltr.Proto3Parser
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree.EnumNode
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree.MessageNode
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree.Node
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree.PackageNode
import java.util.*

/**
 * @property javaUseMultipleFiles If the java use multiple files option is set.
 */
class Proto3FileBuilder(
    private val fileNameWithoutExtensions: String,
    private val fileName: String,
    private val packageTree: PackageNode,
    private val javaUseMultipleFiles: Boolean
) : Proto3BaseListener() {

    private var packageName: String? = null

    //By default, we are in the root package node
    private var ownPackageNode: PackageNode = packageTree
    private var ownPackageNodeParents: List<PackageNode> = emptyList()

    private val topLevelMessages = mutableListOf<ProtoMessage>()

    private val messageReadingStack: Deque<MessageReader> = ArrayDeque()

    /**
     * not null if currently in a one of node
     */
    private var currentOneOfAttributes: MutableList<ProtoMessageAttribute>? = null

    private val services = mutableListOf<ProtoService>()
    private val currentServiceRpcs = mutableListOf<ProtoRpc>()

    private val topLevelEnums = mutableListOf<ProtoEnum>()
    private val currentEnumFields = mutableListOf<ProtoEnumField>()

    /**
     * The resulting representation of the proto file.
     */
    var protoFile: ProtoFile? = null

    override fun enterFile(ctx: Proto3Parser.FileContext?) {

    }

    override fun enterProto_package(ctx: Proto3Parser.Proto_packageContext) {
        val newPackageName = ctx.pkgName.text
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

    override fun enterMessage(ctx: Proto3Parser.MessageContext) {
        val messageName = ctx.messageName.text
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

        messageReadingStack.push(MessageReader(name = messageName, messageNode = newMessageNode))
    }

    override fun exitMessage(ctx: Proto3Parser.MessageContext) {
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

    override fun enterMessage_attribute(ctx: Proto3Parser.Message_attributeContext) {
        val currentReadingStack = messageReadingStack.peekFirst()

        val normalAttributeName = ctx.name.text
        val types = resolveType(currentReadingStack.messageNode, ctx.type.text)

        val isRepeated = ctx.repeated != null

        val type = if (isRepeated) Repeated(types.isEnum) else Scalar(
            ctx.parent.ruleIndex == Proto3Parser.RULE_one_of,
            types.isEnum
        )

        val attr = ProtoMessageAttribute(
            normalAttributeName,
            types.commonType,
            types,
            type,
            ctx.num.text.toInt(),
            isOneOfAttribute = currentOneOfAttributes != null
        )
        currentReadingStack.attributes += attr

        currentOneOfAttributes?.add(attr)
    }

    override fun enterMap(ctx: Proto3Parser.MapContext) {
        val currentReadingStack = messageReadingStack.peekFirst()

        val keyTypes = resolveType(currentReadingStack.messageNode, ctx.key_type.text)
        val valueTypes = resolveType(currentReadingStack.messageNode, ctx.value_type.text)

        val type = MapType(keyTypes, valueTypes)

        val attr = ProtoMessageAttribute(
            ctx.name.text,
            MAP,
            Types(
                MAP,
                MAP,
                MAP,
                MAP,
                doDiffer = false,
                isEnum = false,
                isNullable = false,
                protoType = ProtoType.MAP
            ),
            type,
            ctx.num.text.toInt(),
            isOneOfAttribute = false
        )

        currentReadingStack.attributes += attr
    }

    override fun exitFile(ctx: Proto3Parser.FileContext) {
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

    override fun enterService(ctx: Proto3Parser.ServiceContext) {
        currentServiceRpcs.clear()
    }

    override fun exitRpc(ctx: Proto3Parser.RpcContext) {
        val rpcName = ctx.rpcName.text

        val requestTypes = resolveType(null, ctx.request.text)
        val responseTypes = resolveType(null, ctx.response.text)

        val isResponseStream = ctx.stream != null

        currentServiceRpcs += ProtoRpc(
            rpcName,
            requestTypes,
            responseTypes,
            isResponseStream
        )
    }

    override fun exitService(ctx: Proto3Parser.ServiceContext) {
        val name = ctx.serviceName.text

        services += ProtoService(name, currentServiceRpcs.toList())
    }

    override fun enterOne_of(ctx: Proto3Parser.One_ofContext?) {
        super.enterOne_of(ctx)

        currentOneOfAttributes = mutableListOf()
    }

    override fun exitOne_of(ctx: Proto3Parser.One_ofContext) {
        super.exitOne_of(ctx)

        messageReadingStack.peekFirst().oneOfs += ProtoOneOf(
            ctx.one_of_name.text,
            currentOneOfAttributes!!,
            messageReadingStack.peekFirst().oneOfs.size
        )
        currentOneOfAttributes = null
    }

    override fun exitEnum_field(ctx: Proto3Parser.Enum_fieldContext) {
        val enumField = ProtoEnumField(ctx.name.text, ctx.num.text.toInt())
        currentEnumFields += enumField
    }

    override fun enterProto_enum(ctx: Proto3Parser.Proto_enumContext?) {
        currentEnumFields.clear()
    }

    override fun exitProto_enum(ctx: Proto3Parser.Proto_enumContext) {
        val protoEnum = ProtoEnum(ctx.enumName.text, currentEnumFields.toList())

        if (messageReadingStack.isNotEmpty()) {
            //enum is inside a message
            messageReadingStack.peekFirst().enums += protoEnum
        } else {
            topLevelEnums += protoEnum
        }
    }

    /**
     * Resolves the type with the given type text.
     * First checks if it's just a scalar.
     * Then looks in the current message node if one is supplied, then in the current file and then in the whole tree.
     */
    private fun resolveType(messageNode: MessageNode?, typeText: String): Types {
        val (scalar, protoType) = when (typeText) {
            "double" -> DOUBLE to ProtoType.DOUBLE
            "float" -> FLOAT to ProtoType.FLOAT
            "int32" -> INT to ProtoType.INT_32
            "int64" -> LONG to ProtoType.INT_64
            "bool" -> BOOLEAN to ProtoType.BOOL
            "string" -> STRING to ProtoType.STRING
            else -> null to null
        }

        //Scalar types have the same type on all platforms
        if (scalar != null && protoType != null) {
            return Types(
                scalar,
                scalar,
                scalar,
                scalar,
                doDiffer = false,
                isEnum = false,
                isNullable = false,
                protoType = protoType
            )
        }

        if (messageNode != null) {
            //Priority 1: Look for the message in the current message node
            val relativeInMessage = resolveMessageOrEnumInMessageTree(packageName ?: "", messageNode, typeText)
            if (relativeInMessage != null) return relativeInMessage
        }

        //Priority 2: Look in the current proto file
        val relativeInFile = resolveTypeInTree(ownPackageNode, ownPackageNodeParents, typeText)
        if (relativeInFile != null) return relativeInFile

        //Priority 3: Look in the whole package structure
        return resolveTypeInTree(packageTree, emptyList(), typeText)
            ?: throw RuntimeException("No message or enum $typeText found.")
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
        val attributes: MutableList<ProtoMessageAttribute> = mutableListOf(),
        val oneOfs: MutableList<ProtoOneOf> = mutableListOf(),
        val enums: MutableList<ProtoEnum> = mutableListOf(),
        val childMessages: MutableList<MessageReader> = mutableListOf(),
        val messageNode: MessageNode
    )
}