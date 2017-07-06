package org.scalamu.core.process

import org.scalamu.common.MutantId
import org.scalamu.core.DetectionStatus

/**
 * Fully analysed mutant, with appropriate status.
 */
final case class MutationProcessResponse(id: MutantId, status: DetectionStatus)