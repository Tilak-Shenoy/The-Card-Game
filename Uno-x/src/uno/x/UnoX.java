/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uno.x;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;
import javax.swing.BorderFactory;
import static javax.swing.JComponent.WHEN_FOCUSED;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import org.omg.CORBA.Bounds;

/**
 *
 * @author Ashwin Nayak
 */
class ActionCard extends UNOCard{
	
	private int Function = 0;
	
	//Constructor
	public ActionCard(){
	}
	
	public ActionCard(Color cardColor, String cardValue){
		super(cardColor,ACTION, cardValue);		
	}	
}
class CardDeck implements GameConstants {
	
	private final LinkedList<NumberCard> numberCards;
	private final LinkedList<ActionCard> actionCards;
	private final LinkedList<WildCard> wildCards;
	
	private LinkedList<UNOCard> UNOcards;
	
	public CardDeck(){
		
		//Initialize Cards
		numberCards = new LinkedList<NumberCard>();
		actionCards = new LinkedList<ActionCard>();
		wildCards = new LinkedList<WildCard>();
		
		UNOcards = new LinkedList<UNOCard>();
		
		addCards();
		addCardListener(CARDLISTENER);
	}
	
	
	//Create 108 cards for this CardDeck
	private void addCards() {
		for(Color color:UNO_COLORS){
			
			//Create 76 NumberCards --> doubles except 0s.
			for(int num : UNO_NUMBERS){
				int i=0;
				do{
					UNOcards.add(new NumberCard(color, Integer.toString(num)));
					i++;
				}while(num!=0 && i<2);
			}
			
			//Create 24 ActionCards --> everything twice
			for(String type : ActionTypes){
				for(int i=0;i<2;i++)
					UNOcards.add(new ActionCard(color, type));
			}					
		}		
		
		for(String type : WildTypes){
			for(int i=0;i<4;i++){
				UNOcards.add(new WildCard(type));
			}
		}
		
	}
	
	//Cards have MouseListener
	public void addCardListener(MyCardListener listener){
		for(UNOCard card: UNOcards)
		card.addMouseListener(listener);
	}
	
	public LinkedList<UNOCard> getCards(){
		return UNOcards;
	}
}
class NumberCard extends UNOCard {

	public NumberCard(){
	}
	
	public NumberCard(Color cardColor, String cardValue){
		super(cardColor, NUMBERS, cardValue);		
	}

}
class WildCard extends UNOCard {
	
	private int Function = 0;
	private Color chosenColor;
	
	public WildCard() {
	}
	
	public WildCard(String cardValue){
		super(BLACK, WILD, cardValue);		
	}
	
	public void useWildColor(Color wildColor){
		chosenColor = wildColor;
	}
	
	public Color getWildColor(){
		return chosenColor;
	}

}
class Dealer implements GameConstants {
	
	private CardDeck cardDeck;
	private Stack<UNOCard> CardStack;	
	
	public Dealer(){
		this.cardDeck = new CardDeck();
	}
	
	//Shuffle cards
	public Stack<UNOCard> shuffle(){
		
		LinkedList<UNOCard> DeckOfCards = cardDeck.getCards();
		LinkedList<UNOCard> shuffledCards = new LinkedList<UNOCard>();
		
		while(!DeckOfCards.isEmpty()){
			int totalCards = DeckOfCards.size();
			
			Random random = new Random();
			int pos = (Math.abs(random.nextInt()))% totalCards;
			
			UNOCard randomCard = DeckOfCards.get(pos);
			DeckOfCards.remove(pos);
			shuffledCards.add(randomCard);
		}
		
		CardStack = new Stack<UNOCard>();
		for(UNOCard card : shuffledCards){
			CardStack.add(card);
		}
		
		return CardStack;
	}
	
	//Spread cards to players - 8 each
	public void spreadOut(Player[] players){		
		
		for(int i=1;i<=FIRSTHAND;i++){
			for(Player p : players){
				p.obtainCard(CardStack.pop());
			}
		}		
	}
	
	public UNOCard getCard(){
		return CardStack.pop();
	}
}

class Game implements GameConstants {

	private Player[] players;
	private boolean isOver;
	private int GAMEMODE;
	
