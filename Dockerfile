FROM mcr.microsoft.com/java/jre:17-zulu-alpine

WORKDIR /app

COPY ./build/install/placegremlin-shadow/bin /app/bin
COPY ./build/install/placegremlin-shadow/lib /app/lib
COPY ./src/main/resources/gremlins /app/gremlins

ENV IMAGE_PATH=/app/gremlins

ENTRYPOINT ["./bin/placegremlin"]
