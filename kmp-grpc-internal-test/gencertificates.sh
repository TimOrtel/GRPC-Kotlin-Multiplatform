#!/usr/bin/env bash
set -e

mkdir -p test-server/src/main/resources/
cd test-server/src/main/resources/

# generate standalone cert
openssl req -x509 -newkey rsa:2048 -nodes \
-keyout standalone_leaf.key -out standalone_leaf.pem \
-subj "/" \
-addext "subjectAltName=DNS:localhost" \
-days 365

# generate root ca
openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout ca.key \
  -out ca.pem \
  -subj "/CN=Test CA" \
  -days 365 \
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
  -days 365 \
  -extfile <(printf "\
basicConstraints=CA:FALSE
subjectAltName=DNS:localhost
authorityKeyIdentifier=keyid,issuer
")
