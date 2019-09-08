FROM clojure:openjdk-8-lein-alpine
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN ["lein", "uberjar"]

FROM java:8-alpine
ENV PORT=3000
WORKDIR /usr/src/app
COPY --from=0 /usr/src/app/target/approve-transactions-0.1.0-SNAPSHOT-standalone.jar .
CMD ["java", "-jar", "approve-transactions-0.1.0-SNAPSHOT-standalone.jar", "$PORT"]
