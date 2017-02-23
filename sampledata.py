#!/usr/bin/env python

### Import global Azure Marketplace data into ElasticSearch

import json
import sys

host=sys.argv[1]
input_file=sys.argv[2]
output_file=sys.argv[3]

j=json.load(open(input_file))

docs=[x for x in j if 'ShortDescription' in x]

keys=set(['id', 'ApplicationId','Title','ShortDescription','SmallIconUri','Publisher',"doctype"])

o=open(output_file, "w")

def p(id, obj):
    s = '''curl -XPUT http://%s:9200/macappsv1/app/%s -d '%s'\n''' % (host, id, json.dumps(obj))
    o.write(s)

for doc in docs:
    m={}
    for k in doc:
        if k in keys:
            m[k]=doc[k]
    # Not sure why there is a "s_" prefix
    m['id']="s_"+m['id']
    p(doc['id'], m)

