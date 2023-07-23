package com.v1.junopoker.service;

import com.v1.junopoker.model.Card;
import com.v1.junopoker.model.Deck;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class DeckService {
    //removes the card from the deck and moves it to the "dead" deck
    public Card drawCard(Deck deck) {
        deck.getDeadCards().add(deck.getCards().get(0));
        return deck.getCards().remove(0);
    }

    //shuffle the contents of cards ArrayList in random order
    public void shuffleCards (Deck deck) {
        Collections.shuffle(deck.getCards());
    }

    //joins the deck with the deadCards after hand is over
    public void joinDeck (Deck deck) {
        deck.getCards().addAll(deck.getDeadCards());
        deck.getDeadCards().removeAll(deck.getCards());
    }
}
