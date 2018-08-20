import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.apache.spark.sql.SparkSession
import org.apache.log4j.{Level, Logger}
import schema.stocksLastTenDaysDown
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

class schematest extends FunSuite  {
  var session: SparkSession = _
  session = SparkSession
    .builder()
    .appName("StocksTrends Test")
    .master("local[4]")
    .getOrCreate()

//-------------------------------------------
// Test 1: dataframe schema to be of 7 columns for read
  test("csv schema size should be 7 columns") {
    Logger.getLogger("org").setLevel(Level.ERROR)
    session = SparkSession
      .builder()
      .appName("StocksTrends Test")
      .master("local[4]")
      .getOrCreate()
    val rdd:RDD[(String,Long,Double,Double,Double,Double,Long)] = session.sparkContext.parallelize(Seq(("1AD",20180806,0.305,0.305,0.305,0.305,13562)))
    val testDF:DataFrame = session.createDataFrame(rdd)
    
    assert(schema.checkColumnLength(testDF) === 7)
    session.stop()
  }

//-------------------------------------------
// Test 2:dataframe schema not to be of 7 columns for display
  test("trends") {
    Logger.getLogger("org").setLevel(Level.ERROR)
    session = SparkSession
      .builder()
      .appName("StocksTrends Test")
      .master("local[4]")
      .getOrCreate()
    val rdd:RDD[(Int,Int,Int,Int)] = session.sparkContext.parallelize(Seq((1,2,4,6),(3,5,6,8)))
    val testDF:DataFrame = session.createDataFrame(rdd)

    assert(schema.checkColumnLength(testDF) !== 7)
    session.stop()
  }
}