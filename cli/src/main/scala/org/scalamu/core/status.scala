package org.scalamu.core

/**
 * Represents the status of a single mutant, after mutation analysis is completed.
 *
 * @param killed was the mutant killed
 */
sealed abstract class DetectionStatus(val killed: Boolean)

/**
 * Base class for statuses related to runtime failures.
 */
sealed abstract class RunnerFailure extends DetectionStatus(false)

object RunnerFailure {
  case object TimedOut       extends RunnerFailure
  case object OutOfMemory    extends RunnerFailure
  case object RuntimeFailure extends RunnerFailure
}

object DetectionStatus {
  case object Killed     extends DetectionStatus(true)
  case object Alive      extends DetectionStatus(false)
  case object NotCovered extends DetectionStatus(false)
}