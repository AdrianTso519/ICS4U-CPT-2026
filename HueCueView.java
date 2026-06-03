//******************************************************************************
// Program Name: 	Hues and Cues View
// Authors: 		Adrian Tso, Hansel Suen, Ethan Wong
// Date: 			June 9th 2026
// School: 			St. Augustine CHS Computer Science
// Description: 	An online version of the Hues and Cues board game 
//******************************************************************************

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

public class HueCueView implements ActionListener, MouseMotionListener, MouseListener{

	// Properties
	static boolean blnOnMain = false;
	int ColumnClick = -100;
	int RowClick = -100;
	JFrame theFrame = new JFrame("CPT");
	boolean blnHost = false;
	boolean blnJoined = false;
	int intPort = 6767;
	// game panels
	GamePanel theGamePanel;
	GeneralPanel theMenuPanel;
	GeneralPanel theHelpPanel;
	GeneralPanel theAboutPanel;
	GeneralPanel theJoinPanel;
	GeneralPanel theWaitPanel; 
	JPanel theGamePanelOverlay = new JPanel();

	// timer
	Timer theTimer = new Timer(1000 / 60, this);
	Timer theGameTimer = new Timer(20000, this);
	Timer theScoreTimer = new Timer(15000, this);

	// Main Menu
	JButton theHost = new JButton("Host");
	JButton theJoin = new JButton("Join");
	JButton theHelp = new JButton("Help");
	JButton theAbout = new JButton("About");
	JButton theQuit = new JButton("Quit");
	Font fntLabels = new Font("Impact", 0, 30);
	Font fntButton = new Font("Impact", 0, 30);
	Font fntTitle = new Font("Impact", 0, 75);

	// Host Menu/Waiting Room
	JLabel theWaitingRoom = new JLabel("Waiting...", SwingConstants.CENTER);
	JLabel theIP = new JLabel(/* Insert IP and port number Here */);
	JButton theStart = new JButton("Start");
	JTextArea theArea = new JTextArea();
	JScrollPane theScroll = new JScrollPane(theArea);
	JTextField theField = new JTextField();
	JButton theBack = new JButton("Back");
	
	// port variables
	JLabel thePort  = new JLabel();
	String strIP = "";
	
	//Waiting room stuff
	JTextArea waitChatArea = new JTextArea();
	JScrollPane waitChatScroll = new JScrollPane(waitChatArea);
	JTextField waitChatField = new JTextField();

	// Join Menu
	JLabel theJoinTitle = new JLabel("Join", SwingConstants.CENTER);
	JTextField theIPInput = new JTextField();
	JTextField theUserName = new JTextField();
	JTextField thePortNum = new JTextField();
	JLabel theIPLabel = new JLabel("IP:");
	JLabel theUserLabel = new JLabel("User:");
	JLabel thePortLabel = new JLabel("Port:");
	JButton theConnect = new JButton("Connect");
	// use theBack to go back to main menu

	// Help Menu
	JLabel theHelpTitle = new JLabel("Help", SwingConstants.CENTER);
	JTextArea theHelpText = new JTextArea(/* insert game explanation here */);
	JButton theHelpButton = new JButton("Next");
	// use theBack to go back to main menu

	// About Menu
	JLabel theAboutTitle = new JLabel("About", SwingConstants.CENTER);
	JLabel theAboutAuthor = new JLabel("Created by: Hansel S., Adrian T., Ethan W.");
	JLabel theAboutDate = new JLabel("Created on: June 9, 2026");
	JLabel theAboutCourse = new JLabel("Course: ICS4U1");
	JLabel theAboutTeacher = new JLabel("Teacher: Mr. Cadawas");
	JLabel theAboutInspirtion = new JLabel("Inspiration: Hues and Cues");
	// use theBack to go back to main menu

	// Game Menu
	// use the same text area
	// use the same text field
	JLabel theP1Points = new JLabel("Score: ", SwingConstants.CENTER);
	JLabel theP2Points = new JLabel("Score: ", SwingConstants.CENTER);
	JLabel theP3Points = new JLabel("Score: ", SwingConstants.CENTER);
	JLabel theP4Points = new JLabel("Score: ", SwingConstants.CENTER);
	JLabel theP5Points = new JLabel("Score: ", SwingConstants.CENTER);
	JLabel theP6Points = new JLabel("Score: ", SwingConstants.CENTER);
	
	JLabel theP1Name = new JLabel("Player 1: ", SwingConstants.CENTER);
	JLabel theP2Name = new JLabel("Player 2: ", SwingConstants.CENTER);
	JLabel theP3Name = new JLabel("Player 3: ", SwingConstants.CENTER);
	JLabel theP4Name = new JLabel("Player 4: ", SwingConstants.CENTER);
	JLabel theP5Name = new JLabel("Player 5: ", SwingConstants.CENTER);
	JLabel theP6Name = new JLabel("Player 6: ", SwingConstants.CENTER);
	JComponent GameMenu[];

	// Network Connection Properties
	SuperSocketMaster Socket = null;

	HueCueModel model;

