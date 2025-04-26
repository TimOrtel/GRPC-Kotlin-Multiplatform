// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

/*
 * This file is an adapted translation of: https://github.com/protocolbuffers/protobuf/blob/bf662dca814c7535b75d1520f9f32a76f7205c02/java/core/src/main/java/com/google/protobuf/CodedInputStream.java
 modified:
 - only include the zigzag decodings
 - translate from java to kotlin
 */

package io.github.timortel.kmpgrpc.core.io.internal

internal object DataDecoding {
    fun decodeZigZag32(n: Int): Int {
        return (n ushr 1) xor -(n and 1)
    }

    fun decodeZigZag64(n: Long): Long {
        return (n ushr 1) xor -(n and 1)
    }
}
