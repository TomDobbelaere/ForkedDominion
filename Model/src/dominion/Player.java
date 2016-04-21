package dominion;

import com.sun.org.apache.xpath.internal.operations.Bool;

/**
 * Created by Sam on 23/03/2016.
 */
public class Player
{
    private Account account;
    private Hand hand;
    private Deck discardPile;
    private Deck deck;
    private int actions;
    private int buys;
    private int coins;
    private GameEngine gameEngine;
    private Game game;

    public Player(GameEngine gameEngine, Game game)
    {
        this.gameEngine = gameEngine;
        this.game = game;

        actions = 1;
        buys = 1;
        coins = 0;

        hand = new Hand(deck);
        discardPile = new Deck(false, gameEngine);
        deck = new Deck(true, gameEngine);

        createStartingDeck();
    }


    public void setAccount(Account account)
    {
        this.account = account;
    }

    public Account getAccount() { return account; }

    public Deck getDeck() { return deck; }

    public int getActions()
    {
        return actions;
    }

    public int getBuys()
    {
        return buys;
    }

    public int getCoins()
    {
        return coins;
    }

    public Hand getHand()
    {
        return hand;
    }

    public void setCoins(int coins)
    {
        this.coins = coins;
    }

    public void setBuys(int buys)
    {
        this.buys = buys;
    }

    public void setActions(int actions)
    {
        this.actions = actions;
    }

    public void createStartingDeck()
    {
        for (int i = 0; i < 7; i++)
        {
            buyCard("copper", false);
        }

        for (int i = 0; i < 3; i++)
        {
            buyCard("estate", false);
        }

        deck.shuffle();
    }

    public void buyCard(String cardName, Boolean isKingdomCard)
    {
        Card card = game.retrieveCard(cardName, isKingdomCard);

        //TODO: Check if enough cards are left
        card.setAmount(card.getAmount() - 1);

        Card newCard = new Card(card, game);
        newCard.setAmount(1);

        deck.addCard(newCard);
    }
}