	// Mandatory Methods
	public void actionPerformed(ActionEvent evt) {
		// Field triggered
		if(evt.getSource() == theGameTimer){
			theGameTimer.stop();
			model.nextState(Socket);
			stateChanges();
			System.out.println(model.intGameState);
		}else if(evt.getSource() == theScoreTimer){
			theScoreTimer.stop();
			model.nextRound(Socket);
			model.nextState(Socket);
			stateChanges();
			System.out.println(model.intCueGiver+" "+model.intGameState);
		}else if(evt.getSource() == theTimer){
			theGamePanel.repaint();
		}else if (evt.getSource() == theField) {
			System.out.println("Field event triggered");
			Socket.sendText(theField.getText());
			theField.setText("");
			
		}else if (evt.getSource() == waitChatField) {
			System.out.println("Lobby chat text sent");
				
			if (!waitChatField.getText().trim().equals("")) {
				if (Socket != null) {
					if(model.intCueGiver == model.intPlayerNumber){
						if(model.intGameState == 1 && model.blnCueGiven == false){
							Socket.sendText("<SYSETM> The First Cue is: "+waitChatField.getText());
							Socket.sendText("<CUE1>");
							model.nextState(Socket);
							System.out.println(model.intGameState);
							theGameTimer.start();
							model.blnCueGiven = true;
						}else if(model.intGameState == 3 && model.blnCueGiven == false){
							Socket.sendText("<SYSETM> The Second Cue is: "+waitChatField.getText());
							Socket.sendText("<CUE2>");
							model.nextState(Socket);
							System.out.println(model.intGameState);
							theGameTimer.start();
							model.blnCueGiven = true;
						}else{
							Socket.sendText("<"+model.username+"> "+waitChatField.getText());
						}
					}else{
						Socket.sendText("<"+model.username+"> "+waitChatField.getText());
					}
				}
				waitChatArea.append("<You> " + waitChatField.getText() + "\n");
			}

			waitChatField.setText("");
			
			// Button Triggered
		} else if (evt.getSource() == theConnect) {
			System.out.println("button event triggered");
			
			String targetIP = theIPInput.getText();
			int targetPort = Integer.parseInt(thePortNum.getText());
			model.username = theUserName.getText();
			
			// Safety check: Don't try connecting if they didn't replace the placeholder
			if (targetIP.equals("") || targetIP.trim().isEmpty()) {
				System.out.println("Please enter a valid game code / IP address first!");
				return;
			}

			// tell user they are connecting
			System.out.println("Attempting connection to: " + targetIP);
			clientConnect(targetIP, targetPort);
			// send text when user joins game
			Socket.sendText("<JOIN> "+model.username);
			Socket.sendText("<SYSTEM> "+model.username+" joined the room");
			this.blnJoined = true;
			
			// --- TRANSPORT TO THE WAITING ROOM ---
			// Move the back button to the waiting room panel dynamically
			theWaitPanel.add(theBack);
			
			// Swap the main frame content pane to show the wait lobby
			theFrame.setContentPane(theWaitPanel);
			theFrame.revalidate();
			theFrame.repaint();
			
			// Socket triggered
		} else if (evt.getSource() == Socket) {
			System.out.println("Socket event triggered");
			String strLine = Socket.readText();
			// Detection of backend messages like player count, system messages
			if(strLine.startsWith("<JOIN>")){
				if(blnHost == true){
					model.storeUserName(strLine.substring(6).trim(), Socket);
				}
			}else if(strLine.startsWith("<COUNT>")){
				model.intPlayerCount = Integer.parseInt(strLine.substring(8,9));
				System.out.println(model.intPlayerCount+" Players");
			}else if(strLine.startsWith("<DISCONNECT>")){
				if(blnHost == true){
					model.removeUserName(strLine.substring(13).trim(), Socket);
				}
			// --- NETWORK COMMAND DETECTION ---
			}else if(strLine.equals("<START>")) {
				// Clients receive this message and instantly switch to their game boards
				showLabels(model.intPlayerCount);
				theFrame.setContentPane(theGamePanel);
				theFrame.revalidate();
				theFrame.repaint();
				theWaitPanel.remove(waitChatField);
				theWaitPanel.remove(waitChatScroll);
				theGamePanel.add(waitChatField);
				theGamePanel.add(waitChatScroll);
				//theTimer.start(); // Start your 60 FPS repaint loop
			}else if(strLine.equals("<CLOSE>")){
				theBack();
			}else if(strLine.startsWith("<PLAYERS>")){
				String strPlayers = strLine.substring(11);
				model.loadPlayerData(strPlayers);
				waitChatArea.append("<SYSTEM> You are Player "+model.intPlayerNumber);
			}else if(strLine.startsWith("<CUER>")){
				model.intCueGiver = Integer.parseInt(strLine.substring(6,7));
				if(model.intCueGiver == model.intPlayerNumber){
					model.blnCueGiver = true;
				}else{
					model.blnCueGiver = false;
				}
			}else if(strLine.startsWith("<STATE>")){
				model.intGameState = Integer.parseInt(strLine.substring(7,8));
				stateChanges();
			}else if(strLine.equals("<CUE1>")){
				waitChatArea.append("<SYSTEM> You have 20 seconds to guess the Hue!\n");
			}else if(strLine.equals("<CUE2>")){
				waitChatArea.append("<SYSTEM> You have 20 seconds to guess the Hue\n");
			}else if(strLine.startsWith("<TARGET>")){
				String strTile = strLine.substring(8);
				String[] strTargetTile = strTile.split(",");

				int intRow = Integer.parseInt(strTargetTile[0]);
				int intCol = Integer.parseInt(strTargetTile[1]);

				model.intRandomTile[0] = intRow;
				model.intRandomTile[1] = intCol;
				
				theGamePanel.passRandPos(intRow, intCol);
				model.intMyScore += model.getScore(model.intRandomTile, RowClick, ColumnClick);
				if(model.getScore(model.intRandomTile, RowClick, ColumnClick) > 0){
					Socket.sendText("<SCORED?> Y");
				}else{
					Socket.sendText("<SCORED?> N");
				}
				waitChatArea.append("<SYSTEM> Your Score: "+model.intMyScore+"\n");
			}else if(strLine.startsWith("<SCORED?>")){
				if(model.intCueGiver == model.intPlayerNumber){
					String strKey = (strLine.substring(10, 11));
					if(strKey.equals("Y")){
						model.intMyScore += 1;
					}
					waitChatArea.append("<SYSTEM> Your Score: "+model.intMyScore+"\n");
				}
			}else if(strLine.startsWith("<PLAYERPOS>")){
				String strTile = strLine.substring(12);
				String[] strClickedTile = strTile.split(",");
				int intUNumber = Integer.parseInt(strClickedTile[0]);
				int intUX = Integer.parseInt(strClickedTile[1]);
				int intUY = Integer.parseInt(strClickedTile[2]);
				model.savePlayerPos(intUNumber, intUX, intUY);
				
			}else{
				waitChatArea.append(strLine+"\n");
				waitChatArea.setCaretPosition(waitChatArea.getDocument().getLength());
			}
			
			// --- LIVE CHECK PLAYER COUNT FOR THE LOBBY BUTTON ---
			// Enable start button if player counts are between 3 and 6 (inclusive) and user is host
			if (this.blnHost && model.intPlayerCount >= 3 && model.intPlayerCount <= 6) {
				theStart.setVisible(true);
			} else {
				theStart.setVisible(false);
			}
			if(model.intPlayerCount == 6){
				theStart();
			}
			
		}else if(evt.getSource() == theHost){
			this.blnHost = true;
			theWaitPanel.add(theBack);
			theFrame.setContentPane(theWaitPanel);
			theFrame.revalidate();
			theFrame.repaint();
			hostConnect();
			model.strUserName[1] = model.username;
			model.intPlayerCount = 1;
			strIP = Socket.getMyAddress();
			theIP.setText("IP: " + strIP);
			thePort.setText("Port: " + this.intPort);
			
		}else if(evt.getSource() == theJoin){
			System.out.println("I am Player "+model.intPlayerNumber);
			theJoinPanel.add(theBack);
			theFrame.setContentPane(theJoinPanel);
			theFrame.revalidate();
			theFrame.repaint();
			
		}else if(evt.getSource() == theHelp){
			theHelpPanel.add(theBack);
			theFrame.setContentPane(theHelpPanel);
			theFrame.revalidate();
			theFrame.repaint();
			theHelpText.setText("");
			theHelpText.append("One player has a colored tile that the have to try to give one word hints to the other players");

			
		}else if(evt.getSource() == theAbout){
			theAboutPanel.add(theBack);
			theFrame.setContentPane(theAboutPanel);
			theFrame.revalidate();
			theFrame.repaint();
			
		}else if(evt.getSource() == theStart){
			theStart();
		}else if(evt.getSource() == theQuit){
			System.exit(0);
			
		}else if(evt.getSource() == theBack){
			if(theWaitPanel.isShowing() && this.blnHost == true){
				Socket.sendText("<CLOSE>");
			}
			model.intHelpCnt = 0;
			//model.strUserName = null;
			theBack();
		}else if(evt.getSource() == theHelpButton){
			model.intHelpCnt++;
			switch (model.intHelpCnt) {
				case 1 -> {
                     theHelpText.setText("");
    	             theHelpText.append("The other players have to try and guess/place a tile on the color that they think the clue giver has");
                }case 2 -> {
					theHelpText.setText("");
					theHelpText.append("After the other players have chosen a tile, the clue giver gives another hint");
				}case 3 -> {
					theHelpPanel.repaint();
					theHelpText.setText("");
					theHelpText.append("The guessers can now select a new tile based on both hints given\r\n" +
						"If the guesser gets the correct tile, they get 3 points\r\n" +
						"If the tile chosen is one adjacent to the tile then they get 2 points\r\n" +
						"If the tile chosen is 2 adjacent away from the correct tile they get 1 point");
				}case 4 -> {
					theHelpPanel.repaint();
					theHelpText.setText("");
					theHelpText.append("Players not within this range do not get points\r\n" +
						"The clue giver get points equal to the number of people that scored");
				}case 5 -> {
					theHelpText.setText("");
					theHelpText.append("The player that gives hints changes with every round\r\n" + 
						"The player with the most points at the end, wins!");
					theHelpButton.setVisible(false);
				}default -> {
                        }
			}
		}
		if(theMenuPanel.isShowing()){
			blnOnMain = true;
		}else{
			blnOnMain = false;
		}
	}

