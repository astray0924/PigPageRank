--pagerank.pig
REGISTER libs/jsoup-1.7.3.jar;
REGISTER libs/myudfs.jar;

page = load '../sample-data/enwiki-20121101-pages-articles01.xml-by-line.bz2' as (html: chararray);

parse = FOREACH page GENERATE myudfs.ExtractPageInfo(html) as (info:tuple(id:int,title:chararray,outlinks:chararray,outlinks_count:int));

page_info = FOREACH parse GENERATE info.id, info.title, info.outlinks, info.outlinks_count;

dump page_info;