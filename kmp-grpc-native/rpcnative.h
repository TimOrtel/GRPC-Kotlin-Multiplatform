#ifndef RPCNATIVE_H
#define RPCNATIVE_H

#include <stddef.h>
#include <stdint.h>

#include "rpcnative.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct client_context client_context;
typedef struct grpc_channel grpc_channel;
typedef struct grpc_stub grpc_stub;
typedef struct byte_buffer byte_buffer;
typedef struct byte_buffer_data byte_buffer_data;

void unary_rpc(
    const client_context *client_context, const grpc_stub *stub, const char *method_name,
    byte_buffer *request_buffer, byte_buffer *response_buffer, void *data,
    const void (*callback)(int status, const char *message, void *data));

grpc_channel *create_insecure_channel(const char *host);

void destroy_channel(const grpc_channel *channel);

client_context *create_client_context();

void destroy_client_context(const client_context *context);

void cancel_call(const client_context *context);

grpc_stub *create_stub(const grpc_channel *channel);

void destroy_stub(const grpc_stub *stub);

byte_buffer *create_byte_buffer(const uint8_t *data, size_t size);

byte_buffer *create_empty_byte_buffer();

void destroy_byte_buffer(const byte_buffer *buffer);

byte_buffer_data *get_byte_buffer_data(const byte_buffer *buffer);

const uint8_t *get_byte_buffer_data_data(const byte_buffer_data *data);

size_t get_byte_buffer_data_size(const byte_buffer_data *data);

void destroy_byte_buffer_data(const byte_buffer_data *data);

#ifdef __cplusplus
}
#endif

#endif
