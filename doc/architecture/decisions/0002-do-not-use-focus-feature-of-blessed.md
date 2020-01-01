# 2. Do not use focus feature of blessed

Date: 2019-11-20

## Status

Accepted

## Context

Sometimes we want to move between the current focused input between
different blessed elements, e.g. for typing the search text. 

But the focus management feature of blessed is not well implemented
when used in react-blessed. E.g. when after a rendering the focused
attributed of A changed from `false` to `true` and focused attributed
of B changed from `true` to `false`, B would not automatically get
focused.

## Decision

Do not rely on the focus feature of blessed. Instead, just track the
focused element in the app state by ourselves.

## Consequences

More care would be needed to correctly implement focus-related
functionality.
