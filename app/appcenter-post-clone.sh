#!/usr/bin/env bash

commit=$(git show -s --format="%s %b")
echo "commit: $commit"

if [[ $commit == *"[skip ci]"* ]]; then
    curl -i -X PATCH -H "X-API-Token:$APPCENTER_API_TOKEN" -H "Content-Type: application/json" -d "{\"status\":\"cancelling\"}" https://appcenter.ms/api/v0.1/apps/qwq233/qn/builds/$APPCENTER_BUILD_ID
else
    echo "Continue building..."
fi