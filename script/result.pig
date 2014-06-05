pagerank_temp = load 'output/pagerank_single' as (page_id:int, links:{link:(link_id:int)}, pagerank:float);

  pagerank = foreach pagerank_temp generate page_id, pagerank;

  sorted = order pagerank by pagerank desc;

  top = limit sorted 300;
  id_title_temp = load 'output/id_title' as (page_id:int, title:chararray);
  id_title = distinct id_title_temp;
  top_title_temp = join top by page_id, id_title by page_id using 'replicated';
  top_title = foreach top_title_temp generate id_title::title as title, top::pagerank as pagerank;

  store top_title into 'output/result';