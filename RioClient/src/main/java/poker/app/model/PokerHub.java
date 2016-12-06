package poker.app.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import exceptions.DeckException;
import netgame.common.Hub;
import pokerBase.Action;
import pokerBase.Card;
import pokerBase.CardDraw;
import pokerBase.Deck;
import pokerBase.GamePlay;
import pokerBase.GamePlayPlayerHand;
import pokerBase.Player;
import pokerBase.Rule;
import pokerBase.Table;
import pokerEnums.eAction;
import pokerEnums.eCardDestination;
import pokerEnums.eDrawCount;
import pokerEnums.eGame;
import pokerEnums.eGameState;

public class PokerHub extends Hub {

	private Table HubPokerTable = new Table();
	private GamePlay HubGamePlay;
	private int iDealNbr = 0;
	private eGameState eGameState;
	private eDrawCount drawCount;
	private Card nextCard;

	public PokerHub(int port) throws IOException {
		super(port);
	}

	protected void playerConnected(int playerID) {

		if (playerID == 100) {
			shutdownServerSocket();
		}
	}

	protected void playerDisconnected(int playerID) {
		shutDownHub();
	}


	protected void messageReceived(int ClientID, Object message) {

		if (message instanceof Action) {
			Player actPlayer = (Player) ((Action) message).getPlayer();
			Action act = (Action) message;
			switch (act.getAction()) {
			case Sit:
				HubPokerTable.AddPlayerToTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case Leave:
				HubPokerTable.RemovePlayerFromTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case TableState:
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case StartGame:
				Rule rle = new Rule(act.geteGame());
				HubGamePlay = new GamePlay(rle, HubPokerTable.PickRandomPlayerAtTable().getPlayerID());
				HubGamePlay.setGamePlayers(HubPokerTable.getHashPlayers());
				HubGamePlay.setiActOrder(HubGamePlay.GetOrder(HubGamePlay.getGamePlayer(HubGamePlay.getGameDealer()).getiPlayerPosition()));
				drawCount = eDrawCount.NONE;
				resetOutput();
				sendToAll(HubGamePlay);
			case Draw:
				CardDraw cardDraw = HubGamePlay.getRule().GetDrawCard(drawCount.next());
				HubGamePlay.setCardDraw(cardDraw);
				if (cardDraw.getCardDestination() == eCardDestination.Community) {
					int cardsToDeal = cardDraw.getCardCount().ordinal() + 1;
					for (int cardDeal = 0; cardDeal < cardsToDeal; cardDeal++) {

						try {
							nextCard = HubGamePlay.getGameDeck().Draw();
						} catch (DeckException e) {
							System.out.println("Deck out of Cards");
						}
						HubGamePlay.getCommunityCards().add(nextCard);
					}
				} else if (cardDraw.getCardDestination() == eCardDestination.Player) {
					int cardsToDeal = cardDraw.getCardCount().ordinal()+1;
					for (int cardDeal = 0; cardDeal < cardsToDeal; cardDeal++) {
						for (int playerPosition : HubGamePlay.getiActOrder()) {
								Player player = HubGamePlay.getPlayerByPosition(playerPosition);
								if (player != null) {
								try {
									nextCard = HubGamePlay.getGameDeck().Draw();
								} catch (DeckException e) {
									System.out.println("Deck out of Cards");
								}
								HubGamePlay.getPlayerHand(player).AddToCardsInHand(nextCard);
							}
						}
					}
				}
				HubGamePlay.seteDrawCountLast(drawCount);
				resetOutput();
				sendToAll(HubGamePlay);
				break;
			}
		}

	}

}
