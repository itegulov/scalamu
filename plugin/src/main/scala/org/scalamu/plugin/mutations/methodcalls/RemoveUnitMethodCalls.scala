package org.scalamu.plugin.mutations.methodcalls

import org.scalamu.plugin._

import scala.tools.nsc.Global

/**
 * Mutation, that removes calls to methods with [[Unit]] return type.
 * e.g.
 * {{{
 * val a = 123
 * println(a)
 * }}}
 * is replaced with
 * {{{
 * val a = 123
 * ()
 * }}}
 */
case object RemoveUnitMethodCalls extends Mutation { self =>
  override def mutatingTransformer(
    global: Global,
    config: MutationConfig
  ): MutatingTransformer = new MutatingTransformer(config)(global) {
    import global._

    override def mutation: Mutation = self

    override val transformer: Transformer = new Transformer {
      override protected val mutate: PartialFunction[Tree, Tree] = {
        case TreeWithType(
            tree @ Apply(qualifier, args),
            definitions.UnitTpe
            ) =>
          val mutatedArgs = args.map(super.transform)
          val mutant      = q"()"
          generateMutantReport(tree, mutant)
          guard(mutant, q"$qualifier(..$mutatedArgs)")
      }
    }
  }
}