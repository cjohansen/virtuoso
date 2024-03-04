#!/bin/bash

set -xe

dist="$(dirname $0)/target/site"

if [ ! -d "$dist" ]; then
  echo "$dist does not exist, run `make $dist` first"
  exit 1
fi

bucket="s3://virtuoso.tools/"

if [ -z "$distribution_id" ]; then
  distribution_id="E2TV9YDUC066"
fi

cd $dist

# Sync over bundles, cacheable for a year
aws s3 sync . $bucket --cache-control max-age=31536000,public,immutable --exclude "*" --metadata-directive REPLACE --include "bundles/*"

# Sync pages, do not cache
aws s3 sync . $bucket --cache-control "no-cache,must-revalidate" --exclude "bundles/*"

# Delete older bundles etc
aws s3 sync . $bucket --delete
