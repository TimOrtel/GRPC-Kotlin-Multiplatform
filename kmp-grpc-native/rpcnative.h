#ifndef RPCNATIVE_H
#define RPCNATIVE_H

#include <stddef.h>
#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct client_context client_context;
typedef struct grpc_channel grpc_channel;
typedef struct byte_buffer byte_buffer;
typedef struct byte_buffer_data byte_buffer_data;

typedef struct call_data call_data;

typedef struct call_status call_status;

typedef struct metadata metadata;
typedef struct metadata_const metadata_const;

enum start_call_result {
    SUCCESS = 0,
    FAILURE = 1
};

enum method_type {
    UNARY = 0,
    CLIENT_STREAMING = 1,
    SERVER_STREAMING = 2,
    BIDI_STREAMING = 3
};

struct method_descriptor {
    const char *method_name;
    enum method_type type;
};

typedef void (*OnReceiveInitialMetadata)(void *data, struct method_descriptor method_descriptor, metadata *metadata);


void rpc_impl(const grpc_channel *channel, const client_context *client_context,
              const char *method_name, enum method_type type, call_data *call_data, void *data,
              void (*on_message_received)(void *data, byte_buffer *buffer),
              void (*on_write_done)(void *data),
              void (*on_initial_metadata_received)(void *data),
              void (*on_done)(void *data, call_status *status)
);

grpc_channel *create_insecure_channel(const char *host);

grpc_channel *create_insecure_channel_with_interceptors(const char *host, void *callback_data,
                                                        const OnReceiveInitialMetadata on_receive_initial_metadata);

void destroy_channel(const grpc_channel *channel);

client_context *create_client_context();

void client_context_add_metadata(const client_context *context, const char *key, const char *value);

metadata_const *client_context_get_trailing_metadata(const client_context *context);

void destroy_client_context(const client_context *context);

void cancel_call(const client_context *context);

byte_buffer *create_byte_buffer(const uint8_t *data, size_t size);

byte_buffer *create_empty_byte_buffer();

void destroy_byte_buffer(const byte_buffer *buffer);

byte_buffer_data *get_byte_buffer_data(const byte_buffer *buffer);

const uint8_t *get_byte_buffer_data_data(const byte_buffer_data *data);

size_t get_byte_buffer_data_size(const byte_buffer_data *data);

void destroy_byte_buffer_data(const byte_buffer_data *data);

void write_message(const call_data *call_data, const byte_buffer *buffer);

void signal_client_writing_end(const call_data *call_data);

call_status *create_call_status();

void destroy_call_status(const call_status *status);

int call_status_code(const call_status *status);

char *call_status_message(const call_status *status);

call_data *create_call_data();

void destroy_call_data(const call_data *call_data);

void metadata_iterate(const metadata *metadata, void *data,
                      void (*block)(void *data, const char *key, const char *value));

void metadata_const_iterate(const metadata_const *metadata, void *data,
                            void (*block)(void *data, const char *key, const char *value));

void metadata_insert(const metadata *metadata, const char *key, const char *value);

void metadata_remove(const metadata *metadata, const char *key);

const char *metadata_get(const metadata *metadata, const char *key);

size_t metadata_size(const metadata *metadata);

void metadata_destroy(const metadata *metadata);

void metadata_const_destroy(const metadata_const *metadata);

#ifdef __cplusplus
}
#endif

#endif
