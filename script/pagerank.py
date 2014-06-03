#!/Users/kyoungrok/Library/Enthought/Canopy_64bit/User/bin/python

from org.apache.pig.scripting import *
Pig.registerJar('libs/jython.jar')

P = Pig.compile("""
  previous_pagerank = load '$docs_in' as (page_id:int, outlinks_count:int, inlinks:{link:(link_id:int)}, pagerank:float);
  store previous_pagerank into '$docs_out';
""")

params = { 'd': '0.85', 'docs_in': 'graph' }
K = 5

for i in range(K):
    out = "output/pagerank_" + str(i + 1)
    params["docs_out"] = out
    Pig.fs("rmr " + out)
    bound = P.bind(params)
    stats = bound.runSingle()

    if not stats.isSuccessful():
        raise 'failed'
    
    params["docs_in"] = out
