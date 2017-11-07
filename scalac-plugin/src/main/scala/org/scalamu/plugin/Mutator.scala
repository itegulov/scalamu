package org.scalamu.plugin

import com.typesafe.scalalogging.Logger
import org.scalamu.common.MutationId
import org.scalamu.plugin.util.{CompilerAccess, GlobalExtractors, TreeEnrichment, TreeSanitizer}

import scala.tools.nsc.Global

/**
 * Base trait for all mutations.
 */
trait Mutator {
  def mutatingTransformer(
    global: Global,
    config: ScalamuScalacConfig
  ): MutatingTransformer

  def description: String
}

abstract class MutatingTransformer(
  private val config: ScalamuScalacConfig
)(
  override implicit val global: Global
) extends CompilerAccess
    with GlobalExtractors
    with TreeSanitizer
    with TreeEnrichment {

  override def isMutationGuard(symbolName: String): Boolean =
    config.guard.isGuardSymbol(symbolName)

  trait Transformer extends global.Transformer {
    import global._

    private val fullName: Symbol => String = _.fullNameString

    private val excludedSymbols: Seq[String] = Seq(
      "scala.reflect.api.Exprs.Expr",
      "scala.reflect.api.Trees.Tree",
      "scala.reflect.macros.Universe.Tree"
    )

    private[this] var currentPackage: String = _

    private final def tryUpdatePackage(tree: Tree): Unit = tree match {
        case t: SymTree => currentPackage = t.symbol.enclosingPackage.fullName
        case _          =>
      }

    override final def transform(tree: Tree): Tree = tree match {
      case t if t.attachments.all.toString.contains("MacroExpansionAttachment")         => tree
      case t if Option(t.symbol).exists(s => !config.ignoreSymbols.accepts(s.fullName)) => tree
      case DefDef(mods, _, _, _, _, _) if mods.isSynthetic || mods.isMacro              => tree
      case macroImpl: DefDef if Option(macroImpl.tpt.symbol).exists(fullName andThen excludedSymbols.contains) =>
        tree
      case ClassDef(_, _, _, Template(parents, _, _))
          if parents.map(_.tpe.typeSymbol.fullName).contains("scala.reflect.api.TypeCreator") =>
        tree
      case t @ GuardedMutant(guard, mutated, alternative) =>
        // do not mutate code generated by another mutation
        treeCopy.If(t, guard, mutated, transform(alternative))
      case _ =>
        tryUpdatePackage(tree)
        
        if (config.targetOwners.accepts(currentOwner.fullName)) {
          (mutate andThen retype).applyOrElse(tree, continue)
        } else continue(tree)
    }

    protected final def generateMutantReport(tree: Tree, mutated: Tree): MutationId = {
      val oldTree     = showCode(TreePrettifier(tree))
      val mutatedTree = showCode(TreePrettifier(mutated))

      val info = MutationInfo(
        mutator,
        currentRunId,
        currentPackage,
        tree.pos,
        oldTree,
        mutatedTree
      )
      
      if (!tree.pos.isDefined) {
        Transformer.log.info(s"Mutant $info in tree $tree has undefined position.")
      }
      
      config.reporter.report(info)
      info.id
    }

    private final val continue: PartialFunction[Tree, Tree] = PartialFunction(super.transform)
    private final val retype: PartialFunction[Tree, Tree]   = PartialFunction(typer.typed)

    protected def mutate: PartialFunction[Tree, Tree]

    private def sanitizeTree(tree: Tree): Tree =
      if (config.sanitizeTrees) NestedMutantRemover(tree) else tree

    protected final def guard(mutated: Tree, alternative: Tree, id: MutationId): Tree =
      config.guard(global)(sanitizeTree(mutated), alternative, id)
  }

  object Transformer {
    private val log = Logger[Transformer]
  }

  protected def mutator: Mutator

  protected def transformer: Transformer

  def apply(tree: global.Tree): global.Tree = transformer.transform(tree)
}
