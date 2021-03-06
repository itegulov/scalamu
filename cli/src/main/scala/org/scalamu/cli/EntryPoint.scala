package org.scalamu.cli

import java.io.{BufferedWriter, FileWriter}
import java.nio.file.{Files, Path}

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.common.MutationId
import org.scalamu.core.api.{SourceInfo, TestedMutant}
import org.scalamu.core.compilation.ScalamuGlobal
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.coverage.{CoverageConversionUtils, Statement}
import org.scalamu.core.detection.SourceFileFinder
import org.scalamu.core.runners._
import org.scalamu.core.api._
import org.scalamu.core.{LoggerConfiguration, die, coverage => cov}
import org.scalamu.plugin
import org.scalamu.plugin.MutationInfo
import org.scalamu.report.{HtmlReportWriter, ProjectSummaryFactory}
import org.scalamu.core.testapi.AbstractTestSuite
import org.scalamu.core.utils.FileSystemUtils._

import scala.collection.JavaConverters._
import scala.collection.breakOut
import scala.reflect.io.{Directory, PlainDirectory}

object EntryPoint {
  private def ensureDirExits(dir: Path): Path =
    if (!Files.exists(dir)) {
      Files.createDirectories(dir)
    } else dir

  def main(args: Array[String]): Unit = {
    val config = ScalamuConfig.parseConfig(args)
    LoggerConfiguration.configureLoggingForName("MAIN-APP", config.verbose)
    if (config.verbose) {
      scribe.info(s"Running Scalamu with config:\n ${config.asJson.spaces2}")
    } else {
      scribe.info("Verbose logging is disabled, if you encounter any problems you may want to turn it on.")
    }

    val reportDir       = ensureDirExits(config.reportDir)
    val instrumentation = new cov.MemoryReporter
    val reporter        = new plugin.MemoryReporter

    val outputPath = Files.createTempDirectory("mutated-classes")
    val outDir     = new PlainDirectory(new Directory(outputPath.toFile))

    val sourceFiles = new SourceFileFinder().findAll(config.sourceDirs)

    if (config.verbose) {
      tryWith(new BufferedWriter(new FileWriter(s"$reportDir/sourceFiles.log"))) { writer =>
        sourceFiles.foreach(sf => writer.write(sf.asJson.spaces2 + "\n"))
      }
    }
    
    {
      val global = ScalamuGlobal(config, instrumentation, reporter, outDir)
      val compilationStart = System.currentTimeMillis()
      global.compile(sourceFiles)

      if (global.reporter.hasErrors) {
        scribe.error("Failed to compile sources, exiting.")
        die(1)
      }
      val compilationTime = (System.currentTimeMillis() - compilationStart) / 1000
      scribe.info(s"Finished recompilation in $compilationTime seconds.")
      scribe.info(s"Total mutations generated: ${reporter.mutations.size}")
    }

    if (reporter.mutations.isEmpty) {
      scribe.error(
        "No mutations were generated. Make sure you have correctly applied exclusion filters."
      )
      die(InternalFailure)
    }

    if (config.verbose) {
      tryWith(Files.newBufferedWriter(reportDir / "mutations.log")) { writer =>
        reporter.mutations.foreach(m => writer.write(m.asJson.spaces2 + "\n"))
      }
    }

    if (config.recompileOnly) die(0)

    val coverageAnalyser = new CoverageAnalyser(config, outputPath)
    val coverage         = coverageAnalyser.analyse(instrumentation)

    scribe.info(s"Test suites examined: ${coverage.keySet.mkString("[\n\t", "\n\t", "\n]")}.")

    if (config.verbose) {
      tryWith(Files.newBufferedWriter(reportDir / "testSuites.log")) { writer =>
        coverage.foreach(suite => writer.write(suite.asJson.spaces2 + "\n"))
      }
      tryWith(Files.newBufferedWriter(reportDir / "statements.log")) { writer =>
        instrumentation.instrumentedStatements
          .values()
          .forEach(v => writer.write(v.asJson.spaces2 + "\n"))
      }
    }

    val inverseCoverage = CoverageConversionUtils.statementCoverageToInverseMutationCoverage(
      coverage,
      reporter.mutations
    )

    if (config.verbose) {
      tryWith(Files.newBufferedWriter(reportDir / "inverseCoverage.log")) { writer =>
        inverseCoverage.foreach(mCov => writer.write(mCov.asJson.spaces2 + "\n"))
      }
    }

    val mutationsById: Map[MutationId, MutationInfo] = reporter.mutations.map(m => m.id -> m)(breakOut)

    val analyser      = new MutationAnalyser(config, outputPath)
    val testedMutants = analyser.analyse(inverseCoverage, mutationsById)

    if (config.verbose) {
      tryWith(Files.newBufferedWriter(reportDir / "results.log")) { writer =>
        testedMutants.foreach(mutant => writer.write(mutant.asJson.spaces2 + "\n"))
      }
    }

    val invoked: Set[Statement] = coverage.valuesIterator.flatten.toSet

    val inverseFileCoverage =
      CoverageConversionUtils.inverseMutationCoverageToInverseFileCoverage(inverseCoverage, mutationsById)

    generateReport(
      reportDir,
      sourceFiles.toSet,
      testedMutants,
      instrumentation.instrumentedStatements.values().asScala.toSet,
      invoked,
      inverseFileCoverage,
      config
    )
    scribe.info(s"Mutation analysis is finished. Report was written to $reportDir.")
  }

  def generateReport(
    targetDir: Path,
    sourceFiles: Set[SourceInfo],
    testedMutants: Set[TestedMutant],
    statements: Set[Statement],
    invokedStatements: Set[Statement],
    inverseFileCoverage: collection.Map[String, collection.Set[AbstractTestSuite]],
    config: ScalamuConfig
  ): Unit = {
    val summary = ProjectSummaryFactory(
      statements,
      invokedStatements,
      testedMutants,
      sourceFiles,
      inverseFileCoverage
    )
    HtmlReportWriter.generateFromProjectSummary(summary, config, targetDir)
  }
}