	public void stateChanges(){
		if(model.intGameState == 1){
			for(int intCount = 1; intCount <= 6; intCount++){
				model.intUserClicks[intCount][0] = -100;
				model.intUserClicks[intCount][1] = -100;
			}
			theGamePanel.passRandPos(-1000, -1000);
			theGamePanel.passClickPos(-100, -100);
			waitChatArea.append("\n<SYSTEM> Player "+model.intCueGiver+" is now giving the first Cue\n");
			theGamePanel.removeMouseListener(this);
			if(model.intCueGiver == model.intPlayerNumber){
				model.blnCueGiven = false;
				model.intRandomTile = model.generateTargetTile();
				char chrLetter = (char) ('A' + model.intRandomTile[0] - 1);
				waitChatArea.append("<SYSTEM> Your target tile is: "+chrLetter+model.intRandomTile[1]+"\n");
				waitChatArea.append("<SYSTEM> Please enter a one-word cue\n");
			}
		}else if(model.intGameState == 2){
			if(model.intCueGiver != model.intPlayerNumber){
				theGamePanel.addMouseListener(this);
			}
		}else if(model.intGameState == 3){
			waitChatArea.append("\n<SYSTEM> Player "+model.intCueGiver+" is now giving the second Cue\n");
			theGamePanel.removeMouseListener(this);
			if(model.intCueGiver == model.intPlayerNumber){
				model.blnCueGiven = false;
				waitChatArea.append("<SYSTEM> Please enter a two-word cue\n");
			}
		}else if(model.intGameState == 4){
			if(model.intCueGiver != model.intPlayerNumber){
				theGamePanel.addMouseListener(this);
			}
		}else if(model.intGameState == 5){
			theGamePanel.removeMouseListener(this);
			if(model.intCueGiver == model.intPlayerNumber){
				char chrLetter = (char) ('A' + model.intRandomTile[0] - 1);
				Socket.sendText("<SYSTEM> The target tile is: "+chrLetter+model.intRandomTile[1]+"\n");
				Socket.sendText("<TARGET>"+model.intRandomTile[0]+","+model.intRandomTile[1]);
				theScoreTimer.start();
			}else{
				Socket.sendText("<PLAYERPOS> " + model.intPlayerNumber + "," + ColumnClick + "," + RowClick);
			}
			theGamePanel.passRandPos(model.intRandomTile[0],model.intRandomTile[1]);
		}
	}

