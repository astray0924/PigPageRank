--describe.pig
divs = load 'NYSE_dividends' as (exchange:chararray, symbol:chararray,
date:chararray, dividends:float);
trimmed = foreach divs generate symbol, dividends;
grpd = group trimmed by symbol;
avgdiv = foreach grpd generate group, AVG(trimmed.dividends);
describe trimmed; describe grpd; describe avgdiv;
