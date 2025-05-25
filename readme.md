[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Download](https://img.shields.io/maven-central/v/io.github.timortel/kmp-grpc-core) ](https://central.sonatype.com/artifact/io.github.timortel/kmp-grpc-core)
![version](https://img.shields.io/badge/version-0.5.0-blue)

![badge][badge-android]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-wasmjs]
![badge][badge-ios]

# gRPC Kotlin Multiplatform
This projects implements client-side gRPC for Android, JVM, Native (including iOS), JavaScript and WASM.

## Table of contents
- [Features](#features)
- [Usage](#usage)
- [Setup](#setup)
- [Example Implementation](#example-implementation)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [Implementation details](#implementation-details)
- [License](#license)

## Features
### Supported rpc types:
|                    | JVM/Android | Native (including iOS) | JavaScript (Browser + NodeJs)) | WasmJs (Browser + NodeJs)) |
|--------------------|-------------|------------------------|--------------------------------|----------------------------|
| `Unary`            | ✅           | ✅                      | ✅                              | ✅                          |
| `Client-streaming` | ✅           | ✅                      | ❌                              | ❌                          |
| `Server-streaming` | ✅           | ✅                      | ✅                              | ✅                          |
| `Bidi-streaming`   | ✅           | ✅                      | ❌                              | ❌                          |

### Supported proto types:
| Proto Type   | Kotlin Type  |
|-------------|-------------|
| `double`    | `Double`    |
| `float`     | `Float`     |
| `int32`     | `Int`       |
| `int64`     | `Long`      |
| `uint32`    | `UInt`      |
| `uint64`    | `ULong`     |
| `sint32`    | `Int`       |
| `sint64`    | `Long`      |
| `fixed32`   | `UInt`      |
| `fixed64`   | `ULong`     |
| `sfixed32`  | `Int`       |
| `sfixed64`  | `Long`      |
| `bool`      | `Boolean`   |
| `string`    | `String`    |
| `bytes`     | `ByteArray` |

### Supported proto options:
| Proto Option          | Support Status |
|----------------------|---------------|
| `java_package`       | ✅ Supported  |
| `java_outer_classname` | ✅ Supported  |
| `java_multiple_files` | ✅ Supported  |
| `deprecated`        | ⏳ Planned    |
| `packed`           | ⏳ Planned    |
| `optimize_for`       | ❌ Not Supported |

### Code generation:
| Proto Struct | Support Status |
|--------------|---------------|
| Messages     | ✅ Supported  |
| Enums        | ✅ Supported  |
| Services     | ✅ Supported  |

### Well-known types:
For reference, see [the official documentation](https://protobuf.dev/reference/protobuf/google.protobuf/).

| Protobuf Type        | Supported     |
|----------------------|---------------|
| `any.proto`         | ✅ Supported   |
| `api.proto`         | ✅ Supported   |
| `duration.proto`    | ✅ Supported   |
| `empty.proto`       | ✅ Supported   |
| `field_mask.proto`  | ✅ Supported   |
| `source_context.proto` | ✅ Supported   |
| `struct.proto`      | ✅ Supported   |
| `timestamp.proto`   | ✅ Supported   |
| `type.proto`        | ✅ Supported   |
| `wrappers.proto`    | ✅ Supported   |

### Additional Features
- ✅ Generates DSL syntax to create messages

## Usage
This plugin generates a kotlin class for each message/enum defined in the proto files. Assume you have the following proto file:
```protobuf
syntax = "proto3";

package com.example;

option java_multiple_files = true;

message HelloRequest {
  string greeting = 1;
}

message HelloResponse {
  string response = 1;
}

service HelloService {
  rpc sayHello (HelloRequest) returns (HelloResponse);

  rpc sayHelloMultipleTimes (HelloRequest) returns (stream HelloResponse);
}
```

### Creating proto objects
In your common module, you can create proto objects like this:
```kotlin
val request = helloRequest {
    greeting = "My greeting"
}
```

### Making rpc calls
The request syntax is very similar to the one provided by gRPC Java. Add this code to your common module:
```kotlin
 suspend fun makeCall(): String {
    val channel = Channel.Builder()
        .forAddress("localhost", 8082) // replace with your address and your port
        .usePlaintext() // To force grpc to allow plaintext traffic, if you don't call this https is used.
        .build()
    
    // For each service a unique class is generated.
    val stub = HelloServiceStub(channel)

    val request = helloRequest {
        greeting = "My greeting"
    }
     
     return try {
         val response: HelloResponse = stub
             .withDeadlineAfter(10, TimeUnit.SECONDS) // Specify a deadline if you need to
             .myRpc(request)

         //Handle response
         response.response
     } catch (e: StatusException) {
         "An exception occurred: $e"
     }
}
```

### Working with well known types

#### `Any` Message Extensions

##### Example: `wrap`
Wraps a message inside an `Any` message.
```kotlin
val myMessage = myMessage {} // Some message
val wrapped = Any.wrap(myMessage)
```

##### Example: `unwrap`
Extracts a message from an `Any` object.
```kotlin
val extractedMessage: MyMessage = wrapped.unwrap(MyMessage.Companion)
```

##### Example: `isType`
Checks if an `Any` object holds a specific message type.
```kotlin
if (wrapped.isType(MyMessage.Companion)) {
    println("The message is of type MyMessage")
}
```

#### `Duration` Extensions

##### Example: `ofSeconds`
Creates a `Duration` from seconds.
```kotlin
val duration = Duration.ofSeconds(120)
```

##### Example: `ofMillis`
Creates a `Duration` from milliseconds.
```kotlin
val duration = Duration.ofMillis(500)
```

##### Example: `fromDuration`
Creates a `Duration` from `kotlin.time.Duration`.
```kotlin
val duration = Duration.fromDuration(1.5.seconds)
```

##### Example: `toDuration`
Converts a `Duration` to `kotlin.time.Duration`.
```kotlin
val kotlinDuration: kotlin.time.Duration = duration.toDuration()
```

#### `Timestamp` Extensions

##### Example: `fromInstant`
Creates a `Timestamp` from `Instant`.
```kotlin
val timestamp = Timestamp.fromInstant(Instant.now())
```

##### Example: `toInstant`
Converts a `Timestamp` to `Instant`.
```kotlin
val instant: Instant = timestamp.toInstant()
```

### Unknown Fields Support
Unknown fields are automatically captured when parsing messages and also serialized back to the wire. 
You can access them using the generated property:
```kotlin
class ExampleMessage {
    val unknownFields: List<UnknownField>
}
```

### Intercepting Calls
You can intercept calls to modify what is sent to the server and received from the server. Example:
```kotlin
val loggingInterceptor = object : CallInterceptor {
    override fun onStart(methodDescriptor: MethodDescriptor, metadata: Metadata): Metadata {
        println("Call started ${methodDescriptor.fullMethodName}")
        return super.onStart(methodDescriptor, metadata)
    }

    override fun onClose(
        methodDescriptor: MethodDescriptor,
        status: Status,
        trailers: Metadata
    ): Pair<Status, Metadata> {
        println("Call closed ${methodDescriptor.fullMethodName}")
        return super.onClose(methodDescriptor, status, trailers)
    }
}

val channel = Channel.Builder
    .forAddress("localhost", 8080)
    .withInterceptors(loggingInterceptor)
    .build()
```

## Setup
In your top-level build.gradle.kts, add the following:
```kotlin
buildscript {
    repositories {
        //...
        gradlePluginPortal()
    }

    // ...
}
```

### Common Module build.gradle.kts
Add the following to your plugins block:
```kotlin
plugins {
    kotlin("multiplatform")

    //...
    id("io.github.timortel.kmpgrpc.plugin") version "<latest version>"
}
```

Add the library as a dependency:
```kotlin
repositories {
    // ...
    mavenCentral()
}

kmpGrpc {
    // declare the targets you need.
    common() // required
    jvm()
    androidMain()
    js()
    native() // for native targets like iOS

    // Optional: if the protobuf well known types should be included
    // https://protobuf.dev/reference/protobuf/google.protobuf/
    includeWellKnownTypes = true
    
    // Specify the folders where your proto files are located, you can list multiple.
    protoSourceFolders = project.files("<source to your protos>")
}
```

## Example Implementation
See an example implementation of an Android app and an iOS app in the `example` folder. 

## Roadmap
- Support all proto data types

## Building locally
To build the native targets locally, you will need to have rust installed on your local machine. Once setup, you can run the following Gradle commands:
1. To build the library `gradle kmp-grpc-core:publishToMavenLocal`
2. To build the plugin `gradle kmp-grpc-plugin:publishToMavenLocal`

By default, kmp-grpc-core prints trace logs. To deactivate, build the library with `-Pio.github.timortel.kmp-grpc.internal.native.release=true`.

## Contributing
Feel free to implement improvements, bug fixes and features and create a pull request.
Please send all pull requests to the `develop` branch, as the `main` branch always holds the code for the latest version.

## Implementation details

### How does it work internally?
The plugin generates kotlin code for all provided proto files. No `protoc` is needed. The networking code is handled
by gRPC for JVM and by [tonic](https://github.com/hyperium/tonic) for all native targets. For JavaScript, the requests are handled by [ktor](https://github.com/ktorio/ktor).

## License
Copyright 2025 Tim Ortel

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


[badge-android]: http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat
[badge-ios]: http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat
[badge-js]: http://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat
[badge-wasmjs]: http://img.shields.io/badge/platform-wasmjs-F8DB5D.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat