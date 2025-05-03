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

    auto call_data = create_call_data();

    custom_rpc(
        channel,
        context,
        "/io.github.timortel.kmpgrpc.test.TestService/emptyRpc",
        call_data,
        &p,
        [](void *data, byte_buffer *buffer) {
            std::cout << "Received message" << std::endl;
        },
        [](void *data) {
            std::cout << "WRITING DONE" << std::endl;
        },
        [](void *data) {
            std::cout << "METADATA RECEIVED" << std::endl;
        },
        [](void *data, call_status *status) {
            std::cout << "CALL DONE" << std::endl;
            static_cast<std::promise<int>*>(data)->set_value(3);
        }
    );

    write_message(call_data, create_byte_buffer(nullptr, 0));

    sleep(1);

    signal_client_writing_end(call_data);

    future.wait();
    std::cout << "DONE" << std::endl;
}
