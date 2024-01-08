import org.apache.spark.sql.functions.sum
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Row

/**
  * This object contains an implicit class that extends the DataFrame class. 
  * It provides two functionalities to deal with dataframes: numAlbumPopularity and greaterThan6kAlbums.
  * numAlbumPopularity computes the total album popularity of each label.
  * greaterThan6kAlbums filters the labels whose total album popularity is greater than or equal to 60000.
  * To use these functionalities, an object of RichDataFrame can be created using the DataFrame that needs to be transformed.
  * The new functionalities can then be called with the DataFrame dot notation.
  */
object AlbumsImplicitClassTest {

  implicit class ClassDataFrame(df: DataFrame) {

    /**
      * Computes the total album popularity of each label.
      * @return a DataFrame with two columns: label and summed_album_popularity.
      */
    def numAlbumPopularity : DataFrame = {
      val numAlbumPopularity = df
                .groupBy("label")
                .agg(
                  sum($"album_popularity").as("summed_album_popularity")
                )
      numAlbumPopularity    
    }

    /**
      * Filters the labels whose total album popularity is greater than or equal to 60000.
      * @return a DataFrame with all columns and rows that satisfy the condition.
      */
    def greaterThan6kAlbums: DataFrame = {
      val greaterThan6kAlbums = df.where($"summed_album_popularity" >= 60000.0)

      greaterThan6kAlbums
    }
  }
}

/**
  * This object contains a main method that loads data from a CSV file 
  * and uses a ClassDataFrame object to provide two functionalities to the loaded DataFrame: numAlbumPopularity and greaterThan6kAlbums.
  * numAlbumPopularity computes the total album popularity of each label.
  * greaterThan6kAlbums filters the labels whose total album popularity is greater than or equal to 60000.
  * The result of these two operations is printed to the console using the show method.
  */

import AlbumsImplicitClassTest._
object ImplicitClassMain {

  def main(args: Array[String]) = {

    println( args(0) + " Your main is running now..")

    /**
      * Loads the data for the Spotify albums.
      * @return a dataframe with the data from the CSV file.
      */

    def loadSpotifyAlbum: DataFrame = {
      val inputDF = spark
                .read
                .option("inferSchema", "true")
                .option("header", "true")
                .csv("/tmp/chanu/dq/spotify/spotify_albums_data_2023.csv")

      inputDF            
    }

    val result = loadSpotifyAlbum.numAlbumPopularity.greaterThan6kAlbums
    // This is soo cool.. :) 

    /**
      * Prints the resulting DataFrame to the console.
      */

    result.show
  }
}

// to execute - ImplicitClassMain.main(Array("Hey Chan!"))

/* Now, lets try to approach the same problem using `transform` function */

/**
* This object contains two DataFrame transformers that are designed to work in a chain to retrieve 
* album popularity from an input DataFrame
*/
object ChainedTransformationsTest {

    /**
    * This transformer function performs an aggregation on the input DataFrame by grouping by 'label' and
    * computing the sum of 'album_popularity' column and returns a new DataFrame with the result.
    *
    * @param inputDF The input DataFrame which should contain 'label' and 'album_popularity' columns
    * @return A new DataFrame containing the grouped and aggregated data with columns 'label' and
    *         'summed_album_popularity' representing the sum of 'album_popularity' grouped by 'label' 
    */
    def numAlbumPopularity(inputDF : DataFrame): DataFrame = {
        val df = inputDF
                  .groupBy("label")
                  .agg(
                    sum($"album_popularity").as("summed_album_popularity")
                  )
      df    
    }

    /**
    * This transformer function takes in DataFrame with columns 'label' and 'summed_album_popularity' and 
    * returns a new filtered DataFrame containing only those rows with 'summed_album_popularity' >= 60000.0
    *
    * @param inputDF The input DataFrame which should contain 'label' and 'summed_album_popularity' columns
    * @return A new filtered DataFrame containing only those rows with 'summed_album_popularity' >= 60000.0
    */
    def greaterThan6kAlbums(inputDF : DataFrame): DataFrame = {
      val df = inputDF.where($"summed_album_popularity" >= 60000.0)

      df
    }
}

/**
* Object ChainedTransformationMain runs the main method.
* It reads a CSV file containing data about Spotify album by calling the 'loadSpotifyAlbum' method. 
* It then applies two transformations, 'numAlbumPopularity' and 'greaterThan6kAlbums' in that order on the input data.
* Finally, it displays the resulting DataFrame. 
**/
import ChainedTransformationsTest._

object ChainedTransformationMain {
  def main(args: Array[String]) = {

    println( args(0) + " Your main is running now..")
    /**
    * loads a csv file containing attributes for Spotify albums
    **/
     def loadSpotifyAlbum: DataFrame = {
      val inputDF = spark
                .read
                .option("inferSchema", "true")
                .option("header", "true")
                .csv("/tmp/chanu/dq/spotify/spotify_albums_data_2023.csv")

    inputDF            
    }
    /**
    * applies two transformations to the input DataFrame. 
    * The first transformation aggregates the album_popularity within each label group.
    * The second transformation filters all labeled groups whose summed album_popularity is less than 60000
    **/
    val result = loadSpotifyAlbum
                  .transform(numAlbumPopularity)
                  .transform(greaterThan6kAlbums)
    /**
    * displays the resulting DataFrame.
    **/
    result.show
  }
}

// to execute this - ChainedTransformationMain.main(Array("Hey You!"))
