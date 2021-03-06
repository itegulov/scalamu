package org.scalamu.core.coverage

import cats.data.Validated._
import cats.data.ValidatedNel
import cats.instances.list._
import cats.syntax.traverse._
import org.scalamu.core.process.MeasuredSuite
import org.scalamu.core.testapi.{AbstractTestSuite, SuiteFailure, SuiteSuccess}

class StatementCoverageAnalyzer(
  reader: InvocationDataReader
) {
  def forSuites(
    suites: List[AbstractTestSuite]
  ): ValidatedNel[SuiteFailure, List[SuiteCoverage]] =
    suites.traverse(forSuite)

  def forSuite(suite: AbstractTestSuite): ValidatedNel[SuiteFailure, SuiteCoverage] = {
    val result = suite.execute() match {
      case SuiteSuccess(_, completionTime) =>
        val statements: Set[StatementId] = reader.invokedStatements().map(StatementId(_))(collection.breakOut)
        valid(SuiteCoverage(MeasuredSuite(suite, completionTime), statements))
      case sf: SuiteFailure =>
        invalidNel(sf)
    }

    reader.clearData()
    result
  }
}
