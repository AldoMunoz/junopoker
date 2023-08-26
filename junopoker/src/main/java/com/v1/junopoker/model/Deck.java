package com.v1.junopoker.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumSet;


@Getter
@Setter
public class Deck {
    private ArrayList<Card> cards;
    private ArrayList<Card> deadCards;

    public Deck() {
        cards = new ArrayList<>(EnumSet.allOf(Card.class));
        deadCards = new ArrayList<>();

        //creates 52 card Records for each card
        /*Cardx TWO_S = new Cardx(2, 's', "/images/cards/TWO_S.png", 1);
        Cardx TWO_C = new Cardx(2, 'c', "/images/cards/TWO_C.png", 2);
        Cardx TWO_H = new Cardx(2, 'h', "/images/cards/TWO_H.png", 3);
        Cardx TWO_D = new Cardx(2, 'd', "/images/cards/TWO_D.png", 4);
        Cardx THREE_S = new Cardx(3, 's', "/images/cards/THREE_S.png",5);
        Cardx THREE_C = new Cardx(3, 'c', "/images/cards/THREE_C.png",6);
        Cardx THREE_H = new Cardx(3, 'h', "/images/cards/THREE_H.png",7);
        Cardx THREE_D = new Cardx(3, 'd', "/images/cards/THREE_D.png",8);
        Cardx FOUR_S = new Cardx(4, 's', "/images/cards/FOUR_S.png",9);
        Cardx FOUR_C = new Cardx(4, 'c', "/images/cards/FOUR_C.png",10);
        Cardx FOUR_H = new Cardx(4, 'h', "/images/cards/FOUR_H.png",11);
        Cardx FOUR_D = new Cardx(4, 'd', "/images/cards/FOUR_D.png",12);
        Cardx FIVE_S = new Cardx(5, 's', "/images/cards/FIVE_S.png",13);
        Cardx FIVE_C = new Cardx(5, 'c', "/images/cards/FIVE_C.png",14);
        Cardx FIVE_H = new Cardx(5, 'h', "/images/cards/FIVE_H.png",15);
        Cardx FIVE_D = new Cardx(5, 'd', "/images/cards/FIVE_D.png",16);
        Cardx SIX_S = new Cardx(6, 's', "/images/cards/SIX_S.png",17);
        Cardx SIX_C = new Cardx(6, 'c', "/images/cards/SIX_C.png",18);
        Cardx SIX_H = new Cardx(6, 'h', "/images/cards/SIX_H.png",19);
        Cardx SIX_D = new Cardx(6, 'd', "/images/cards/SIX_D.png",20);
        Cardx SEVEN_S = new Cardx(7, 's', "/images/cards/SEVEN_S.png",21);
        Cardx SEVEN_C = new Cardx(7, 'c', "/images/cards/SEVEN_C.png",22);
        Cardx SEVEN_H = new Cardx(7, 'h', "/images/cards/SEVEN_H.png",23);
        Cardx SEVEN_D = new Cardx(7, 'd', "/images/cards/SEVEN_D.png",24);
        Cardx EIGHT_S = new Cardx(8, 's', "/images/cards/EIGHT_S.png",25);
        Cardx EIGHT_C = new Cardx(8, 'c', "/images/cards/EIGHT_C.png",26);
        Cardx EIGHT_H = new Cardx(8, 'h', "/images/cards/EIGHT_H.png",27);
        Cardx EIGHT_D = new Cardx(8, 'd', "/images/cards/EIGHT_D.png",28);
        Cardx NINE_S = new Cardx(9, 's', "/images/cards/NINE_S.png",29);
        Cardx NINE_C = new Cardx(9, 'c', "/images/cards/NINE_C.png",30);
        Cardx NINE_H = new Cardx(9, 'h', "/images/cards/NINE_H.png" ,31);
        Cardx NINE_D = new Cardx(9, 'd', "/images/cards/NINE_D.png", 32);
        Cardx TEN_S = new Cardx(10, 's', "/images/cards/TEN_S.png", 33);
        Cardx TEN_C = new Cardx(10, 'c', "/images/cards/TEN_C.png", 34);
        Cardx TEN_H = new Cardx(10, 'h', "/images/cards/TEN_H.png",35);
        Cardx TEN_D = new Cardx(10, 'd', "/images/cards/TEN_D.png", 36);
        Cardx JACK_S = new Cardx(11, 's', "/images/cards/JACK_S.png", 37);
        Cardx JACK_C = new Cardx(11, 'c', "/images/cards/JACK_C.png", 38);
        Cardx JACK_H = new Cardx(11, 'h', "/images/cards/JACK_H.png",39);
        Cardx JACK_D = new Cardx(11, 'd', "/images/cards/JACK_D.png", 40);
        Cardx QUEEN_S = new Cardx(12, 's', "/images/cards/QUEEN_S.png", 41);
        Cardx QUEEN_C = new Cardx(12, 'c', "/images/cards/QUEEN_C.png", 42);
        Cardx QUEEN_H = new Cardx(12, 'h', "/images/cards/QUEEN_H.png", 43);
        Cardx QUEEN_D = new Cardx(12, 'd', "/images/cards/QUEEN_D.png", 44);
        Cardx KING_S = new Cardx(13, 's', "/images/cards/KING_S.png", 45);
        Cardx KING_C = new Cardx(13, 'c', "/images/cards/KING_C.png", 46);
        Cardx KING_H = new Cardx(13, 'h', "/images/cards/KING_H.png", 47);
        Cardx KING_D = new Cardx(13, 'd', "/images/cards/KING_D.png", 48);
        Cardx ACE_S = new Cardx(14, 's', "/images/cards/ACE_S.png", 49);
        Cardx ACE_C = new Cardx(14, 'c', "/images/cards/ACE_C.png", 50);
        Cardx ACE_H = new Cardx(14, 'h', "/images/cards/ACE_H.png", 51);
        Cardx ACE_D = new Cardx(14, 'd', "/images/cards/ACE_D.png", 52);
        */
        //initializes "cards" ArrayList amd adds each card to the ArrayList
        /*
        cards.add(TWO_S);
        cards.add(TWO_C);
        cards.add(TWO_H);
        cards.add(TWO_D);
        cards.add(THREE_S);
        cards.add(THREE_C);
        cards.add(THREE_H);
        cards.add(THREE_D);
        cards.add(FOUR_S);
        cards.add(FOUR_C);
        cards.add(FOUR_H);
        cards.add(FOUR_D);
        cards.add(FIVE_S);
        cards.add(FIVE_C);
        cards.add(FIVE_H);
        cards.add(FIVE_D);
        cards.add(SIX_S);
        cards.add(SIX_C);
        cards.add(SIX_H);
        cards.add(SIX_D);
        cards.add(SEVEN_S);
        cards.add(SEVEN_C);
        cards.add(SEVEN_H);
        cards.add(SEVEN_D);
        cards.add(EIGHT_S);
        cards.add(EIGHT_C);
        cards.add(EIGHT_H);
        cards.add(EIGHT_D);
        cards.add(NINE_S);
        cards.add(NINE_C);
        cards.add(NINE_H);
        cards.add(NINE_D);
        cards.add(TEN_S);
        cards.add(TEN_C);
        cards.add(TEN_H);
        cards.add(TEN_D);
        cards.add(JACK_S);
        cards.add(JACK_C);
        cards.add(JACK_H);
        cards.add(JACK_D);
        cards.add(QUEEN_S);
        cards.add(QUEEN_C);
        cards.add(QUEEN_H);
        cards.add(QUEEN_D);
        cards.add(KING_S);
        cards.add(KING_C);
        cards.add(KING_H);
        cards.add(KING_D);
        cards.add(ACE_S);
        cards.add(ACE_C);
        cards.add(ACE_H);
        cards.add(ACE_D);
        */
    }
}
