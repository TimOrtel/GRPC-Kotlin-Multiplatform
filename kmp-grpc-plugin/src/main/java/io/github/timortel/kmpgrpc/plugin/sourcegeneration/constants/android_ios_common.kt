package io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

val WireFormatMakeTag = MemberName(PACKAGE_IO, "wireFormatMakeTag")
val WireFormatForType = MemberName(PACKAGE_IO, "wireFormatForType")

val DataType = ClassName(PACKAGE_MESSAGE, "DataType")