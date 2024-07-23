MAKEFILE_DIR:=$(realpath $(dir $(abspath $(lastword $(MAKEFILE_LIST)))))
PRJECT_ROOT=${MAKEFILE_DIR}
SWIFT_VERSION=5.9

# 全部実行。通常これを使う
all: format lint-fix
# CI 用
ci: format-lint lint sourcery-lint localize-lint
buildtool:
	mint bootstrap
format: buildtool
	mint run swiftformat ${PRJECT_ROOT} --swiftversion ${SWIFT_VERSION}
format-lint: buildtool
	mint run swiftformat ${PRJECT_ROOT} --swiftversion ${SWIFT_VERSION} --lint
lint: buildtool
	mint run swiftlint $(PRJECT_ROOT) --strict
lint-fix: buildtool
	mint run swiftlint $(PRJECT_ROOT) --fix --quiet