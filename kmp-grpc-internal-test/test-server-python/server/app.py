import logging
from concurrent import futures
from grpc_reflection.v1alpha import reflection
import grpc
import basicMessages_pb2
import basicMessages_pb2_grpc
import editions_test_service_pb2_grpc

class EditionsTestService(editions_test_service_pb2_grpc.EditionsTestServiceServicer):
    def sendPacked(self, request, context):
        return request

    def sendExpanded(self, request, context):
        return request

    def sendLegacyRequiredField(self, request, context):
        return request

    def sendMessageWithEveryExtension(self, request, context):
        return request

class TestService(basicMessages_pb2_grpc.TestServiceServicer):
    def emptyRpc(self, request, context):
        return request

def serve():
    logging.basicConfig(level = logging.DEBUG)
    grpc_logger = logging.getLogger("grpc")
    grpc_logger.setLevel(logging.DEBUG)

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=2))
    editions_test_service_pb2_grpc.add_EditionsTestServiceServicer_to_server(EditionsTestService(), server)
    basicMessages_pb2_grpc.add_TestServiceServicer_to_server(TestService(), server)

    service_names = (
        basicMessages_pb2.DESCRIPTOR.services_by_name['TestService'].full_name,
        reflection.SERVICE_NAME,
    )

    reflection.enable_server_reflection(service_names, server)

    server.add_insecure_port("[::]:17892")
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()
