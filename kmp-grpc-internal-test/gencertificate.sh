#!/usr/bin/env bash

openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout test-server/src/main/resources/server.key -out test-server/src/main/resources/server.pem \
  -subj "/CN=localhost" -days 10000 \
