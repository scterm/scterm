# SCTerm - Scrapy Cloud on Your Terminal

## Usage

The only params required are the api key and the job id.

```bash
$ export SHUB_APIKEY=xxxxxx
$ scterm <JOB-ID>
# e.g.
$ scterm 1/2/3
# Or use the dash url directly
$ scterm https://app.scrapinghub.com/p/1/2/3
```

## Build

First you'll need these tools installed:

- Java 8+
- [NodeJS](https://nodejs.org)
- [Yarn](https://yarnpkg.com/lang/en/docs/install/#mac-stable)

Then download the deps and build it.

```bash
$ yarn install
$ yarn build-once
```

The output would be a node script in `lib/main.js`. You can launch it with the node command:
```
$ node lib/main.js 1/2/3
```

## Development

### Launch the shadow-cljs watcher

```bash
yarn watch
```

### Running tests

To run the unit tests:

```bash
$ yarn test
# Only run tests matching the given pattern
$ yarn test -k jobinfo
```

To run the integration/ui tests:

```bash
$ yarn uitest
# Only run tests matching the given pattern
$ yarn test -k jobinfo
```
