package org.combinators.solitaire.castle

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.{Name, SimpleName}
import org.combinators.cls.interpreter._
import org.combinators.cls.types.Constructor
import org.combinators.cls.types.syntax._
import domain.castle.Domain
import domain.{Move, Solitaire, SolitaireContainerTypes}
import org.scalatest.FunSpec
import test.Helper

import scala.collection.JavaConverters._

class CastleTests extends FunSpec  {


  describe("The possible inhabited domain models") {
    val domainModel:Solitaire = new Domain()

    describe("(using the only possible domain model)") {
      describe("the domain model") {
        it ("should have a tableau of size 8") {
          assert(domainModel.containers.get(SolitaireContainerTypes.Tableau).size == 8)
        }
        it ("should have a foundation of size 4") {
          assert(domainModel.containers.get(SolitaireContainerTypes.Foundation).size == 4)
        }

        describe("For synthesis") {
          val controllerRepository = new CastleDomain(domainModel) with controllers {}
          import controllerRepository._

          val reflected = ReflectedRepository(controllerRepository, classLoader = controllerRepository.getClass.getClassLoader)
          val Gamma= controllerRepository.init(reflected, domainModel)

          val helper = new Helper()

          it ("Check for base classes") {
            assert(helper.singleClass("ConstraintHelper",    Gamma.inhabit[CompilationUnit](constraints(complete))))

            assert(helper.singleClass("BeleagueredCastle",   Gamma.inhabit[CompilationUnit](game(complete :&: game.solvable))))
            assert(helper.singleClass("RowController",       Gamma.inhabit[CompilationUnit](controller(row, complete))))
            assert(helper.singleClass("PileController",      Gamma.inhabit[CompilationUnit](controller(pile, complete))))

            // Ensure all moves in the domain generate move classes as Compilation Units
            val combined = domainModel.getRules.drags.asScala ++ domainModel.getRules.presses.asScala ++ domainModel.getRules.clicks.asScala
            for (mv:Move <- combined) {
              val sym = Constructor(mv.getName)
              assert(helper.singleClass(mv.getName, Gamma.inhabit[CompilationUnit](move(sym :&: move.generic, complete))))
            }
          }

          // these are implied by the successful completion of 'game'
          it ("Structural validation") {
            assert(helper.singleInstance[SimpleName](Gamma.inhabit[SimpleName](variationName)))
            assert(helper.singleInstance[Name](Gamma.inhabit[Name](packageName)))
          }
        }
      }
    }
  }
}
