package io.github.timortel.kotlin_multiplatform_grpc_plugin.modeltree

import io.github.timortel.kmpgrpc.plugin.NamingStrategy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.validation.BaseValidationTest.ProtoVersion
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DeclarationClashTest : BaseModelTreeTest() {

    @Test
    fun testNameCollisionResolution() {
        val project = buildProject(
            content = """
            message collision_test {
                // These both map to 'myField'
                string my_field = 1;
                string myField = 2;
                
                // This matches the class name 'CollisionTest' after transformation
                string CollisionTest = 3;
            }
        """.trimIndent(),
            protoVersion = ProtoVersion.PROTO3,
            namingStrategy = NamingStrategy.KOTLIN_IDIOMATIC
        )

        val msg = project.findMessage("collision_test")

        // The first one gets the preferred name
        val field1 = msg.findField("my_field").assertIsInstance<ProtoMessageField>()
        Assertions.assertEquals("myField", field1.codeName)

        // The second one must be disambiguated
        val field2 = msg.findField("myField").assertIsInstance<ProtoMessageField>()
        Assertions.assertTrue(field2.codeName.startsWith("myField_"))

        // The field matching the class name must be disambiguated
        // to avoid constructor/class shadowing
        val field3 = msg.findField("CollisionTest").assertIsInstance<ProtoMessageField>()
        Assertions.assertNotEquals("CollisionTest", field3.codeName)
    }

    @Test
    fun testClassNameCollisionResolution() {
        val project = buildProject(
            content = """            
            // Peer Collision: Both map to 'UserRequest'
            message user_request {
                string id = 1;
            }
            message UserRequest {
                string email = 1;
            }
        """.trimIndent(),
            protoVersion = ProtoVersion.PROTO3,
            namingStrategy = NamingStrategy.KOTLIN_IDIOMATIC
        )

        // Test Peer Collision Resolution
        // The first message encountered should get the "clean" name.
        val firstMsg = project.findMessage("user_request")
        Assertions.assertEquals("UserRequest", firstMsg.className.simpleName)

        // The second message must be disambiguated (usually by appending a suffix).
        val secondMsg = project.findMessage("UserRequest")
        Assertions.assertEquals("UserRequest1", secondMsg.className.simpleName)
    }
}
