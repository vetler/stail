import sbt._
import Keys._

object Build extends Build
{
  lazy val root = Project("stail", file(".")) settings(sbtassembly.Plugin.assemblySettings: _*)

  override lazy val settings = super.settings ++ Seq(
    name := "stail",
    libraryDependencies ++= Seq("org.scala-lang" % "scala-compiler" % "2.9.1",
				"jline" % "jline" % "0.9.9"),
    scalaVersion := "2.9.1"
  )

}
