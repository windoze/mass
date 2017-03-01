#!/bin/sh

HOST="localhost"

if [ "x$1" != "x" ]; then
    HOST="$1"
fi

curl -k -XPOST -H 'Content-Type:application/json' -H 'api-key:123' "https://$HOST:443/indexes/macappsv1/docs/suggest?api-version=123" -d '{
    "search":"clea",
    "fuzzy":true,
    "top":5,
    "suggesterName":"spzaappsuggest",
    "searchFields":"Title, ShortDescription",
    "select":"ApplicationId,ShortDescription,Title,SmallIconUri,Publisher"
}'

curl -k -XPOST -H 'Content-Type:application/json' -H 'api-key:123' "https://$HOST:443/indexes/macappsv1/docs/search?api-version=123" -d '{
    "search":"lin* OR lin~ OR lin",
    "top":"7000",
    "orderby":"ApplicationId",
    "select":"ApplicationId",
    "queryType":"full"
}'
