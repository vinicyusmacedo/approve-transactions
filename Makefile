TAG=approve-transactions/latest
PORT=3000

.PHONY: test

run:
	lein run 3000

test:
	lein midje :autotest

build:
	lein uberjar

docker: docker-build docker-push

docker-build:
	docker build . -t ${TAG}

docker-run:
	docker run -p ${PORT}:${PORT} ${TAG}

docker-tag:
	docker tag ${TAG} ${NEWTAG}

docker-push:
	docker push ${TAG}

repl:
	lein repl
