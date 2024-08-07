# Virtuoso tools

[virtuoso.tools](https://virtuoso.tools) is a collection of tools to help
musicians practice more efficiently.

## Overview

Virtuoso is a static web site with a JavaScript frontend. Specifically, the
static web pages are built with Clojure using
[Powerpack](https://github.com/cjohansen/powerpack), and the frontend is written
in ClojureScript and uses [Datascript](https://github.com/tonsky/datascript) to
store state, and [Replicant](https://github.com/cjohansen/replicant) to render
the UI. The frontend is [state-less and
data-driven](https://vimeo.com/861600197) - every function, except those in
[`virtuoso.ui.main`](./src/virtuoso/ui/main.cljs), is pure.

## Tests

Run tests with

```sh
make test
```
