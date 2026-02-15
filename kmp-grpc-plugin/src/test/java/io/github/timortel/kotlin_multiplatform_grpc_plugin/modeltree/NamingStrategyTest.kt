package io.github.timortel.kotlin_multiplatform_grpc_plugin.modeltree

import io.github.timortel.kmpgrpc.plugin.NamingStrategy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMapField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.validation.BaseValidationTest.ProtoVersion
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NamingStrategyTest : BaseModelTreeTest() {

    // --- MESSAGE & FIELD NAMING ---

    @Test
    fun testKotlinIdiomaticNamingStrategy() {
        val project = buildProject(
            content = """
            message user_profile_data {
                int32 user_id = 1;
                string display_name = 2;
            }
        """.trimIndent(),
            protoVersion = ProtoVersion.PROTO3,
            namingStrategy = NamingStrategy.KOTLIN_IDIOMATIC
        )

        val msg = project.findMessage("user_profile_data")
        Assertions.assertEquals("UserProfileData", msg.className.simpleName)

        val idField = msg.findField("user_id").assertIsInstance<ProtoMessageField>()
        Assertions.assertEquals("userId", idField.codeName)

        val nameField = msg.findField("display_name").assertIsInstance<ProtoMessageField>()
        Assertions.assertEquals("displayName", nameField.codeName)
    }

    @Test
    fun testProtoLiteralNamingStrategy() {
        val project = buildProject(
            content = """
            message message_a {
                int32 field_1 = 1;
            }
        """.trimIndent(),
            protoVersion = ProtoVersion.PROTO3,
            namingStrategy = NamingStrategy.PROTO_LITERAL
        )

        val msg = project.findMessage("message_a")
        Assertions.assertEquals("message_a", msg.className.simpleName)

        val field = msg.findField("field_1").assertIsInstance<ProtoMessageField>()
        Assertions.assertEquals("field_1", field.codeName)
    }

    // --- REPEATED FIELDS & MAPS ---

    @Test
    fun testCollectionNamingConventions() {
        val project = buildProject(
            content = """
            message collection_container {
                repeated string tag_names = 1;
                map<string, int32> attribute_map = 2;
            }
        """.trimIndent(),
            protoVersion = ProtoVersion.PROTO3,
            namingStrategy = NamingStrategy.KOTLIN_IDIOMATIC
        )

        val msg = project.findMessage("collection_container")

        // Repeated fields: fieldName + List
        val repeatedField = msg.findField("tag_names").assertIsInstance<ProtoMessageField>()
        Assertions.assertEquals("tagNamesList", repeatedField.codeName)

        // Maps: mapName (usually mapping to a Kotlin Map property)
        val mapField = msg.findField("attribute_map").assertIsInstance<ProtoMapField>()
        Assertions.assertEquals("attributeMap", mapField.codeName)
    }

    // --- PROTO2 EXTENSIONS ---

    @Test
    fun testProto2ExtensionNaming() {
        val project = buildProject(
            content = """
            message BaseMessage {
                extensions 100 to 200;
            }
            
            extend BaseMessage {
                optional string extension_field_name = 101;
                repeated int32 extra_score = 102;
            }
        """.trimIndent(),
            protoVersion = ProtoVersion.PROTO2,
            namingStrategy = NamingStrategy.KOTLIN_IDIOMATIC
        )

        val scalarExt = project.findExtensionField("extension_field_name")
        Assertions.assertEquals("extensionFieldName", scalarExt.codeName)

        val repeatedExt = project.findExtensionField("extra_score")
        Assertions.assertEquals("extraScoreList", repeatedExt.codeName)
    }

    // --- SERVICES & RPCS ---

    @Test
    fun testServiceAndRpcNaming() {
        val project = buildProject(
            content = """
            service auth_manager {
                rpc login_user (LoginRequest) returns (LoginResponse);
                rpc GetStatus (Empty) returns (Status);
            }
            message LoginRequest {}
            message LoginResponse {}
            message Empty {}
            message Status {}
        """.trimIndent(),
            protoVersion = ProtoVersion.PROTO3,
            namingStrategy = NamingStrategy.KOTLIN_IDIOMATIC
        )

        val service = project.findService("auth_manager")
        Assertions.assertEquals("AuthManagerStub", service.className.simpleName)

        // rpc login_user -> loginUser
        val loginMethod = service.findRpc("login_user")
        Assertions.assertEquals("loginUser", loginMethod.transformedKotlinName)

        // rpc GetStatus -> getStatus (Pascal to lowerCamel)
        val statusMethod = service.findRpc("GetStatus")
        Assertions.assertEquals("getStatus", statusMethod.transformedKotlinName)
    }

    // --- EDGE CASES & RESERVED KEYWORDS ---

    @Test
    fun testReservedKeywordsAndEdgeCases() {
        val project = buildProject(
            content = """
            message keyword_holder {
                string class = 1;
                string val = 2;
                string __internal_id__ = 3;
            }
        """.trimIndent(),
            protoVersion = ProtoVersion.PROTO3,
            namingStrategy = NamingStrategy.KOTLIN_IDIOMATIC
        )

        val msg = project.findMessage("keyword_holder")

        // KotlinPoet handles backticks; the model attribute name should be the clean string
        val classField = msg.findField("class").assertIsInstance<ProtoMessageField>()
        Assertions.assertEquals("class", classField.codeName)

        val valField = msg.findField("val").assertIsInstance<ProtoMessageField>()
        Assertions.assertEquals("val", valField.codeName)

        // Clean up underscores
        val internalField = msg.findField("__internal_id__").assertIsInstance<ProtoMessageField>()
        Assertions.assertEquals("internalId", internalField.codeName)
    }

    // --- ONEOFS ---

    @Test
    fun testOneOfNamingStrategy() {
        val project = buildProject(
            content = """
            message identity_provider {
                oneof authentication_method {
                    string email_address = 1;
                    int32 phone_number = 2;
                    string oauth_token = 3;
                }
            }
        """.trimIndent(),
            protoVersion = ProtoVersion.PROTO3,
            namingStrategy = NamingStrategy.KOTLIN_IDIOMATIC
        )

        val msg = project.findMessage("identity_provider")

        // 1. The property in the class representing the oneof
        val oneOf = msg.findOneOf("authentication_method")
        Assertions.assertEquals("authenticationMethod", oneOf.codeName)

        // 2. The Sealed Class/Interface name
        Assertions.assertEquals("AuthenticationMethod", oneOf.sealedClassName.simpleName)

        // 3. The individual cases inside the OneOf
        val emailCase = oneOf.findCase("email_address")
        Assertions.assertEquals("EmailAddress", emailCase.sealedClassChildName.simpleName)

        val phoneCase = oneOf.findCase("phone_number")
        Assertions.assertEquals("PhoneNumber", phoneCase.sealedClassChildName.simpleName)
    }

    // --- ENUMS ---

    @Test
    fun testEnumNamingStrategy() {
        val project = buildProject(
            content = """
            enum user_status {
                USER_STATUS_UNSPECIFIED = 0;
                USER_STATUS_ACTIVE = 1;
                user_status_suspended = 2;
                DELETED = 3;
            }
        """.trimIndent(),
            protoVersion = ProtoVersion.PROTO3,
            namingStrategy = NamingStrategy.KOTLIN_IDIOMATIC
        )

        val protoEnum = project.findEnum("user_status")
        Assertions.assertEquals("UserStatus", protoEnum.className.simpleName)

        Assertions.assertEquals("UserStatusUnspecified", protoEnum.findEntry("USER_STATUS_UNSPECIFIED").className.simpleName)
        Assertions.assertEquals("UserStatusSuspended", protoEnum.findEntry("user_status_suspended").className.simpleName)
        Assertions.assertEquals("Deleted", protoEnum.findEntry("DELETED").className.simpleName)
    }

    // --- MIXED INPUTS & NUMBERS ---

    @Test
    fun testMixedNamingInputsAndNumbers() {
        val project = buildProject(
            content = """
            message mixed_input {
                string AlreadyCamelCase = 1;
                string snake_case_field = 2;
                string SCREAMING_SNAKE = 3;
                string simple = 4;
                string address_line_1 = 5;
                string DNS_Record = 6;
            }
        """.trimIndent(),
            protoVersion = ProtoVersion.PROTO3,
            namingStrategy = NamingStrategy.KOTLIN_IDIOMATIC
        )

        val msg = project.findMessage("mixed_input")

        // PascalCase to lowerCamelCase
        Assertions.assertEquals(
            "alreadyCamelCase",
            msg.findField("AlreadyCamelCase").assertIsInstance<ProtoMessageField>().codeName
        )

        // Standard snake_case
        Assertions.assertEquals(
            "snakeCaseField",
            msg.findField("snake_case_field").assertIsInstance<ProtoMessageField>().codeName
        )

        // Screaming snake to lowerCamelCase
        Assertions.assertEquals(
            "screamingSnake",
            msg.findField("SCREAMING_SNAKE").assertIsInstance<ProtoMessageField>().codeName
        )

        // Numbers handling
        Assertions.assertEquals(
            "addressLine1",
            msg.findField("address_line_1").assertIsInstance<ProtoMessageField>().codeName
        )

        // Acronyms/Mixed case with underscores
        Assertions.assertEquals(
            "dnsRecord",
            msg.findField("DNS_Record").assertIsInstance<ProtoMessageField>().codeName
        )
    }
}
