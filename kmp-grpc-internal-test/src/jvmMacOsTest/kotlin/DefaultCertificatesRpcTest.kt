import hello.HelloRequest
import hello.HelloServiceStub
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.StatusException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.integration.ServerTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

abstract class DefaultCertificatesRpcTest : ServerTest {

    override val address: String = "grpcb.in"
    override val port: Int = 9001

    private val channelWithCertificate = Channel.Builder.forAddress(address, port)
        .build()

    private val channelWithoutCertificates = Channel.Builder.forAddress(address, port)
        .trustOnlyProvidedCertificates()
        .build()

    @Test
    fun testConnectionWithCertificates() = runTest {
        val stub = HelloServiceStub(channelWithCertificate)

        val msg = HelloRequest(greeting = "Hello World")
        stub.SayHello(msg)
    }

    @Test
    fun testConnectionFailsWithoutCertificates() = runTest {
        val stub = HelloServiceStub(channelWithoutCertificates)

        val msg = HelloRequest(greeting = "Hello World")
        assertFailsWith<StatusException> { stub.SayHello(msg) }
    }
}