.PHONY: test
run:
	lein run 3000

test:
	lein midje :autotest

build:
	lein uberjar

repl:
	lein repl
