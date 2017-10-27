package org.combinators.solitaire.idiot

import de.tu_dortmund.cs.ls14.cls.interpreter._
import de.tu_dortmund.cs.ls14.cls.types.syntax._
import domain.{Move, Solitaire, SolitaireContainerTypes}
import domain.idiot.Domain
import org.combinators.solitaire.shared._
import org.scalatest.FunSpec
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.CompilationUnit
import de.tu_dortmund.cs.ls14.cls.types.Constructor
import scala.collection.JavaConverters._

class IdiotTests extends FunSpec  {


  describe("The possible inhabited domain models") {
    val domainModel:Solitaire = new Domain()

    describe("(using the only possible domain model)") {
      describe("the domain model") {
        it ("should have a tableau of size 4") {
          assert(domainModel.containers.get(SolitaireContainerTypes.Tableau).size == 4)
        }
        it ("should have a deck") {
          assert(domainModel.containers.get(SolitaireContainerTypes.Stock).size == 1)
        }

        describe("For synthesis") {
          val controllerRepository = new gameDomain(domainModel) with controllers {}
          import controllerRepository._

          val reflected = ReflectedRepository(controllerRepository, classLoader = controllerRepository.getClass.getClassLoader)
          val Gamma= controllerRepository.init(reflected, domainModel)
          val helper = new Helper(controllerRepository)

          it ("Check for base classes") {
            assert(helper.singleClass("ConstraintHelper",    Gamma.inhabit[CompilationUnit](constraints(complete))))

            // note that there are two copies of game(complete) -- one solvable, and one that is not.
            assert(helper.singleClass("Idiot",               Gamma.inhabit[CompilationUnit](game(complete :&: game.solvable))))
            assert(helper.singleClass("DeckController",      Gamma.inhabit[CompilationUnit](controller(deck, complete))))
            assert(helper.singleClass("ColumnController",    Gamma.inhabit[CompilationUnit](controller(column, complete))))

            // Ensure all moves in the domain generate move classes as Compilation Units
            val combined = domainModel.getRules.drags.asScala ++ domainModel.getRules.presses.asScala ++ domainModel.getRules.clicks.asScala
            for (mv:Move <- combined) {
              val sym = Constructor(mv.name)
              assert(helper.singleClass(mv.name, Gamma.inhabit[CompilationUnit](move(sym :&: move.generic, complete))))
            }

            // some potentials remain for the Idiot variation.
            assert(helper.singleClass("PotentialMoveCard", Gamma.inhabit[CompilationUnit](move('MoveCard :&: move.potential, complete))))
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