	private PC pc;
	private Dealer dealer;
	private Stack<UNOCard> cardStack;
	
	
	public Game(int mode){
		
		GAMEMODE = mode;
		
		//Create players..output depends on gamemode
		String name = (GAMEMODE==MANUAL) ? JOptionPane.showInputDialog("Player 1") : "PC";	
		String name2 = JOptionPane.showInputDialog("Player 2");
		
		if(GAMEMODE==vsPC)
			pc = new PC();
		
		Player player1 = (GAMEMODE==vsPC) ? pc : new Player(name);
		Player player2 = new Player(name2);		
		player2.toggleTurn();				//Initially, player2's turn		
			
		players = new Player[]{player1, player2};			
		
		//Create Dealer
		dealer = new Dealer();
		cardStack = dealer.shuffle();
		dealer.spreadOut(players);
		
		isOver = false;
	}

	public Player[] getPlayers() {
		return players;
	}

	public UNOCard getCard() {
		return dealer.getCard();
	}
	
	public void removePlayedCard(UNOCard playedCard) {

		for (Player p : players) {
			if (p.hasCard(playedCard)){
				p.removeCard(playedCard);
				
				if (p.getTotalCards() == 1 && !p.getSaidUNO()) {
					infoPanel.setError(p.getName() + " Forgot to say UNO");
					p.obtainCard(getCard());
					p.obtainCard(getCard());
				}else if(p.getTotalCards()>2){
					p.setSaidUNOFalse();
				}
			}			
		}
	}
	
	//give player a card
	public void drawCard(UNOCard topCard) {

		boolean canPlay = false;

		for (Player p : players) {
			if (p.isMyTurn()) {
				UNOCard newCard = getCard();
				p.obtainCard(newCard);
				canPlay = canPlay(topCard, newCard);
				break;
			}
		}

		if (!canPlay)
			switchTurn();
	}

	public void switchTurn() {
		for (Player p : players) {
			p.toggleTurn();
		}
		whoseTurn();
	}
	
	//Draw cards x times
	public void drawPlus(int times) {
		for (Player p : players) {
			if (!p.isMyTurn()) {
				for (int i = 1; i <= times; i++)
					p.obtainCard(getCard());
			}
		}
	}
	
	//response whose turn it is
	public void whoseTurn() {

		for (Player p : players) {
			if (p.isMyTurn()){
				infoPanel.updateText(p.getName() + "'s Turn");
				System.out.println(p.getName() + "'s Turn");
			}
		}
		infoPanel.setDetail(playedCardsSize(), remainingCards());
		infoPanel.repaint();
	}
	
	//return if the game is over
	public boolean isOver() {
		
		if(cardStack.isEmpty()){
			isOver= true;
			return isOver;
		}
		
		for (Player p : players) {
			if (!p.hasCards()) {
				isOver = true;
				break;
			}
		}
		
		return isOver;
	}

	public int remainingCards() {
		return cardStack.size();
	}

	public int[] playedCardsSize() {
		int[] nr = new int[2];
		int i = 0;
		for (Player p : players) {
			nr[i] = p.totalPlayedCards();
			i++;
		}
		return nr;
	}

	//Check if this card can be played
	private boolean canPlay(UNOCard topCard, UNOCard newCard) {

		// Color or value matches
		if (topCard.getColor().equals(newCard.getColor())
				|| topCard.getValue().equals(newCard.getValue()))
			return true;
		// if chosen wild card color matches
		else if (topCard.getType() == WILD)
			return ((WildCard) topCard).getWildColor().equals(newCard.getColor());

		// suppose the new card is a wild card
		else if (newCard.getType() == WILD)
			return true;

		// else
		return false;
	}

	//Check whether the player said or forgot to say UNO
	public void checkUNO() {
		for (Player p : players) {
			if (p.isMyTurn()) {
				if (p.getTotalCards() == 1 && !p.getSaidUNO()) {
					infoPanel.setError(p.getName() + " Forgot to say UNO");
					p.obtainCard(getCard());
					p.obtainCard(getCard());
				}
			}
		}		
	}

	public void setSaidUNO() {
		for (Player p : players) {
			if (p.isMyTurn()) {
				if (p.getTotalCards() == 2) {
					p.saysUNO();
					infoPanel.setError(p.getName() + " said UNO");
				}
			}
		}
	}
	
