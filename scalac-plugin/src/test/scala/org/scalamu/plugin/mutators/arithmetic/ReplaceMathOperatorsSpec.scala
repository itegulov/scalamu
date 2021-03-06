package org.scalamu.plugin.mutators.arithmetic

import org.scalamu.plugin.Mutator
import org.scalamu.plugin.testutil.SingleMutationSpec

class ReplaceMathOperatorsSpec extends SingleMutationSpec {

  override def mutation: Mutator = ReplaceMathOperators

  "MathOperatorReplacer" should "replace integer and floating point math operators" in withScalamuCompiler {
    (global, reporter) =>
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
      val mutantsInfo = mutantsFor(code)(global, reporter)
      mutantsInfo should have size 5
  }

  it should "work in nested cases" in withScalamuCompiler { (global, reporter) =>
    val code =
      """
        |object Foo {
        |  val x = 123
        |  val y = 4d
        |  val z = 2f
        |  def foo(a: Double): Double = a * 2
        |  
        | val b = x + y * z
        | val c = b * (x ^ 12)
        | val a = (x + (b - 2)) / (x | (x + 1))
        | val f = a - foo(b * c / z)
        |}
        """.stripMargin
    val mutantsInfo = mutantsFor(code)(global, reporter)
    mutantsInfo should have size 13
  }

  it should "not mutate unsupported types" in withScalamuCompiler { (global, reporter) =>
    val code =
      """
        |object Foo {
        |  val a = Map.empty[Int, Int]
        |  val b = a + (1 -> 2)
        |
        |  case class A(val i: Int) {
        |    def *(other: A): A = A(0)
        |  }
        |
        |  val c = A(2) * A(2)
        |}
        """.stripMargin
    val mutantsInfo = mutantsFor(code)(global, reporter)
    mutantsInfo shouldBe empty
  }
}
