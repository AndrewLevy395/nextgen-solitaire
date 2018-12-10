package org.combinators.solitaire

import org.combinators.solitaire.domain._
import org.combinators.solitaire.gypsy.variationPoints


package object giant extends variationPoints {

  override def getDeal(): Seq[DealStep] = {
    Seq(DealStep(ContainerTarget(Tableau)))
  }

  val giantFoundationToTableauConstraint:Constraint = AndConstraint(
    IsEmpty(StockContainer),
    OrConstraint(
    IsEmpty(Destination),
    AndConstraint(
      OppositeColor(MovingCard, TopCardOf(Destination)),
      NextRank(TopCardOf(Destination), MovingCard))
    ))

  override val foundationToTableauMove:Move = SingleCardMove("MoveFoundationToTableau", Drag,
    source=(Foundation,Truth), target=Some((Tableau, giantFoundationToTableauConstraint)))

  val giant:Solitaire = {
    Solitaire(name = "Giant",
      structure = structureMap,
      layout = Layout(map),
      deal = getDeal,
      specializedElements = Seq.empty,
      moves = Seq(tableauToTableauMove, buildFoundation, flipMove, foundationToTableauMove, deckDealMove),
      logic = BoardState(Map(Foundation -> 104)),
      solvable = false
    )
  }
}