package org.scalamu.idea.runner

import java.util

import com.intellij.ide.browsers.{BrowserFamily, WebBrowser, WebBrowserManager}
import org.scalamu.idea.runner.ui.RegexFilter

import scala.collection.JavaConverters._

object ScalamuDefaultSettings {
  val parallelism: Int                = 1
  val timeoutConst: Long              = 2000
  val timeoutFactor: Double           = 1.5
  val verboseLogging: Boolean         = false
  val openInBrowser: Boolean          = false
  val scalacParameters: String        = ""
  val vmParameters: String            = ""
  val browser: Option[WebBrowser]     = Option(WebBrowserManager.getInstance().getFirstBrowser(BrowserFamily.CHROME))
  val targetTests: Seq[RegexFilter]   = Seq.empty
  val targetSources: Seq[RegexFilter] = Seq.empty

  val activeMutators: Seq[String] = Seq(
    "ReplaceCaseWithWildcard",
    "ReplaceMathOperators",
    "ReplaceWithIdentityFunction",
    "InvertNegations",
    "AlwaysExecuteConditionals",
    "NeverExecuteConditionals",
    "ReplaceConditionalBoundaries",
    "NegateConditionals",
    "RemoveUnitMethodCalls",
    "ChangeRangeBoundary",
    "ReplaceLogicalOperators",
    "ReplaceWithNone",
    "ReplaceWithNil"
  )

  val ignoredSymbols: Seq[RegexFilter] = Seq(
    "scala.Predef.println",
    "com.typesafe.scalalogging.Logger.*",
    "org.slf4j.Logger.*"
  ).map(RegexFilter.apply)

  def getIgnoredSymbolsAsJava: util.List[RegexFilter] = ignoredSymbols.asJava
}