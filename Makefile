ifndef TAG
TAG=approve-transactions/latest
endif

ifndef PORT
PORT=3000
endif

.PHONY: test

run:
	PORT=${PORT} lein run

test:
	lein midje :autotest

build:
	lein uberjar

docker: docker-build docker-push

docker-build:
	docker build . -t ${TAG}

docker-run:
	docker run -p ${PORT}:${PORT} -e PORT=${PORT} ${TAG}

docker-tag:
	docker tag ${TAG} ${NEWTAG}

docker-push:
	docker push ${TAG}

repl:
	lein repl
