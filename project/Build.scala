
package axle

import sbt._
import Keys._

object AxleBuild extends Build {

  val sharedSettings = Project.defaultSettings ++ Seq(

    organization := "org.pingel",

    version := "0.1-SNAPSHOT", // M8 was last milestone

    scalaVersion := "2.10.1",

    crossScalaVersions := Seq("2.10.1"),

    libraryDependencies += "org.specs2" %% "specs2" % "1.13" % "test",

    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:existentials",
      "-language:postfixOps"),

    initialCommands in console := """
import axle._
import axle.algebra._
import axle.stats._
import axle.quanta._
import axle.graph._
import axle.matrix._
import axle.ml._
//import axle.visualize._
import axle.ast._
import collection._
""",

    // http://www.scala-sbt.org/using_sonatype.html
    publishMavenStyle := true,

    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) 
        Some("snapshots" at nexus + "content/repositories/snapshots") 
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },

    publishArtifact in Test := false,

    pomIncludeRepository := { _ => false },

    pomExtra := (
  <url>http://axle-lang.org</url>
  <licenses>
    <license>
      <name>BSD-2-Clause</name>
      <url>http://www.opensource.org/licenses/BSD-2-Clause</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:adampingel/axle.git</url>
    <connection>scm:git:git@github.com:adampingel/axle.git</connection>
  </scm>
  <developers>
    <developer>
      <id>pingel</id>
      <name>Adam Pingel</name>
      <url>http://pingel.org</url>
    </developer>
  </developers>)

  )

  lazy val hadoopVersion = "1.1.2"
  lazy val jungVersion = "2.0.1"

  lazy val axleCore = Project(
    id = "axle-core",
    base = file("axle-core"),
    settings = sharedSettings
  ).settings(
    name := "axle-core",
    libraryDependencies ++= Seq(
      "net.sf.jung" % "jung-algorithms" % jungVersion,
      "net.sf.jung" % "jung-api" % jungVersion,
      "net.sf.jung" % "jung-graph-impl" % jungVersion,
      "net.sf.jung" % "jung-io" % jungVersion,
      "joda-time" % "joda-time" % "2.1",
      "org.joda" % "joda-convert" % "1.2",
      "org.jblas" % "jblas" % "1.2.3",
      "org.spire-math" %% "spire" % "0.3.0",
      "com.typesafe.akka" %% "akka-actor" % "2.2-M1"
    )
  )

  lazy val axleScalding = Project(
    id = "axle-scalding",
    base = file("axle-scalding"),
    settings = sharedSettings
  ).settings(
    name := "axle-scalding",
    libraryDependencies ++= Seq(
      "org.apache.hadoop" % "hadoop-core" % hadoopVersion,
      "com.twitter" %% "scalding-core" % "0.9.0-SNAPSHOT"
    )
  ).dependsOn(axleCore)

  lazy val axleLanguages = Project(
    id = "axle-languages",
    base = file("axle-languages"),
    settings = sharedSettings
  ).settings(
    name := "axle-languages",
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.1.3"
      //"net.liftweb" % "lift-json_2.9.0-1" % "2.4",
      //"net.liftweb" % "lift-common_2.9.0-1" % "2.4"
    )
  ).dependsOn(axleCore)

  lazy val axleHBase = Project(
    id = "axle-hbase",
    base = file("axle-hbase"),
    settings = sharedSettings
  ).settings(
    name := "axle-hbase",
    libraryDependencies ++= Seq(
      "org.apache.hadoop" % "hadoop-core" % hadoopVersion,
      "org.apache.hbase" % "hbase" % "0.94.5"
    )
  ).dependsOn(axleCore)

  lazy val axleVisualize = Project(
    id = "axle-visualize",
    base = file("axle-visualize"),
    settings = sharedSettings
  ).settings(
    name := "axle-visualize",
    libraryDependencies ++= Seq(
      "net.sf.jung" % "jung-visualization" % jungVersion
    )
  ).dependsOn(axleCore)

  lazy val axleAggregate = Project(
    id = "axle-aggregate",
    base = file("."),
    settings = sharedSettings
  ).settings(
    test := { },
    publish := { },
    publishLocal := { }
  ).aggregate(axleCore, axleVisualize, axleScalding, axleHBase, axleLanguages)

}