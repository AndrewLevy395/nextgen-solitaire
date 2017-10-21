package org.combinators.solitaire.archway

import com.github.javaparser.ast.body.{FieldDeclaration, MethodDeclaration}
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.{IntegerLiteralExpr, Name, SimpleName}
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.stmt.Statement
import de.tu_dortmund.cs.ls14.cls.interpreter.combinator
import de.tu_dortmund.cs.ls14.cls.types._
import de.tu_dortmund.cs.ls14.cls.types.syntax._
import de.tu_dortmund.cs.ls14.twirl.Java
import domain._
import domain.ui._
import org.combinators.solitaire.shared
import org.combinators.solitaire.shared._

/**
  * Defines Java package, the game's name, initializes the domain model,
  * the UI, and the controllers (doesn't define them, just generates),
  * and includes extra fields and methods.
  * TODO: Score52...
  */
class ArchwayDomain(override val solitaire: Solitaire) extends SolitaireDomain(solitaire) with GameTemplate with Score52 with Controller {

  @combinator object RootPackage {
    def apply: Name = Java("org.combinators.solitaire.archway").name()
    val semanticType: Type = 'RootPackage
  }

  @combinator object NameOfTheGame {
    def apply: SimpleName = Java("Archway").simpleName()
    val semanticType: Type = 'NameOfTheGame
  }

  /**
    * Creates the objects used in the game, and associates them with their View classes.
    * - Deck (which has no view in Archway), Tableau, Reserve, Foundation.
    * The names are copied from those generated by the ExtraFields combinator.
    */
  @combinator object ArchwayInitModel {

    // note: we could avoid passing in these parameters and just solely
    // visit the domain model. That is an alternative worth considering.

    def apply(): Seq[Statement] = {

      /**
        * I'll add the deck to the model, but in 'Init('InitialDeal),
        * I need to remove all the cards and add them back from a
        * temporary deck in order to set up the Aces and Kings Foundations.
        */
//      val deck = Java(
//        s"""
//           | deck = new MultiDeck(2);
//           | int seed = getSeed();
//           | deck.create(seed);
//           | addModelElement(deck);
//         """.stripMargin).statements()
      val deck = deckGen("deck")

      val reserve = loopConstructGen(solitaire.containers.get(ArchwayContainerTypes.Reserve), "fieldReservePiles", "fieldReservePileViews", "Pile")
      val tableau = loopConstructGen(solitaire.containers.get(ArchwayContainerTypes.Tableau), "fieldTableauColumns", "fieldTableauColumnViews", "Column")

      /*
       * The Foundation is split between Aces and Kings, so I have to manually
       * generate them instead of using loopConstructGen()
       */
      val aces = loopConstructGen(solitaire.containers.get(ArchwayContainerTypes.Foundation), "fieldAcesFoundationPiles", "fieldAcesFoundationPileViews", "AcesUpPile")

      val kings = loopConstructGen(solitaire.containers.get(ArchwayContainerTypes.KingsDown), "fieldKingsFoundationPiles", "fieldKingsFoundationPileViews", "KingsDownPile")


      deck ++ aces ++ kings ++ reserve ++ tableau
    }

