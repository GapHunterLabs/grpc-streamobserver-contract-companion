<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# gRPC StreamObserver Contract Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Warning icon on a StreamObserver onNext()/onError() call that
  appears textually after onCompleted() on the same receiver, in the
  same method -- gRPC's own javadoc says onCompleted() must be the
  last method called.
- 100% static text/PSI analysis, Java and Kotlin, no network calls,
  no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/grpc-streamobserver-contract-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/grpc-streamobserver-contract-companion/commits/0.1.0