	public void mouseMoved(MouseEvent evt) {
	}

	public void mouseDragged(MouseEvent evt) {
	}

	public void mouseExited(MouseEvent evt) {
	}

	public void mouseEntered(MouseEvent evt) {
	}

	public void mouseReleased(MouseEvent evt) {
	}

	public void mouseClicked(MouseEvent evt) {
	}

	public void mousePressed(MouseEvent evt) {
		// Get pixel coordinates of the mouse
		int MouseX = evt.getX();
		int MouseY = evt.getY();

		// Get grid dimensions
		int TileWidth = theGamePanel.tileWidth;
		int TileHeight = theGamePanel.tileHeight;
		int StartX = theGamePanel.GridStartX;
		int StartY = theGamePanel.GridStartY;

		// Convert coordinates to array (rows and columns)
		ColumnClick = (MouseX - StartX) / TileWidth;
		RowClick = (MouseY - StartY) / TileHeight;
		if (RowClick >= 0 && RowClick < 16 && ColumnClick >= 0 && ColumnClick < 30) {
			theGamePanel.passClickPos(ColumnClick, RowClick);
		}else{
			// Draws the rectangle out of the screen if what you clicked isn't part of the grid
			theGamePanel.passClickPos(-100, -100);
		}

		// Check to make sure click is inside grid boundaries
		if (RowClick >= 0 && RowClick < 16 && ColumnClick >= 0 && ColumnClick < 30) {

			// FIXED: use model instead of View array
			HueCueModel.ColourTile clickedTile = model.getTile(RowClick, ColumnClick);

			System.out.println("Clicked Grid Cell is Row: " + (RowClick + 1) + " Column: " + (ColumnClick + 1));

			// print tile clicked to terminal
			if (clickedTile != null) {
				Color c = clickedTile.ColorValue;
				System.out.println("Tile RGB: (" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ")");
			} else {
				System.out.println("Clicked an empty/null tile slot.");
			}
		}
	}
	
	public void clientConnect(String targetIP, int targetPort){
		// Initialize the connection
		Socket = new SuperSocketMaster(targetIP, targetPort, this);
		Socket.connect();
		theConnect.setEnabled(false); // Disable to prevent multiple click spam
		this.blnHost = false;

	}
	
	public void hostConnect(){
		if(this.blnHost == true){
			Socket = new SuperSocketMaster(this.intPort, this);
			Socket.connect();
			System.out.println("Awaiting connections");
		}
	}
	
	public void showLabels(int intPlayerCount){
		// Store labels into arrays to allow easy visible toggling
		JLabel[] nameLabels = { theP1Name, theP2Name, theP3Name, theP4Name, theP5Name, theP6Name };
		JLabel[] scoreLabels = { theP1Points, theP2Points, theP3Points, theP4Points, theP5Points, theP6Points };

		for (int intCount = 0; intCount < 6; intCount++) {
			if (intCount < intPlayerCount) {
				nameLabels[intCount].setVisible(true);
				scoreLabels[intCount].setVisible(true);
			} else {
				nameLabels[intCount].setVisible(false);
				scoreLabels[intCount].setVisible(false);
			}
		}
	}
	
	public void hideLabels(int intPlayerCount){
		// Store labels into arrays to allow easy visible toggling
		JLabel[] nameLabels = { theP1Name, theP2Name, theP3Name, theP4Name, theP5Name, theP6Name };
		JLabel[] scoreLabels = { theP1Points, theP2Points, theP3Points, theP4Points, theP5Points, theP6Points };

		for (int intCount = 0; intCount < 6; intCount++) {
			nameLabels[intCount].setVisible(false);
			scoreLabels[intCount].setVisible(false);
		}
	}
	
