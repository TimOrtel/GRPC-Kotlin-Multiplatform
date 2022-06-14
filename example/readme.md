Illustrates how grpc multiplatform can work.

## 1. Run the server
Run the server located in the jvm module.

## Android/JVM example
Run the client in the jvm module.

## Web/JS example
1. Start the envoy server:
```
cd jvm
docker run --rm -it -v $(pwd)/envoy-custom.yaml:/envoy-custom.yaml --network=host envoyproxy/envoy-dev:8ec461e3a6ff2503a05e599029c47252d732d87b -c /envoy-custom.yaml
```
2. Run the gradle task js:browserDevelopmentRun