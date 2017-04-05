package org.scalamu.plugin

import org.scalamu.plugin.mutations.{CompilerAccess, GlobalExtractors, TypeEnrichment}

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.{Plugin, PluginComponent}
import scala.tools.nsc.transform.{Transform, TypingTransformers}

class ScalamuPlugin(
  val global: Global,
  val mutations: Seq[Mutation],
  val config: MutationConfig
) extends Plugin { plugin =>

  override val name: String                      = "scalamu"
  override val components: List[PluginComponent] = List(MutationComponent)
  override val description: String               = "scalamu mutation testing compiler plugin"

  case object MutationComponent
      extends PluginComponent
      with TypingTransformers
      with Transform
      with CompilerAccess
      with TypeEnrichment
      with GlobalExtractors
      with MutationVerifier {

    override val global: Global           = plugin.global
    override val phaseName: String        = "mutating-transform"
    override val runsAfter: List[String]  = List("typer")
    override val runsBefore: List[String] = List("patmat")

    import global._

    override def newPhase(prev: scala.tools.nsc.Phase): Phase = new Phase(prev) {
      override def run(): Unit = super.run()
    }

    override protected def newTransformer(unit: CompilationUnit): Transformer =
      new Transformer(unit)

    class Transformer(unit: CompilationUnit) extends TypingTransformer(unit) {
      override def transform(tree: Tree): Tree = {

        def applyTransformations(tree: Tree, mutations: Seq[MutatingTransformer]): Tree =
          mutations match {
            case Nil => tree
            case tr :: rest =>
              val intermediate = tr(tree.asInstanceOf[tr.global.Tree])
              applyTransformations(intermediate.asInstanceOf[Tree], rest)
          }
        println(config)
        val mutatedUnit = applyTransformations(
          tree,
          mutations.map(_.mutatingTransformer(global, config.reporter, config.guard))
        )
        if (config.verifyTrees) {
          val noNestedMutations = hasNoNestedMutations(mutatedUnit)
          if (!noNestedMutations) reporter.error(NoPosition, s"Unit $unit contains nested mutations")
        }
        mutatedUnit
      }
    }
  }
}
