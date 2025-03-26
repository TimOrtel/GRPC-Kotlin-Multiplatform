package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration

import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser
import io.github.timortel.kmpgrpc.anltr.Protobuf3Visitor
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoImport
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.ProtoOneOf
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.ProtoReservation
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.ProtoReservation.Companion.fold
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field.ProtoMapField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field.ProtoOneOfField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.enumeration.ProtoEnumField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.service.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.service.ProtoService
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode

typealias CompileException = Protobuf3CompilationException
typealias ParseException = Protobuf3ParserException

class Protobuf3ModelBuilderVisitor(
    private val filePath: String,
    private val fileName: String,
    private val fileNameWithoutExtension: String,
) : Protobuf3Visitor<Any> {
    override fun visitProto(ctx: Protobuf3Parser.ProtoContext): ProtoFile {
        val imports = ctx.importStatement().map { visitImportStatement(it) }
        val options = ctx.optionStatement().map { visitOptionStatement(it) }

        if (ctx.packageStatement().size > 1) throw CompileException("Found more than one package statements", filePath, ctx)

        val pkg = ctx.packageStatement().firstOrNull()?.fullIdent()?.text

        val messages = ctx.topLevelDef().mapNotNull { it.messageDef() }.map { visitMessageDef(it) }
        val topLevelEnums = ctx.topLevelDef().mapNotNull { it.enumDef() }.map { visitEnumDef(it) }
        val services = ctx.topLevelDef().mapNotNull { it.serviceDef() }.map { visitServiceDef(it) }

        return ProtoFile(
            `package` = pkg,
            fileName = fileName,
            fileNameWithoutExtension = fileNameWithoutExtension,
            messages = messages,
            enums = topLevelEnums,
            services = services,
            options = options,
            imports = imports
        )
    }

    override fun visitImportStatement(ctx: Protobuf3Parser.ImportStatementContext): ProtoImport {
        return ProtoImport(ctx.strLit().text, ctx)
    }

    override fun visitOptionStatement(ctx: Protobuf3Parser.OptionStatementContext): ProtoOption {
        return ProtoOption(ctx.optionName().text, ctx.constant().text, ctx)
    }

    // Message parsing

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

        return ProtoMessage(
            name = name,
            messages = messages,
            enums = enums,
            fields = fields,
            oneOfs = oneOfs,
            mapFields = mapFields,
            reservation = reservation,
            options = options,
            ctx = ctx
        )
    }

    override fun visitReserved(ctx: Protobuf3Parser.ReservedContext): ProtoReservation {
        return when {
            ctx.ranges() != null -> visitRanges(ctx.ranges())
            ctx.reservedFieldNames() != null -> visitReservedFieldNames(ctx.reservedFieldNames())
            else -> throw ParseException("Could not read reserved field", ctx)
        }
    }

    override fun visitRanges(ctx: Protobuf3Parser.RangesContext): ProtoReservation {
        return ctx.range_().map { visitRange_(it) }.fold()
    }

    override fun visitRange_(ctx: Protobuf3Parser.Range_Context): ProtoReservation {
        val start = ctx.intLit(0).parseInt()
        val end = when {
            ctx.intLit(1) != null -> ctx.intLit(1).parseInt()
            else -> Integer.MAX_VALUE
        }

        return ProtoReservation(ranges = listOf(start..end))
    }

    override fun visitReservedFieldNames(ctx: Protobuf3Parser.ReservedFieldNamesContext): ProtoReservation {
        return ProtoReservation(
            nums = ctx.strLit().map { it.parseInt() }
        )
    }

    override fun visitMessageBody(ctx: Protobuf3Parser.MessageBodyContext): Any = Unit

    override fun visitMessageElement(ctx: Protobuf3Parser.MessageElementContext?): Any = Unit

    // Enum Parsing

    override fun visitEnumDef(ctx: Protobuf3Parser.EnumDefContext): ProtoEnum {
        val name = ctx.enumName().text
        val elements = ctx.enumBody().enumElement()

        val options = elements.mapNotNull { it.optionStatement() }.map { visitOptionStatement(it) }
        val fields = elements.mapNotNull { it.enumField() }.map { visitEnumField(it) }

        return ProtoEnum(name = name, fields = fields, options = options, ctx = ctx)
    }

    // Field parsing

    override fun visitField(ctx: Protobuf3Parser.FieldContext): ProtoMessageField {
        val label = ctx.fieldLabel()

        val fieldCardinality = when {
            label?.OPTIONAL() != null -> ProtoFieldCardinality.Optional
            label?.REPEATED() != null -> ProtoFieldCardinality.Repeated
            else -> ProtoFieldCardinality.Implicit
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
            cardinality = fieldCardinality
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
            valuesType = valuesType
        )
    }

    override fun visitFieldOptions(ctx: Protobuf3Parser.FieldOptionsContext?): List<ProtoOption> {
        return ctx?.fieldOption().orEmpty().map { visitFieldOption(it) }
    }

    override fun visitFieldOption(ctx: Protobuf3Parser.FieldOptionContext): ProtoOption {
        return ProtoOption(ctx.optionName().text, ctx.constant().text, ctx)
    }

    override fun visitEnumField(ctx: Protobuf3Parser.EnumFieldContext): ProtoEnumField {
        val name = ctx.ident().text
        val number = ctx.intLit().parseInt()
        val options = visitEnumValueOptions(ctx.enumValueOptions())

        return ProtoEnumField(
            name = name,
            number = if (ctx.MINUS() != null) -number else number,
            options = options
        )
    }

    override fun visitEnumValueOptions(ctx: Protobuf3Parser.EnumValueOptionsContext?): List<ProtoOption> {
        return ctx?.enumValueOption().orEmpty().map { visitEnumValueOption(it) }
    }

    override fun visitEnumValueOption(ctx: Protobuf3Parser.EnumValueOptionContext): ProtoOption {
        return ProtoOption(name = ctx.optionName().text, value = ctx.constant().text, ctx)
    }

    // One-Of Parsing

    override fun visitOneof(ctx: Protobuf3Parser.OneofContext): ProtoOneOf {
        val name = ctx.oneofName().text

        val options = ctx.optionStatement().map { visitOptionStatement(it) }
        val fields = ctx.oneofField().map { visitOneofField(it) }

        return ProtoOneOf(name = name, fields = fields, options = options)
    }

    override fun visitOneofField(ctx: Protobuf3Parser.OneofFieldContext): ProtoOneOfField {
        val type = visitType_(ctx.type_())
        val name = ctx.fieldName().text
        val number = ctx.fieldNumber().parseInt()

        val options = visitFieldOptions(ctx.fieldOptions())

        return ProtoOneOfField(type = type, name = name, number = number, options = options)
    }

    // Service parsing

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

    override fun visitRpc(ctx: Protobuf3Parser.RpcContext): ProtoRpc {
        val name = ctx.rpcName().text
        val clientType = visitMessageType(ctx.messageType(0))
        val serverType = visitMessageType(ctx.messageType(1))
        val isClientStream = ctx.clientStream != null
        val isServerStream = ctx.serverStream != null
        val options = ctx.optionStatement().map { visitOptionStatement(it) }

        return ProtoRpc(name, clientType, serverType, isClientStream, isServerStream, options)
    }

    override fun visitServiceElement(ctx: Protobuf3Parser.ServiceElementContext?): Any = Unit

    // Type parsing

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

    override fun visitMessageType(ctx: Protobuf3Parser.MessageTypeContext): ProtoType.DefType {
        return ProtoType.DefType(ctx.text, ctx)
    }

    override fun visit(tree: ParseTree): Any = Unit

    override fun visitChildren(node: RuleNode?): Any= Unit

    override fun visitTerminal(node: TerminalNode?): Any= Unit

    override fun visitErrorNode(node: ErrorNode?): Any= Unit

    override fun visitSyntax(ctx: Protobuf3Parser.SyntaxContext?): Any= Unit

    override fun visitPackageStatement(ctx: Protobuf3Parser.PackageStatementContext?): Any= Unit

    override fun visitOptionName(ctx: Protobuf3Parser.OptionNameContext?): Any= Unit

    override fun visitFieldLabel(ctx: Protobuf3Parser.FieldLabelContext?): Any= Unit

    override fun visitFieldNumber(ctx: Protobuf3Parser.FieldNumberContext?): Any= Unit

    override fun visitTopLevelDef(ctx: Protobuf3Parser.TopLevelDefContext?): Any= Unit

    override fun visitEnumBody(ctx: Protobuf3Parser.EnumBodyContext?): Any= Unit

    override fun visitEnumElement(ctx: Protobuf3Parser.EnumElementContext?): Any= Unit

    override fun visitExtendDef(ctx: Protobuf3Parser.ExtendDefContext?): Any= Unit

    override fun visitConstant(ctx: Protobuf3Parser.ConstantContext?): Any= Unit

    override fun visitBlockLit(ctx: Protobuf3Parser.BlockLitContext?): Any= Unit

    override fun visitEmptyStatement_(ctx: Protobuf3Parser.EmptyStatement_Context?): Any= Unit

    override fun visitIdent(ctx: Protobuf3Parser.IdentContext?): Any= Unit

    override fun visitFullIdent(ctx: Protobuf3Parser.FullIdentContext?): Any= Unit

    override fun visitMessageName(ctx: Protobuf3Parser.MessageNameContext?): Any= Unit

    override fun visitEnumName(ctx: Protobuf3Parser.EnumNameContext?): Any= Unit

    override fun visitFieldName(ctx: Protobuf3Parser.FieldNameContext?): Any= Unit

    override fun visitOneofName(ctx: Protobuf3Parser.OneofNameContext?): Any= Unit

    override fun visitMapName(ctx: Protobuf3Parser.MapNameContext?): Any= Unit

    override fun visitServiceName(ctx: Protobuf3Parser.ServiceNameContext?): Any= Unit

    override fun visitRpcName(ctx: Protobuf3Parser.RpcNameContext?): Any= Unit

    override fun visitEnumType(ctx: Protobuf3Parser.EnumTypeContext?): Any= Unit

    override fun visitIntLit(ctx: Protobuf3Parser.IntLitContext?): Any= Unit

    override fun visitStrLit(ctx: Protobuf3Parser.StrLitContext?): Any= Unit

    override fun visitBoolLit(ctx: Protobuf3Parser.BoolLitContext?): Any= Unit

    override fun visitFloatLit(ctx: Protobuf3Parser.FloatLitContext?): Any= Unit

    override fun visitKeywords(ctx: Protobuf3Parser.KeywordsContext?): Any= Unit

    private fun ParserRuleContext.parseInt(): Int {
        return text.toIntOrNull() ?: throw ParseException("Could not parse integer", this)
    }
}