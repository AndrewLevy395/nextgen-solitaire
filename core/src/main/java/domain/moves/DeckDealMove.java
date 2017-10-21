package domain.moves;

import domain.*;
import java.util.*;

/**
 * A number of cards are dealt from the Stock.
 */
public class DeckDealMove extends Move {

    /**
     * Determine conditions for moving column of cards from src to target. 
     */
    public DeckDealMove (String name, Container src, Container target, Constraint constraint) {
        super(name, src, target, constraint);
    }

    /** By definition will allow multiple cards to be moved. Less relevant for deck, but at least consistent. */
    @Override
    public boolean isSingleCardMove() {
        return false;
    }

    /** By definition, deal to all elements in the container. */
    public boolean isSingleDestination() { return false; }

    /**
     * Get element being moved.
     *
     * Even though no card is dragged, this is accurate.
     */
    public Element   getMovableElement() {
        return new Card();
    }
}