	public boolean isPCsTurn(){
		if(pc.isMyTurn()){
			return true;
		}
		return false;
	}

	//if it's PC's turn, play it for pc
	public void playPC(UNOCard topCard) {		
		
		if (pc.isMyTurn()) {
			boolean done = pc.play(topCard);
			
			if(!done)
				drawCard(topCard);
		}
	}
}
class PC extends Player implements GameConstants {

	public PC() {
            //sets name
		setName("SuperComputer");
		super.setCards();
	}

	public PC(Player player) {
	}
	
	//PC plays a card
	public boolean play(UNOCard topCard) {

		boolean done = false;

		Color color = topCard.getColor();
		String value = topCard.getValue();
		
		if(topCard.getType()==WILD){
			color = ((WildCard) topCard).getWildColor();			
		}

		for (UNOCard card : getAllCards()) {

			if (card.getColor().equals(color) || card.getValue().equals(value)) {
				
				MouseEvent doPress = new MouseEvent(card, MouseEvent.MOUSE_PRESSED,
						System.currentTimeMillis(),
						(int) MouseEvent.MOUSE_EVENT_MASK, 5, 5, 1, true);				
				card.dispatchEvent(doPress);
				
				MouseEvent doRelease = new MouseEvent(card, MouseEvent.MOUSE_RELEASED,
						System.currentTimeMillis(),
						(int) MouseEvent.MOUSE_EVENT_MASK, 5, 5, 1, true);
				card.dispatchEvent(doRelease);
				
				done = true;
				break;
			}
		}

		// if no card was found, play wild card
		if (!done) {
			for (UNOCard card : getAllCards()) {
				if (card.getType() == WILD) {
					MouseEvent doPress = new MouseEvent(card,
							MouseEvent.MOUSE_PRESSED,
							System.currentTimeMillis(),
							(int) MouseEvent.MOUSE_EVENT_MASK, 5, 5, 1, true);
					card.dispatchEvent(doPress);
					
					MouseEvent doRelease = new MouseEvent(card, MouseEvent.MOUSE_RELEASED,
							System.currentTimeMillis(),
							(int) MouseEvent.MOUSE_EVENT_MASK, 5, 5, 1, true);
					card.dispatchEvent(doRelease);
					
					done = true;
					break;
				}
			}
		}
		
		if(getTotalCards()==1 || getTotalCards()==2)
			saysUNO();
		
		return done;
	}
}
class Player {
	
	private String name = null;
	private boolean isMyTurn = false;
	private boolean saidUNO = false;
	private LinkedList<UNOCard> myCards;
	
	private int playedCards = 0;
	
	public Player(){
		myCards = new LinkedList<UNOCard>();
	}
	
	public Player(String player){
		setName(player);
		myCards = new LinkedList<UNOCard>();
	}
	
	public void setName(String newName){
		name = newName;
	}
	
	public String getName(){
		return this.name;
	}
	public void obtainCard(UNOCard card){
		myCards.add(card);
	}
	
	public LinkedList<UNOCard> getAllCards(){
		return myCards;
	}
	
	public int getTotalCards(){
		return myCards.size();
	}
	
	public boolean hasCard(UNOCard thisCard){
		return myCards.contains(thisCard);		
	}
	
	public void removeCard(UNOCard thisCard){
		myCards.remove(thisCard);
		playedCards++;
	}
	
	public int totalPlayedCards(){
		return playedCards;
	}
	
	public void toggleTurn(){
		isMyTurn = (isMyTurn) ? false : true;
	}
	
	public boolean isMyTurn(){
		return isMyTurn;
	}
	
	public boolean hasCards(){
		return (myCards.isEmpty()) ? false : true;
	}
	
	public boolean getSaidUNO(){
		return saidUNO;
	}
	
	public void saysUNO(){
		saidUNO = true;
	}
	
	public void setSaidUNOFalse(){
		saidUNO = false;
	}
	
	public void setCards(){
		myCards = new LinkedList<UNOCard>();
	}
}

interface CardInterface{
	
	int WIDTH = 50;
	int HEIGHT = 75;
	Dimension SMALL = new Dimension(WIDTH,HEIGHT);
	Dimension MEDIUM = new Dimension(WIDTH*2,HEIGHT*2);
	Dimension BIG = new Dimension(WIDTH*3,HEIGHT*3);	
	
