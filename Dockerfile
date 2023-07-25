FROM mcr.microsoft.com/java/jre:11-zulu-alpine

WORKDIR /app

COPY ./build/install/placegremlin-shadow/bin /app/bin
COPY ./build/install/placegremlin-shadow/lib /app/lib

ENTRYPOINT ["./bin/placegremlin"]