	public void theBack(){
		if(this.blnJoined == true){
			// if client presses back send text
			Socket.sendText("<DISCONNECT> "+model.username);
			Socket.sendText("<SYSTEM> "+model.username+" left the room");
			Socket.disconnect();
			theConnect.setEnabled(true);
		}
		waitChatArea.setText("");
		theFrame.setContentPane(theMenuPanel);
		theFrame.revalidate();
		theFrame.repaint();
	}
	
	public void theStart(){
		System.out.println("Host started the game session!");
		
		// Broadcast start signal to all connected clients
		Socket.sendText("<START>");
		model.sendPlayerData(Socket);
		
		if(blnHost){
			waitChatArea.append("<SYSTEM> You are Player "+model.intPlayerNumber+"\n");
			model.nextRound(Socket);
			model.nextState(Socket);
			stateChanges();
		}else{
			waitChatArea.append("<SYSTEM> You are Player "+model.intPlayerNumber+"\n");
		}
		
		// Move the host's screen to the game board immediately
		theWaitPanel.remove(waitChatField);
		theWaitPanel.remove(waitChatScroll);
		theGamePanel.add(waitChatField);
		theGamePanel.add(waitChatScroll);
		theFrame.setContentPane(theGamePanel);
		theFrame.revalidate();
		theFrame.repaint();
		showLabels(model.intPlayerCount);
		// Activate the game panel update timer loop
		//theTimer.start(); 
	}