	//Default card size
	Dimension CARDSIZE = MEDIUM;
	
	//Default offset
	int OFFSET = 71;
	
	void setColor(Color newColor);
	Color getColor();
	
	void setValue(String newValue);
	String getValue();
	
	void setType(int newType);
	int getType();
}

interface GameConstants extends UNOConstants {
	
	int TOTAL_CARDS = 108;
	int FIRSTHAND = 8;
	
	Color[] UNO_COLORS = {RED, BLUE, GREEN, YELLOW};
	Color WILD_CARDCOLOR = BLACK;
	
	int[] UNO_NUMBERS =  {0,1,2,3,4,5,6,7,8,9};	
	String[] ActionTypes = {REVERSE,SKIP,DRAW2PLUS};	
	String[] WildTypes = {W_COLORPICKER, W_DRAW4PLUS};
	
	int vsPC = 1;
	int MANUAL = 2;
	
	int[] GAMEMODES = {vsPC, MANUAL};
	
	MyCardListener CARDLISTENER = new MyCardListener();
	MyButtonListener BUTTONLISTENER = new MyButtonListener();
	
	InfoPanel infoPanel = new InfoPanel();
}

interface UNOConstants {
	
	//Colors
	public static Color RED = new Color(192,80,77);
	public static Color BLUE = new Color(31,73,125);
	public static Color GREEN = new Color(0,153,0);
	public static Color YELLOW = new Color(255,204,0);
	
	public static Color BLACK = new Color(0,0,0);
	
	//Types
	public static int NUMBERS = 1;
	public static int ACTION = 2;
	public static int WILD = 3;
	
	//ActionCard Characters
	Character charREVERSE = (char) 8634;							//Decimal
	Character charSKIP    = (char) Integer.parseInt("2718",16); 	//Unicode
	
	//ActionCard Functions
	String REVERSE = charREVERSE.toString();
	String SKIP	= charSKIP.toString();
	String DRAW2PLUS = "2+";
	
	//Wild card functions
	String W_COLORPICKER = "W";
	String W_DRAW4PLUS = "4+";	
}

class MyButtonListener implements ActionListener {
		
	Server myServer;
	
	public void setServer(Server server){
		myServer = server;
	}
	
	public void drawCard() {
		if(myServer.canPlay)
			myServer.requestCard();	
	}
	
	public void sayUNO() {
		if(myServer.canPlay)
			myServer.submitSaidUNO();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}

	
}

class MyCardListener extends MouseAdapter {
	
	UNOCard sourceCard;
	Server myServer;
	
	public void setServer(Server server){
		myServer = server;
	}
	
	public void mousePressed(MouseEvent e) {		
		sourceCard = (UNOCard) e.getSource();
		
		try{
			if(myServer.canPlay)
				myServer.playThisCard(sourceCard);			
			
		}catch(NullPointerException ex){
			ex.printStackTrace();
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		super.mouseEntered(e);		
		
		sourceCard = (UNOCard) e.getSource();
		Point p = sourceCard.getLocation();
		p.y -=20;
		sourceCard.setLocation(p);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		sourceCard = (UNOCard) e.getSource();
		Point p = sourceCard.getLocation();
		p.y +=20;
		sourceCard.setLocation(p);
	}	

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}	

}
class Server implements GameConstants {
	private Game game;
	private Session session;
        //using data structure stack which takes in objects of type uno card...
	private Stack<UNOCard> playedCards;
	public boolean canPlay;
	private int mode;

	public Server() {
                
		mode = requestMode();
                //request mode return what type of game to be played
		game = new Game(mode);
		playedCards = new Stack<UNOCard>();

		// First Card
		UNOCard firstCard = game.getCard();
		modifyFirstCard(firstCard);

		playedCards.add(firstCard);
		session = new Session(game, firstCard);

		game.whoseTurn();
		canPlay = true;
	}

	//return if it's 2-Player's mode or PC-mode
	private int requestMode() {

		Object[] options = { "vs PC", "Manual", "Cancel" };

		int n = JOptionPane.showOptionDialog(null,
				"Choose a Game Mode to play", "Game Mode",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[0]);

		if (n == 2)
			System.exit(1);

		return GAMEMODES[n];
	}
	
