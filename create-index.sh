#!/usr/bin/env bash

HOST="localhost"

if [ "x$1" != "x" ]; then
    HOST="$1"
fi

curl -XDELETE "http://$HOST:9200/macappsv1"
curl -XPOST "http://$HOST:9200/macappsv1" -d '{
    "mappings" : {
        "_default_" : {
            "properties" : {
                "id" : {
                    "type" : "string",
                    "index" : "not_analyzed"
                },
                "doctype" : {
                    "type" : "string",
                    "index" : "not_analyzed"
                },
                "ApplicationId" : {
                    "type" : "string",
                    "index" : "not_analyzed"
                },
                "Title" : {
                    "type" : "string",
                    "analyzer": "jieba_index",
                    "search_analyzer": "jieba_search"
                },
                "ShortDescription" : {
                    "type" : "string",
                    "analyzer": "jieba_index",
                    "search_analyzer": "jieba_search"
                },
                "SmallIconUri" : {
                    "type" : "string",
                    "index" : "not_analyzed"
                },
                "Publisher" : {
                    "type" : "string",
                    "analyzer": "jieba_index",
                    "search_analyzer": "jieba_search"
                }
            }
        }
    },
    "settings": {
        "number_of_shards" : 5,
        "number_of_replicas" : 1
    }
}'