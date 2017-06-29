package org.scalamu.core.runners

import java.io.DataOutputStream
import java.net.ServerSocket
import java.nio.file.Path

import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamu.common.MutantId
import org.scalamu.core.configuration.ScalamuConfig
import org.scalamu.core.process.{MutationAnalysisProcess, _}

class MutationAnalysisRunner(
  override val socket: ServerSocket,
  override val config: ScalamuConfig,
  override val compiledSourcesDir: Path,
  val workerId: Long
) extends Runner[(MutantId, Set[MeasuredSuite]), MutationAnalysisProcess.Result] {

  override protected def worker: Process[Result] = MutationAnalysisProcess

  override protected def sendConfigurationToWorker(os: DataOutputStream): Unit = {
    val configData = config.derive[MutationAnalysisProcessConfig].asJson.noSpaces
    os.writeUTF(configData)
    os.flush()
  }

  override protected def generateProcessArgs(
    worker: Process[_],
    executablePath: String,
    jvmArgs: Seq[String],
    runnerArgs: Seq[String]
  ): Seq[String] =
    super.generateProcessArgs(
      worker,
      executablePath,
      s"-Dworker.name=$workerId" +: jvmArgs,
      runnerArgs
    )
}