	//coustom settings for the first card
	private void modifyFirstCard(UNOCard firstCard) {
		firstCard.removeMouseListener(CARDLISTENER);
		if (firstCard.getType() == WILD) {
			int random = new Random().nextInt() % 4;
			try {
				((WildCard) firstCard).useWildColor(UNO_COLORS[Math.abs(random)]);
			} catch (Exception ex) {
				System.out.println("something wrong with modifyFirstcard");
			}
		}
	}
	
	//return Main Panel
	public Session getSession() {
		return this.session;
	}
	
	
	//request to play a card
	public void playThisCard(UNOCard clickedCard) {

		// Check player's turn
		if (!isHisTurn(clickedCard)) {
			infoPanel.setError("It's not your turn");
			infoPanel.repaint();
		} else {

			// Card validation
			if (isValidMove(clickedCard)) {

				clickedCard.removeMouseListener(CARDLISTENER);
				playedCards.add(clickedCard);
				game.removePlayedCard(clickedCard);

				// function cards ??
				switch (clickedCard.getType()) {
				case ACTION:
					performAction(clickedCard);
					break;
				case WILD:
					performWild((WildCard) clickedCard);
					break;
				default:
					break;
				}

				game.switchTurn();
				session.updatePanel(clickedCard);
				checkResults();
			} else {
				infoPanel.setError("invalid move");
				infoPanel.repaint();
			}
			
		}
		
		
		
		if(mode==vsPC && canPlay){
			if(game.isPCsTurn()){
				game.playPC(peekTopCard());
			}
		}
	}

	//Check if the game is over
	private void checkResults() {

		if (game.isOver()) {
			canPlay = false;
			infoPanel.updateText("GAME OVER");
		}
	}
	
	//check player's turn
	public boolean isHisTurn(UNOCard clickedCard) {

		for (Player p : game.getPlayers()) {
			if (p.hasCard(clickedCard) && p.isMyTurn())
				return true;
		}
		return false;
	}

	//check if it is a valid card
	public boolean isValidMove(UNOCard playedCard) {
		UNOCard topCard = peekTopCard();

		if (playedCard.getColor().equals(topCard.getColor())
				|| playedCard.getValue().equals(topCard.getValue())) {
			return true;
		}

		else if (playedCard.getType() == WILD) {
			return true;
		} else if (topCard.getType() == WILD) {
			Color color = ((WildCard) topCard).getWildColor();
			if (color.equals(playedCard.getColor()))
				return true;
		}
		return false;
	}

	// ActionCards
	private void performAction(UNOCard actionCard) {

		// Draw2PLUS
		if (actionCard.getValue().equals(DRAW2PLUS))
			game.drawPlus(2);
		else if (actionCard.getValue().equals(REVERSE))
			game.switchTurn();
		else if (actionCard.getValue().equals(SKIP))
			game.switchTurn();
	}

	private void performWild(WildCard functionCard) {		

		//System.out.println(game.whoseTurn());
		if(mode==1 && game.isPCsTurn()){			
			int random = new Random().nextInt() % 4;
			functionCard.useWildColor(UNO_COLORS[Math.abs(random)]);
		}
		else{
			
			ArrayList<String> colors = new ArrayList<String>();
			colors.add("RED");
			colors.add("BLUE");
			colors.add("GREEN");
			colors.add("YELLOW");
			
			String chosenColor = (String) JOptionPane.showInputDialog(null,
					"Choose a color", "Wild Card Color",
					JOptionPane.DEFAULT_OPTION, null, colors.toArray(), null);
	
			functionCard.useWildColor(UNO_COLORS[colors.indexOf(chosenColor)]);
		}
		
		if (functionCard.getValue().equals(W_DRAW4PLUS))
			game.drawPlus(4);
	}
	
	public void requestCard() {
		game.drawCard(peekTopCard());
		
		if(mode==vsPC && canPlay){
			if(game.isPCsTurn())
				game.playPC(peekTopCard());
		}
		
		session.refreshPanel();
	}

	public UNOCard peekTopCard() {
		return playedCards.peek();
	}

	public void submitSaidUNO() {
		game.setSaidUNO();
	}
}
class InfoPanel extends JPanel {
	
	private String error;
	private String text;
	private int panelCenter;
	
	private int you = 0;
	private int pc = 0;
	private int rest = 0;
	
