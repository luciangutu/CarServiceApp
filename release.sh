#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

PROJECT_NAME="CarServiceApp"
GRADLE_FILE="app/build.gradle.kts"
MODULE_PATH=":app"
BUNDLE_TASK="${MODULE_PATH}:bundleRelease"
BUNDLE_DIR="app/build/outputs/bundle/release"
BUNDLE_BASENAME="app-release.aab"
REMOTE="${REMOTE:-origin}"

die() {
    echo "ERROR: $*" >&2
    exit 1
}

require_command() {
    command -v "$1" >/dev/null 2>&1 || die "Required command not found: $1"
}

extract_version_code() {
    grep -oP 'versionCode\s*=\s*\K\d+' "$1" | tail -n 1 || true
}

extract_version_name() {
    grep -oP 'versionName\s*=\s*"\K[^"]+' "$1" | tail -n 1 || true
}

version_gt() {
    [[ "$(printf '%s\n%s\n' "$1" "$2" | sort -V | tail -n 1)" == "$1" && "$1" != "$2" ]]
}

require_branch_up_to_date() {
    local branch
    branch="$(git rev-parse --abbrev-ref HEAD)"

    git fetch "$REMOTE" --tags

    if git rev-parse --verify --quiet "$REMOTE/$branch" >/dev/null; then
        local local_head remote_head merge_base
        local_head="$(git rev-parse HEAD)"
        remote_head="$(git rev-parse "$REMOTE/$branch")"
        merge_base="$(git merge-base HEAD "$REMOTE/$branch")"

        if [[ "$local_head" == "$remote_head" ]]; then
            return
        elif [[ "$local_head" == "$merge_base" ]]; then
            die "Local branch '$branch' is behind '$REMOTE/$branch'. Pull first."
        elif [[ "$remote_head" == "$merge_base" ]]; then
            echo "Local branch '$branch' is ahead of '$REMOTE/$branch'; it will be pushed."
        else
            die "Local branch '$branch' and '$REMOTE/$branch' have diverged."
        fi
    fi
}

commit_release_changes() {
    if [[ -z "$(git status --porcelain)" ]]; then
        echo "No local changes to commit."
        return
    fi

    echo "Committing local release changes..."
    git add .
    git commit -m "Release $CURRENT_VERSION_NAME"
}

require_command git
require_command gh

[[ -f "$GRADLE_FILE" ]] || die "Could not find $GRADLE_FILE"
[[ -x "./gradlew" ]] || die "./gradlew is missing or is not executable"

CURRENT_VERSION_CODE="$(extract_version_code "$GRADLE_FILE")"
CURRENT_VERSION_NAME="$(extract_version_name "$GRADLE_FILE")"

[[ -n "$CURRENT_VERSION_CODE" ]] || die "Could not parse versionCode from $GRADLE_FILE"
[[ -n "$CURRENT_VERSION_NAME" ]] || die "Could not parse versionName from $GRADLE_FILE"

VERSION_CODE_COUNT="$(grep -cP 'versionCode\s*=' "$GRADLE_FILE" || true)"
VERSION_NAME_COUNT="$(grep -cP 'versionName\s*=' "$GRADLE_FILE" || true)"
if [[ "$VERSION_CODE_COUNT" -gt 1 || "$VERSION_NAME_COUNT" -gt 1 ]]; then
    echo "WARNING: Multiple version declarations found in $GRADLE_FILE; using the last one."
fi

TAG="v$CURRENT_VERSION_NAME"
AAB_FILE="$BUNDLE_DIR/${PROJECT_NAME}-${CURRENT_VERSION_NAME}.aab"

echo "$PROJECT_NAME release"
echo "versionCode: $CURRENT_VERSION_CODE"
echo "versionName: $CURRENT_VERSION_NAME"
echo "tag: $TAG"
echo

require_branch_up_to_date

if git rev-parse "$TAG" >/dev/null 2>&1; then
    die "Tag already exists locally: $TAG"
fi

if git ls-remote --exit-code --tags "$REMOTE" "$TAG" >/dev/null 2>&1; then
    die "Tag already exists on $REMOTE: $TAG"
fi

LATEST_TAG="$(git tag --sort=-v:refname | head -n 1 || true)"

if [[ -n "$LATEST_TAG" ]]; then
    TAG_VERSION="${LATEST_TAG#v}"

    echo "Latest tag: $LATEST_TAG"

    if ! version_gt "$CURRENT_VERSION_NAME" "$TAG_VERSION"; then
        die "versionName ($CURRENT_VERSION_NAME) must be higher than latest tag ($LATEST_TAG)."
    fi

    TAGGED_GRADLE_FILE="$(mktemp)"
    if git show "$LATEST_TAG:$GRADLE_FILE" > "$TAGGED_GRADLE_FILE" 2>/dev/null; then
        TAGGED_VERSION_CODE="$(extract_version_code "$TAGGED_GRADLE_FILE")"
    else
        TAGGED_VERSION_CODE=""
    fi
    rm -f "$TAGGED_GRADLE_FILE"

    if [[ -n "$TAGGED_VERSION_CODE" ]]; then
        if [[ "$CURRENT_VERSION_CODE" -le "$TAGGED_VERSION_CODE" ]]; then
            die "versionCode ($CURRENT_VERSION_CODE) must be higher than tagged versionCode ($TAGGED_VERSION_CODE)."
        fi
    fi
else
    echo "No previous tags found; creating first release."
fi

echo ""
echo "Building release AAB..."
./gradlew "$BUNDLE_TASK"

SOURCE_AAB="$BUNDLE_DIR/$BUNDLE_BASENAME"
[[ -f "$SOURCE_AAB" ]] || die "Expected bundle not found: $SOURCE_AAB"

cp "$SOURCE_AAB" "$AAB_FILE"
echo "Built: $AAB_FILE"

echo ""
commit_release_changes

echo ""
echo "Creating and pushing git tag..."
git tag -a "$TAG" -m "Release $CURRENT_VERSION_NAME"
git push "$REMOTE" HEAD
git push "$REMOTE" "$TAG"

echo ""
echo "Creating GitHub release and uploading AAB..."

gh release create "$TAG" \
    "$AAB_FILE" \
    --title "$PROJECT_NAME $CURRENT_VERSION_NAME" \
    --generate-notes

echo ""
echo "Done: GitHub release $TAG created with $AAB_FILE uploaded."
