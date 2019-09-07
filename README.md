# approve-transactions

A Clojure library designed to ... well, that part is up to you.

## Usage

### Docker

You can automatically build and push a Docker image by running `TAG=registry.io/exaple:tag make docker`.

To build a Docker image, just run `make docker-build`. This will build an image and tag it as `approve-transactions:latest`. To tag it with other version, run `TAG=approve-transactions/example make docker-build`.

To run it, use `make docker-run` and to specify a port, `PORT=5000 make docker-run`. You can also supply a variable `TAG` to run another docker image.

To tag it again, use `TAG=old-tag:example NEWTAG=new-tag:example make docker-tag`. To push if, run `TAG=tag:example make docker-push`
