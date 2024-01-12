package com.example.movieratings

final case class MovieRate(customerID: Long, rating: Double, date: String)

object MovieRate {
  def apply(values: Array[String]): MovieRate = values match {
    case Array(id, rating, date) => MovieRate(id.toLong, rating.toDouble, date)
    case _ =>
      throw new IllegalArgumentException(
        "Number of elements doesn't match expected size"
      )
  }
}
