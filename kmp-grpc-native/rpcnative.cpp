#include "rpcnative.h"
#include <grpcpp/grpcpp.h>
#include <grpcpp/generic/generic_stub.h>

struct client_context {
    grpc::ClientContext *context;
};

struct grpc_channel {
    std::shared_ptr<grpc::Channel> channel;
};

struct grpc_stub {
    grpc::GenericStub *stub;
};

struct byte_buffer {
    grpc::ByteBuffer *buffer;

    explicit byte_buffer(grpc::ByteBuffer *buffer) {
        this->buffer = buffer;
    }
};

struct byte_buffer_data {
    grpc::Slice *slice;

    explicit byte_buffer_data(grpc::Slice *slice) {
        this->slice = slice;
    }
};

client_context *create_client_context() {
    const auto context = new client_context;
    context->context = new grpc::ClientContext;
    return context;
}

void destroy_client_context(const client_context *context) {
    delete context->context;
    delete context;
}

void cancel_call(const client_context *context) {
    context->context->TryCancel();
}

grpc_channel *create_insecure_channel(const char *host) {
    const auto channel = new grpc_channel;
    channel->channel = grpc::CreateChannel(host, grpc::InsecureChannelCredentials());
    return channel;
}

void destroy_channel(const grpc_channel *channel) {
    delete channel;
}

grpc_stub *create_stub(const grpc_channel *channel) {
    const auto stub = new grpc_stub;
    stub->stub = new grpc::GenericStub(channel->channel);
    return stub;
}

void destroy_stub(const grpc_stub *stub) {
    delete stub->stub;
    delete stub;
}

byte_buffer *create_byte_buffer(const uint8_t *data, size_t size) {
    const auto slice = new grpc::Slice(reinterpret_cast<const char *>(data), size);
    const auto buffer = new grpc::ByteBuffer(slice, 1);

    return new byte_buffer(buffer);
}

byte_buffer *create_empty_byte_buffer() {
    return new byte_buffer(new grpc::ByteBuffer());
}

void destroy_byte_buffer(const byte_buffer *buffer) {
    delete buffer->buffer;
    delete buffer;
}

byte_buffer_data *get_byte_buffer_data(const byte_buffer *buffer) {
    const auto data = new byte_buffer_data(new grpc::Slice());
    buffer->buffer->DumpToSingleSlice(data->slice);
    return data;
}

const uint8_t *get_byte_buffer_data_data(const byte_buffer_data *data) {
    return data->slice->begin();
}

size_t get_byte_buffer_data_size(const byte_buffer_data *data) {
    return data->slice->size();
}

void destroy_byte_buffer_data(const byte_buffer_data *data) {
    delete data->slice;
    delete data;
}

void unary_rpc(const client_context *client_context, const grpc_stub *stub, const char *method_name,
               byte_buffer *request_buffer, byte_buffer *response_buffer, void *data,
               const void (*callback)(int status, const char *message, void *data)) {
    std::cout << "unary_rpc()" << std::endl;

    constexpr grpc::StubOptions options; {
    }

    std::cout << "making call" << std::endl;

    // Make the call
    stub->stub->UnaryCall(client_context->context, method_name, options, request_buffer->buffer, response_buffer->buffer,
                          [callback, data](const grpc::Status &status) {
                              std::cout << "Reached callback" << std::endl;
                              std::cout << "status: " << status.error_code() << " " << status.error_message() <<
                                      std::endl;

                              callback(status.error_code(), status.error_message().c_str(), data);
                          });

    std::cout << "call done" << std::endl;
}
