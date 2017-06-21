package org.scalamu.testutil.fixtures

import java.nio.file.{Path, Paths}

import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.plugin.{Mutation, ScalamuPluginConfig}
import org.scalamu.testapi.TestingFramework
import org.scalatest.TestSuite

import scala.util.matching.Regex

trait ScalamuConfigFixture extends TestSuite {
  def reportDir: Path                               = Paths.get(".")
  def sourceDirs: Set[Path]                         = Set.empty
  def testClassDirs: Set[Path]                      = Set.empty
  def classPath: Set[Path]                          = Set.empty
  def scalaPath: String                             = ""
  def jvmArgs: Seq[String]                          = Seq.empty
  def mutations: Seq[Mutation]                      = ScalamuPluginConfig.allMutations
  def excludeSources: Seq[Regex]                    = Seq.empty
  def excludeTestsClasses: Seq[Regex]               = Seq.empty
  def testingOptions: Map[TestingFramework, String] = Map.empty
  def timeoutFactor: Double                         = 1.5
  def timeoutConst: Long                            = 2000
  def threads: Int                                  = Runtime.getRuntime.availableProcessors()
  def verbose: Boolean                              = false

  def withConfig(code: ScalamuConfig => Any): Any =
    code(
      ScalamuConfig(
        reportDir,
        sourceDirs,
        testClassDirs,
        classPath,
        scalaPath,
        jvmArgs,
        mutations,
        excludeSources,
        excludeTestsClasses,
        testingOptions,
        timeoutFactor,
        timeoutConst,
        threads,
        verbose
      )
    )
}
