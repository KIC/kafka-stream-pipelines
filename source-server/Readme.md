

On windows you might want to install cURL using the installer: http://www.confusedbycode.com/curl/#downloads

curl "https://query1.finance.yahoo.com/v8/finance/chart/AAPL?interval=2m"
With:

AAPL substituted with your stock ticker
interval one of [1m, 2m, 5m, 15m, 30m, 60m, 90m, 1h, 1d, 5d, 1wk, 1mo, 3mo]
optional period1 query param with your epoch range start date e.g. period1=1510340760
optional period2 query param with your epoch range end date e.g. period2=1510663712