	public InfoPanel(){
		setPreferredSize(new Dimension(275,200));
		setOpaque(false);
		error = "";
		text = "Game Started";
		
		updateText(text);
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		panelCenter = getWidth()/2;
		
		printMessage(g);
		printError(g);
		printDetail(g);
	}

	private void printError(Graphics g) {
		if(!error.isEmpty()){
			Font adjustedFont = new Font("Calibri", Font.PLAIN,	25);
			
			//Determine the width of the word to position
			FontMetrics fm = this.getFontMetrics(adjustedFont);
			int xPos = panelCenter - fm.stringWidth(error) / 2;
			
			g.setFont(adjustedFont);
			g.setColor(Color.red);
			g.drawString(error, xPos, 35);
			
			error = "";
		}
	}

	private void printMessage(Graphics g) {
		Font adjustedFont = new Font("Calibri", Font.BOLD,	25);	
		
		//Determine the width of the word to position
		FontMetrics fm = this.getFontMetrics(adjustedFont);
		int xPos = panelCenter - fm.stringWidth(text) / 2;
		
		g.setFont(adjustedFont);
		g.setColor(new Color(228,108,10));
		g.drawString(text, xPos, 75);		
	}
	
	private void printDetail(Graphics g){
		Font adjustedFont = new Font("Calibri", Font.BOLD,	25);	
		FontMetrics fm = this.getFontMetrics(adjustedFont);
		g.setColor(new Color(127,127,127));
		
		//Determine the width of the word to position
		String text = "Played Cards";
		int xPos = panelCenter - fm.stringWidth(text) / 2;
		
		g.setFont(adjustedFont);
		g.drawString(text, xPos, 120);
		
		text = "Remaining: " + rest;
		xPos = panelCenter - fm.stringWidth(text) / 2;
		g.drawString(text, xPos, 180);
		
		//Details
		adjustedFont = new Font("Calibri", Font.PLAIN,	20);
		g.setFont(adjustedFont);
		fm = this.getFontMetrics(adjustedFont);
		
		text = "You : "+you + "  PC : " + pc;
		xPos = panelCenter - fm.stringWidth(text) / 2;
		g.drawString(text, xPos, 140);
		
		text = String.valueOf(rest);
		xPos = panelCenter - fm.stringWidth(text) / 2;
		//g.drawString(text, xPos, 190);
	}

	public void updateText(String newText) {
		text = newText;
	}
	
	public void setError(String errorMgs){
		error = errorMgs;
	}
	
	public void setDetail(int[] playedCards, int remaining){
		you = playedCards[1];
		pc = playedCards[0];
		rest = remaining;
	}
}

class MainFrame extends JFrame implements GameConstants {
	
	private Session mainPanel;
	private Server server;
	
	public MainFrame(){	
		server = new Server();
		CARDLISTENER.setServer(server);
		BUTTONLISTENER.setServer(server);
		
		mainPanel = server.getSession();
		add(mainPanel);
	}
}

class PlayerPanel extends JPanel implements GameConstants {

	private Player player;
	private String name;

	private Box myLayout;
        //a box like JPanel has a special layout in which components run horizontally or vertically
	private JLayeredPane cardHolder;
        //JLayered Pane somewhat similar to Jpanel ..but it provides depthfor positioning components
	private Box controlPanel;

	private JButton draw;
	private JButton sayUNO;
	private JLabel nameLbl;
	private MyButtonHandler handler;

	// Constructor
	public PlayerPanel(Player newPlayer) {
		setPlayer(newPlayer);

		myLayout = Box.createHorizontalBox();
                //creates a Horizontal Box where swing components can be added
		cardHolder = new JLayeredPane();
		cardHolder.setPreferredSize(new Dimension(600, 175));

		// Set
		setCards();
		setControlPanel();

		myLayout.add(cardHolder);
		myLayout.add(Box.createHorizontalStrut(40));
		myLayout.add(controlPanel);
		add(myLayout);

		// Register Listeners
		handler = new MyButtonHandler();
		draw.addActionListener(BUTTONLISTENER);
		draw.addActionListener(handler);
		
		sayUNO.addActionListener(BUTTONLISTENER);
		sayUNO.addActionListener(handler);
	}

