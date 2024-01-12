name := "ai-kyiv-interview-task"

val commonsCsvVersion = "1.10.0"
val slf4jVersion = "2.0.5"
val logbackVersion = "1.4.12"
val typesafeConfigVersion = "1.4.2"

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-csv" % commonsCsvVersion,
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.typesafe" % "config" % typesafeConfigVersion
)
