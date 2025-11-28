package io.github.timortel.kmpgrpc.plugin.sourcegeneration.parsing

import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser
import io.github.timortel.kmpgrpc.anltr.Protobuf3Visitor
import io.github.timortel.kmpgrpc.anltr.ProtobufEditionsParser
import io.github.timortel.kmpgrpc.anltr.ProtobufEditionsVisitor
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoExtensionDefinition
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoLanguageVersion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoImport
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.enumeration.ProtoEnumField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoExtensionRanges
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoExtensionRanges.Companion.fold
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoRange
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoReservation
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoReservation.Companion.fold
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMapField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoOneOfField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoRpc
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoService
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode

typealias ParseException = ProtobufParserException

class ProtobufModelBuilderVisitor(
    private val filePath: String,
    private val fileName: String,
    private val fileNameWithoutExtension: String,
) : Protobuf3Visitor<Any>, ProtobufEditionsVisitor<Any> {

    private fun visitProto(
        ctx: ParserRuleContext,
        languageVersion: ProtoLanguageVersion,
        imports: List<ProtoImport>,
        options: List<ProtoOption>,
        messages: List<ProtoMessage>,
        topLevelEnums: List<ProtoEnum>,
        services: List<ProtoService>,
        packages: List<String>,
        extensionDefinitions: List<ProtoExtensionDefinition>
    ): ProtoFile {
        if (packages.size > 1) throw CompilationException.DuplicatePackageStatement(
            "Found more than one package statements",
            filePath,
            ctx
        )

        return ProtoFile(
            `package` = packages.firstOrNull(),
            fileName = fileName,
            fileNameWithoutExtension = fileNameWithoutExtension,
            languageVersion = languageVersion,
            messages = messages,
            enums = topLevelEnums,
            services = services,
            options = options,
            imports = imports,
            extensionDefinitions = extensionDefinitions
        )
    }

    override fun visitProto(ctx: ProtobufEditionsParser.ProtoContext): ProtoFile {
        val imports = ctx.importStatement().map { visitImportStatement(it) }
        val options = ctx.option().map { visitOption(it) }

        val messages = ctx.topLevelDef().mapNotNull { it.messageDef() }.map { visitMessageDef(it) }
        val topLevelEnums = ctx.topLevelDef().mapNotNull { it.enumDef() }.map { visitEnumDef(it) }
        val services = ctx.topLevelDef().mapNotNull { it.serviceDef() }.map { visitServiceDef(it) }
        val extensionDefinitions = ctx.topLevelDef().mapNotNull { it.extendDef() }.map { visitExtendDef(it) }

        val packages = ctx.packageStatement().mapNotNull { it.fullIdent()?.text }

        return visitProto(
            ctx = ctx,
            languageVersion = ProtoLanguageVersion.EDITION2023,
            imports = imports,
            options = options,
            messages = messages,
            topLevelEnums = topLevelEnums,
            services = services,
            packages = packages,
            extensionDefinitions = extensionDefinitions
        )
    }

    override fun visitProto(ctx: Protobuf3Parser.ProtoContext): ProtoFile {
        val imports = ctx.importStatement().map { visitImportStatement(it) }
        val options = ctx.optionStatement().map { visitOptionStatement(it) }

        val messages = ctx.topLevelDef().mapNotNull { it.messageDef() }.map { visitMessageDef(it) }
        val topLevelEnums = ctx.topLevelDef().mapNotNull { it.enumDef() }.map { visitEnumDef(it) }
        val services = ctx.topLevelDef().mapNotNull { it.serviceDef() }.map { visitServiceDef(it) }
        val extensionDefinitions = ctx.topLevelDef().mapNotNull { it.extendDef() }.map { visitExtendDef(it) }

        val packages = ctx.packageStatement().mapNotNull { it.fullIdent()?.text }

        return visitProto(
            ctx = ctx,
            languageVersion = ProtoLanguageVersion.PROTO3,
            imports = imports,
            options = options,
            messages = messages,
            topLevelEnums = topLevelEnums,
            services = services,
            packages = packages,
            extensionDefinitions = extensionDefinitions
        )
    }

    private fun visitImportStatement(ctx: ParserRuleContext, identifier: String): ProtoImport {
        return ProtoImport(identifier, ctx)
    }

    override fun visitImportStatement(ctx: ProtobufEditionsParser.ImportStatementContext): ProtoImport {
        return visitImportStatement(ctx, ctx.strLit().text)
    }

    override fun visitImportStatement(ctx: Protobuf3Parser.ImportStatementContext): ProtoImport {
        return visitImportStatement(ctx, ctx.strLit().text)
    }

    private fun visitOption(ctx: ParserRuleContext, name: String, constant: String): ProtoOption {
        val value = if (constant.startsWith("\"") && constant.endsWith("\"")) {
            constant.substring(1, constant.length - 1)
        } else constant

        return ProtoOption(name, value, ctx)
    }

    override fun visitOption(ctx: ProtobufEditionsParser.OptionContext): ProtoOption {
        return visitOption(ctx, ctx.optionName().text, ctx.constant().text)
    }

    override fun visitOptionStatement(ctx: Protobuf3Parser.OptionStatementContext): ProtoOption {
        return visitOption(ctx, ctx.optionName().text, ctx.constant().text)
    }

    // Message parsing

    override fun visitMessageDef(ctx: ProtobufEditionsParser.MessageDefContext): ProtoMessage {
        val name = ctx.messageName().text

        val elements = ctx.messageBody().messageElement()

        val messages = elements.mapNotNull { it.messageDef() }.map { visitMessageDef(it) }
        val enums = elements.mapNotNull { it.enumDef() }.map { visitEnumDef(it) }

        val fields = elements.mapNotNull { it.field() }.map { visitField(it) }
        val mapFields = elements.mapNotNull { it.mapField() }.map { visitMapField(it) }

        val oneOfs = elements.mapNotNull { it.oneof() }.map { visitOneof(it) }

        val reservation = elements.mapNotNull { it.reserved() }.map { visitReserved(it) }.fold()
        val options = elements.mapNotNull { it.option() }.map { visitOption(it) }

        val extensionDefinitions = elements.mapNotNull { it.extendDef() }.map { visitExtendDef(it) }
        val extensionRange = elements.mapNotNull { it.extensions() }.map { visitExtensions(it) }.fold()

        return ProtoMessage(
            name = name,
            messages = messages,
            enums = enums,
            fields = fields,
            oneOfs = oneOfs,
            mapFields = mapFields,
            reservation = reservation,
            options = options,
            extensionDefinitions = extensionDefinitions,
            extensionRange = extensionRange,
            ctx = ctx
        )
    }

    override fun visitMessageDef(ctx: Protobuf3Parser.MessageDefContext): ProtoMessage {
        val name = ctx.messageName().text

        val elements = ctx.messageBody().messageElement()

        val messages = elements.mapNotNull { it.messageDef() }.map { visitMessageDef(it) }
        val enums = elements.mapNotNull { it.enumDef() }.map { visitEnumDef(it) }

        val fields = elements.mapNotNull { it.field() }.map { visitField(it) }
        val mapFields = elements.mapNotNull { it.mapField() }.map { visitMapField(it) }

        val oneOfs = elements.mapNotNull { it.oneof() }.map { visitOneof(it) }

        val reservation = elements.mapNotNull { it.reserved() }.map { visitReserved(it) }.fold()
        val options = elements.mapNotNull { it.optionStatement() }.map { visitOptionStatement(it) }

        val extensionDefinitions = elements.mapNotNull { it.extendDef() }.map { visitExtendDef(it) }

        return ProtoMessage(
            name = name,
            messages = messages,
            enums = enums,
            fields = fields,
            oneOfs = oneOfs,
            mapFields = mapFields,
            reservation = reservation,
            options = options,
            extensionDefinitions = extensionDefinitions,
            extensionRange = ProtoExtensionRanges(),
            ctx = ctx
        )
    }

    override fun visitReserved(ctx: ProtobufEditionsParser.ReservedContext): ProtoReservation {
        return when {
            ctx.ranges() != null -> ProtoReservation(ranges = visitRanges(ctx.ranges()))
            ctx.reservedFieldNames() != null -> visitReservedFieldNames(ctx.reservedFieldNames())
            else -> throw ParseException("Could not read reserved field", ctx)
        }
    }

    override fun visitReserved(ctx: Protobuf3Parser.ReservedContext): ProtoReservation {
        return when {
            ctx.ranges() != null -> ProtoReservation(ranges = visitRanges(ctx.ranges()))
            ctx.reservedFieldNames() != null -> visitReservedFieldNames(ctx.reservedFieldNames())
            else -> throw ParseException("Could not read reserved field", ctx)
        }
    }

    override fun visitRanges(ctx: ProtobufEditionsParser.RangesContext): List<ProtoRange> {
        return ctx.range_().map { visitRange_(it) }
    }

    override fun visitRanges(ctx: Protobuf3Parser.RangesContext): List<ProtoRange> {
        return ctx.range_().map { visitRange_(it) }
    }

    private fun visitRange(start: Int, end: Int?, isMax: Boolean, ctx: ParserRuleContext): ProtoRange {
        val end = when {
            isMax -> Const.FIELD_NUMBER_MAX_VALUE
            end != null -> end
            else -> start
        }

        return ProtoRange(start..end, ctx)
    }

    override fun visitRange_(ctx: ProtobufEditionsParser.Range_Context): ProtoRange {
        return visitRange(ctx.intLit(0).parseInt(), ctx.intLit(1)?.parseInt(), ctx.MAX() != null, ctx)
    }

    override fun visitRange_(ctx: Protobuf3Parser.Range_Context): ProtoRange {
        return visitRange(ctx.intLit(0).parseInt(), ctx.intLit(1)?.parseInt(), ctx.MAX() != null, ctx)
    }

    private fun visitReservedFieldNames(names: List<String>): ProtoReservation {
        return ProtoReservation(
            // Remove ""
            names = names.map { it.substring(1, it.length - 1) }
        )
    }

    override fun visitReservedFieldNames(ctx: ProtobufEditionsParser.ReservedFieldNamesContext): ProtoReservation {
        return visitReservedFieldNames(ctx.strLit().map { it.text })
    }

    override fun visitReservedFieldNames(ctx: Protobuf3Parser.ReservedFieldNamesContext): ProtoReservation {
        return visitReservedFieldNames(ctx.strLit().map { it.text })
    }

    override fun visitMessageBody(ctx: ProtobufEditionsParser.MessageBodyContext): Any = Unit
    override fun visitMessageBody(ctx: Protobuf3Parser.MessageBodyContext): Any = Unit

    override fun visitMessageElement(ctx: ProtobufEditionsParser.MessageElementContext): Any = Unit
    override fun visitMessageElement(ctx: Protobuf3Parser.MessageElementContext?): Any = Unit

    // Enum Parsing

    override fun visitEnumDef(ctx: ProtobufEditionsParser.EnumDefContext): ProtoEnum {
        val name = ctx.enumName().text
        val elements = ctx.enumBody().enumElement()

        val options = elements.mapNotNull { it.option() }.map { visitOption(it) }
        val fields = elements.mapNotNull { it.enumField() }.map { visitEnumField(it) }
        val reservation = elements.mapNotNull { it.reserved() }.map { visitReserved(it) }.fold()

        return ProtoEnum(
            name = name,
            fields = fields,
            options = options,
            reservation = reservation,
            ctx = ctx
        )
    }

    override fun visitEnumDef(ctx: Protobuf3Parser.EnumDefContext): ProtoEnum {
        val name = ctx.enumName().text
        val elements = ctx.enumBody().enumElement()

        val options = elements.mapNotNull { it.optionStatement() }.map { visitOptionStatement(it) }
        val fields = elements.mapNotNull { it.enumField() }.map { visitEnumField(it) }
        val reservation = elements.mapNotNull { it.reserved() }.map { visitReserved(it) }.fold()

        return ProtoEnum(
            name = name,
            fields = fields,
            options = options,
            reservation = reservation,
            ctx = ctx
        )
    }

    // Field parsing

    override fun visitField(ctx: ProtobufEditionsParser.FieldContext): ProtoMessageField {
        val label = ctx.fieldLabel()

        val fieldCardinality = when {
            label?.REPEATED() != null -> ProtoMessageField.FieldCardinality.REPEATED
            else -> ProtoMessageField.FieldCardinality.SINGULAR
        }

        val type = visitType_(ctx.type_())
        val name = ctx.fieldName().text
        val number = ctx.fieldNumber().parseInt()

        val options = visitFieldOptions(ctx.fieldOptions())

        return ProtoMessageField(
            type = type,
            name = name,
            number = number,
            options = options,
            fieldCardinality = fieldCardinality,
            ctx = ctx
        )
    }

    override fun visitField(ctx: Protobuf3Parser.FieldContext): ProtoMessageField {
        val label = ctx.fieldLabel()

        val fieldCardinality = when {
            label?.OPTIONAL() != null -> ProtoMessageField.FieldCardinality.SINGULAR_OPTIONAL
            label?.REPEATED() != null -> ProtoMessageField.FieldCardinality.REPEATED
            else -> ProtoMessageField.FieldCardinality.SINGULAR
        }

        val type = visitType_(ctx.type_())
        val name = ctx.fieldName().text
        val number = ctx.fieldNumber().parseInt()

        val options = visitFieldOptions(ctx.fieldOptions())

        return ProtoMessageField(
            type = type,
            name = name,
            number = number,
            options = options,
            fieldCardinality = fieldCardinality,
            ctx = ctx
        )
    }

    override fun visitMapField(ctx: ProtobufEditionsParser.MapFieldContext): ProtoMapField {
        val keyType = visitKeyType(ctx.keyType())
        val valuesType = visitType_(ctx.type_())

        val name = ctx.mapName().text
        val number = ctx.fieldNumber().parseInt()

        val options = visitFieldOptions(ctx.fieldOptions())

        return ProtoMapField(
            name = name,
            number = number,
            options = options,
            keyType = keyType,
            valuesType = valuesType,
            ctx = ctx
        )
    }

    override fun visitMapField(ctx: Protobuf3Parser.MapFieldContext): ProtoMapField {
        val keyType = visitKeyType(ctx.keyType())
        val valuesType = visitType_(ctx.type_())

        val name = ctx.mapName().text
        val number = ctx.fieldNumber().parseInt()

        val options = visitFieldOptions(ctx.fieldOptions())

        return ProtoMapField(
            name = name,
            number = number,
            options = options,
            keyType = keyType,
            valuesType = valuesType,
            ctx = ctx
        )
    }

    // Nullability is required - otherwise NPE
    override fun visitFieldOptions(ctx: ProtobufEditionsParser.FieldOptionsContext?): List<ProtoOption> {
        return ctx?.fieldOption().orEmpty().map { visitFieldOption(it) }
    }

    override fun visitFieldOptions(ctx: Protobuf3Parser.FieldOptionsContext?): List<ProtoOption> {
        return ctx?.fieldOption().orEmpty().map { visitFieldOption(it) }
    }

    private fun visitFieldOption(ctx: ParserRuleContext, name: String, value: String): ProtoOption {
        return ProtoOption(name, value, ctx)
    }

    override fun visitFieldOption(ctx: ProtobufEditionsParser.FieldOptionContext): ProtoOption {
        return visitFieldOption(ctx, ctx.optionName().text, ctx.constant().text)
    }

    override fun visitFieldOption(ctx: Protobuf3Parser.FieldOptionContext): ProtoOption {
        return visitFieldOption(ctx, ctx.optionName().text, ctx.constant().text)
    }

    private fun visitEnumField(
        ctx: ParserRuleContext,
        name: String,
        number: Int,
        options: List<ProtoOption>,
        hasFieldMinus: Boolean
    ): ProtoEnumField {
        return ProtoEnumField(
            name = name,
            number = if (hasFieldMinus) -number else number,
            options = options,
            ctx = ctx
        )
    }

    override fun visitEnumField(ctx: ProtobufEditionsParser.EnumFieldContext): ProtoEnumField {
        val name = ctx.ident().text
        val number = ctx.intLit().parseInt()
        val options = visitEnumValueOptions(ctx.enumValueOptions())

        return visitEnumField(ctx, name, number, options, ctx.MINUS() != null)
    }

    override fun visitEnumField(ctx: Protobuf3Parser.EnumFieldContext): ProtoEnumField {
        val name = ctx.ident().text
        val number = ctx.intLit().parseInt()
        val options = visitEnumValueOptions(ctx.enumValueOptions())

        return visitEnumField(ctx, name, number, options, ctx.MINUS() != null)
    }

    // Nullability is required - otherwise NPE
    override fun visitEnumValueOptions(ctx: ProtobufEditionsParser.EnumValueOptionsContext?): List<ProtoOption> {
        return ctx?.enumValueOption().orEmpty().map { visitEnumValueOption(it) }
    }

    override fun visitEnumValueOptions(ctx: Protobuf3Parser.EnumValueOptionsContext?): List<ProtoOption> {
        return ctx?.enumValueOption().orEmpty().map { visitEnumValueOption(it) }
    }

    override fun visitEnumValueOption(ctx: ProtobufEditionsParser.EnumValueOptionContext): ProtoOption {
        return ProtoOption(name = ctx.optionName().text, value = ctx.constant().text, ctx)
    }

    override fun visitEnumValueOption(ctx: Protobuf3Parser.EnumValueOptionContext): ProtoOption {
        return ProtoOption(name = ctx.optionName().text, value = ctx.constant().text, ctx)
    }

    // One-Of Parsing

    override fun visitOneof(ctx: ProtobufEditionsParser.OneofContext): ProtoOneOf {
        val name = ctx.oneofName().text

        val options = ctx.option().map { visitOption(it) }
        val fields = ctx.oneofField().map { visitOneofField(it) }

        return ProtoOneOf(name = name, fields = fields, options = options)
    }

    override fun visitOneof(ctx: Protobuf3Parser.OneofContext): ProtoOneOf {
        val name = ctx.oneofName().text

        val options = ctx.optionStatement().map { visitOptionStatement(it) }
        val fields = ctx.oneofField().map { visitOneofField(it) }

        return ProtoOneOf(name = name, fields = fields, options = options)
    }

    override fun visitOneofField(ctx: ProtobufEditionsParser.OneofFieldContext): ProtoOneOfField {
        val type = visitType_(ctx.type_())
        val name = ctx.fieldName().text
        val number = ctx.fieldNumber().parseInt()

        val options = visitFieldOptions(ctx.fieldOptions())

        return ProtoOneOfField(
            type = type,
            name = name,
            number = number,
            options = options,
            ctx = ctx
        )
    }

    override fun visitOneofField(ctx: Protobuf3Parser.OneofFieldContext): ProtoOneOfField {
        val type = visitType_(ctx.type_())
        val name = ctx.fieldName().text
        val number = ctx.fieldNumber().parseInt()

        val options = visitFieldOptions(ctx.fieldOptions())

        return ProtoOneOfField(
            type = type,
            name = name,
            number = number,
            options = options,
            ctx = ctx
        )
    }

    // Service parsing

    override fun visitServiceDef(ctx: ProtobufEditionsParser.ServiceDefContext): ProtoService {
        val name = ctx.serviceName().text
        val options = ctx.serviceElement().mapNotNull { it.option() }.map { visitOption(it) }
        val rpcs = ctx.serviceElement().mapNotNull { it.rpc() }.map { visitRpc(it) }

        return ProtoService(
            name = name,
            options = options,
            rpcs = rpcs,
            ctx = ctx
        )
    }

    override fun visitServiceDef(ctx: Protobuf3Parser.ServiceDefContext): ProtoService {
        val name = ctx.serviceName().text
        val options = ctx.serviceElement().mapNotNull { it.optionStatement() }.map { visitOptionStatement(it) }
        val rpcs = ctx.serviceElement().mapNotNull { it.rpc() }.map { visitRpc(it) }

        return ProtoService(
            name = name,
            options = options,
            rpcs = rpcs,
            ctx = ctx
        )
    }

    override fun visitRpc(ctx: ProtobufEditionsParser.RpcContext): ProtoRpc {
        val name = ctx.rpcName().text
        val clientType = visitMessageType(ctx.messageType(0))
        val serverType = visitMessageType(ctx.messageType(1))
        val isClientStream = ctx.clientStream != null
        val isServerStream = ctx.serverStream != null
        val options = ctx.option().map { visitOption(it) }

        return ProtoRpc(
            name = name,
            sendType = clientType,
            returnType = serverType,
            isSendingStream = isClientStream,
            isReceivingStream = isServerStream,
            options = options
        )
    }

    override fun visitRpc(ctx: Protobuf3Parser.RpcContext): ProtoRpc {
        val name = ctx.rpcName().text
        val clientType = visitMessageType(ctx.messageType(0))
        val serverType = visitMessageType(ctx.messageType(1))
        val isClientStream = ctx.clientStream != null
        val isServerStream = ctx.serverStream != null
        val options = ctx.optionStatement().map { visitOptionStatement(it) }

        return ProtoRpc(
            name = name,
            sendType = clientType,
            returnType = serverType,
            isSendingStream = isClientStream,
            isReceivingStream = isServerStream,
            options = options
        )
    }

    override fun visitServiceElement(ctx: ProtobufEditionsParser.ServiceElementContext?): Any = Unit
    override fun visitServiceElement(ctx: Protobuf3Parser.ServiceElementContext?): Any = Unit

    // Extensions

    override fun visitExtendDef(ctx: ProtobufEditionsParser.ExtendDefContext): ProtoExtensionDefinition {
        val messageDef = ctx.messageType().text
        val fields = ctx.field().mapNotNull { visitField(it) }

        return ProtoExtensionDefinition(ProtoType.DefType(messageDef, ctx.messageType()), fields, ctx)
    }

    override fun visitExtendDef(ctx: Protobuf3Parser.ExtendDefContext): ProtoExtensionDefinition {
        val messageDef = ctx.messageType().text
        val fields = ctx.field().mapNotNull { visitField(it) }

        return ProtoExtensionDefinition(ProtoType.DefType(messageDef, ctx.messageType()), fields, ctx)
    }

    override fun visitExtensions(ctx: ProtobufEditionsParser.ExtensionsContext): ProtoExtensionRanges {
        return ProtoExtensionRanges(ranges = visitRanges(ctx.ranges()))
    }

    // Type parsing

    override fun visitType_(ctx: ProtobufEditionsParser.Type_Context): ProtoType {
        return when {
            ctx.messageType() != null || ctx.enumType() != null -> ProtoType.DefType(ctx.text, ctx)
            ctx.DOUBLE() != null -> ProtoType.DoubleType
            ctx.FLOAT() != null -> ProtoType.FloatType
            ctx.INT32() != null -> ProtoType.Int32Type
            ctx.INT64() != null -> ProtoType.Int64Type
            ctx.UINT32() != null -> ProtoType.UInt32Type
            ctx.UINT64() != null -> ProtoType.UInt64Type
            ctx.SINT32() != null -> ProtoType.SInt32Type
            ctx.SINT64() != null -> ProtoType.SInt64Type
            ctx.FIXED32() != null -> ProtoType.Fixed32Type
            ctx.FIXED64() != null -> ProtoType.Fixed64Type
            ctx.SFIXED32() != null -> ProtoType.SFixed32Type
            ctx.SFIXED64() != null -> ProtoType.SFixed64Type
            ctx.BOOL() != null -> ProtoType.BoolType
            ctx.STRING() != null -> ProtoType.StringType
            ctx.BYTES() != null -> ProtoType.BytesType
            else -> throw ParseException("Unknown type found.", ctx)
        }
    }

    override fun visitType_(ctx: Protobuf3Parser.Type_Context): ProtoType {
        return when {
            ctx.messageType() != null || ctx.enumType() != null -> ProtoType.DefType(ctx.text, ctx)
            ctx.DOUBLE() != null -> ProtoType.DoubleType
            ctx.FLOAT() != null -> ProtoType.FloatType
            ctx.INT32() != null -> ProtoType.Int32Type
            ctx.INT64() != null -> ProtoType.Int64Type
            ctx.UINT32() != null -> ProtoType.UInt32Type
            ctx.UINT64() != null -> ProtoType.UInt64Type
            ctx.SINT32() != null -> ProtoType.SInt32Type
            ctx.SINT64() != null -> ProtoType.SInt64Type
            ctx.FIXED32() != null -> ProtoType.Fixed32Type
            ctx.FIXED64() != null -> ProtoType.Fixed64Type
            ctx.SFIXED32() != null -> ProtoType.SFixed32Type
            ctx.SFIXED64() != null -> ProtoType.SFixed64Type
            ctx.BOOL() != null -> ProtoType.BoolType
            ctx.STRING() != null -> ProtoType.StringType
            ctx.BYTES() != null -> ProtoType.BytesType
            else -> throw ParseException("Unknown type found.", ctx)
        }
    }

    override fun visitKeyType(ctx: ProtobufEditionsParser.KeyTypeContext): ProtoType.MapKeyType {
        return when {
            ctx.INT32() != null -> ProtoType.Int32Type
            ctx.INT64() != null -> ProtoType.Int64Type
            ctx.UINT32() != null -> ProtoType.UInt32Type
            ctx.UINT64() != null -> ProtoType.UInt64Type
            ctx.SINT32() != null -> ProtoType.SInt32Type
            ctx.SINT64() != null -> ProtoType.SInt64Type
            ctx.FIXED32() != null -> ProtoType.Fixed32Type
            ctx.FIXED64() != null -> ProtoType.Fixed64Type
            ctx.SFIXED32() != null -> ProtoType.SFixed32Type
            ctx.SFIXED64() != null -> ProtoType.SFixed64Type
            ctx.BOOL() != null -> ProtoType.BoolType
            ctx.STRING() != null -> ProtoType.StringType
            else -> throw ParseException("Unknown type found.", ctx)
        }
    }

    override fun visitKeyType(ctx: Protobuf3Parser.KeyTypeContext): ProtoType.MapKeyType {
        return when {
            ctx.INT32() != null -> ProtoType.Int32Type
            ctx.INT64() != null -> ProtoType.Int64Type
            ctx.UINT32() != null -> ProtoType.UInt32Type
            ctx.UINT64() != null -> ProtoType.UInt64Type
            ctx.SINT32() != null -> ProtoType.SInt32Type
            ctx.SINT64() != null -> ProtoType.SInt64Type
            ctx.FIXED32() != null -> ProtoType.Fixed32Type
            ctx.FIXED64() != null -> ProtoType.Fixed64Type
            ctx.SFIXED32() != null -> ProtoType.SFixed32Type
            ctx.SFIXED64() != null -> ProtoType.SFixed64Type
            ctx.BOOL() != null -> ProtoType.BoolType
            ctx.STRING() != null -> ProtoType.StringType
            else -> throw ParseException("Unknown type found.", ctx)
        }
    }

    override fun visitMessageType(ctx: ProtobufEditionsParser.MessageTypeContext): ProtoType.DefType {
        return ProtoType.DefType(ctx.text, ctx)
    }

    override fun visitMessageType(ctx: Protobuf3Parser.MessageTypeContext): ProtoType.DefType {
        return ProtoType.DefType(ctx.text, ctx)
    }

    override fun visit(tree: ParseTree): Any = Unit

    override fun visitChildren(node: RuleNode?): Any = Unit

    override fun visitTerminal(node: TerminalNode?): Any = Unit

    override fun visitErrorNode(node: ErrorNode?): Any = Unit

    override fun visitEdition(ctx: ProtobufEditionsParser.EditionContext?): Any = Unit
    override fun visitSyntax(ctx: Protobuf3Parser.SyntaxContext?): Any = Unit

    override fun visitPackageStatement(ctx: ProtobufEditionsParser.PackageStatementContext?): Any = Unit
    override fun visitPackageStatement(ctx: Protobuf3Parser.PackageStatementContext?): Any = Unit

    override fun visitOptionName(ctx: ProtobufEditionsParser.OptionNameContext?): Any = Unit
    override fun visitOptionName(ctx: Protobuf3Parser.OptionNameContext?): Any = Unit

    override fun visitFieldLabel(ctx: ProtobufEditionsParser.FieldLabelContext?): Any = Unit
    override fun visitFieldLabel(ctx: Protobuf3Parser.FieldLabelContext?): Any = Unit

    override fun visitFieldNumber(ctx: ProtobufEditionsParser.FieldNumberContext?): Any = Unit
    override fun visitFieldNumber(ctx: Protobuf3Parser.FieldNumberContext?): Any = Unit

    override fun visitTopLevelDef(ctx: ProtobufEditionsParser.TopLevelDefContext?): Any = Unit
    override fun visitTopLevelDef(ctx: Protobuf3Parser.TopLevelDefContext?): Any = Unit

    override fun visitEnumBody(ctx: ProtobufEditionsParser.EnumBodyContext?): Any = Unit
    override fun visitEnumBody(ctx: Protobuf3Parser.EnumBodyContext?): Any = Unit

    override fun visitEnumElement(ctx: ProtobufEditionsParser.EnumElementContext?): Any = Unit
    override fun visitEnumElement(ctx: Protobuf3Parser.EnumElementContext?): Any = Unit

    override fun visitConstant(ctx: ProtobufEditionsParser.ConstantContext?): Any = Unit
    override fun visitConstant(ctx: Protobuf3Parser.ConstantContext?): Any = Unit

    override fun visitBlockLit(ctx: ProtobufEditionsParser.BlockLitContext?): Any = Unit
    override fun visitBlockLit(ctx: Protobuf3Parser.BlockLitContext?): Any = Unit

    override fun visitEmptyStatement_(ctx: ProtobufEditionsParser.EmptyStatement_Context?): Any = Unit
    override fun visitEmptyStatement_(ctx: Protobuf3Parser.EmptyStatement_Context?): Any = Unit

    override fun visitIdent(ctx: ProtobufEditionsParser.IdentContext?): Any = Unit
    override fun visitIdent(ctx: Protobuf3Parser.IdentContext?): Any = Unit

    override fun visitFullIdent(ctx: ProtobufEditionsParser.FullIdentContext?): Any = Unit
    override fun visitFullIdent(ctx: Protobuf3Parser.FullIdentContext?): Any = Unit

    override fun visitMessageName(ctx: ProtobufEditionsParser.MessageNameContext?): Any = Unit
    override fun visitMessageName(ctx: Protobuf3Parser.MessageNameContext?): Any = Unit

    override fun visitEnumName(ctx: ProtobufEditionsParser.EnumNameContext?): Any = Unit
    override fun visitEnumName(ctx: Protobuf3Parser.EnumNameContext?): Any = Unit

    override fun visitFieldName(ctx: ProtobufEditionsParser.FieldNameContext?): Any = Unit
    override fun visitFieldName(ctx: Protobuf3Parser.FieldNameContext?): Any = Unit

    override fun visitOneofName(ctx: ProtobufEditionsParser.OneofNameContext?): Any = Unit
    override fun visitOneofName(ctx: Protobuf3Parser.OneofNameContext?): Any = Unit

    override fun visitMapName(ctx: ProtobufEditionsParser.MapNameContext?): Any = Unit
    override fun visitMapName(ctx: Protobuf3Parser.MapNameContext?): Any = Unit

    override fun visitServiceName(ctx: ProtobufEditionsParser.ServiceNameContext?): Any = Unit
    override fun visitServiceName(ctx: Protobuf3Parser.ServiceNameContext?): Any = Unit

    override fun visitRpcName(ctx: ProtobufEditionsParser.RpcNameContext?): Any = Unit
    override fun visitRpcName(ctx: Protobuf3Parser.RpcNameContext?): Any = Unit

    override fun visitEnumType(ctx: ProtobufEditionsParser.EnumTypeContext?): Any = Unit
    override fun visitEnumType(ctx: Protobuf3Parser.EnumTypeContext?): Any = Unit

    override fun visitIntLit(ctx: ProtobufEditionsParser.IntLitContext?): Any = Unit
    override fun visitIntLit(ctx: Protobuf3Parser.IntLitContext?): Any = Unit

    override fun visitStrLit(ctx: ProtobufEditionsParser.StrLitContext?): Any = Unit
    override fun visitStrLit(ctx: Protobuf3Parser.StrLitContext?): Any = Unit

    override fun visitBoolLit(ctx: ProtobufEditionsParser.BoolLitContext?): Any = Unit
    override fun visitBoolLit(ctx: Protobuf3Parser.BoolLitContext?): Any = Unit

    override fun visitFloatLit(ctx: ProtobufEditionsParser.FloatLitContext?): Any = Unit
    override fun visitFloatLit(ctx: Protobuf3Parser.FloatLitContext?): Any = Unit

    override fun visitKeywords(ctx: ProtobufEditionsParser.KeywordsContext?): Any = Unit
    override fun visitKeywords(ctx: Protobuf3Parser.KeywordsContext?): Any = Unit

    private fun ParserRuleContext.parseInt(): Int {
        return text.toIntOrNull() ?: throw ParseException("Could not parse integer", this)
    }
}
