import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat

fun main() {
    CodedOutputStream.computeRawVarint32Size()
}