	// Constructor
	public HueCueView() {

		model = new HueCueModel();
		model.CSVGrid("colors.csv");

		// Panels
		theGamePanel = new GamePanel();
		theGamePanel.setBackground(Color.BLACK);
		theGamePanel.setLayout(null);
		theGamePanel.setPreferredSize(new Dimension(1280, 720));
		//theGamePanel.addMouseListener(this);
		theGamePanel.addMouseMotionListener(this);
		
		
		// set up username & score labels in the game panel
		theP1Name.setBounds(63, 536, 273, 29);
		theP1Name.setForeground(Color.WHITE);
		theP1Name.setFont(fntLabels);
		theGamePanel.add(theP1Name);
		theP1Name.setVisible(false);
		theP1Points.setBounds(63, 565, 273, 29);
		theP1Points.setForeground(Color.WHITE);
		theP1Points.setFont(fntLabels);
		theGamePanel.add(theP1Points);
		theP1Points.setVisible(false);
		
		theP2Name.setBounds(336, 536, 273, 29);
		theP2Name.setForeground(Color.WHITE);
		theP2Name.setFont(fntLabels);
		theGamePanel.add(theP2Name);
		theP2Name.setVisible(false);
		theP2Points.setBounds(336, 565, 273, 29);
		theP2Points.setForeground(Color.WHITE);
		theP2Points.setFont(fntLabels);
		theGamePanel.add(theP2Points);
		theP2Points.setVisible(false);
		
		theP3Name.setBounds(609, 536, 273, 29);
		theP3Name.setForeground(Color.WHITE);
		theP3Name.setFont(fntLabels);
		theGamePanel.add(theP3Name);
		theP3Name.setVisible(false);
		theP3Points.setBounds(609, 565, 273, 29);
		theP3Points.setForeground(Color.WHITE);
		theP3Points.setFont(fntLabels);
		theGamePanel.add(theP3Points);
		theP3Points.setVisible(false);
		
		theP4Name.setBounds(63, 621, 273, 29);
		theP4Name.setForeground(Color.WHITE);
		theP4Name.setFont(fntLabels);
		theGamePanel.add(theP4Name);
		theP4Name.setVisible(false);
		theP4Points.setBounds(63, 650, 273, 29);
		theP4Points.setForeground(Color.WHITE);
		theP4Points.setFont(fntLabels);
		theGamePanel.add(theP4Points);
		theP4Points.setVisible(false);
		
		theP5Name.setBounds(336, 621, 273, 29);
		theP5Name.setForeground(Color.WHITE);
		theP5Name.setFont(fntLabels);
		theGamePanel.add(theP5Name);
		theP5Name.setVisible(false);
		theP5Points.setBounds(336, 650, 273, 29);
		theP5Points.setForeground(Color.WHITE);
		theP5Points.setFont(fntLabels);
		theGamePanel.add(theP5Points);
		theP5Points.setVisible(false);
		
		theP6Name.setBounds(609, 621, 273, 29);
		theP6Name.setForeground(Color.WHITE);
		theP6Name.setFont(fntLabels);
		theGamePanel.add(theP6Name);
		theP6Name.setVisible(false);
		theP6Points.setBounds(609, 650, 273, 29);
		theP6Points.setForeground(Color.WHITE);
		theP6Points.setFont(fntLabels);
		theGamePanel.add(theP6Points);
		theP6Points.setVisible(false);
		
		// menu panel
		theMenuPanel = new GeneralPanel();
		theMenuPanel.setLayout(null);
		theMenuPanel.setPreferredSize(new Dimension(1280, 720));
		
		// help panel
		theHelpPanel = new GeneralPanel();
		theHelpPanel.setLayout(null);
		theHelpPanel.setPreferredSize(new Dimension(1280, 720));

		// Text Area
		theScroll.setBounds(960, 0, 320, 620);
		//theGamePanel.add(theScroll);

		// Text Field
		theField.setBounds(960, 620, 320, 100);
		theField.addActionListener(this);
		//theGamePanel.add(theField);

		// Main Menu
		// set fonts for main menu JComponents
		theHost.setFont(fntButton);
		theJoin.setFont(fntButton);
		theHelp.setFont(fntButton);
		theAbout.setFont(fntButton);
		theQuit.setFont(fntButton);
		theBack.setFont(fntButton);

		// set bounds of main menu JComponents
		theHost.setBounds(570, 250, 140, 50);
		theJoin.setBounds(570, 325, 140, 50);
		theHelp.setBounds(570, 400, 140, 50);
		theAbout.setBounds(570, 475, 140, 50);
		theQuit.setBounds(570, 550, 140, 50);
		theBack.setBounds(590, 550, 100, 50);

		// add the buttons to the menu panel
		theMenuPanel.add(theHost);
		theMenuPanel.add(theJoin);
		theMenuPanel.add(theHelp);
		theMenuPanel.add(theAbout);
		theMenuPanel.add(theQuit);

		// make the buttons invisible (just the text)
		theHost.setOpaque(false);
		theHost.setContentAreaFilled(false);
		theHost.setBorderPainted(false);
		theJoin.setOpaque(false);
		theJoin.setContentAreaFilled(false);
		theJoin.setBorderPainted(false);
		theHelp.setOpaque(false);
		theHelp.setContentAreaFilled(false);
		theHelp.setBorderPainted(false);
		theAbout.setOpaque(false);
		theAbout.setContentAreaFilled(false);
		theAbout.setBorderPainted(false);
		theQuit.setOpaque(false);
		theQuit.setContentAreaFilled(false);
		theQuit.setBorderPainted(false);
		theBack.setOpaque(false);
		theBack.setContentAreaFilled(false);
		theBack.setBorderPainted(false);

		// set the text to the color white
		theHost.setForeground(Color.white);
		theJoin.setForeground(Color.white);
		theHelp.setForeground(Color.white);
		theAbout.setForeground(Color.white);
		theQuit.setForeground(Color.white);
		theBack.setForeground(Color.white);
		
		// add a listener to the buttons
		theHost.addActionListener(this);
		theJoin.addActionListener(this);
		theHelp.addActionListener(this);
		theAbout.addActionListener(this);
		theQuit.addActionListener(this);
		theBack.addActionListener(this);


	// Host Menu
		// set fonts

		// waiting rom panel
		theWaitPanel = new GeneralPanel();
		theWaitPanel.setLayout(null);
		theWaitPanel.setPreferredSize(new Dimension(1280, 720));

		theWaitPanel.add(theWaitingRoom);
		theWaitingRoom.setFont(fntTitle);
		theWaitingRoom.setForeground(Color.white);

		// set bounds
		theWaitingRoom.setBounds(0, 75, 920, 75);
		
		theIP.setBounds(0, 300, 920, 75);
		theIP.setFont(fntButton);
		theIP.setForeground(Color.white);
		theIP.setHorizontalAlignment(JTextField.CENTER);
		
		thePort.setBounds(0, 350, 920, 75);
		thePort.setFont(fntButton);
		thePort.setForeground(Color.white);
		thePort.setHorizontalAlignment(JTextField.CENTER);
		
		theWaitPanel.add(thePort);
		theWaitPanel.add(theIP);
		
		// --- START BUTTON HERE ---
		theStart.setBounds(360, 450, 200, 60); // Centered on the left half of the lobby
		theStart.setFont(fntButton);
		theStart.setForeground(Color.white);
		theStart.setOpaque(false);
		theStart.setContentAreaFilled(false);
		theStart.setBorderPainted(false);
		theStart.addActionListener(this);
		theStart.setVisible(true); // Hide it initially until 3 players are present
		theWaitPanel.add(theStart);

		waitChatScroll.setBounds(920, 0, 360, 600); 
		waitChatScroll.setOpaque(false);
		waitChatScroll.getViewport().setOpaque(false); 

		waitChatArea.setEditable(false);
		waitChatArea.setLineWrap(true);
		waitChatArea.setWrapStyleWord(true);
		waitChatArea.setFont(new Font("Arial", Font.PLAIN, 16));
		waitChatArea.setForeground(Color.white); 
		waitChatArea.setOpaque(false);
		waitChatArea.setBackground(new Color(0, 0, 0, 50)); 
		theWaitPanel.add(waitChatScroll);

		// --- TRANSPARENT INPUT FIELD ---
		waitChatField.setBounds(920, 600, 360, 140);
		waitChatField.setFont(new Font("Arial", Font.PLAIN, 16));
		waitChatField.setForeground(Color.white);
		waitChatField.setCaretColor(Color.white); 
		waitChatField.setOpaque(false);
		waitChatField.setBackground(new Color(0, 0, 0, 80)); 
		waitChatField.addActionListener(this); 
		theWaitPanel.add(waitChatField);

		// Join Menu
		theJoinPanel = new GeneralPanel();
		theJoinPanel.setLayout(null);
		theJoinPanel.setPreferredSize(new Dimension(1280, 720));

		theJoinPanel.add(theJoinTitle);
		theJoinTitle.setFont(fntTitle);
		theJoinTitle.setForeground(Color.white);

		theJoinTitle.setBounds(0, 75, 1280, 75);	
		
		// Join Input Field Setup
		theIPInput.setFont(new Font("Arial", Font.PLAIN, 24));
		theIPInput.setHorizontalAlignment(JTextField.CENTER);
		theIPInput.setBounds(490, 175, 300, 75); 
		theJoinPanel.add(theIPInput);
		thePortNum.setFont(new Font("Arial", Font.PLAIN, 24));
		thePortNum.setHorizontalAlignment(JTextField.CENTER);
		thePortNum.setBounds(490, 275, 300, 75); 
		theJoinPanel.add(thePortNum);
		theUserName.setFont(new Font("Arial", Font.PLAIN, 24));
		theUserName.setHorizontalAlignment(JTextField.CENTER);
		theUserName.setBounds(490, 375, 300, 75); 
		theJoinPanel.add(theUserName);

		// Join Labels for text fields
		theIPLabel.setBounds(450, 175, 100, 75);
		theIPLabel.setFont(fntButton);
		theIPLabel.setForeground(Color.white);
		theJoinPanel.add(theIPLabel);
		thePortLabel.setBounds(425, 275, 100, 75);
		thePortLabel.setFont(fntButton);
		thePortLabel.setForeground(Color.white);
		theJoinPanel.add(thePortLabel);
		theUserLabel.setBounds(425, 375, 100, 75);
		theUserLabel.setFont(fntButton);
		theUserLabel.setForeground(Color.white);
		theJoinPanel.add(theUserLabel);

		// Connect Button Setup
		theConnect.setFont(fntButton);
		theConnect.setForeground(Color.white);
		theConnect.setOpaque(false);
		theConnect.setContentAreaFilled(false);
		theConnect.setBorderPainted(false);
		theConnect.setBounds(540, 475, 200, 50); 
		theConnect.addActionListener(this); 
		theJoinPanel.add(theConnect);

		// Help Menu
		theHelpPanel.add(theHelpTitle);
		theHelpTitle.setFont(fntTitle);
		theHelpTitle.setForeground(Color.white);
		theHelpTitle.setBounds(0, 75, 1280, 75);

		theHelpButton.setFont(fntButton);
		theHelpButton.setBounds(900, 550, 200, 50);
		theHelpButton.setForeground(Color.white);
		theHelpButton.setOpaque(false);
		theHelpButton.setContentAreaFilled(false);
		theHelpButton.setBorderPainted(false);
		theHelpButton.addActionListener(this);
		theHelpPanel.add(theHelpButton);

		theHelpText.setBounds(200, 300, 820, 200);
		theHelpText.setEditable(false);
		theHelpText.setLineWrap(true);
		theHelpText.setWrapStyleWord(true);
		theHelpText.setFont(new Font("Arial", Font.PLAIN, 20));
		theHelpText.setForeground(Color.white);
		theHelpText.setOpaque(false);
		theHelpText.setBackground(new Color(0, 0, 0, 50));
		theHelpPanel.add(theHelpText);


		// About Menu
		theAboutPanel = new GeneralPanel();
		theAboutPanel.setLayout(null);
		theAboutPanel.setPreferredSize(new Dimension(1280, 720));

		theAboutPanel.add(theAboutTitle);
		theAboutTitle.setFont(fntTitle);
		theAboutTitle.setForeground(Color.white);

		// about menu text
		theAboutAuthor.setBounds(0, 200, 1280, 75);
		theAboutAuthor.setHorizontalAlignment(JTextField.CENTER);
		theAboutAuthor.setFont(fntButton);
		theAboutAuthor.setForeground(Color.white);
		theAboutPanel.add(theAboutAuthor);
		theAboutDate.setBounds(0, 250, 1280, 75);
		theAboutDate.setHorizontalAlignment(JTextField.CENTER);
		theAboutDate.setFont(fntButton);
		theAboutDate.setForeground(Color.white);
		theAboutPanel.add(theAboutDate);
		theAboutCourse.setBounds(0, 300, 1280, 75);
		theAboutCourse.setHorizontalAlignment(JTextField.CENTER);
		theAboutCourse.setFont(fntButton);
		theAboutCourse.setForeground(Color.white);
		theAboutPanel.add(theAboutCourse);
		theAboutTeacher.setBounds(0, 350, 1280, 75);
		theAboutTeacher.setHorizontalAlignment(JTextField.CENTER);
		theAboutTeacher.setFont(fntButton);
		theAboutTeacher.setForeground(Color.white);
		theAboutPanel.add(theAboutTeacher);
		theAboutInspirtion.setBounds(0, 400, 1280, 75);
		theAboutInspirtion.setHorizontalAlignment(JTextField.CENTER);
		theAboutInspirtion.setFont(fntButton);
		theAboutInspirtion.setForeground(Color.white);
		theAboutPanel.add(theAboutInspirtion);

		theAboutTitle.setBounds(0, 75, 1280, 75);	

		// Frame
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setContentPane(theMenuPanel);
		// theFrame.setContentPane(theGamePanel);
		theTimer.start();
		theFrame.pack();
		theFrame.setResizable(false);
		theFrame.setVisible(true);
	}

