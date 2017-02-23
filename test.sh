#!/bin/sh

HOST="localhost"

if [ "x$1" != "x" ]; then
    HOST="$1"
fi

curl -XPOST -H 'Content-Type:application/json' "http://$HOST:8080/indexes/macappsv1/docs/suggest?api-version=123" -d '{
    "search":"ass",
    "fuzzy":true,
    "top":5,
    "suggesterName":"spzaappsuggest",
    "searchFields":"Title, ShortDescription",
    "select":"ApplicationId,ShortDescription,Title,SmallIconUri,Publisher"
}'

curl -XPOST -H 'Content-Type:application/json' "http://$HOST:8080/indexes/macappsv1/docs/search?api-version=123" -d '{
    "search":"linux* OR linux~ OR linux",
    "top":"7000",
    "orderby":"ApplicationId",
    "select":"ApplicationId"
}'
