<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# gRPC StreamObserver Contract Companion Changelog

## [Unreleased]

### Changed

- The rating prompt's local counter keeps one-way fingerprints of findings
  instead of their file paths, and deletes the list that earlier versions
  kept.
- `PRIVACY.md` describes the values the plugin keeps in the IDE's local
  settings.

## [0.1.1]

### Fixed

- Review/star CTA now links to this plugin's own Marketplace
  reviews page instead of the vendor's generic plugin list.

## [0.1.0]

### Added

- Warning icon on a StreamObserver onNext()/onError() call that
  appears textually after onCompleted() on the same receiver, in the
  same method -- gRPC's own javadoc says onCompleted() must be the
  last method called.
- 100% static text/PSI analysis, Java and Kotlin, no network calls,
  no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/grpc-streamobserver-contract-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/grpc-streamobserver-contract-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/grpc-streamobserver-contract-companion/commits/0.1.0