	public class GamePanel extends JPanel {

		BufferedImage imgCord = null;
		BufferedImage imgScoreArea = null;
		BufferedImage imgLogo = null;
		
		BufferedImage imgP1 = null;
		BufferedImage imgP2 = null;
		BufferedImage imgP3 = null;
		BufferedImage imgP4 = null;
		BufferedImage imgP5 = null;
		BufferedImage imgP6 = null;
		BufferedImage imgYOU = null;
		
		// tile variables
		int tileWidth = 29;
		int tileHeight = 29;
		int GridStartX = 25;
		int GridStartY = 25;
		int ColumnClick = -100;
		int RowClick = -100;
		int intRandX = -100;
		int intRandY = -100;

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;

			// background colour
			g2.setColor(new Color(40, 40, 40));
			g2.fillRect(0, 0, 1280, 720);
			
			g2.drawImage(imgCord, 0, 0, null);
			g2.drawImage(imgLogo, 0, 635, null);

			// for loops to get through all 480 slots
			for (int row = 0; row < 16; row++) {
				for (int col = 0; col < 30; col++) {

					// FIXED: use model instead of direct array
					HueCueModel.ColourTile tile = model.getTile(row, col);

					// safety check
					if (tile == null)
						continue;

					// math coordinate position
					int x = GridStartX + (col * tileWidth);
					int y = GridStartY + (row * tileHeight);

					// draw rectangle blocks
					g2.setColor(tile.ColorValue);
					g2.fillRect(x, y, tileWidth, tileHeight);

					// draw borders
					g2.setColor(new Color(40, 40, 40));
					g2.drawRect(x, y, tileWidth, tileHeight);
				}
			}
			
