#!/Users/kyoungrok/Library/Enthought/Canopy_64bit/User/bin/python

from org.apache.pig.scripting import *

P1 = Pig.compile("""
  previous_pagerank = load '$docs_in' as (page_id:int, 
                                            links:{link:(link_id:int)}, 
                                            pagerank:float);

  outbound_pagerank = foreach previous_pagerank generate 
                        ((float)pagerank / COUNT(links)) as pagerank,
                        flatten(links) as to_link;

  cogrpd            = cogroup outbound_pagerank by to_link,
                        previous_pagerank by page_id;

  p_groupd = group previous_pagerank all;
  N = foreach p_groupd generate COUNT(previous_pagerank) as count;

  new_pagerank      = foreach cogrpd generate group as page_id,
                        flatten(previous_pagerank.links) as links,
                        ((float) (1 - $d)/N.count + $d * SUM(outbound_pagerank.pagerank)) as pagerank:float;

  store new_pagerank into '$docs_out';

  --explain -out explain/pagerank_$iteration.dot -dot new_pagerank;

""")

P2 = Pig.compile("""
  pagerank_temp = load '$docs_in' as (page_id:int, links:{link:(link_id:int)}, pagerank:float);

  pagerank = foreach pagerank_temp generate page_id, pagerank;

  sorted = order pagerank by pagerank desc;

  top = limit sorted 300;
  id_title_temp = load 'output/id_title' as (page_id:int, title:chararray);
  id_title = distinct id_title_temp;
  top_title_temp = join top by page_id, id_title by page_id using 'replicated';
  top_title = foreach top_title_temp generate id_title::title as title, top::pagerank as pagerank;

  store top_title into '$docs_out';

  --explain -out explain/result.dot -dot top_title;
""")

# Calculate PageRank
params = { 'd': '0.85', 'docs_in': 'output/graph' }
K = 20
for i in range(K):
    out = "output/pagerank_" + str(i + 1)
    params["docs_out"] = out
    params["iteration"] = str(i + 1)
    # Pig.fs("rmr " + out)
    bound = P1.bind(params)
    stats = bound.runSingle()

    if not stats.isSuccessful():
        raise 'failed'
    
    params["docs_in"] = out

# Sort and Save Result
params = {'docs_in': 'output/pagerank_' + str(K), 'docs_out': 'output/result'}
bound = P2.bind(params)
stats = bound.runSingle()
if not stats.isSuccessful():
  raise 'failed'
