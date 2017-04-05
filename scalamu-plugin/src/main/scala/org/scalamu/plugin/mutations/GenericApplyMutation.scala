package org.scalamu.plugin.mutations

import org.scalamu.plugin.{MutatingTransformer, Mutation, MutationGuard, MutationReporter}

import scala.tools.nsc.Global

/**
 * Base trait for mutations, which are applicable to `apply` method call trees.
 */
trait GenericApplyMutation extends Mutation { self: SupportedTypes =>
  protected def replaceWith(global: Global): global.Tree

  override def mutatingTransformer(
    global: Global,
    mutationReporter: MutationReporter,
    mutationGuard: MutationGuard
  ): MutatingTransformer = new MutatingTransformer(mutationReporter, mutationGuard)(global) {
    import global._

    override protected def mutation: Mutation = self

    override protected def transformer: Transformer = new Transformer {
      override protected def mutate: PartialFunction[Tree, Tree] = {
        case expr @ q"${TreeWithType(tree, tpe)}.apply[$targs](..$_)"
            if supportedTypes.exists(_ <:< tpe) =>
          val mutationResult = q"${replaceWith(global)}[$targs]"
          reportMutation(expr, mutationResult)
          guard(mutationResult, expr)
      }
    }
  }
}
