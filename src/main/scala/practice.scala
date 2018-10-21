
import org.apache.spark.sql.functions._
import dataframe.dataframe
import config.config
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{SparkSession, functions , DataFrame}
import org.apache.spark.sql.functions.{avg, when, to_date }
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.lag



object practice {


  def main(args: Array[String]) {

    Logger.getLogger("org").setLevel(Level.ERROR)

    //var dataobj = new config()
    //var stockobj = new dataframe()

    val cwd = System.getProperty("user.dir")

    val path = cwd + "/data/week20180810/*"
    println(path)

    //stockobj.readCsv(dataobj.dataPath()).show()
    //val stocks = stockobj.readCsv(dataobj.dataPath())
    val session = SparkSession.builder().appName("practice")
      .master("local[4]")
      .getOrCreate()

    val data = session.read.option("header", "false")
      .option("inferschemea", false)
      .csv(path)
      .toDF("symbol", "date", "openprice", "highprice", "closeprice", "volume", "adjcloseprice")

    data.printSchema()

    // type the data (only 2 for POC,adjcloseprice and date)

    var data_typed = data.withColumn("adjcloseprice", data.col("adjcloseprice").cast("decimal(10,3)"))
    data_typed = data_typed.withColumn("date", to_date(date_format(unix_timestamp(data.col("date"), "yyyyMMdd").cast("timestamp"), "yyyy-MM-dd")))
    data_typed.printSchema()


    //find trends

    //create the window of size 3
    val frame_4 = Window.partitionBy("symbol").orderBy("date").rowsBetween(-2, 1)
    val trends = data_typed.withColumn("moving_average", avg(data_typed("adjcloseprice")).over(frame_4))
    trends.printSchema()
    trends.show()
   var joined = trends.join(data_typed, data_typed("symbol") === trends("symbol"))
       .select(trends("symbol"),trends("moving_average"),trends("adjcloseprice"))

    joined.show()
    joined = joined.withColumn("price_diff",joined("adjcloseprice") - joined("moving_average"))
    joined.printSchema()
    joined.show()
    val anlze = when(joined.col("price_diff").isNull || joined.col("price_diff").===(0), "SAME")
              .when(joined.col("price_diff").>(0), "UP")
              .otherwise("DOWN")



    joined = joined.withColumn("trend", anlze)
    joined.show()
    val buy = when(joined.col("trend") === "DOWN" ,"CHEAP TODAY")
      .when(joined.col("trend") === "UP","Expensive Today")
      .otherwise("Same Price")
    joined.withColumn("infaltion",buy).show()


  }
}


