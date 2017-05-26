package org.combinators.solitaire.shared

import com.github.javaparser.ast.body.{FieldDeclaration, MethodDeclaration}
import com.github.javaparser.ast.expr.{Name, SimpleName}
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.{CompilationUnit, ImportDeclaration}
import de.tu_dortmund.cs.ls14.cls.interpreter.combinator
import de.tu_dortmund.cs.ls14.cls.types._
import de.tu_dortmund.cs.ls14.cls.types.syntax._
import org.combinators.solitaire.shared
// domain
import domain._

trait GameTemplate {

  // domain model elements for game defined here...
  lazy val tableauType = Variable("TableauType")

  @combinator object NewEmptySolitaire {
    def apply(): Solitaire = new Solitaire()
    val semanticType: Type =
      'Solitaire ('Tableau ('None)) :&:
        'Solitaire ('Foundation ('None)) :&:
        'Solitaire ('Reserve ('None)) :&:
        'Solitaire ('Layout ('None)) :&:
        'Solitaire ('Rules ('None))
  }

  class NColumnTableau(n: Int, nAsType: Type) {
    def apply(): Tableau = {
      val t = new Tableau()
      for (_ <- 1 to n)
        t.add(new Column())
      t
    }

    val semanticType: Type = 'Tableau ('Valid :&: nAsType :&: 'Column)
  }

  // generic 8-column tableau
  @combinator object EightColumnTableau extends NColumnTableau(8, 'Eight)

  // generic 4-column tableau
  @combinator object FourColumnTableau extends NColumnTableau(4, 'Four)

  // Standard Layout with Tableau below a Reserve (Left) and Foundation (Right)
  @combinator object FoundationReserveTableauLayout {
    def apply(): Layout = {
      val lay = new Layout()

      // width = 73
      // height = 97

      lay.add(Layout.Foundation, 390, 20, 680, 97)
      lay.add(Layout.Reserve, 15, 20, 680, 97)
      lay.add(Layout.Tableau, 15, 137, 1360, 13 * 97)

      lay
    }

    val semanticType: Type = 'Layout ('Valid :&: 'FoundationReserveTableau)
  }


  @combinator object MainGame {

    def apply(rootPackage: Name,
      nameParameter: SimpleName,
      extraImports: Seq[ImportDeclaration],
      extraFields: Seq[FieldDeclaration],
      extraMethods: Seq[MethodDeclaration],
      initializeSteps: Seq[Statement],
      winParameter: Seq[Statement]): CompilationUnit = {

      shared.java.GameTemplate
        .render(
          rootPackage = rootPackage,
          extraImports = extraImports,
          nameParameter = nameParameter,
          extraFields = extraFields,
          extraMethods = extraMethods,
          winParameter = winParameter,
          initializeSteps = initializeSteps)
        .compilationUnit()
    }

    val semanticType: Type =
      'RootPackage =>:
        'NameOfTheGame =>:
        'ExtraImports =>:
        'ExtraFields =>:
        'ExtraMethods =>:
        'Initialization :&: 'NonEmptySeq =>:
        'WinConditionChecking :&: 'NonEmptySeq =>:
        'SolitaireVariation
  }

  @combinator object SolvableGame {

    def apply(rootPackage: Name,
      nameParameter: SimpleName,
      extraImports: Seq[ImportDeclaration],
      extraFields: Seq[FieldDeclaration],
      extraMethods: Seq[MethodDeclaration],
      initializeSteps: Seq[Statement],
      winParameter: Seq[Statement]): CompilationUnit = {

      shared.java.SolvableGameTemplate
        .render(
          rootPackage = rootPackage,
          extraImports = extraImports,
          nameParameter = nameParameter,
          extraFields = extraFields,
          extraMethods = extraMethods,
          winParameter = winParameter,
          initializeSteps = initializeSteps)
        .compilationUnit()
    }

    val semanticType: Type =
      'RootPackage =>:
        'NameOfTheGame =>:
        'ExtraImports =>:
        'ExtraFields =>:
        'ExtraMethods :&: 'AvailableMoves =>:
        'Initialization :&: 'NonEmptySeq =>:
        'WinConditionChecking :&: 'NonEmptySeq =>:
        'SolitaireVariation :&: 'Solvable
  }
}
