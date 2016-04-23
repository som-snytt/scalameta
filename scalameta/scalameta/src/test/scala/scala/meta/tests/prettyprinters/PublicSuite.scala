package scala.meta.tests
package prettyprinters

import org.scalatest._
import scala.meta._

class PublicSuite extends FunSuite {
  test("Dialect.toString") {
    Dialect.all.foreach(d => assert(d.name == d.toString))
  }

  test("Input.String.toString") {
    val input = Input.String("foo")
    assert(input.toString == """Input.String("foo")""")
  }

  test("Input.Stream.toString") {
    import java.io._
    import java.nio.charset.Charset
    val cp1251 = Charset.forName("cp1251")
    val stream = new ByteArrayInputStream("Привет(мир!)".getBytes(cp1251))
    val input1 = Input.Stream(stream, cp1251)
    val input2 = Input.Stream(stream, Charset.forName("UTF-8"))
    assert(input1.toString == """Input.Stream(<stream>, Charset.forName("windows-1251"))""")
    assert(input2.toString == """Input.Stream(<stream>, Charset.forName("UTF-8"))""")
  }

  test("Input.File.toString") {
    import java.io._
    import java.nio.charset.Charset
    val file = new File("hello.scala")
    val input1 = Input.File(file, Charset.forName("cp1251"))
    val input2 = Input.File(file, Charset.forName("UTF-8"))
    assert(input1.toString == """Input.File(new File("hello.scala"), Charset.forName("windows-1251"))""")
    assert(input2.toString == """Input.File(new File("hello.scala"), Charset.forName("UTF-8"))""")
  }

  test("Input.Slice.toString") {
    val input = Input.Slice(Input.String("foo"), 0, 2)
    assert(input.toString == """Input.Slice(Input.String("foo"), 0, 2)""")
  }

  test("Position.toString") {
    val Term.ApplyInfix(lhs, _, _, _) = "foo + bar".parse[Term].get
    assert(lhs.position.toString === """[0..0..3) in Input.String("foo + bar")""")
  }

  test("Point.toString") {
    val Term.ApplyInfix(lhs, _, _, _) = "foo + bar".parse[Term].get
    assert(lhs.position.start.toString === """0 in Input.String("foo + bar")""")
    assert(lhs.position.end.toString === """3 in Input.String("foo + bar")""")
  }

  test("Parsed.Success.toString") {
    val parsed = "foo + bar".parse[Term]
    assert(parsed.toString === "foo + bar")
  }

  test("Parsed.Error.toString") {
    val parsed = "foo + class".parse[Term]
    assert(parsed.toString === """
      |<content>:1: error: end of file expected but class found
      |foo + class
      |      ^
    """.trim.stripMargin)
  }

  test("ParseException.toString") {
    intercept[ParseException] {
      try "foo + class".parse[Term].get
      catch {
        case ex: ParseException =>
          assert(ex.toString === """
            |<content>:1: error: end of file expected but class found
            |foo + class
            |      ^
          """.trim.stripMargin)
          throw ex
      }
    }
  }

  test("Tokenized.Success.toString") {
    val tokenized = "foo + bar".tokenize
    assert(tokenized.toString === "Tokens(BOF [0..0), foo [0..3),   [3..4), + [4..5),   [5..6), bar [6..9), EOF [9..9))")
  }

  test("Tokenized.Error.toString") {
    val tokenized = """"c""".tokenize
    assert(tokenized.toString === """
      |<content>:1: error: unclosed string literal
      |"c
      |^
    """.trim.stripMargin)
  }

  test("TokenizeException.toString") {
    intercept[TokenizeException] {
      try """"c""".tokenize.get
      catch {
        case ex: TokenizeException =>
          assert(ex.toString === """
            |<content>:1: error: unclosed string literal
            |"c
            |^
          """.trim.stripMargin)
          throw ex
      }
    }
  }

  test("Tokens.toString") {
    val tokens = "foo + bar".tokenize.get
    assert(tokens.toString === "Tokens(BOF [0..0), foo [0..3),   [3..4), + [4..5),   [5..6), bar [6..9), EOF [9..9))")
  }

  test("Tokens.show[Structure]") {
    val tokens = "foo + bar".tokenize.get
    assert(tokens.show[Structure] === "Tokens(BOF [0..0), foo [0..3),   [3..4), + [4..5),   [5..6), bar [6..9), EOF [9..9))")
  }

  test("Tokens.show[Syntax]") {
    val tokens = "foo + bar".tokenize.get
    assert(tokens.show[Syntax] === "foo + bar")
  }

  test("Token.toString") {
    val token = "foo + bar".tokenize.get(1)
    assert(token.toString === "foo")
  }

  test("Token.show[Structure]") {
    val token = "foo + bar".tokenize.get(1)
    assert(token.show[Structure] === "foo [0..3)")
  }

  test("Token.show[Syntax]") {
    val token = "foo + bar".tokenize.get(1)
    assert(token.show[Syntax] === "foo")
  }

  test("(Manual) Tree.toString") {
    val tree = Term.ApplyInfix(Term.Name("foo"), Term.Name("+"), Nil, List(Term.Name("bar")))
    assert(tree.toString === "foo + bar")
  }

  test("(Manual) Tree.show[Structure]") {
    val tree = Term.ApplyInfix(Term.Name("foo"), Term.Name("+"), Nil, List(Term.Name("bar")))
    assert(tree.show[Structure] === """Term.ApplyInfix(Term.Name("foo"), Term.Name("+"), Nil, Seq(Term.Name("bar")))""")
  }

  test("(Manual) Tree.show[Syntax]") {
    val tree = Term.ApplyInfix(Term.Name("foo"), Term.Name("+"), Nil, List(Term.Name("bar")))
    assert(tree.show[Syntax] === "foo + bar")
  }

  test("(Parsed) Tree.toString") {
    val tree = "foo + bar // baz".parse[Term].get
    assert(tree.toString === "foo + bar // baz")
  }

  test("(Parsed) Tree.show[Structure]") {
    val tree = "foo + bar // baz".parse[Term].get
    assert(tree.show[Structure] === """Term.ApplyInfix(Term.Name("foo"), Term.Name("+"), Nil, Seq(Term.Name("bar")))""")
  }

  test("(Parsed) Tree.show[Syntax]") {
    val tree = "foo + bar // baz".parse[Term].get
    assert(tree.show[Syntax] === "foo + bar // baz")
  }

  test("(Quasiquoted) Tree.toString") {
    val tree = q"foo + bar // baz"
    assert(tree.toString === "foo + bar")
  }

  test("(Quasiquoted) Tree.show[Structure]") {
    val tree = q"foo + bar // baz"
    assert(tree.show[Structure] === """Term.ApplyInfix(Term.Name("foo"), Term.Name("+"), Nil, Seq(Term.Name("bar")))""")
  }

  test("(Quasiquoted) Tree.show[Syntax]") {
    val tree = q"foo + bar // baz"
    assert(tree.show[Syntax] === "foo + bar")
  }
}
