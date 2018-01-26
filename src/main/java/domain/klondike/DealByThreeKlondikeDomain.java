package domain.klondike;

import domain.*;
import domain.Container;
import domain.ui.HorizontalPlacement;
import domain.ui.Layout;

import java.awt.*;

public class DealByThreeKlondikeDomain extends KlondikeDomain {

    /** Parameterizable API. */
    public int numToDeal() {
        return 3;
    }

    private Container waste;
    private Layout layout;

    @Override
    public Layout getLayout() {
        if (layout == null) {
            layout = new KlondikeLayout()
                    .remove(SolitaireContainerTypes.Waste)
                    .add(SolitaireContainerTypes.Waste, new HorizontalPlacement(new Point(95, 20),
                            Solitaire.card_width + 2*Solitaire.card_overlap,  Solitaire.card_height,  Solitaire.card_gap));
        }

        return layout;
    }

    /** Waste pile is now a row. */
    @Override
    protected Container getWaste() {
        if (waste == null) {
            waste = new Waste();
            waste.add (new FanPile(numToDeal()));
        }

        return waste;
    }

    public DealByThreeKlondikeDomain() {
        super("DealByThree");
        init();
    }

    private void init() {

        // register new elements for this domain
        registerElement(new FanPile(numToDeal()));

        // remove old move and swap with new one.
//
//        Constraint deck_move = new NotConstraint(new IsEmpty(MoveComponents.Source));
//        Move replaceMove = new DeckDealNCardsMove(numToDeal(), "DealDeck", getStock(), deck_move, getWaste());
//        if (remove(rules.presses(), "DealDeck")) {
//            addPressMove(replaceMove);
//        }
    }
}
