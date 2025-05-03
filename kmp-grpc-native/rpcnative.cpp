#include "rpcnative.h"
#include <grpcpp/grpcpp.h>
#include <grpcpp/generic/generic_stub.h>
#include <functional>
#include <grpcpp/impl/channel_interface.h>
#include <grpcpp/impl/client_unary_call.h>
#include <grpcpp/support/client_callback.h>

using namespace std;
using namespace grpc;
using namespace grpc::experimental;

struct client_context {
    ClientContext *context;

    explicit client_context(ClientContext *context) {
        this->context = context;
    }
};

struct grpc_channel {
    shared_ptr<Channel> channel;

    explicit grpc_channel(const shared_ptr<Channel> &channel) {
        this->channel = channel;
    }
};

struct byte_buffer {
    ByteBuffer *buffer;

    explicit byte_buffer(ByteBuffer *buffer) {
        this->buffer = buffer;
    }
};

struct byte_buffer_data {
    Slice *slice;

    explicit byte_buffer_data(Slice *slice) {
        this->slice = slice;
    }
};

struct completion_queue {
    CompletionQueue *queue;

    explicit completion_queue(CompletionQueue *queue) {
        this->queue = queue;
    }
};

struct call_status {
    Status *status;

    explicit call_status(Status *status) {
        this->status = status;
    }
};

struct metadata {
    multimap<string_ref, string_ref> *data;

    explicit metadata(multimap<string_ref, string_ref> *data) {
        this->data = data;
    }
};

struct metadata_const {
    const multimap<string_ref, string_ref> *data;

    explicit metadata_const(const multimap<string_ref, string_ref> *data) {
        this->data = data;
    }
};

class CustomReactor final : ClientBidiReactor<ByteBuffer, ByteBuffer> {
public:
    explicit CustomReactor(
        const grpc_channel *channel,
        const client_context *client_context,
        const internal::RpcMethod &method,
        function<void(ByteBuffer *)> on_message_received,
        function<void()> on_write_done,
        function<void()> on_initial_metadata_received,
        function<void(Status)> on_done
    ): on_message_received_(std::move(on_message_received)), on_write_done_(std::move(on_write_done)),
       on_done_(std::move(on_done)), on_initial_metadata_received_(std::move(on_initial_metadata_received)) {
        ::internal::ClientCallbackReaderWriterFactory<ByteBuffer, ByteBuffer>::Create(
            channel->channel.get(), method, client_context->context, this);

        StartCall();
        StartRead(&server_response);
    }

    void OnDone(const Status &status) override {
        on_done_(status);
    }

    void OnReadDone(const bool ok) override {
        if (ok) {
            on_message_received_(&server_response);

            StartRead(&server_response);
        }
    }

    void OnWriteDone(const bool ok) override {
        if (ok) {
            on_write_done_();
        }
    }

    void PerformWrite(const ByteBuffer *buffer) {
        StartWrite(buffer);
    }

    void SignalWritesDone() {
        StartWritesDone();
    }

    void OnReadInitialMetadataDone(bool ok) override {
        if (ok) {
            on_initial_metadata_received_();
        }
    }

private:
    ByteBuffer server_response;

    function<void(ByteBuffer *)> on_message_received_;

    function<void()> on_write_done_;
    function<void(Status)> on_done_;
    function<void()> on_initial_metadata_received_;
};

class KmpGrpcInterceptor final : public Interceptor {
public:
    explicit KmpGrpcInterceptor(
        ClientRpcInfo *info, void *data,
        const OnReceiveInitialMetadata on_receive_initial_metadata): on_receive_initial_metadata_(
                                                                         on_receive_initial_metadata), data_(data),
                                                                     info_(info) {
    }

    void Intercept(InterceptorBatchMethods *methods) override {
        if (methods->
            QueryInterceptionHookPoint(InterceptionHookPoints::PRE_RECV_INITIAL_METADATA)) {
            if (const auto pairs = methods->GetRecvInitialMetadata(); pairs != nullptr) {
                const method_descriptor method_descriptor = {
                    info_->method(), static_cast<enum method_type>(info_->type())
                };
                
                on_receive_initial_metadata_(data_, method_descriptor, new metadata(pairs));
            }
        }

        methods->Proceed();
    }

    const OnReceiveInitialMetadata on_receive_initial_metadata_;

    void *data_;
    ClientRpcInfo *info_;
};

class KmpGrpcInterceptorFactory : public ClientInterceptorFactoryInterface {
public:
    explicit KmpGrpcInterceptorFactory(
        void *data, const OnReceiveInitialMetadata on_receive_initial_metadata): on_receive_initial_metadata_(
        on_receive_initial_metadata), data_(data) {
    }


    Interceptor *CreateClientInterceptor(ClientRpcInfo *info) override {
        return new KmpGrpcInterceptor(info, data_, on_receive_initial_metadata_);
    }

    const OnReceiveInitialMetadata on_receive_initial_metadata_;

    void *data_;
};

client_context *create_client_context() {
    const auto context = new client_context(new ClientContext);

    return context;
}

void client_context_add_metadata(const client_context *context, const char *key, const char *value) {
    context->context->AddMetadata(string(key), string(value));
}

metadata_const *client_context_get_trailing_metadata(const client_context *context) {
    return new metadata_const(&context->context->GetServerTrailingMetadata());
}

void destroy_client_context(const client_context *context) {
    delete context->context;
    delete context;
}

void cancel_call(const client_context *context) {
    context->context->TryCancel();
}

