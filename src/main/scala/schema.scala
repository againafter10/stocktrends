import org.apache.spark.sql.functions._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{SparkSession, functions}
import org.apache.spark.sql.functions.{avg, when, to_date }
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.lag
import org.apache.spark.sql.DataFrame


object schema {

//-------------------------------------------
// List all stocks with all computed columns
def stocksHealth(stocksDF: DataFrame ): Unit = {
        if (stocksDF.count() > 0) {

            stocksDF.show()
        }
        else
        {
            stocksDF.sqlContext.emptyDataFrame
        }

    }

//-------------------------------------------
// List stocks that have a +ve price change during the last 10 days
def stocksLastTenDaysUP(stocksDF: DataFrame,columns: List[String] ): Unit = {
    if (stocksDF.count() > 0) {
        println("last tendays upward trend for symbols\n")
        stocksDF.filter("trend == 'UP'").sort("symbol")
              .sort("symbol")
              .sort("date")
              .filter("date <= 'date_sub(current_date(),10'")
              .select(columns.map(col): _*)
              .show()


        }
    else
        {
            stocksDF.sqlContext.emptyDataFrame
        }

    }

//-------------------------------------------
// List stocks that have a -ve price change during the last 10 days
def stocksLastTenDaysDown(stocksDF: DataFrame ,columns: List[String]): Unit = {
    if (stocksDF.count() > 0) {
        println("last tendays upward trend for symbols\n")
        stocksDF.filter("trend == 'DOWN'").sort("symbol")
              .sort("symbol")
              .sort("date")
              .filter("date <= 'date_sub(current_date(),10'")
              .select(columns.map(col): _*)
              .show()

        }
    else
        {
            stocksDF.sqlContext.emptyDataFrame
        }

    }

//-------------------------------------------
// List stocks that have no price change during the last 10 days
def stocksLastTenDaysSame(stocksDF: DataFrame,columns: List[String] ): Unit = {

    if (stocksDF.count() > 0) {
            println("last tendays upward trend for symbols\n")
            var tempDF = stocksDF.filter("trend == 'SAME'")
              .sort("symbol")
              .sort("date")
              .filter("date <= 'date_sub(current_date(),10'")
              .select(columns.map(col): _*)
              .show()
        

        }
    else
        {
            stocksDF.sqlContext.emptyDataFrame
        }

    }

//-------------------------------------------
// Read the CSV data (partitioned on date)
def readCsv(path:String):DataFrame = {

    val session = SparkSession.builder().appName("StocksTrends").master("local[4]").getOrCreate()
    session.read.option("header","false").option("inferschemea","true").csv(path).toDF("symbol","date","openprice","highprice","closeprice","volume","adjcloseprice")

    }

//-------------------------------------------
// find a 3 day moving average & determine if it is a +ve,-ve or no price change
def findTrends(sampleDF:DataFrame):DataFrame = {
    //create the window of size 3 & calculate the simple moving average
    val frame_3 = Window.partitionBy("symbol").orderBy("date").rowsBetween(-1, 1)
    var trends = sampleDF.withColumn("moving_average",avg(sampleDF("adjcloseprice")).over(frame_3))
    trends.printSchema()
    val window = Window.partitionBy("symbol").orderBy("date")
    val laggingCol = lag(trends.col("moving_average"), 1).over(window)
    trends = trends.withColumn("laggingcol",lag(trends.col("moving_average"), 1).over(window))
    trends = trends.withColumn("priceDiff",(trends.col("moving_average") - trends.col("laggingCol")))
    
    //Calculate trend  +ve , -ve or no change
    val anlze = when(trends.col("priceDiff").isNull || trends.col("priceDiff").===(0), "SAME")
          .when(trends.col("priceDiff").>(0), "UP")
          .otherwise("DOWN")

    trends.withColumn("trend", anlze)
    }

//-------------------------------------------
// return dataframe schema length
def checkColumnLength(df : DataFrame):Int = {
        df.columns.length
    }
 
//-------------------------------------------
// List all stocks for the last ten days with all computed columns
def stocksReport(stocksDF: DataFrame ): DataFrame = {

    if (stocksDF.count() > 0) {
        println("last tendays upward trend for symbols\n")
        stocksDF.sort("symbol")
              .sort("date")
              .filter("date <= 'date_sub(current_date(),10'")  
        }
    else
        {
            stocksDF.sqlContext.emptyDataFrame
        }

    }

//--------------------------//
//-------  main()  --------//
//------------------------//
def main(args: Array[String]) {

    Logger.getLogger("org").setLevel(Level.ERROR)
    var stocks = readCsv("../stockanalysis/src/data/week20180810/*")

    // type the data (only 2 for POC,adjcloseprice and date)
    var stocks_typed = stocks.withColumn("adjcloseprice", stocks.col("adjcloseprice").cast("decimal(10,3)"))
    stocks_typed = stocks.withColumn("date",to_date(date_format(unix_timestamp(stocks.col("date"), "yyyyMMdd").cast("timestamp"), "yyyy-MM-dd")))

    //checkpoint 1
    val trends =findTrends(stocks_typed )
    stocksHealth(trends)

    // display trends on selected columns
    val displayColumns = List("symbol","date","adjcloseprice","trend")
    stocksLastTenDaysDown(trends,displayColumns)
    stocksLastTenDaysSame(trends,displayColumns)
    stocksLastTenDaysUP(trends,displayColumns)

    }

}
