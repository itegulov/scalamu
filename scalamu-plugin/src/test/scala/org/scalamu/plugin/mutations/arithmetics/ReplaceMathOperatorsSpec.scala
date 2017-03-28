package org.scalamu.plugin.mutations.arithmetics

import org.scalamu.plugin.fixtures.SharedScalamuCompilerFixture
import org.scalamu.plugin.{Mutation, MutationOnlyRunner}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class ReplaceMathOperatorsSpec
    extends FlatSpec
    with MutationOnlyRunner
    with SharedScalamuCompilerFixture {

  override def mutations: Seq[Mutation] = List(ReplaceMathOperators)

  "MathOperatorReplacer" should "replace integer and floating point math operators" in withScalamuCompiler {
    implicit global =>
      val code =
        """
          |object Foo {
          |  def foo(i: Int): Int = 42
          |  def bar(d: Long): Long = d | d
          |  
          |  val a = 123
          |  val b = 234f
          |  val c = a + b
          |  val d = 123 * c
          |  val e = d / foo(a)
          |  val poly = e % bar(a)
          |}
        """.stripMargin
      val mutationsInfo = mutationsFor(code)
      mutationsInfo should have size 5
  }

  it should "work in nested cases" in withScalamuCompiler { implicit global =>
    val code =
      """
        |object Foo {
        |  val x = 123
        |  val y = 4d
        |  val z = 2f
        |  
        | val b = x + y * z
        | val c = b * (x ^ 12)
        | val a = (x + (b - 2)) / (x | (x + 1))
        |}
        """.stripMargin
    val mutationsInfo = mutationsFor(code)
    mutationsInfo should have size 9
  }
}
