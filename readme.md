[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
![version](https://img.shields.io/badge/version-0.3.0-blue)

![badge][badge-android]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-ios]

# gRPC Kotlin Multiplatform
This projects implements client-side gRPC for Android, JVM, iOS and the web.

**⚠️ Warning: This project is still under development and does not support all gRPC features!**

**⚠️ Warning: The implementation for javascript is currently not covered by unit tests!**

## Table of contents
- [Features](#features)
- [Usage](#usage)
- [Setup](#setup)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [Implementation details](#implementation_details)
- [License](#license)

## Features
- Parses proto3 files and generates Kotlin files for these proto3 files.
- DSL Syntax for creating proto objects.
- Suspending rpc calls like in Kotlin/GRPC.
- Heavily influenced by the Java/Kotlin gRPC and Proto API.

### Supported features:
- suspending unary-rpc and server side streaming.
- nested messages

### Supported proto types:
- int32
- int64
- double
- float
- bool
- string
- enums
- map
- all messages built of these types

_Feel free to add support for more types, I just implemented those I needed for now._

## Usage
This plugin generates a kotlin class for each message/enum defined in the proto files.
They all start with KM and then have the name of the message/enum.

First, run common:generateMPProtos to generate the multiplatform proto files.

### Creating proto objects
In your common module, you can create proto objects like this:
```kotlin
val myMessage = kmMyMessage {
    myStringField = "foo"
    
    myNumberField = 23
    
    myOtherMessageField = kmOtherMessage { 
        //...
    }
}
```

### Making rpc calls
```kotlin
 suspend fun makeCall() {
    val channel = KMChannel.Builder()
        .forAddress("localhost", 8082) //replace with your address and your port
        .usePlaintext() //To force grpc to allow plaintext traffic, if you don't call this https is used.
        .build()
    
    //For each service a unique class is generated. KM(serviceName)Stub
    val stub = KMMyServiceStub(channel)
    
    val request = kmRequest {
        //...
    }
     
     try {
         val response = stub
             .withDeadlineAfter(10, TimeUnit.SECONDS) //Specify a deadline if you need to
             .myRpc(request)

         //Handle response
     } catch (e: KMStatusException) {
         
     } catch (e: Exception) {
         
     }
}
```
You can call this common code from JVM/Android, iOS and JS modules.

## Setup
In your top-level build.gradle.kts, add the following:
```kotlin
buildscript {
    repositories {
        //...
        gradlePluginPortal()
    }

    //...
}
```

### Common Module build.gradle.kts
Add the following to your plugins block:
```kotlin
plugins {
    kotlin("multiplatform")

    //...
    
    id("io.github.timortel.kotlin-multiplatform-grpc-plugin") version "<latest version>"
}
```

Configure your JS configuration:
```kotlin
kotlin {
    //...

    js(IR) {
        useCommonJs()

        browser {
            webpackTask {
                output.libraryTarget = "commonjs2"
            }
        }

        binaries.executable()
    }
    
    //...
}
```
Other configurations may work, but I have not tested others.

Add the library as a dependency:
```kotlin
import io.github.timortel.kotlin_multiplatform_grpc_plugin.GrpcMultiplatformExtension.OutputTarget

plugins {
    //Required when targeting iOS
    kotlin("native.cocoapods")
}

repositories {
    //...
    maven(url = "https://jitpack.io")
}

kotlin {
    //For iOS support, configure cocoapods
    cocoapods {
        summary = "..."
        homepage = "..."
        ios.deploymentTarget = //...
            
        pod("gRPC-ProtoRPC", moduleName = "GRPCClient")
        pod("Protobuf")
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("com.github.TimOrtel.GRPC-Kotlin-Multiplatform:grpc-multiplatform-lib:<latest version>")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:<latest version>")
            }
        }

        val jvmMain by getting {
            dependencies {
                //Make sure the generated Kotlin JVM protos are available
                api(project(":generate-proto"))
            }
        }
    }
}

grpcKotlinMultiplatform {
    targetSourcesMap.put(OutputTarget.COMMON, listOf(kotlin.sourceSets.getByName("commonMain")))
    targetSourcesMap.put(OutputTarget.IOS, listOf(kotlin.sourceSets.getByName("iosMain")))
    targetSourcesMap.put(OutputTarget.JVM, listOf(kotlin.sourceSets.getByName("androidMain")))
    targetSourcesMap.put(OutputTarget.JS, listOf(kotlin.sourceSets.getByName("jsMain")))

    //Specify the folders where your proto files are located, you can list multiple.
    protoSourceFolders.set(listOf(projectDir.resolve("../protos/src/main/proto")))
}

```

### JVM/Android
Make sure your Kotlin-Protobuf and Kotlin-GRPC generated files are available in you JVM and Android modules.
An example on how to configure this can be found in [here](example).

### JS
You must add the following npm dependencies to your JS module:
```kotlin
dependencies {
    //...
    api(npm("google-protobuf", "^<latest version>"))
    api(npm("grpc-web", "^<latest version>"))
    api(npm("protobufjs", "^<latest version>"))
}
```

### iOS
This library uses the updated memory manager. Therefore, you might have to [enable this memory manager in your project](https://github.com/JetBrains/kotlin/blob/master/kotlin-native/NEW_MM.md#switch-to-the-new-mm), too.

## Roadmap
- Similar to the message generation in iOS, the messages should also be generated for JVM/Android. Therefore, it should no longer be necessary to include the generated proto files by protoc.

## Contributing
Feel free to implement improvements, bug fixes and features and create a pull request.
Please send all pull requests to the develop branch, as the master always holds the code for the latest version.

## Implementation details

### How does it work internally?
- The JVM implementation simply delegates all logic to the files generated by the proto plugin.
- The JS implementation fully replaces the javascript protoc files and implements the logic in Kotlin. It then utilizes the google-protobuf and grpc-web the same way the javascript files would do.
- The iOS implementation uses the objective-c implementation of gRPC. The necessary message implementations are generated by the gradle plugin.

## License
Copyright 2022 Tim Ortel

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


[badge-android]: http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat
[badge-ios]: http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat
[badge-js]: http://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat