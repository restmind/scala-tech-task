package com.example.movieratings

import com.example.movieratings.CsvUtils.readFromFileAsList
import org.slf4j.LoggerFactory

import java.io.File
import java.util.concurrent.Executors
import scala.concurrent.Await.result
import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{ExecutionContext, Future}
import com.typesafe.config.{Config, ConfigFactory}

object ReportGenerator {
  private val config: Config = ConfigFactory.load()

  implicit val context: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(config.getInt("app.execution_context.threads"))
  )
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      logger.error(
        "Need to indicate these three args <moviesDescriptionFile> <trainingDatasetDirectory> <reportOutputPath> to run this app"
      )
      System.exit(1)
    }

    val List(moviesFile, trainingDataDirectory, reportOutputPath) = args.toList

    val movies = readMovies(moviesFile)

    val ratedMovies = result(
      collectTrainingData(trainingDataDirectory, movies),
      Duration(config.getInt("app.execution_context.duration"), SECONDS)
    )

    logger.info(s"Writing report to $reportOutputPath")
    generateReport(reportOutputPath, ratedMovies)
  }

  private def collectTrainingData(
      directory: String,
      movies: List[Movie]
  ): Future[List[MovieStats]] = {
    Future
      .sequence(
        movies
          .foldLeft(List[Future[MovieStats]]())((acc, movie) => {
            movie.yearOfRelease match {
              case Some(year) =>
                acc :+ Future {
                  val ratingsList = readMoviesRates(directory, movie)
                  MovieStats(
                    movie.id,
                    year,
                    movie.title,
                    calculateAverageRating(ratingsList),
                    ratingsList.length
                  )
                }
              case None =>
                logger.warn(
                  s"There is missing yearOfRelease value for movieId = ${movie.id}. Skipping this movie."
                )
                acc
            }
          })
      )
      .map(_.filter(filterByYearAndNumberOfReviews))
      .map(_.sortBy(movie => (-movie.averageRating, movie.title)))
  }

  private def filterByYearAndNumberOfReviews(movie: MovieStats): Boolean = {
    movie.yearOfRelease >= 1970 && movie.yearOfRelease <= 1990 && movie.numberOfReviews > 1000
  }

  private def readMovies(moviesFile: String) = {
    readFromFileAsList(new File(moviesFile)).map(movie => Movie(movie.values()))
  }

  private def readMoviesRates(
      directory: String,
      movie: Movie
  ): List[MovieRate] = {
    CsvUtils
      .readFromFileAsList(new File(generateFilePath(directory, movie.id)))
      .drop(1)
      .map(record => MovieRate(record.values()))
  }

  private def calculateAverageRating(ratingsList: List[MovieRate]): Double =
    ratingsList.map(_.rating).sum / ratingsList.length

  private def generateFilePath(directory: String, movieId: Long): String =
    f"$directory/mv_$movieId%07d.txt"

  private def generateReport(
      outputPath: String,
      movieStats: List[MovieStats]
  ): Unit = {
    val records = movieStats.map(movie =>
      List(
        movie.title,
        movie.yearOfRelease,
        movie.averageRating,
        movie.numberOfReviews
      )
    )
    CsvUtils.writeToFile(records, new File(outputPath))
  }
}
