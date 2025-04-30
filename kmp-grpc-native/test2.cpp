//
// Created by Tim Ortel on 30.04.25.
//

#include <cstdint>

#include "rpcnative.h"

#include <future>
#include <iostream>
#include <grpcpp/grpcpp.h>
#include <grpcpp/generic/generic_stub.h>

int main() {
    const std::uint8_t raw_bytes[] = {};

    std::promise<int> p;
    auto future = p.get_future();

    auto channel = create_insecure_channel("localhost:17888");
    auto stub = create_stub(channel);
    auto context = create_client_context();

    auto request_data = create_byte_buffer(raw_bytes, sizeof(raw_bytes));
    // auto request_data = create_empty_byte_buffer();
    auto response_data = create_empty_byte_buffer();

    unary_rpc(
        context,
        stub,
        "/io.github.timortel.kmpgrpc.test.TestService/emptyRpc",
        request_data,
        response_data,
        &p,
        [](int status, const char *message, void *data) -> const void {
            std::cout << "callback" << std::endl;
            static_cast<std::promise<int> *>(data)->set_value(3);
        });

    future.wait();
    std::cout << "DONE" << std::endl;
}