			// Highlights the selected tile (TEMPORARY)
			// Will switch to game a game piece later
			g2.drawImage(imgYOU, this.ColumnClick*tileHeight+GridStartX,this.RowClick*tileWidth+GridStartY, null);
			
			// P1
			g2.drawImage(imgP1, model.intUserClicks[1][0]*tileHeight+GridStartX,model.intUserClicks[1][1]*tileWidth+GridStartY, null);
			
			// P2
			g2.drawImage(imgP2, model.intUserClicks[2][0]*tileHeight+GridStartX,model.intUserClicks[2][1]*tileWidth+GridStartY, null);
			
			// P3
			g2.drawImage(imgP3, model.intUserClicks[3][0]*tileHeight+GridStartX,model.intUserClicks[3][1]*tileWidth+GridStartY, null);
			
			// P4
			g2.drawImage(imgP4, model.intUserClicks[4][0]*tileHeight+GridStartX,model.intUserClicks[4][1]*tileWidth+GridStartY, null);
			
			// P5
			g2.drawImage(imgP5, model.intUserClicks[5][0]*tileHeight+GridStartX,model.intUserClicks[5][1]*tileWidth+GridStartY, null);
			
			// P6
			g2.drawImage(imgP6, model.intUserClicks[6][0]*tileHeight+GridStartX,model.intUserClicks[6][1]*tileWidth+GridStartY, null);
			
			g2.drawImage(imgScoreArea, GridStartX-1 + (intRandY - 3) * tileWidth, GridStartY-1 + (intRandX - 3) * tileHeight, null);

			
		}
			// Method used to load images
		public BufferedImage loadImage(String strFileName){  
			// Try to read from jar file
			InputStream imgClass = null;
			imgClass = this.getClass().getResourceAsStream(strFileName);

			if(imgClass != null){
				try{
					return ImageIO.read(imgClass);
				}catch(IOException e){
				}
			}

			// Try to read from local file
			try{
				BufferedImage theImg = ImageIO.read(new File(strFileName));
				return theImg;
			}catch(IOException e){
				System.out.println("Unable to load image: " + strFileName);
				return null;
			}
		}
		
		// Method used to pass position of where you clicked
		public void passClickPos(int ColumnClick, int RowClick){
			this.ColumnClick = ColumnClick;
			this.RowClick = RowClick;
		}	
		
		public void passRandPos(int intRandX, int intRandY){
			this.intRandX = intRandX;
			this.intRandY = intRandY;
		}
	
		public GamePanel(){
			super();
			// Load cord image
			imgCord = loadImage("Coordinates.png");
			imgScoreArea = loadImage("Game Score Area.png");
			imgLogo = loadImage("Small Logo.png");
			imgP1 = loadImage("P1.png");
			imgP2 = loadImage("P2.png");
			imgP3 = loadImage("P3.png");
			imgP4 = loadImage("P4.png");
			imgP5 = loadImage("P5.png");
			imgP6 = loadImage("P6.png");
			imgYOU = loadImage("YOU.png");
		}	
		
	}

	public class GeneralPanel extends JPanel{
		
		BufferedImage imgBG = null;
		BufferedImage imgHelp = null;
		BufferedImage imgBigLogo = null;
		BufferedImage imgLogo = null;
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(imgBG, 0, 0, null);
			if(model.intHelpCnt == 3){
				g.drawImage(imgHelp, 882, 190, null);
			}
			if(HueCueView.blnOnMain == true){
				g.drawImage(imgBigLogo, 433, 84, null);
			}else{
				g.drawImage(imgLogo, 0, 635, null);
			}
			

		}
		
		// Method used to load images
		public BufferedImage loadImage(String strFileName){  
			// Try to read from jar file
			InputStream imgClass = null;
			imgClass = this.getClass().getResourceAsStream(strFileName);

			if(imgClass != null){
				try{
					return ImageIO.read(imgClass);
				}catch(IOException e){
				}
			}

			// Try to read from local file
			try{
				BufferedImage theImg = ImageIO.read(new File(strFileName));
				return theImg;
			}catch(IOException e){
				System.out.println("Unable to load image: " + strFileName);
				return null;
			}
		}
		
		// Constructor
		public GeneralPanel(){
			super();
			// Load grid image
			imgBG = loadImage("Background.png");
			imgHelp = loadImage("Help Score Area.png");
			imgBigLogo = loadImage("Main Logo.png");
			imgLogo = loadImage("Small Logo.png");
		}
		
	}

	public static void main(String[] args) {
		new HueCueView();
	}
}