    val semanticType: Type = 'Init ('Model)
  }

  /** Deal the cards.
    * There is no mechanism to instantiate individual cards, so in order to create
    * the Aces and Kings Foundation, I have to create a temporary deck,
    * manually remove the Aces and Kings, add them to the foundation, and add
    * the rest back to the original deck.
    */
  @combinator object ArchwayInitLayout {
    def apply(): Seq[Statement] = {
      Java(
        s"""
           |deck.removeAll();
           |
           |MultiDeck tmpDeck = new MultiDeck(2);
           |tmpDeck.create(seed);
           |tmpDeck.shuffle(seed);
           |
           |// Remove the Aces and Kings and place them in the Foundation.
           |for (int i = 0; i < 104; i++) {
           |  Card card = tmpDeck.get();
           |  String card_string = card.toString();
           |
           |  if (card_string.equals("AH")) { fieldAcesFoundationPiles[0].add(card); }
           |  else if (card_string.equals("AS")) {
           |    fieldAcesFoundationPiles[1].add(card);
           |  }
           |  else if (card_string.equals("AC")) {
           |    fieldAcesFoundationPiles[2].add(card);
           |  }
           |  else if (card_string.equals("AD")) {
           |    fieldAcesFoundationPiles[3].add(card);
           |  }
           |  else if (card_string.equals("KH")) {
           |    fieldKingsFoundationPiles[0].add(card);
           |  }
           |  else if (card_string.equals("KS")) {
           |    fieldKingsFoundationPiles[1].add(card);
           |  }
           |  else if (card_string.equals("KC")) {
           |    fieldKingsFoundationPiles[2].add(card);
           |  }
           |  else if (card_string.equals("KD")) {
           |    fieldKingsFoundationPiles[3].add(card);
           |  }
           |  else {
           |    deck.add(card);
           |  }
           |}
           |
           |// Fill the Tableau.
           |for (int i = 0; i < 12; i++) {
           |  for (int j = 0; j < 4; j++) {
           |    fieldTableauColumns[j].add(deck.get());
           |  }
           |}
           |
           |// The rest goes to the Reserves.
           |for (int i = 0; i < 40; i++) {
           |  Card card = deck.get();
           |  fieldReservePiles[card.getRank() - 2].add(card);
           |}
         """.stripMargin).statements()
    }

    val semanticType: Type = 'Init ('InitialDeal)
  }

  /*
   * Because Aces and Kings have differing behavior in the Foundation, I have to make them as subclasses.
   */
  @combinator object MakeAcesUpPile        extends ExtendModel("Pile",    "AcesUpPile",    'AcesUpPileClass)
  @combinator object MakeKingsDownPile     extends ExtendModel("Pile",    "KingsDownPile", 'KingsDownPileClass)
  @combinator object MakeAcesUpPileView    extends ExtendView("PileView", "AcesUpPileView",    "AcesUpPile",    'AcesUpPileViewClass)
  @combinator object MakeKingsDownPileView extends ExtendView("PileView", "KingsDownPileView", "KingsDownPile", 'KingsDownPileViewClass)

  /**
    * Generate the statements which set the location of the Views.
    */
  @combinator object ArchwayInitView {
    def apply(): Seq[Statement] = {


//      val lay = solitaire.getLayout
//
//      /* I had originally figured out the coordinates on graph paper,
//       * then figured out the scale to make them all fit.
//       */
//      val scale = 27

//      var x = Array( 2,  2, 2, 4, 10, 14, 18, 24, 26, 26, 26).map(_ * scale)
//      var y = Array(15, 11, 7, 3, 1,  1,  1,  3,  7, 11, 15).map(_ * scale)
//
//      // Reserve
//      val rs = layout_place_custom(lay, solitaire.getReserve, Java("fieldReservePileViews").name(), x, y, 97)

      // Tableau
//      x = Array(8, 12, 16, 20).map(_ * scale)
//      y = Array(6, 6, 6, 6).map(_ * scale)
//      val tab = layout_place_custom(lay, solitaire.getTableau, Java("fieldTableauColumnViews").name(), x, y, 12*97)

      // Aces Foundation
//      x = Array( 2,  5,  2,  5).map(_ * scale)
//      y = Array(19, 19, 23, 23).map(_ * scale)
//      val fnd = layout_place_custom(lay, solitaire.getFoundation, Java("fieldAcesFoundationPileViews").name(), x, y, 97)

      // Kings Foundation
//      x = Array(23, 26, 23, 26).map(_ * scale)
//      y = Array(19, 19, 23, 23).map(_ * scale)
//      val kf = layout_place_custom(lay, solitaire.getContainer("KingsDownFoundation"), Java("fieldKingsFoundationPileViews").name(), x, y, 97)
//
//      rs ++ tab ++ fnd ++ kf

      var stmts = layout_place_it(solitaire.containers.get(ArchwayContainerTypes.Foundation), Java("fieldAcesFoundationPileViews").name())
      stmts = stmts ++ layout_place_it(solitaire.containers.get(ArchwayContainerTypes.Reserve), Java("fieldReservePileViews").name())
      stmts = stmts ++ layout_place_it(solitaire.containers.get(ArchwayContainerTypes.Tableau), Java("fieldTableauColumnViews").name())
      stmts = stmts ++ layout_place_it(solitaire.containers.get(ArchwayContainerTypes.KingsDown), Java("fieldKingsFoundationPileViews").name())

      stmts

    }
    val semanticType: Type = 'Init ('View)
  }

  /**
    * Generate code to associate Views with Controllers,
    * and to associate Controllers with Mouse Adapters.
    */
  @combinator object ArchwayInitControl {
    def apply(NameOfGame: SimpleName): Seq[Statement] = {
      val name = NameOfGame.toString()

      /* Aces Foundation Controller */
      val aces = loopControllerGen(solitaire.containers.get(ArchwayContainerTypes.Foundation), "fieldAcesFoundationPileViews", "AcesUpPileController")

      /* Kings Foundation Controller */
      val kings = loopControllerGen(solitaire.containers.get(ArchwayContainerTypes.KingsDown), "fieldKingsFoundationPileViews", "KingsDownPileController")

      /* Tableau Controller */
      val tableau = loopControllerGen(solitaire.containers.get(ArchwayContainerTypes.Tableau), "fieldTableauColumnViews", "ColumnController")

      /* Reserve Controller */
      val reserve = loopControllerGen(solitaire.containers.get(ArchwayContainerTypes.Reserve), "fieldReservePileViews", "PileController")

      aces ++ kings ++ tableau ++ reserve
    }

    val semanticType: Type = 'NameOfTheGame =>: 'Init ('Control)
  }

  /**
    * Generates import statements for the model and controller packages.
    */
  @combinator object ExtraImports {
    def apply(nameExpr: Name): Seq[ImportDeclaration] = {
      Seq(
        Java(s"import $nameExpr.controller.*;").importDeclaration(),
        Java(s"import $nameExpr.model.*;").importDeclaration()
      )
    }
    val semanticType: Type = 'RootPackage =>: 'ExtraImports
  }

  /**
    * Generate extra methods. Here we only need the preferred window size of the game.
    */
  @combinator object ExtraMethods {
    def apply(): Seq[MethodDeclaration] = {
      Java(s"""|public Dimension getPreferredSize() {
               |  return new Dimension (1280, 1280);
               |}
               |
               |// Available moves based on this variation. Note this was hard-coded in generated code
               |// and then manually moved into this combinator.
               |public java.util.Enumeration<Move> availableMoves() {
               |		java.util.Vector<Move> v = new java.util.Vector<Move>();
               |
               |	// Try all moves from the Reserve to the Aces and Kings Foundation and the Tableau.
               |  for (Pile reserve : fieldReservePiles) {
               |
               |    for (AcesUpPile acesFoundation : fieldAcesFoundationPiles) {
               |      ReserveToFoundation rtf = new PotentialReserveToFoundation(reserve, acesFoundation);
               |      if (rtf.valid(this)) {
               |        v.add(rtf);
               |      }
               |    }
               |    for (KingsDownPile kingsFoundation : fieldKingsFoundationPiles) {
               |      ReserveToKingsFoundation rkf = new PotentialReserveToKingsFoundation(reserve, kingsFoundation);
               |      if (rkf.valid(this)) {
               |        v.add(rkf);
               |      }
               |    }
               |    for (Column tableau : fieldTableauColumns) {
               |      ReserveToTableau rt = new PotentialReserveToTableau(reserve, tableau);
               |      if (rt.valid(this)) {
               |        v.add(rt);
               |      }
               |    }
               |  }
               |
               |  // Try all moves from the Tableau to the Aces and Kings Foundation.
               |  for (Column tableau : fieldTableauColumns) {
               |    for (AcesUpPile acesFoundation : fieldAcesFoundationPiles) {
               |      TableauToFoundation tf = new PotentialTableauToFoundation(tableau, acesFoundation);
               |      if (tf.valid(this)) {
               |        v.add(tf);
               |      }
               |    }
               |
               |    // TODO: The 3H is duplicated when returned to the Tableau.
               |    for (KingsDownPile kingsFoundation : fieldKingsFoundationPiles) {
               |      TableauToKingsFoundation tk = new PotentialTableauToKingsFoundation(tableau, kingsFoundation);
               |      if (tk.valid(this)) {
               |        v.add(tk);
               |      }
               |    }
               |  }
               |
               |  return v.elements();
               |	}
               |
               |""".stripMargin).classBodyDeclarations().map(_.asInstanceOf[MethodDeclaration])

    }

    val semanticType: Type = 'ExtraMethods :&: 'AvailableMoves
  }

  /**
    * Generate Archway.java's attributes:
    * one array for the domain model and one array for the view, and any other fields
    * Ex: Multideck deck;
    *     Pile reservePiles = new Pile[11];
    *     PileView reservePileViews = new PileView[11];
    */
  @combinator object ExtraFields {
    def apply(): Seq[FieldDeclaration] = {
      val fields =
        Java(
          s"""
             |IntegerView scoreView;
             |IntegerView numLeftView;
             """.stripMargin).classBodyDeclarations().map(_.asInstanceOf[FieldDeclaration])

      /* The first argument becomes fieldTableauColumns */
      val tableau = fieldGen("TableauColumn",         4)   // HACK: get from size of container
      val reserve = fieldGen("ReservePile",          11)
      val aces    = fieldGen("AcesFoundationPile",   4)
      val kings   = fieldGen("KingsFoundationPile",  4)

      val deck = Java("MultiDeck deck;").classBodyDeclarations().map(_.asInstanceOf[FieldDeclaration])

      deck ++ fields ++ tableau ++ reserve ++ aces ++ kings
    }

    val semanticType: Type = 'ExtraFields
  }
}