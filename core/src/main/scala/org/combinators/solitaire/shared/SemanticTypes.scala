package org.combinators.solitaire.shared

import de.tu_dortmund.cs.ls14.cls.types._
import de.tu_dortmund.cs.ls14.cls.types.syntax._
import domain.{ContainerType, SolitaireContainerTypes}


/**
  * These codify the semantic types used by the Solitaire combinators.
  *
  * For any of these that are ever going to be translated directly into Java Type Names, you must
  * make them Constructor.
  */
trait SemanticTypes {

  // structural high-level concerns
  val packageName: Type = 'RootPackage
  val variationName: Type = 'NameOfTheGame
  val className:Type = 'ClassName

  // library of all synthesized games
  val libraryName: Type = 'Library

  // meta-concerns. When you have completed the definition of a constructor
  val complete: Type = 'Complete
  val initialized:Type = 'Initialized

  /**
    * Constructing combinators from scratch require unique ids
    */
  object dynamic {
    def apply (uniq:Symbol) : Constructor = 'Dynamic(uniq)
  }

  // common structures
  val deck: Constructor          = 'Deck
  val column: Constructor        = 'Column
  val buildablePile: Constructor = 'BuildablePile
  val pile: Constructor          = 'Pile
  val row: Constructor           = 'Row

  /**
    * Return lower-case string matching the container type in domain.
    *
    * @param tpe    the container type
    */
  def typeOfContainer(tpe: ContainerType): String = {
    tpe match {
      case SolitaireContainerTypes.Foundation => "foundation"
      case SolitaireContainerTypes.Tableau => "tableau"
      case SolitaireContainerTypes.Reserve => "reserve"
      case SolitaireContainerTypes.Waste => "waste"
      case SolitaireContainerTypes.Stock => "deck"      // special
      case _ => "None"
     }
  }

  /** only one part since synthesizing 'the' game. */
  object game {
    def apply (part:Type): Constructor = 'Game (part)

    val winCondition: Type = 'WinCondition
    val autoMoves:Type = 'AutoMoves

    val fields:Type = 'Fields
    val methods:Type = 'Methods
    val imports:Type = 'Imports    // Should be part of context...

    val model:Type = 'Model
    val view:Type= 'View
    val control:Type = 'Control

    val availableMoves:Type = 'AvailableMoves
    val solvable:Type = 'Solvable   // does game provide availableMoves

    val deal:Type = 'Deal
  }

  // parts of the widgets during move : Dynamic Behavior
  object widget {
    def apply (entity:Type, part:Type): Constructor = 'MoveElement (entity, part)

    val movable:Type = 'MovableElementName
    val source:Type = 'SourceWidgetName
    val target:Type = 'TargetWidgetName
  }

  /**
    * Manages the press/click/release on widgets.
    *
    * When games (i.e., Klondike) have both natural press events (i.e., flipcard) that coexist with
    * the beginning of a drag (i.e., moveColumn) then there needs to be special handling. Resolution
    * comes in the controller shared trait.
    */
  object controller {
    def apply(context: Type, part: Type): Constructor = 'Controller (context, part)

    val pressed:Type = 'Pressed         // Pure press events
    val dragStart:Type = 'DragStart     // Press events that will ultimately become drag events.
    val clicked:Type = 'Clicked
    val released:Type = 'Released

  }

  /**
    * Used to clarify the helper code to be synthesized.
    */
  object constraints {
    def apply(part: Type): Constructor = 'Constraints (part)

    val methods:Type = 'Methods
    val generator:Type = 'ConstraintGen
  }

}
