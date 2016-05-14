package dominion;

import dominion.exceptions.CardNotAvailableException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Digaly on 23/03/2016.
 */
public class Game
{
    private Card[] fixedCards;
    private Card[] kingdomCards;
    private Card[] cards;
    private ArrayList<String> cardsOnTable;
    private int currentPlayerIndex;
    private Player[] players;
    private int phase;
    private boolean isOver;
    private HashMap<String, Card> cardList;

    public Game(String[] playerNames, String kingdomCardSet, HashMap<String, Card> cardList)
    {
        this.cardList = cardList;
        this.kingdomCards = cardSet(kingdomCardSet);
        fixedCards = makeFixedCards(playerNames.length);
        players = new Player[playerNames.length];
        cardsOnTable = new ArrayList<>();

        for (int i = 0; i < playerNames.length; i++)
        {
            Player newPlayer = new Player();

            newPlayer.setName(playerNames[i]);

            createStartingDeck(newPlayer);

            makeHand(newPlayer);

            players[i] = newPlayer;
        }

        currentPlayerIndex = pickRandomPlayer();
        phase = -1;
        isOver = false;
        advancePhase();
    }

    private int pickRandomPlayer()
    {
        return (int) (Math.random() * players.length);
    }

    public void advancePhase()
    {
        phase++;

        if (phase == 0 && !findCurrentPlayer().hasActionCards()) phase++;

        if (phase >= 2)
        {
            advancePlayer();
        }
    }

    public void advancePlayer()
    {
        cleanup();
        cardsOnTable = new ArrayList<>();

        currentPlayerIndex++;

        if (currentPlayerIndex > players.length - 1)
        {
            currentPlayerIndex = 0;
        }

        findCurrentPlayer().setBuys(1);
        findCurrentPlayer().setActions(1);
        findCurrentPlayer().setCoins(0);

        phase = -1;
        advancePhase();
    }

    public int getPhase()
    {
        return phase;
    }

    public Player findCurrentPlayer()
    {
        return players[currentPlayerIndex];
    }

    private void makeCards(String name, int playerCount)
    {
        String[] kingdomCardsInSet = getKingdomCardsIn(name);
        cards = new Card[17];
        addKingdomCardsToCards(kingdomCardsInSet);
    }

    private String[] getKingdomCardsIn(String cardSet)
    {
        String[] cardNames = null;
        switch (cardSet)
        {
            case "default":
                cardNames = new String[]{"cellar", "market", "militia", "mine", "moat", "remodel", "smithy", "village", "woodcutter", "workshop",};
                break;
            case "testWitch":
                cardNames = new String[]{"cellar", "market", "militia", "mine", "moat", "remodel", "witch", "village", "woodcutter", "workshop",};
                break;
        }
        return cardNames;
    }

    private void addKingdomCardsToCards(String[] kingdomCardsInSet)
    {
        for (int i = 0; i < kingdomCardsInSet.length; i++)
        {
            Card card = cardList.get(kingdomCardsInSet[i]);
            cards[i] = new Card(card);
            cards[i].setAmount(10);
        }
    }

    private Card[] cardSet(String name)
    {
        String[] cardSetNames = null;
        Card[] cardSet = new Card[10];
        if (name.equals("default"))
        {
            cardSetNames = new String[]{"cellar", "market", "militia", "mine", "moat", "remodel", "smithy", "village", "woodcutter", "workshop",};
        }
        else if (name.equals("testWitch"))
        {
            cardSetNames = new String[]{"cellar", "market", "militia", "mine", "moat", "remodel", "witch", "village", "woodcutter", "workshop",};
        }

        for (int i = 0; i < cardSetNames.length; i++)
        {
            Card card = cardList.get(cardSetNames[i]);
            cardSet[i] = new Card(card);
            cardSet[i].setAmount(10);
        }
        return cardSet;
    }

    private Card[] makeFixedCards(int playerCount)
    {
        Card[] fixedCards = new Card[7];

        fixedCards[0] = new Card(cardList.get("province"));
        fixedCards[1] = new Card(cardList.get("duchy"));
        fixedCards[2] = new Card(cardList.get("estate"));
        fixedCards[3] = new Card(cardList.get("curse"));
        fixedCards[4] = new Card(cardList.get("gold"));
        fixedCards[5] = new Card(cardList.get("silver"));
        fixedCards[6] = new Card(cardList.get("copper"));


        if (playerCount == 2)
        {
            fixedCards[0].setAmount(8);
            fixedCards[1].setAmount(8);
            fixedCards[2].setAmount(8);
        } else
        {
            fixedCards[0].setAmount(12);
            fixedCards[1].setAmount(12);
            fixedCards[2].setAmount(12);
        }

        fixedCards[3].setAmount(playerCount * 10 - 10);
        fixedCards[4].setAmount(30);
        fixedCards[5].setAmount(40);
        fixedCards[6].setAmount(60);

        return fixedCards;
    }

