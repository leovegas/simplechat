package models

object CodeGen extends App {
  slick.codegen.SourceCodeGenerator.run(
    "slick.jdbc.PostgresProfile",
    "org.postgresql.Driver",
    "jdbc:postgresql://mydatabase.chzoyzx7jwp5.us-west-2.rds.amazonaws.com:5432/postgres?user=leonid&password=dltimofeev1",
    "/home/leonid/Programs/SimpleWebChat/app",
    "models", None, None, true, false
  )
}