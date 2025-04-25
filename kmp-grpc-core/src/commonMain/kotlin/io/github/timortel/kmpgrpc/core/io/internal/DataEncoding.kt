// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

/*
 * This file is an adapted translation of: https://github.com/protocolbuffers/protobuf/blob/bf34ebee2801d089083977b087a2f6e65f3c7022/java/core/src/main/java/com/google/protobuf/CodedOutputStream.java
 modified:
 - only include the zigzag encodings
 - translate from java to kotlin
 */

package io.github.timortel.kmpgrpc.core.io.internal

internal object DataEncoding {
    fun encodeZigZag32(n: Int): UInt {
        return ((n shl 1) xor (n shr 31)).toUInt()
    }

    fun encodeZigZag64(n: Long): ULong {
        return ((n shl 1) xor (n shr 63)).toULong()
    }
}
