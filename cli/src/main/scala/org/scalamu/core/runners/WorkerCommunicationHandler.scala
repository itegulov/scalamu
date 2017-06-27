package org.scalamu.core.runners

import java.io.DataOutputStream
import java.net.ServerSocket

import cats.syntax.either._
import io.circe.{Decoder, Encoder}
import org.scalamu.core.CommunicationException

class WorkerCommunicationHandler[I: Encoder, O: Decoder](
  override val socket: ServerSocket,
  initialize: DataOutputStream => Unit
) extends SocketConnectionHandler[I, O] {

  override def handle(): Either[CommunicationException, CommunicationPipe[I, O]] = {
    val clientPipe = super.handle()
    for {
      pipe <- clientPipe
      _    <- Either.catchNonFatal(initialize(pipe.os)).leftMap(CommunicationException)
    } yield pipe
  }
}
