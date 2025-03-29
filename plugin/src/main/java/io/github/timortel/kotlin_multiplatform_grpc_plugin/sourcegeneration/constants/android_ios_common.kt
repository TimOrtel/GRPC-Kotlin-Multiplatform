package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

val WireFormatMakeTag = MemberName(PACKAGE_IO, "wireFormatMakeTag")
val WireFormatForType = MemberName(PACKAGE_IO, "wireFormatForType")
val ComputeTagSize = MemberName(PACKAGE_IO, "computeTagSize")
val ComputeInt32SizeNoTag = MemberName(PACKAGE_IO, "computeInt32SizeNoTag")

val DataType = ClassName(PACKAGE_MESSAGE, "DataType")