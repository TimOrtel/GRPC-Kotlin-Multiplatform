#!/usr/bin/env bash
set -e
cd test-server/src/main/resources/

# generate standalone cert
openssl req -x509 -newkey rsa:2048 -nodes \
-keyout standalone_leaf.key -out standalone_leaf.pem \
-subj "/CN=localhost" \
-addext "subjectAltName=DNS:localhost" \
-addext "basicConstraints=CA:FALSE" \
-days 1

# generate root ca
openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout ca.key \
  -out ca.pem \
  -subj "/CN=Test CA" \
  -days 1 \
  -addext "basicConstraints=CA:TRUE" \

#generate leaf private key
openssl req -newkey rsa:2048 -nodes \
  -keyout ca_leaf.key \
  -out ca_leaf.csr \
  -subj "/CN=localhost" \
  -addext "subjectAltName=DNS:localhost" \
  -addext "basicConstraints=CA:FALSE" \

# generate leaf pem
openssl x509 -req \
  -in ca_leaf.csr \
  -CA ca.pem \
  -CAkey ca.key \
  -CAcreateserial \
  -out ca_leaf.pem \
  -days 1 \
  -extfile <(printf "\
basicConstraints=CA:FALSE
subjectAltName=DNS:localhost
authorityKeyIdentifier=keyid,issuer
")
