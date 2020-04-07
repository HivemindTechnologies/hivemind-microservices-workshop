scalaVersion := "2.13.1"
version := "0.1.0-SNAPSHOT"
organization := "com.hivemindtechnologies"
organizationName := "Hivemind Technologies AG"

Global / onChangedBuildSource := ReloadOnSourceChanges

val circeVersion = "0.12.3"
val catsVersion = "2.1.0"
val http4sVersion = "0.21.0-RC2"
val monocleVersion = "2.0.0"
val doobieVersion = "0.8.4"
val pureconfigVersion = "0.12.1"
val log4catsVersion = "1.0.0"
val monixVersion = "3.1.0"

lazy val root = (project in file("."))
  .settings(
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
    ),
    name := "sentiments",
    libraryDependencies ++= Seq(
      // Functional programming
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsVersion,
      "org.typelevel" %% "cats-tagless-macros" % "0.11",
      // Config
      "com.github.pureconfig" %% "pureconfig" % pureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % pureconfigVersion,
      // Logging
      "io.chrisdavenport" %% "log4cats-slf4j" % log4catsVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      // Json
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      // Http
      "com.softwaremill.sttp.client" %% "core" % "2.0.0-RC7",
      "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % "2.0.0-RC7",
// Streams
      "io.monix" %% "monix" % monixVersion,
      "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.1",
      // Testing
      "org.specs2" %% "specs2-core" % "4.8.2" % "test"
    ),
    javacOptions ++= Seq("-source", "11", "-target", "11"),
    scalacOptions := Seq(
      "-Ymacro-annotations",
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-explaintypes", // Explain type errors in more detail.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds", // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
      "-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
      "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
      "-Xlint:option-implicit", // Option.apply used implicit view.
      "-Xlint:package-object-classes", // Class or object defined in package object.
      "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
      "-Ywarn-dead-code", // Warn when dead code is identified.
      "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
      "-Ywarn-numeric-widen", // Warn when numerics are widened.
      "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals", // Warn if a local definition is unused.
      "-Ywarn-unused:params", // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates", // Warn if a private member is unused.
      "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
      "-Ybackend-parallelism",
      "8", // Enable paralellisation — change to desired number!
      "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
      "-Ycache-macro-class-loader:last-modified" // and macro definitions. This can lead to performance improvements.
    ),
    mainClass in assembly := Some("com.hivemindtechnologies.sentiments.Main"),
    assemblyOutputPath in assembly := file("target/sentiments.jar"),

    assemblyMergeStrategy in assembly := {
      case PathList(ps @ _*) if ps.last endsWith "io.netty.versions.properties" => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
