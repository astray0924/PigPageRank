--pagerank.pig
register libs/jsoup-1.7.3.jar;
register libs/myudfs.jar;

page = load '../sample-data/enwiki-20121101-pages-articles01.xml-by-line.bz2' as (html: chararray);

-- Parsing
parse = FOREACH page GENERATE myudfs.ExtractPageInfo(html) as (info:tuple(id:int,title:chararray,outlinks:chararray,outlinks_count:int));
page_info = FILTER parse BY (info.title is not null and info.outlinks_count != 0);

-- id-title, title-id map
id_title = foreach page_info generate (info.id, info.title);
title_id = foreach page_info generate (info.title, info.id);
store id_title into 'output/id_title';
store title_id into 'output/title_id';

-- links to ids
-- (id, link) 튜플을 여러개 생성하는 방안 고려.
