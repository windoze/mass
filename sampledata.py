#!/usr/bin/env python

### Import global Azure Marketplace data into ElasticSearch

import json
import sys
import urllib2

host = sys.argv[1]
input_file = sys.argv[2]
output_file = sys.argv[3]

posturl = "http://%s:9200/macappsv1/app" % host

j = json.load(open(input_file))

docs = [x for x in j if 'ShortDescription' in x]

keys = {'id', 'ApplicationId', 'Title', 'ShortDescription', 'SmallIconUri', 'Publisher', "doctype"}

o = open(output_file, "w")

def p(id, obj):
    req = urllib2.Request(posturl)
    opener = urllib2.build_opener(urllib2.HTTPCookieProcessor())
    response = opener.open(req, json.dumps(obj))
    print response.read()

for doc in docs:
    m = {}
    for k in doc:
        if k in keys:
            m[k] = doc[k]
    # Not sure why there is a "s_" prefix
    m['id'] = "s_" + m['id']
    p(doc['id'], m)
