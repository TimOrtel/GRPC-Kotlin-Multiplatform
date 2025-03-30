[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Download](https://img.shields.io/maven-central/v/io.github.timortel/kmp-grpc-core) ](https://central.sonatype.com/artifact/io.github.timortel/kmp-grpc-core)
![version](https://img.shields.io/badge/version-0.5.0-blue)

![badge][badge-android]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-ios]

# gRPC Kotlin Multiplatform
This projects implements client-side gRPC for Android, JVM, iOS and the JS for browser.

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
| RPC Type              | Support Status |
|-----------------------|---------------|
| Unary                | ✅ Supported  |
| Server-side Streaming | ✅ Supported  |
| Client-side Streaming | ⏳ Planned    |

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
import com.example.*

val request = helloRequest {
    greeting = "My greeting"
}
```

### Making rpc calls
The request syntax is very similar to the one provided by gRPC Java. Add this code to your common module:
```kotlin
 suspend fun makeCall(): String {
    val channel = KMChannel.Builder()
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
     } catch (e: KMStatusException) {
         "An exception occurred: $e"
     }
}
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
    id("io.github.timortel.kmp-grpc-plugin") version "<latest version>"

    // Required when targeting iOS
    kotlin("native.cocoapods")
}
```

Update your JS configuration:
```kotlin
kotlin {
    // ...

    js(IR) {
        useCommonJs()

        browser {
            webpackTask {
                output.libraryTarget = "commonjs2"
            }
        }

        binaries.executable()
    }
    
    // ...
}
```
While other configurations may work, I have only tested this one.

Add the library as a dependency:
```kotlin
repositories {
    // ...
    mavenCentral()
}

kotlin {
    // For iOS support, configure cocoapods
    cocoapods {
        summary = "..."
        homepage = "..."
        ios.deploymentTarget = //...
    }
    
    // ...
}

kmpGrpc {
    // declare the targets you need.
    common() // required
    jvm()
    androidMain()
    js()
    ios()

    // Optional: if the protobuf well known types should be included
    // https://protobuf.dev/reference/protobuf/google.protobuf/
    includeWellKnownTypes = true
    
    // Specify the folders where your proto files are located, you can list multiple.
    protoSourceFolders = project.files("<source to your protos>")
}
```

### iOS setup
This library is using Cocoapods, so please refer to the [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/native-cocoapods.html) on how to set up your iOS app with a Kotlin Multiplatform project, or refer to the example iOS app in the `example` folder.
When you build your app, you might encounter compilation errors of multiple Pods. This is because the gRPC Pod required C++17 to be enabled:
1. In Xcode, double-click on the `Pods` project and for project `Pods` go to `Build Settings`. Search for `C++ Language Dialect` and set it to `C++17`. 
2. You can now build the app.

## Example Implementation
See an example implementation of an Android app and an iOS app in the `example` folder. 

## Roadmap
- Support all proto data types

## Building locally
Run the following Gradle commands:
1. To build the library `gradle kmp-grpc-core:publishToMavenLocal`
2. To build the plugin `gradle kmp-grpc-plugin:publishToMavenLocal`

## Contributing
Feel free to implement improvements, bug fixes and features and create a pull request.
Please send all pull requests to the develop branch, as the master always holds the code for the latest version.

## Implementation details

### How does it work internally?
The plugin generates kotlin code for all provided proto files. No `protoc` is needed. The networking code is handled
by the native gRPC implementations for each platform.

## License
Copyright 2025 Tim Ortel

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


[badge-android]: http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat
[badge-ios]: http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat
[badge-js]: http://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat