# stocktrends
## Purpose: 
You are a stock market trader working with ABC investment
group. As a stock market trader, you would like to show your clients the
current trends in the market and help them make the right investments.
Identifying trends is one of the key functions of moving averages.

This Prototype code takes a historical stock dataset partitioned by
weeks and finds the 3 day moving average of the closing prices.
Functions to find stocks that have a upward,downward or static trend
have been defined. A feature to find the health of stocks in the last 10
days has also been implemented.

##  Dependencies/Tools Used:

Operating System : Darwin 17.5.0 x86\_64 Language : Scala : 2.11.11
(SBT: 0.13.9 ) IDE : IntelliJ(spark core :2.1),Zeppelin,VS Code Dataset
used: https://www.asxhistoricaldata.com/ (Recent Data/10th August) (
This header less dataset contains an end-of-day (EOD) Comma separated
.txt file for each trading day that week. Schema of dataset has 7
columns corresponding to "Ticker, Date, Open, High, Low, Close, Volume"
The data has been “cleaned” to remove non-trading days, illiquid stocks
and non 3 character codes (Options, Warrants etc))

##  Compiling/Packaging/execution:

sbt compile/assembly/run

## Test:

sbt test




