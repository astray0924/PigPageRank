previous_pagerank = load 'output/graph' as (page_id:int, 
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
                        ((float) (1 - 0.85)/N.count + 0.85 * SUM(outbound_pagerank.pagerank)) as pagerank:float;

  store new_pagerank into 'output/pagerank_single';