    public Player getPlayer(String name)
    {
        Player player = null;

        for (Player p : players)
        {
            if (p.getName().equals(name))
                player = p;
        }

        return player;
    }

    public Card retrieveCard(String cardName)
    {
        Card foundCard = null;

        for (Card c : fixedCards)
        {
            if (c.getName().equals(cardName))
                foundCard = c;
        }

        if (foundCard == null)
        {
            for (Card c : kingdomCards)
            {
                if (c.getName().equals(cardName))
                    foundCard = c;
            }
        }

        return foundCard;
    }

    public void playCard(String cardName) throws CardNotAvailableException
    {
        Player currentPlayer = findCurrentPlayer();
        Card currentCard = currentPlayer.getHand().findCard(cardName);

        if (currentCard != null && currentCard.getType() != 2)
        {
            executeCardAbilities(currentCard);

            discardCard(currentCard);
            int cardType = currentCard.getType();

            if (cardType == 3 || cardType == 4 || cardType == 5)
                currentPlayer.setActions(currentPlayer.getActions() - 1);
        }
        else throw new CardNotAvailableException();
    }

    public void executeCardAbilities(Card currentCard) throws CardNotAvailableException
    {
        Ability[] cardAbilities = currentCard.getAbilities();
        cardsOnTable.add(currentCard.getName());
        for (Ability ability : cardAbilities)
        {
            if (ability.getId() < 6 || ability.getId() == 12)
            {
                ability.doAbility(this);
            }
            else if (ability.getId() == 6)
            {
                ability.doAbility(this, currentCard);
            }
        }
    }

    public void buyCard(String cardName) throws CardNotAvailableException
    {
        int cardCost = retrieveCard(cardName).getCost();
        Player currentPlayer = findCurrentPlayer();

        if (currentPlayer.getCoins() >= cardCost && currentPlayer.getBuys() > 0)
        {
            addCard(cardName);
            currentPlayer.setBuys(currentPlayer.getBuys() - 1);
            currentPlayer.setCoins(currentPlayer.getCoins() - cardCost);
        }
    }

    public void gainCardCostingUpTo(String cardName, int value) throws CardNotAvailableException
    {
        int cardCost = retrieveCard(cardName).getCost();

        if (value >= cardCost)
        {
            addCard(cardName);
        }
    }

    public void discardCardFromPlayer(Card card, Player player)
    {
        player.getDiscardPile().addCard(card);
        player.getHand().removeCard(card);
    }

    public void discardCard(Card card)
    {
        discardCardFromPlayer(card, findCurrentPlayer());
    }

    public void addCardToPlayer(String cardName, Player player) throws CardNotAvailableException
    {
        Card card = retrieveCard(cardName);

        if (card.getAmount() > 0)
        {
            card.setAmount(card.getAmount() - 1);

            Card newCard = new Card(card);
            newCard.setAmount(1);

            player.getDiscardPile().addCard(newCard);
        } else throw new CardNotAvailableException();
    }

    public void addCard(String cardName) throws CardNotAvailableException
    {
        addCardToPlayer(cardName, findCurrentPlayer());
    }

    private void createStartingDeck(Player player)
    {
        for (int i = 0; i < 7; i++)
        {
            try
            {
                addCardToPlayer("copper", player);
            }
            catch (CardNotAvailableException e)
            {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 3; i++)
        {
            try
            {
                addCardToPlayer("estate", player);
            }
            catch (CardNotAvailableException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void makeHand(Player player)
    {
        player.getHand().makeHand(player.getDeck(), player.getDiscardPile());
    }

    private void cleanup()
    {
        Player currentPlayer = findCurrentPlayer();

        ArrayList<Card> currentHand = currentPlayer.getHand().getCards();

        for (int i = 0; i < currentHand.size(); i++)
        {
            currentPlayer.getDiscardPile().addCard(currentHand.get(i));
        }

        makeHand(currentPlayer);
    }

    public ArrayList<String> findBuyableCards()
    {
        ArrayList<String> buyableCards = new ArrayList<>();
        for (Card kingdomCard : kingdomCards)
        {
            if (isBuyable(kingdomCard))
            {
                buyableCards.add(kingdomCard.getName());
            }
        }

        for (Card fixedCard : fixedCards)
        {
            if (isBuyable(fixedCard))
            {
                buyableCards.add(fixedCard.getName());
            }
        }
        return buyableCards;
    }

    public boolean isBuyable(Card card)
    {
        int money = findCurrentPlayer().getCoins();
        return money >= card.getCost();
    }

    public boolean getIsOver()
    {
        return isOver;
    }

    public Card[] getKingdomCards()
    {
        return kingdomCards;
    }

    public Card[] getFixedCards()
    {
        return fixedCards;
    }

    public void setCurrentPlayerIndex(int index)
    {
        currentPlayerIndex = index;
    }

    public ArrayList<String> getCardsOnTable()
    {
        return cardsOnTable;
    }

    public Card findCard(String name)
    {
        return cardList.get(name);
    }

    public Player[] getPlayers()
    {
        return players;
    }
}
