# approve-transactions

This is an API that checks whether a transaction can be authorized based on a set of rules.

It has a single endpoint, `/check-transactions`, that take an account, transaction and last transactions and returns whether the transaction was approved or not, the new limit to be set and, if the transaction was denied, all the reasons why it happened. 

The API provides a Swagger UI (by hitting `/`) and a healthcheck (by hitting `/healthcheck`).

## Schema

The API accepts three schemas: `account`, `transaction` and `lastTransactions` (which is an array of transactions).

```json
{
  "account": {
    "cardIsActive": Boolean,
    "limit": Number,
    "denylist": [String]
  },
  "transaction": {
    "merchant": String,
    "amount": Number,
    "time": String
  },
  "lastTransactions": [transaction]
}
```

It should return a payload on this format:

```json
{
  "approved": Boolean,
  "newLimit": Number,
  "deniedReasons": [String]
}
```

## Rules

All rules are implemented on `approve-transactions.logic` and tested on `approve-transactions.logic-test`

- [x] The transaction amount should not be above limit
- [x] No transaction should be approved when the card is blocked
- [x] The first transaction shouldn't be above 90% of the limit
- [x] There should not be more than 10 transactions on the same merchant
- [x] Merchant denylist
- [x] There should not be more than 3 transactions on a 2 minutes interval 

## Usage

Use makefile to make it run:

`PORT=5000 make run`

to run all tests:

`make tests`

to start a REPL session:

`make repl`

to build a .jar file:

`make build`

### Docker

You can automatically build and push a Docker image by running `TAG=registry.io/exaple:tag make docker`.

To build a Docker image, just run `make docker-build`. This will build an image and tag it as `approve-transactions:latest`. To tag it with other version, run `TAG=approve-transactions/example make docker-build`.

To run it, use `make docker-run` and to specify a port, `PORT=5000 make docker-run`. You can also supply a variable `TAG` to run another docker image.

To tag it again, use `TAG=old-tag:example NEWTAG=new-tag:example make docker-tag`. To push if, run `TAG=tag:example make docker-push`
