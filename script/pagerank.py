#!/Users/kyoungrok/Library/Enthought/Canopy_64bit/User/bin/python

from org.apache.pig.scripting import *

P = Pig.compile("""
  previous_pagerank = load '$docs_in' as (page_id:int, 
                                            links:{link:(link_id:int)}, 
                                            pagerank:float);

  outbound_pagerank = foreach previous_pagerank generate
                      pagerank / COUNT(links) as pagerank,
                      flatten(links) as to_link;
  
  cogrpd            = cogroup outbound_pagerank by to_link,
                        previous_pagerank by page_id;
  
  new_pagerank      = foreach cogrpd generate group as page_id,
                        (1 - $d) + $d * SUM (outbound_pagerank.pagerank)
                        as pagerank,
                        flatten(previous_pagerank.links) as links,
                        flatten(previous_pagerank.pagerank) AS previous_pagerank;
  
  store new_pagerank into '$docs_out';

""")

params = { 'd': '0.85', 'docs_in': 'output/graph' }
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
