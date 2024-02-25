FROM markhobson/maven-chrome:jdk-17

WORKDIR /tests
COPY gradlew gradlew
COPY gradle gradle
RUN ./gradlew --version

COPY . .

ENTRYPOINT ["./gradlew"]
RUN ./gradlew :grpc-mp-test:jsTestClasses