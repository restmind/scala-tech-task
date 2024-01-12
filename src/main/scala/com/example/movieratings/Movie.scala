package com.example.movieratings

import scala.util.Try

final case class Movie(id: Long, yearOfRelease: Option[Int], title: String)

object Movie {
  def apply(values: Array[String]): Movie = {
    if (values.length < 3)
      throw new IllegalArgumentException(
        "Number of elements doesn't match expected size"
      )

    val (left, right) = values.splitAt(2)
    left match {
      case Array(id, year) =>
        Movie(id.toLong, Try(year.toInt).toOption, right.mkString(","))
    }
  }
}
