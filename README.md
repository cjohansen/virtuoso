# Virtuoso tools

[virtuoso.tools](https://virtuoso.tools) is a collection of tools to help
musicians practice more efficiently.

## Overview

Virtuoso is a static web site with a JavaScript frontend. Specifically, the
static web pages are built with Clojure using
[Powerpack](https://github.com/cjohansen/powerpack), and the frontend is written
in ClojureScript, usin [Datascript](https://github.com/tonsky/datascript) to
store state, and [Replicant](https://github.com/cjohansen/replicant) to render
the UI. The frontend is [state-less and
data-driven](https://vimeo.com/861600197) - every function, except those in
[`virtuoso.ui.main`](./src/virtuoso/ui/main.cljs), is pure.

### `virtuoso.core`

This namespace defines the Powerpack app, and is used to boot the "backend"
(which only consists of static pages). There really isn't much to this part.
Pages can boot a dynamic frontend by adding the class `replicant-root` to an
element, and setting the `data-view` attribute to a string identifier.

### `virtuoso.ui.main`

This namespace contains most of the machinery of the frontend. At the top you
will find a definition of all the "features" implemented, each using the same
identifier from the above `data-view` attribute as an id. Each feature defines a
few functions:

- `:feature/prepare` - When the app state changes, this function is passed the
  Datascript database to prepare UI data.
- `:feature/render` - This function will be called with what's returned by
  `:feature/prepare`.
- `:feature/get-boot-actions` - An optional function that can return actions to
  execute when the feature is first loaded. Actions are described below.
- `:feature/prepare-modal` - Like `:feature/prepare`, but specifically for modal
  data.
- `:feature/render-modal` - Like `:feature/render`, but specifically for
  rendering modals.
- `:feature/get-keypress-actions` - A function that is called with the database
  and a map of `{:key}`, and can return actions to perform for key-presses. This
  is how the app implements keyboard shortcuts.

### The render loop

All the app state lives in a Datascript database. Whenever it changes, a render
is triggered. Each change causes all mounted features to render (though in
practice, each page only has a single feature at this point).

Features are rendered in two distinct layers: the app layer, and the modal
layer. The idea behind this is that there isn't much of a point in rendering the
underlying page when focus is on a modal.

Every change causes a feature's `:feature/prepare` function to be called with
the current Datascript database. The resulting data structure is then passed to
`:feature/render`, which is expected to return hiccup. The resulting hiccup is
rendered to the DOM with Replicant.

When there is a modal (as determined by
`virtuoso.elements.modal/get-current-modal`, and the current feature has a
`:feature/prepare-modal` function, the rendering focus switches to the modal. If
the underlying app has been rendered at least once, it will not be rendered
again until the modal goes away. If the app hasn't yet been rendered, it is
rendered just once before being paused. The modal will be rendered with
`:feature/prepare-modal` and `:feature/render-modal` (or
`virtuoso.elements.modal/render` by default) for every change for as long as
there is a current modal.

The database, and the rendering loop is initiated by `virtuoso.ui.main/boot`.

### Actions

The frontend uses keyword dispatch of actions (e.g. event handlers, key presses,
boot actions, etc). An action is a tuple like so:

```clj
[action-kind & args]
```

E.g.:

```clj
[:action/transact [{:music/tempo 60}]]
```

An action is dispatched with `virtuoso.ui.actions/perform-action`. This is a
multi-method that dispatches on the action kind keyword, and is expected to
return a sequence of effects. In other words, actions are pure functions. This
makes it convenient to implement page/feature-specific actions without having to
write side-effecting functions. Side-effects are handled by "effects", and there
are a limited few effects ever necessary to implement.

Actions may use a placeholder for event-time data, with these keywords:

- `:event/key` the key pressed (in key events)
- `:event/target-value` - the value of the event target element (e.g. to get the
  value of an edited input field)
- `:event/target-value-num` - the event target value as a number
- `:event/target-value-kw` - the event target value as a keyword

These can be used like so:

```clj
[:action/transact [{:music/tempo :event/target-value-num}]]
```

Which will store an entity with `:music/tempo` set to the value of the input
field it triggers on.

#### A note on multi-methods

The action system uses multi-methods to add new actions, and to implement
side-effects. Normally I'm cautious with using multi-methods in application
code, so allow me to explain this choice.

I wanted all the effects to be implemented in `virtuoso.ui.main`, to keep all
the side-effecting code in one place. I also wanted to be able to work with
actions in tests, etc, without loading the main namespace. To satisfy these
requirements, there is a `virtuoso.ui.actions` namespace that performs actions
and executes effects, but the side-effect implementations themselves are defined
in main for the app.

I wanted individual features to be able to register custom actions. This is the
main reason I separated actions and effects in the first place, so that you can
add custom actions with pure functions (by returning a mix of the existing
effects). I wanted feature-specific actions to be co-located with the feature
they're used by. In other words there was a need for some sort registration
mechanism, and I opted for a multi-method instead of building a home-grown
solution.

### Working with the code

My setup assumes Emacs. Launch a REPL with launchpad:

```sh
make launch
```

Then run `cider-connect-sibling-cljs` in Emacs to add a ClojureScript sibling
REPL.

The UI is built with [Tailwind](https://tailwindcss.com/) and
[DaisyUI](https://daisyui.com/). Start the build process with:

```sh
make tailwind
```

Now start the Powerpack app by evaluating `(dev/start)` from
[dev/virtuoso/dev.clj](./dev/virtuoso/dev.clj), and you should be off to the
races.

- The app runs on [http://localhost:4848/](http://localhost:4848/).
- Portfolio displays UI elements on [http://localhost:4847/](http://localhost:4847/).

## Tests

Run tests with

```sh
make test
```
