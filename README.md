# gRPC StreamObserver Contract Companion

Warning icon on a `.onNext(...)` or `.onError(...)` call on a gRPC
`StreamObserver` found, in textual order, AFTER an `.onCompleted()`
call on the same receiver, within the same method body — gRPC's own
`StreamObserver` javadoc states the contract explicitly: `onNext` "is
never called after onError(Throwable) or onCompleted() are called",
and "onCompleted may only be called once and if called it must be the
last method called". Calling either after completion is a real,
documented contract violation, not just a style nit.

## Why it exists

`responseObserver.onCompleted();` followed later in the same method by
`responseObserver.onNext(extra);` compiles fine and runs — but it
violates gRPC's own `StreamObserver` contract, which explicitly says
`onCompleted()` must be the last method called. The downstream
behavior after that point is undefined per the contract, not just
unconventional.

## Why built this way

- **100% static text/PSI analysis** — matches receiver/method names by
  simple text, so it works whether the real gRPC jar is on the
  classpath or not. Java and Kotlin.
- **Confirmed gap**: no bundled or third-party JetBrains Marketplace
  plugin covers this specific ordering check — the existing gRPC
  plugins found (gRPC, Universal gRPC Navigation, gRPC Federation) are
  navigation/documentation tools, not correctness checks on
  `StreamObserver` usage.

## v0.1 scope — stated honestly, not exhaustively

Only catches the straight-line, same-method-body case (both calls as
direct statements, textual order = execution order) — doesn't trace
calls split across branches/loops/helper methods, which would need
real control-flow analysis out of scope for v0.1. A call guarded by an
early `return` between the two (so they can never both execute) is a
possible false positive not traced here. Matches by simple method
name, not real type resolution.

## Usage

Open any Java/Kotlin file using gRPC's `StreamObserver`. A call after
`onCompleted()` on the same receiver, in the same method, shows a
warning icon.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
