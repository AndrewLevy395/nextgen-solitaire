package org.combinators.solitaire.idiot

import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.stmt.Statement
import de.tu_dortmund.cs.ls14.cls.types.Type
import de.tu_dortmund.cs.ls14.cls.types.syntax._
import de.tu_dortmund.cs.ls14.twirl.Java
import org.combinators.solitaire.shared._
import org.combinators.solitaire.shared
import de.tu_dortmund.cs.ls14.cls.interpreter.ReflectedRepository
import org.combinators.generic
import domain._

trait controllers extends shared.Controller with shared.Moves with WinningLogic with generic.JavaCodeIdioms  {

  // dynamic combinators added as needed
  override def init[G <: SolitaireDomain](gamma : ReflectedRepository[G], s:Solitaire) :  ReflectedRepository[G] = {
    var updated = super.init(gamma, s)
    println (">>> Idiot Controller dynamic combinators.")

    updated = createMoveClasses(updated, s)

    updated = createDragLogic(updated, s)

    updated = generateMoveLogic(updated, s)

    // Each move has a source and a target. The SOURCE is the locus
    // for the PRESS while the TARGET is the locus for the RELEASE.
    // These are handling the PRESS events... SHOULD BE ABLE TO
    // INFER THESE FROM THE AVAILABLE MOVES
    updated = updated
      .addCombinator (new SingleCardMoveHandler(column))
      .addCombinator (new DealToTableauHandlerLocal())
      .addCombinator (new TryRemoveCardHandlerLocal(column))

    updated = updated
      .addCombinator (new IgnoreReleasedHandler(deck))
      .addCombinator (new IgnoreClickedHandler(deck))

    updated = createWinLogic(updated, s)

    updated
  }

  /**
    * When dealing card(s) from the stock to all elements in Tableau. Handling
    * deck events is a bit annoying. Must not change ignore variable
    */
  class DealToTableauHandlerLocal() {
    def apply():(SimpleName, SimpleName) => Seq[Statement] = (widget,ignore) =>{
      Java(s"""|{Move m = new DealDeck(theGame.deck, theGame.tableau);
               |if (m.doMove(theGame)) {
               |   theGame.pushMove(m);
               |   theGame.refreshWidgets();
               |}}""".stripMargin).statements()
    }

    val semanticType: Type = drag(drag.variable, drag.ignore) =>: controller(deck, controller.pressed)

  }

  /**
    * HACK: Move this logic into class which is synthesized, rather than taking existing RSC class as is
    * from the template area.
    *
    * @param source
    */
  class TryRemoveCardHandlerLocal(source:Type) {
    def apply():Seq[Statement] = {
      Java(s"""|Column srcColumn = (Column) src.getModelElement();
               |Move m = new RemoveCard(srcColumn);
               |if (m.doMove(theGame)) {
               |   theGame.pushMove(m);
               |}""".stripMargin).statements()
    }

    val semanticType: Type = controller (source, controller.clicked)
  }
}


