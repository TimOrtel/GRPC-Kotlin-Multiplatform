package io.github.timortel.kmpgrpc.composeexample.server

import io.github.timortel.kmpgrpc.composeexample.shared.Communication
import io.github.timortel.kmpgrpc.composeexample.shared.CommunicationServiceGrpcKt
import io.github.timortel.kmpgrpc.composeexample.shared.numMessage
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionServiceV1
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

fun main() {
    ServerBuilder.forPort(17888)
        .addService(object : CommunicationServiceGrpcKt.CommunicationServiceCoroutineImplBase() {
            override suspend fun squareNumber(request: Communication.NumMessage): Communication.NumMessage {
                return numMessage { value = request.value * request.value }
            }

            override suspend fun finalAverage(requests: Flow<Communication.NumMessage>): Communication.NumMessage {
                val nums = requests.toList().map { it.value }
                val avg = nums.sum() / nums.size

                return numMessage { value = avg }
            }

            override fun countdown(request: Communication.NumMessage): Flow<Communication.NumMessage> {
                val num = request.value

                return flow {
                    (0 until num).reversed().forEach { currentNum ->
                        emit(numMessage { value = currentNum + 1 })
                        delay(1000)
                    }
                }
            }

            override fun runningAverage(requests: Flow<Communication.NumMessage>): Flow<Communication.NumMessage> {
                val numbers = mutableListOf<Int>()

                return flow {
                    requests.map { it.value }.collect { currentNum ->
                        numbers += currentNum

                        val newAvg = numbers.sum() / numbers.size
                        emit(numMessage { value = newAvg })
                    }
                }
            }

            override suspend fun emptyRpc(request: Communication.NumMessage): Communication.NumMessage {
                return request
            }
        })
        .addService(ProtoReflectionServiceV1.newInstance())
        .build()
        .start()
        .awaitTermination()
}