	public void setCards() {
		cardHolder.removeAll();

		// Origin point at the center
		Point origin = getPoint(cardHolder.getWidth(), player.getTotalCards());
		int offset = calculateOffset(cardHolder.getWidth(),
				player.getTotalCards());

		int i = 0;
		for (UNOCard card : player.getAllCards()) {
			card.setBounds(origin.x, origin.y, card.CARDSIZE.width,
					card.CARDSIZE.height);
			cardHolder.add(card, i++);
			cardHolder.moveToFront(card);
			origin.x += offset;
		}
		repaint();
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
		setPlayerName(player.getName());
	}

	public void setPlayerName(String playername) {
		this.name = playername;
	}

	private void setControlPanel() {
		draw = new JButton("Draw");
		sayUNO = new JButton("Say UNO");
		nameLbl = new JLabel(name);

		// style
		draw.setBackground(new Color(79, 129, 189));
		draw.setFont(new Font("Arial", Font.BOLD, 20));
		draw.setFocusable(false);

		sayUNO.setBackground(new Color(149, 55, 53));
		sayUNO.setFont(new Font("Arial", Font.BOLD, 20));
		sayUNO.setFocusable(false);

		nameLbl.setForeground(Color.WHITE);
		nameLbl.setFont(new Font("Arial", Font.BOLD, 15));

		controlPanel = Box.createVerticalBox();
		controlPanel.add(nameLbl);
		controlPanel.add(draw);
                //strut is a fixed width invisible component that forces a certain amount of space between componenets
		controlPanel.add(Box.createVerticalStrut(15));
		controlPanel.add(sayUNO);
	}

	private int calculateOffset(int width, int totalCards) {
		int offset = 71;
		if (totalCards <= 8) {
			return offset;
		} else {
			double o = (width - 100) / (totalCards - 1);
			return (int) o;
		}
	}

	private Point getPoint(int width, int totalCards) {
		Point p = new Point(0, 20);
		if (totalCards >= 8) {
			return p;
		} else {
			p.x = (width - calculateOffset(width, totalCards) * totalCards) / 2;
			return p;
		}
	}
	
	class MyButtonHandler implements ActionListener{
		
		public void actionPerformed(ActionEvent e) {
			
			if(player.isMyTurn()){
				
				if(e.getSource()==draw)
					BUTTONLISTENER.drawCard();
				else if(e.getSource()==sayUNO)
					BUTTONLISTENER.sayUNO();
			}
		}
	}
}

class Session extends JPanel {
	private PlayerPanel player1;
	private PlayerPanel player2;
	private TablePanel table;	
	
	private Game game;
	
	public Session(Game newGame, UNOCard firstCard){
            //size of the game box
		setPreferredSize(new Dimension(960,720));
		setBackground(new Color(30,36,40));
                //in borderlayout we can place componenets in any 4 borders of the container adn the center
		setLayout(new BorderLayout());
		
		game = newGame;
		
		setPlayers();
		table = new TablePanel(firstCard);
		player1.setOpaque(false);
		player2.setOpaque(false);
		
		add(player1,BorderLayout.NORTH);
		add(table, BorderLayout.CENTER);
		add(player2, BorderLayout.SOUTH);		
	}
	
	private void setPlayers() {
		player1 = new PlayerPanel(game.getPlayers()[0]);
		player2 = new PlayerPanel(game.getPlayers()[1]);		
	}
	
	public void refreshPanel(){
		player1.setCards();
		player2.setCards();
		
		table.revalidate();		
		revalidate();
	}
	
	public void updatePanel(UNOCard playedCard){
		table.setPlayedCard(playedCard);
		refreshPanel();
	}	
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
}


class TablePanel extends JPanel implements GameConstants {
	
	private UNOCard topCard;	
	private JPanel table;
	
	public TablePanel(UNOCard firstCard){
		setOpaque(false);
		setLayout(new GridBagLayout());
		
		topCard = firstCard;
		table = new JPanel();
		table.setBackground(new Color(64,64,64));
		
		setTable();
		setComponents();
	}
	
	private void setTable(){
		
		table.setPreferredSize(new Dimension(500,200));
		table.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		table.add(topCard, c);
	}
	
