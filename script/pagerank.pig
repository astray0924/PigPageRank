--pagerank.pig
register libs/jsoup-1.7.3.jar;
register libs/myudfs.jar;

page = load '../sample-data/enwiki-20121101-pages-articles01.xml-by-line.bz2' as (html: chararray);

-- Parsing
parse = FOREACH page GENERATE myudfs.ExtractPageInfo(html) as (info:tuple(id:int,title:chararray,outlinks:chararray,outlinks_count:int));
page_info = FILTER parse BY (info.title is not null and info.outlinks_count != 0);

-- id-title, title-id map
id_title = foreach page_info generate info.id, info.title;
title_id = foreach page_info generate info.title, info.id;
--store id_title into 'output/id_title.gz';
--store title_id into 'output/title_id.gz';

-- InDegree & OutDegree
plt = foreach page_info generate info.id, flatten(TOKENIZE(info.outlinks, '|')) as outlink;
pli_temp = join plt by outlink, title_id by title using 'replicated';
pli = foreach pli_temp generate plt::id as page_id, title_id::id as link_id;

outdegree_temp = group pli by page_id;
outdegree = foreach outdegree_temp generate group as page_id, COUNT(pli.link_id) as outcount;
indegree_temp = group pli by link_id;
indegree = foreach indegree_temp generate group as page_id, pli.page_id as inlink_ids;

dump outdegree;

-- Build Graph
-- outdegree_count = foreach 
