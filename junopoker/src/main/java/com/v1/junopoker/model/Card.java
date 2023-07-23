package com.v1.junopoker.model;

import jakarta.persistence.Entity;

//enum of all cards in regular deck
public enum Card {
    TWO_S(2, 's'),
    TWO_C(2, 'c'),
    TWO_H(2, 'h'),
    TWO_D(2, 'd'),
    THREE_S(3, 's'),
    THREE_C(3, 'c'),
    THREE_H(3, 'h'),
    THREE_D(3, 'd'),
    FOUR_S(4, 's'),
    FOUR_C(4, 'c'),
    FOUR_H(4, 'h'),
    FOUR_D(4, 'd'),
    FIVE_S(5, 's'),
    FIVE_C(5, 'c'),
    FIVE_H(5, 'h'),
    FIVE_D(5, 'd'),
    SIX_S(6, 's'),
    SIX_C(6, 'c'),
    SIX_H(6, 'h'),
    SIX_D(6, 'd'),
    SEVEN_S(7, 's'),
    SEVEN_C(7, 'c'),
    SEVEN_H(7, 'h'),
    SEVEN_D(7, 'd'),
    EIGHT_S(8, 's'),
    EIGHT_C(8, 'c'),
    EIGHT_H(8, 'h'),
    EIGHT_D(8, 'd'),
    NINE_S(9, 's'),
    NINE_C(9, 'c'),
    NINE_H(9, 'h'),
    NINE_D(9, 'd'),
    TEN_S(10, 's'),
    TEN_C(10, 'c'),
    TEN_H(10, 'h'),
    TEN_D(10, 'd'),
    JACK_S(11, 's'),
    JACK_C(11, 'c'),
    JACK_H(11, 'h'),
    JACK_D(11, 'd'),
    QUEEN_S(12, 's'),
    QUEEN_C(12, 'c'),
    QUEEN_H(12, 'h'),
    QUEEN_D(12, 'd'),
    KING_S(13, 's'),
    KING_C(13, 'c'),
    KING_H(13, 'h'),
    KING_D(13, 'd'),
    ACE_S(14, 's'),
    ACE_C(14, 'c'),
    ACE_H(14, 'h'),
    ACE_D(14, 'd');

    private final int val;
    private final char suit;

    Card (int val, char suit) {
        this.val = val;
        this.suit = suit;
    }

    public int getVal() {
        return val;
    }

    public char getSuit() {
        return suit;
    }
}

