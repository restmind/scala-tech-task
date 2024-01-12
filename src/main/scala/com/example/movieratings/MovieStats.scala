package com.example.movieratings

final case class MovieStats(
    id: Long,
    yearOfRelease: Int,
    title: String,
    averageRating: Double,
    numberOfReviews: Int
)