grpc_channel *create_insecure_channel(const char *host) {
    return new grpc_channel(CreateChannel(host, InsecureChannelCredentials()));
}

grpc_channel *create_insecure_channel_with_interceptors(const char *host, void *callback_data,
                                                        const OnReceiveInitialMetadata on_receive_initial_metadata) {
    const auto args = ChannelArguments();

    vector<
                unique_ptr<ClientInterceptorFactoryInterface> >
            interceptor_creators;
    interceptor_creators.push_back(
        make_unique<KmpGrpcInterceptorFactory>(callback_data, on_receive_initial_metadata));

    const auto channel = CreateCustomChannelWithInterceptors(
        host, InsecureChannelCredentials(), args, std::move(interceptor_creators)
    );

    return new grpc_channel(channel);
}

void destroy_channel(const grpc_channel *channel) {
    delete channel;
}

byte_buffer *create_byte_buffer(const uint8_t *data, size_t size) {
    const auto slice = new Slice(reinterpret_cast<const char *>(data), size);
    const auto buffer = new ByteBuffer(slice, 1);

    return new byte_buffer(buffer);
}

byte_buffer *create_empty_byte_buffer() {
    return new byte_buffer(new ByteBuffer());
}

void destroy_byte_buffer(const byte_buffer *buffer) {
    delete buffer->buffer;
    delete buffer;
}

byte_buffer_data *get_byte_buffer_data(const byte_buffer *buffer) {
    const auto data = new byte_buffer_data(new Slice());
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

struct call_data {
    unique_ptr<CustomReactor> call;
};

void rpc_impl(const grpc_channel *channel, const client_context *client_context,
              const char *method_name, enum method_type type, call_data *call_data, void *data,
              void (*on_message_received)(void *data, byte_buffer *buffer),
              void (*on_write_done)(void *data),
              void (*on_initial_metadata_received)(void *data),
              void (*on_done)(void *data, call_status *status)
) {
    const auto rpc_type = static_cast<internal::RpcMethod::RpcType>(type);

    auto rpc_method = internal::RpcMethod(method_name, rpc_type);

    cout << "Init call" << endl;

    call_data->call = make_unique<CustomReactor>(channel, client_context, rpc_method,
                                                      [on_message_received, data](ByteBuffer *buffer) -> void {
                                                          auto bb = byte_buffer(buffer);
                                                          on_message_received(data, &bb);
                                                      },
                                                      [data, on_write_done] {
                                                          on_write_done(data);
                                                      },
                                                      [data, on_initial_metadata_received] {
                                                          on_initial_metadata_received(data);
                                                      },
                                                      [data, on_done](Status status) {
                                                          auto cs = call_status(&status);
                                                          on_done(data, &cs);
                                                      }
    );
}

void write_message(const call_data *call_data, const byte_buffer *buffer) {
    call_data->call->PerformWrite(buffer->buffer);
}

void signal_client_writing_end(const call_data *call_data) {
    call_data->call->SignalWritesDone();
}

call_data *create_call_data() {
    return new call_data();
}

void destroy_call_data(const call_data *call_data) {
    delete call_data;
}


completion_queue *create_completion_queue() {
    return new completion_queue(new CompletionQueue());
}

void shutdown_completion_queue(const completion_queue *queue) {
    queue->queue->Shutdown();
}

void destroy_completion_queue(const completion_queue *queue) {
    delete queue->queue;
    delete queue;
}

bool completion_queue_next(const completion_queue *queue, void **tag, bool *ok) {
    return queue->queue->Next(tag, ok);
}

call_status *create_call_status() {
    return new call_status(new Status());
}

void destroy_call_status(const call_status *status) {
    delete status->status;
    delete status;
}

int call_status_code(const call_status *status) {
    return status->status->error_code();
}

char *call_status_message(const call_status *status) {
    return strdup(status->status->error_message().c_str());
}

metadata *metadata_create() {
    return new metadata(new multimap<string_ref, string_ref>());
}

void metadata_iterate_impl(const multimap<string_ref, string_ref> *map, void *data,
                           void (*block)(void *data, const char *key, const char *value)) {
    for (auto &[key, value]: *map) {
        block(data, key.data(), value.data());
    }
}

void metadata_iterate(const metadata *metadata, void *data,
                      void (*block)(void *data, const char *key, const char *value)) {
    metadata_iterate_impl(metadata->data, data, block);
}

void metadata_const_iterate(const metadata_const *metadata, void *data,
                            void (*block)(void *data, const char *key, const char *value)) {
    metadata_iterate_impl(metadata->data, data, block);
}

void metadata_insert(const metadata *metadata, const char *key, const char *value) {
    metadata->data->insert(make_pair(string_ref(key), string_ref(value)));
}

void metadata_remove(const metadata *metadata, const char *key) {
    metadata->data->erase(key);
}

const char *metadata_get(const metadata *metadata, const char *key) {
    auto [fst, snd] = metadata->data->equal_range(string_ref(key));

    if (fst == snd) {
        return nullptr;
    }

    string value;

    for (auto it = fst; it != snd; ++it) {
        const bool is_last = next(it) == snd;
        value.append(it->second.data(), it->second.size());
        if (!is_last) {
            value.append("; ");
        }
    }

    return strdup(value.c_str());
}

size_t metadata_size(const metadata *metadata) {
    return metadata->data->size();
}

void metadata_destroy(const metadata *metadata) {
    delete metadata;
}

void metadata_const_destroy(const metadata_const *metadata) {
    delete metadata;
}
