package poker.app.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import poker.app.MainApp;
import pokerBase.Action;
import pokerBase.Card;
import pokerBase.GamePlay;
import pokerBase.Hand;
import pokerBase.Player;
import pokerBase.Table;
import pokerEnums.eAction;
import pokerEnums.eCardVisibility;
import pokerEnums.eDrawCount;
import pokerEnums.eGame;
import pokerEnums.ePlayerPosition;

public class PokerTableController implements Initializable {

	// Reference to the main application.
	private MainApp mainApp;

	public PokerTableController() {
	}

	@FXML
	private Label lblPlayerPos1;
	@FXML
	private Label lblPlayerPos2;
	@FXML
	private Label lblPlayerPos3;
	@FXML
	private Label lblPlayerPos4;

	@FXML
	private ImageView imgViewDealerButtonPos1;
	@FXML
	private ImageView imgViewDealerButtonPos2;
	@FXML
	private ImageView imgViewDealerButtonPos3;
	@FXML
	private ImageView imgViewDealerButtonPos4;

	@FXML
	private BorderPane OuterBorderPane;

	@FXML
	private Label lblNumberOfPlayers;
	@FXML
	private TextArea txtPlayerArea;

	@FXML
	private Button btnStartGame;
	@FXML
	private ToggleButton btnPos1SitLeave;
	@FXML
	private ToggleButton btnPos2SitLeave;
	@FXML
	private ToggleButton btnPos3SitLeave;
	@FXML
	private ToggleButton btnPos4SitLeave;

	@FXML
	private Label lblPos1Name;
	@FXML
	private Label lblPos2Name;
	@FXML
	private Label lblPos3Name;
	@FXML
	private Label lblPos4Name;

	@FXML
	private HBox hBoxDeck;

	@FXML
	private HBox hboxP1Cards;
	@FXML
	private HBox hboxP2Cards;
	@FXML
	private HBox hboxP3Cards;
	@FXML
	private HBox hboxP4Cards;
	@FXML
	private HBox hboxCommunity;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		imgViewDealerButtonPos3.setVisible(true);
		imgViewDealerButtonPos4.setVisible(true);

		lblPlayerPos1.setText("1");
		lblPlayerPos2.setText("2");
		lblPlayerPos3.setText("3");
		lblPlayerPos4.setText("4");
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	private void handlePlay() {
	}

	@FXML
	public void GetGameState() {
		Action act = new Action(eAction.GameState, mainApp.getPlayer());
		mainApp.messageSend(act);
	}

	public void btnSitLeave_Click(ActionEvent event) {
		ToggleButton btnSitLeave = (ToggleButton) event.getSource();
		int iPlayerPosition = 0;
		if (btnSitLeave.isSelected()) {
			switch (btnSitLeave.getId().toString()) {
			case "btnPos1SitLeave":
				iPlayerPosition = ePlayerPosition.ONE.getiPlayerPosition();
				break;
			case "btnPos2SitLeave":
				iPlayerPosition = ePlayerPosition.TWO.getiPlayerPosition();
				break;
			case "btnPos3SitLeave":
				iPlayerPosition = ePlayerPosition.THREE.getiPlayerPosition();
				break;
			case "btnPos4SitLeave":
				iPlayerPosition = ePlayerPosition.FOUR.getiPlayerPosition();
				break;
			}
		} else {
			iPlayerPosition = 0;
		}

		// Set the PlayerPosition in the Player
		mainApp.getPlayer().setiPlayerPosition(iPlayerPosition);

		// Build an Action message
		Action act = new Action(btnSitLeave.isSelected() ? eAction.Sit : eAction.Leave, mainApp.getPlayer());

		// Send the Action to the Hub
		mainApp.messageSend(act);
	}

	public void MessageFromMainApp(String strMessage) {
		System.out.println("Message received by PokerTableController: " + strMessage);
	}

	private Label getPlayerLabel(int iPosition) {
		switch (iPosition) {
		case 1:
			return lblPlayerPos1;
		case 2:
			return lblPlayerPos2;
		case 3:
			return lblPlayerPos3;
		case 4:
			return lblPlayerPos4;
		default:
			return null;
		}
	}

	private ToggleButton getSitLeave(int iPosition) {
		switch (iPosition) {
		case 1:
			return btnPos1SitLeave;
		case 2:
			return btnPos2SitLeave;
		case 3:
			return btnPos3SitLeave;
		case 4:
			return btnPos4SitLeave;
		default:
			return null;
		}

	}

	private HBox getHBox(int iPosition) {
		switch (iPosition) {
		case 1:
			return hboxP1Cards;
		case 2:
			return hboxP2Cards;
		case 3:
			return hboxP3Cards;
		case 4:
			return hboxP4Cards;
		default:
			return null;
		}

	}

