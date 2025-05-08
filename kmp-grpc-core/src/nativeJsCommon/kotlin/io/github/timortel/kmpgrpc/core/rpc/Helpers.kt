/*
 * Copyright 2020 gRPC authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Adapted from: https://github.com/grpc/grpc-kotlin/blob/6f774052d1d6923f8af2e0023886d69949b695ee/stub/src/main/java/io/grpc/kotlin/Helpers.kt
 * Changed:
 * - Only taken the singleOrStatusFlow and singleOrStatus methods
 * - Changed the method to use classes of this library.
 * - Stripped these methods of, in the context of this library, unnecessary parameters.
 */

package io.github.timortel.kmpgrpc.core.rpc

import io.github.timortel.kmpgrpc.core.StatusException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single

/**
 * Returns this flow, save that if there is not exactly one element, it throws a [StatusException].
 *
 * The purpose of this function is to enable the one element to get processed before we have
 * confirmation that the input flow is done.
 */
internal fun <T> Flow<T>.singleOrStatusFlow(): Flow<T> = flow {
    var found = false
    collect {
        if (!found) {
            found = true
            emit(it)
        } else {
            throw StatusException.InternalOnlyExpectedOneElement
        }
    }
    if (!found) {
        throw StatusException.InternalExpectedAtLeastOneElement
    }
}

/**
 * Returns the one and only element of this flow, and throws a [StatusException] if there is not
 * exactly one element.
 */
internal suspend fun <T> Flow<T>.singleOrStatus(): T = singleOrStatusFlow().single()
