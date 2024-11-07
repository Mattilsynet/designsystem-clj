#!/bin/bash

make refresh
version=$(clojure -T:build bump-version)

git add .
git commit -m "Release $version"
git tag -s v$version -m "Tag $version"
git push
git push -tags

make deploy