	public void Handle_TableState(Table HubPokerTable) {

		lblPlayerPos1.setText("");
		lblPlayerPos2.setText("");
		lblPlayerPos3.setText("");
		lblPlayerPos4.setText("");
		boolean bSeated = false;

		for (int a = 1; a < 5; a++) {
			Player p = HubPokerTable.getPlayerByPosition(a);
			if (p != null) {
				getPlayerLabel(a).setText(p.getPlayerName());

				if (p.getPlayerID().equals(mainApp.getPlayer().getPlayerID())) {
					getSitLeave(a).setText("Leave");
					bSeated = true;
				} else {
					getSitLeave(a).setVisible(false);
				}
			}
		}

		// Note: Players have been seated, set the Sit/Leave text
		// based on players already seated.

		for (int a = 1; a < 5; a++) {

			if (getPlayerLabel(a).getText() == "") {
				if (bSeated) {
					getSitLeave(a).setVisible(false);
				} else {
					getSitLeave(a).setVisible(true);
					getSitLeave(a).setText("Sit");
				}
			}
		}
	}

	public void Handle_GameState(GamePlay HubPokerGame) {

		hboxCommunity.getChildren().clear();
		for (int position = 1; position < 5; position++) {
			getHBox(position).getChildren().clear();
		}

		ArrayList<Card> communityCards = HubPokerGame.getCommunityCards();
		for (Card card : communityCards) {
			ImageView nextCard = BuildImage(card.getiCardNbr());
			hboxCommunity.getChildren().add(nextCard);
		}
		HashMap<UUID, Player> HashPlayers = HubPokerGame.getGamePlayers();
		for (Player player : HashPlayers.values()) {
			ArrayList<Card> playerHand = HubPokerGame.getPlayerHand(player).getCardsInHand();
			for (Card card : playerHand) {
				if (player.getPlayerID().equals(mainApp.getPlayer().getPlayerID()) || HubPokerGame.getCardDraw().getCardVisibility()==eCardVisibility.VisibleEveryone) {
					ImageView nextCard = BuildImage(card.getiCardNbr());
					getHBox(player.getiPlayerPosition()).getChildren().add(nextCard);
				}
				else{
					ImageView nextCard = BuildImage(0);
					getHBox(player.getiPlayerPosition()).getChildren().add(nextCard);
				}
			}

		}

	}

	private ImageView BuildImage(int CardNbr) {
		String imageName;
		if (CardNbr == 0) {
			imageName = "/img/b2fv.png";
		} else {
			imageName = "/img/" + CardNbr + ".png";
		}
		ImageView image = new ImageView(new Image(getClass().getResourceAsStream(imageName), 75, 75, true, true));
		return image;
	}

	@FXML
	void btnStart_Click(ActionEvent event) {
		// Code is given...
		// Start the Game
		// Send the message to the hub

		Action act = new Action(eAction.StartGame, mainApp.getPlayer());

		// figure out which game is selected in the menu
		eGame gme = eGame.getGame(Integer.parseInt(mainApp.getRuleName().replace("PokerGame", "")));

		// Set the gme in the action
		act.seteGame(gme);

		// Send the Action to the Hub
		mainApp.messageSend(act);
	}

	@FXML
	void btnDeal_Click(ActionEvent event) {
		// Example - how to set card images in the HBOX
		hboxP1Cards.getChildren().clear();

		ImageView i1 = new ImageView(new Image(getClass().getResourceAsStream("/img/26.png"), 75, 75, true, true));
		hboxP1Cards.getChildren().add(i1);

		ImageView i2 = new ImageView(new Image(getClass().getResourceAsStream("/img/27.png"), 75, 75, true, true));
		hboxP1Cards.getChildren().add(i2);

		ImageView i3 = new ImageView(new Image(getClass().getResourceAsStream("/img/28.png"), 75, 75, true, true));
		hboxP1Cards.getChildren().add(i3);

		ImageView i4 = new ImageView(new Image(getClass().getResourceAsStream("/img/29.png"), 75, 75, true, true));
		hboxP1Cards.getChildren().add(i4);

		ImageView i5 = new ImageView(new Image(getClass().getResourceAsStream("/img/30.png"), 75, 75, true, true));
		hboxP1Cards.getChildren().add(i5);

	}

	@FXML
	public void btnFold_Click(ActionEvent event) {
		Button btnFold = (Button) event.getSource();
		switch (btnFold.getId().toString()) {
		case "btnPlayer1Fold":
			// Fold for Player 1
			break;
		case "btnPlayer2Fold":
			// Fold for Player 2
			break;
		case "btnPlayer3Fold":
			// Fold for Player 3
			break;
		case "btnPlayer4Fold":
			// Fold for Player 4
			break;

		}
	}

	@FXML
	public void btnCheck_Click(ActionEvent event) {
		Button btnCheck = (Button) event.getSource();
		switch (btnCheck.getId().toString()) {
		case "btnPlayer1Check":
			// Check for Player 1
			break;
		case "btnPlayer2Check":
			// Check for Player 2
			break;
		case "btnPlayer3Check":
			// Check for Player 3
			break;
		case "btnPlayer4Check":
			// Check for Player 4
			break;
		}
	}

	private void FadeButton(Button btn) {
		FadeTransition ft = new FadeTransition(Duration.millis(3000), btn);
		ft.setFromValue(1.0);
		ft.setToValue(0.3);
		ft.setCycleCount(4);
		ft.setAutoReverse(true);

		ft.play();
	}

}