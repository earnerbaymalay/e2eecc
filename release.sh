#!/usr/bin/env bash
# release.sh — Tag a version to trigger a GitHub release with APK
# Usage: ./release.sh 1.0.0

set -e

VERSION="${1:?Usage: ./release.sh <version>}"

echo "Creating release tag v$VERSION..."

git tag -a "v$VERSION" -m "Release v$VERSION"
git push origin "v$VERSION"

echo ""
echo "Tag v$VERSION pushed."
echo "GitHub Actions will now build the APK and create a release."
echo "Check: https://github.com/earnerbaymalay/e2eecc/actions"
echo "Release will appear at: https://github.com/earnerbaymalay/e2eecc/releases"
