--pagerank.pig
register libs/jsoup-1.7.3.jar;
register libs/myudfs.jar;

page = load '../xml-wiki/' as html:chararray;

-- Parsing
parse = FOREACH page GENERATE myudfs.ExtractPageInfo(html) as (info:tuple(id:int,title:chararray,outlinks:chararray,outlinks_count:int));
page_info = FILTER parse BY (info.title is not null and info.outlinks_count != 0);

-- id-title, title-id map
id_title = foreach page_info generate info.id, info.title;
title_id = foreach page_info generate info.title, info.id;
store id_title into 'output/id_title';
-- store title_id into 'output/title_id';

-- The number of pages
page_ids = group id_title all;
page_count = foreach page_ids generate COUNT(id_title) as count;

-- InDegree & OutDegree
plt = foreach page_info generate info.id, flatten(TOKENIZE(info.outlinks, '|')) as outlink;
pli_temp = join plt by outlink, title_id by title using 'replicated';
pli = foreach pli_temp generate plt::id as page_id, title_id::id as link_id;
pli_distinct = DISTINCT pli;

outdegree_temp = group pli_distinct by page_id;
outdegree = foreach outdegree_temp generate group as page_id:int, pli_distinct.link_id as links;
--store outdegree into 'output/outdegree';

indegree_temp = group pli_distinct by link_id;
indegree = foreach indegree_temp generate group as page_id: int, pli_distinct.page_id as links;
--store indegree into 'output/indegree';

-- Build Graph
node_temp = join outdegree by page_id, indegree by page_id;
graph = foreach node_temp generate outdegree::page_id as page_id, outdegree::links as links, ((float) 1 / page_count.count) as score:float;
store graph into 'output/graph';
