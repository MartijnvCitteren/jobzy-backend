#!/usr/bin/env bash
# Downloads a release asset, verifies its SHA-256 checksum and installs the binary on the PATH of later steps.
# Usage: install-tool.sh <binary-name> <url> <sha256>
# .tar.gz assets are extracted (the archive must contain <binary-name>); any other asset is the binary itself.
set -euo pipefail

name=$1
url=$2
sha256=$3

bin="${RUNNER_TEMP:?}/bin"
mkdir -p "$bin"
tmp=$(mktemp -d)

curl -sSfL -o "$tmp/asset" "$url"
echo "$sha256  $tmp/asset" | sha256sum --check --strict -

case "$url" in
  *.tar.gz)
    tar -xzf "$tmp/asset" -C "$tmp" "$name"
    mv "$tmp/$name" "$bin/$name"
    ;;
  *) mv "$tmp/asset" "$bin/$name" ;;
esac

chmod +x "$bin/$name"
echo "$bin" >>"$GITHUB_PATH"
