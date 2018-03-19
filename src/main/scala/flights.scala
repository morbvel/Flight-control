import java.io.{File, PrintWriter}

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.col

import scala.collection.mutable.ListBuffer

object flights {

  def main(args: Array[String]) {
    val spark = SparkSession.builder()
      .appName("Flights")
      .master("local")
      .getOrCreate()

    val small = new ListBuffer[List[String]]
    val big = new ListBuffer[List[String]]
    val aterrizados = new ListBuffer[String]

    val log = new PrintWriter(new File("src/main/resources/landings.txt" ))

    val aviones = lecturaInicial("src/main/resources/planes.csv", spark)

    aviones.show(false)
    aviones.as("a").orderBy(col("a.Passengers").desc).orderBy(col("a.Size").desc).collect().map(x => {
      val aux = List(x(0).toString, x(3).toString)
      x(2) match{
        case "SMALL" => small += aux
        case "BIG"  => big += aux
      }
    })


    var auxS = 0
    var auxB =0
    do{
      if ( (small(auxS)(1).toInt + small(auxS+1)(1).toInt) < big(auxB)(1).toInt){
        aterrizados += aterrizar(big(auxB)(0), log)
        auxB += 1
      }else{
        aterrizados += aterrizar(small(auxS)(0), log)
        aterrizados += aterrizar(small(auxS+1)(0), log)
        auxS += 2
      }

    }while(auxS<small.length)

    log.close

    println("-----------------------------------ATERRIZADOS---------------------------------------")
    aterrizados.map(x => println(x))

  }

  private def lecturaInicial(path: String, spark: SparkSession):DataFrame = {
    spark.read
      .format("com.databricks.spark.csv")
      .option("header", "true") // Use first line of all files as header
      .option("inferSchema", "true") // Automatically infer data types
      .load(path)
  }

  private def aterrizar(avion:String, log:PrintWriter):String = {
    log.write(avion+"\n")
    avion
  }

}