	private void setComponents() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 130, 0, 45);
		add(table,c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(0, 1, 0, 1);
		add(infoPanel, c);	
	}

	public void setPlayedCard(UNOCard playedCard){
		table.removeAll();
		topCard = playedCard;
		setTable();
		
		setBackgroundColor(playedCard);
	}
	
	public void setBackgroundColor(UNOCard playedCard){
		Color background;
		if(playedCard.getType()==WILD)
			background = ((WildCard) playedCard).getWildColor();
		else
			background = playedCard.getColor();
		
		table.setBackground(background);
	}
}
abstract class UNOCard extends JPanel implements CardInterface, UNOConstants {
//implementation of abstract class	
	private Color cardColor = null;
	private String value = null;
	private int type = 0;
	
	private Border defaultBorder = BorderFactory.createEtchedBorder(WHEN_FOCUSED, Color.white, Color.gray);
        //when mouse is placed on the card it gets a white background or black background 
	private Border focusedBorder = BorderFactory.createEtchedBorder(WHEN_FOCUSED, Color.black, Color.gray);
	
	public UNOCard(){
	}
	
	public UNOCard(Color cardColor, int cardType, String cardValue){
		this.cardColor = cardColor;
		this.type = cardType;
		this.value = cardValue;
		
		this.setPreferredSize(CARDSIZE);//size of card
		this.setBorder(defaultBorder);
		
		this.addMouseListener(new MouseHandler());
	}
	
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
		int cardWidth = CARDSIZE.width;
		int cardHeight = CARDSIZE.height;
		
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, cardWidth, cardHeight);
		
		int margin = 5;
		g2.setColor(cardColor);
		g2.fillRect(margin, margin, cardWidth-2*margin, cardHeight-2*margin);
		
		g2.setColor(Color.white);
		AffineTransform org = g2.getTransform();
		g2.rotate(45,cardWidth*3/4,cardHeight*3/4);		

		g2.fillOval(0,cardHeight*1/4,cardWidth*3/5, cardHeight);
		g2.setTransform(org);		
		
		//Value in the center		
		Font defaultFont = new Font("Helvetica", Font.BOLD, cardWidth/2+5);		
		FontMetrics fm = this.getFontMetrics(defaultFont);
		int StringWidth = fm.stringWidth(value)/2;
		int FontHeight = defaultFont.getSize()*1/3;
		
		g2.setColor(cardColor);
		g2.setFont(defaultFont);
		g2.drawString(value, cardWidth/2-StringWidth, cardHeight/2+FontHeight);
		
		//Value in the corner
		defaultFont = new Font("Helvetica", Font.ITALIC, cardWidth/5);		
		fm = this.getFontMetrics(defaultFont);
		StringWidth = fm.stringWidth(value)/2;
		FontHeight = defaultFont.getSize()*1/3;
		
		g2.setColor(Color.white);
		g2.setFont(defaultFont);
		g2.drawString(value, 2*margin,5*margin);		
	}	
	
	/**
	 * My Mouse Listener 
	 */
	class MouseHandler extends MouseAdapter {
		
		public void mouseEntered(MouseEvent e){
			setBorder(focusedBorder);
		}
		
		public void mouseExited(MouseEvent e){
			setBorder(defaultBorder);
		}
	}
	
	public void setCardSize(Dimension newSize){
		this.setPreferredSize(newSize);
	}
	
	public void setColor(Color newColor) {
		this.cardColor = newColor;
	}

	public Color getColor() {
		return cardColor;
	}

	@Override
	public void setValue(String newValue) {
		this.value = newValue;		
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setType(int newType) {
		this.type = newType;
	}

	@Override
	public int getType() {
		return type;
	}
}

public class UnoX {
	
	public static void main(String[] args) {
		
		//Create Frame and invoke it.
                //Swing utilities are not thread safe...as there a lot of gui changes goin on this program we can fire off these changes regularly using this
		SwingUtilities.invokeLater(new Runnable() {					
			public void run() {
				JFrame frame = new MainFrame();
                                frame.setVisible(true);
                                //keeps the gui box visisble..if set false it goes invisible
				
				frame.setResizable(false);
                                //cant resize the box with mox
				frame.setLocation(200, 100);
                                //the Location of the box set to x,y 200 ,100
                                //the pack method sizes the frame so that all contents are at thier preferred sizes
				frame.pack();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                //keeps the close button on the box ..'X'
			}
		});	
	}
}

