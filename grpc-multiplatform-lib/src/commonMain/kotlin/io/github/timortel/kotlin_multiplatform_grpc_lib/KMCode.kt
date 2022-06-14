package io.github.timortel.kotlin_multiplatform_grpc_lib

/**
 * Copied from [the Java GRPC Status implementation](https://github.com/grpc/grpc-java/blob/master/api/src/main/java/io/grpc/Status.java).
 * Also see this link if you want to know further details on what each code means.
 */
enum class KMCode(val value: Int) {
    OK(0),
    CANCELLED(1),
    UNKNOWN(2),
    INVALID_ARGUMENT(3),
    DEADLINE_EXCEEDED(4),
    NOT_FOUND(5),
    ALREADY_EXISTS(6),
    PERMISSION_DENIED(7),
    RESOURCE_EXHAUSTED(8),
    FAILED_PRECONDITION(9),
    ABORTED(10),
    OUT_OF_RANGE(11),
    UNIMPLEMENTED(12),
    INTERNAL(13),
    UNAVAILABLE(14),
    DATA_LOSS(15),
    UNAUTHENTICATED(16);

    companion object {
        fun getCodeForValue(value: Int): KMCode = when (value) {
            0 -> OK
            1 -> CANCELLED
            2 -> UNKNOWN
            3 -> INVALID_ARGUMENT
            4 -> DEADLINE_EXCEEDED
            5 -> NOT_FOUND
            6 -> ALREADY_EXISTS
            7 -> PERMISSION_DENIED
            8 -> RESOURCE_EXHAUSTED
            9 -> FAILED_PRECONDITION
            10 -> ABORTED
            11 -> OUT_OF_RANGE
            12 -> UNIMPLEMENTED
            13 -> INTERNAL
            14 -> UNAVAILABLE
            15 -> DATA_LOSS
            16 -> UNAUTHENTICATED
            else -> throw IllegalArgumentException("Value=$value not known.")
        }
    }
}