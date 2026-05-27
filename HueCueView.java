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

public class HueCueView implements ActionListener, MouseMotionListener, MouseListener {

	// Properties
	int intPlayerCount = 1;
	boolean blnHost = true;
	boolean blnJoined = false;
	String username = "Host";
	JFrame theFrame = new JFrame("CPT");
	// game panels
	GamePanel theGamePanel;
	GeneralPanel theMenuPanel;
	GeneralPanel theHelpPanel;
	GeneralPanel theAboutPanel;
	GeneralPanel theJoinPanel;
	GeneralPanel theWaitPanel; 

	// timer
	Timer theTimer = new Timer(1000 / 60, this);

	// Main Menu
	JButton theHost = new JButton("Host");
	JButton theJoin = new JButton("Join");
	JButton theHelp = new JButton("Help");
	JButton theAbout = new JButton("About");
	JButton theQuit = new JButton("Quit");
	Font fntButton = new Font("Impact", 0, 30);
	Font fntTitle = new Font("Impact", 0, 75);
	JLabel theTitleScreen = new JLabel("Hues & Cues", SwingConstants.CENTER);

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
	int intPort = 6767;
	
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
	JLabel theHelpText = new JLabel(/* insert game explanation here */);
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
	JLabel theP1Points = new JLabel("Player 1: " /* Add variable of player 1 points */);
	JLabel theP2Points = new JLabel("Player 2: " /* Add variable of player 2 points */);
	JLabel theP3Points = new JLabel("Player 2: " /* Add variable of player 3 points */);
	JLabel theP4Points = new JLabel("Player 4: " /* Add variable of player 4 points */);
	JLabel theP5Points = new JLabel("Player 5: " /* Add variable of player 4 points */);
	JLabel theP6Points = new JLabel("Player 6: " /* Add variable of player 4 points */);
	JComponent GameMenu[];

	// Network Connection Properties
	SuperSocketMaster Socket = null;

	HueCueModel model;

	// Mandatory Methods
	public void actionPerformed(ActionEvent evt) {
		// Field triggered
		if(evt.getSource() == theTimer){
			theGamePanel.repaint();
			
		}else if (evt.getSource() == theField) {
			System.out.println("Field event triggered");
			Socket.sendText(theField.getText());
			theField.setText("");
			
		} else if (evt.getSource() == waitChatField) {
			System.out.println("Lobby chat text sent");
			if (Socket != null) {
				Socket.sendText("<"+this.username+"> "+waitChatField.getText());
				waitChatArea.append("<You> " + waitChatField.getText() + "\n");
			} else {
				// Local visual testing fallback if offline
				waitChatArea.append("<You> " + waitChatField.getText() + "\n");
			}
			waitChatField.setText("");
			
			// Button Triggered
		} else if (evt.getSource() == theConnect) {
			System.out.println("button event triggered");
			
			String targetIP = theIPInput.getText();
			int targetPort = Integer.parseInt(thePortNum.getText());
			username = theUserName.getText();
			
			// Safety check: Don't try connecting if they didn't replace the placeholder
			if (targetIP.equals("") || targetIP.trim().isEmpty()) {
				System.out.println("Please enter a valid game code / IP address first!");
				return;
			}

			// tell user they are connecting
			System.out.println("Attempting connection to: " + targetIP);
			clientConnect(targetIP, targetPort);
			// send text when user joins game
			Socket.sendText("<JOIN> "+this.username);
			Socket.sendText("<SYSTEM> "+this.username+" joined the room");
			blnJoined = true;
			
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
				this.intPlayerCount++;
				Socket.sendText("<COUNT> "+this.intPlayerCount);
				System.out.println("<COUNT> "+this.intPlayerCount);
			}else if(strLine.startsWith("<COUNT>")){
				this.intPlayerCount = Integer.parseInt(strLine.substring(8,9));
				System.out.println(this.intPlayerCount+" Players");
			}else if(strLine.equals("<DISCONNECT>")){
				this.intPlayerCount--;
				Socket.sendText("<COUNT> "+this.intPlayerCount);
				System.out.println("<COUNT> "+this.intPlayerCount);
			// --- NETWORK COMMAND DETECTION ---
			}else if(strLine.equals("<START>")) {
				// Clients receive this message and instantly switch to their game boards
				theFrame.setContentPane(theGamePanel);
				theFrame.revalidate();
				theFrame.repaint();
				theTimer.start(); // Start your 60 FPS repaint loop
			}else{
				waitChatArea.append(strLine+"\n");
			}
			
			// --- LIVE CHECK PLAYER COUNT FOR THE LOBBY BUTTON ---
			// Enable start button if player counts are between 3 and 6 (inclusive) and user is host
			if (this.blnHost && this.intPlayerCount >= 3 && this.intPlayerCount <= 6) {
				theStart.setVisible(true);
			} else {
				theStart.setVisible(false);
			}
			
		}else if(evt.getSource() == theHost){
			theWaitPanel.add(theBack);
			theFrame.setContentPane(theWaitPanel);
			theFrame.revalidate();
			theFrame.repaint();
			hostConnect();
			strIP = Socket.getMyAddress();
			theIP.setText("IP: " + strIP);
			thePort.setText("Port: " + intPort);
			
		}else if(evt.getSource() == theJoin){
			theJoinPanel.add(theBack);
			theFrame.setContentPane(theJoinPanel);
			theFrame.revalidate();
			theFrame.repaint();
			
		}else if(evt.getSource() == theHelp){
			theHelpPanel.add(theBack);
			theFrame.setContentPane(theHelpPanel);
			theFrame.revalidate();
			theFrame.repaint();
			
		}else if(evt.getSource() == theAbout){
			theAboutPanel.add(theBack);
			theFrame.setContentPane(theAboutPanel);
			theFrame.revalidate();
			theFrame.repaint();
			
		}else if(evt.getSource() == theStart){
		System.out.println("Host started the game session!");
		
		// Broadcast start signal to all connected clients
		Socket.sendText("<START>");
		
		// Move the host's screen to the game board immediately
		theFrame.setContentPane(theGamePanel);
		theFrame.revalidate();
		theFrame.repaint();
		// Activate the game panel update timer loop
		theTimer.start(); 
			
		}else if(evt.getSource() == theQuit){
			System.exit(0);
			
		}else if(evt.getSource() == theBack){
			if(blnJoined == true){
				// if client presses back send text
				Socket.sendText("<DISCONNECT>");
				Socket.sendText("<SYSTEM> "+this.username+" left the room");
				Socket.disconnect();
				theConnect.setEnabled(true);
			}
			theFrame.setContentPane(theMenuPanel);
			theFrame.revalidate();
			theFrame.repaint();
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
		int ColumnClick = (MouseX - StartX) / TileWidth;
		int RowClick = (MouseY - StartY) / TileHeight;
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

	public void mouseClicked(MouseEvent evt) {
	}
	
	public void clientConnect(String targetIP, int targetPort){
		// Initialize the connection
		Socket = new SuperSocketMaster(targetIP, targetPort, this);
		Socket.connect();
		theConnect.setEnabled(false); // Disable to prevent multiple click spam
		blnHost = false;

	}
	
	public void hostConnect(){
		if(blnHost == true){
			Socket = new SuperSocketMaster(intPort, this);
			Socket.connect();
			System.out.println("Awaiting connections");
		}
	}


	// Constructor
	public HueCueView() {

		model = new HueCueModel();
		model.CSVGrid("colors.csv");

		// Panels
		theGamePanel = new GamePanel();
		theGamePanel.setLayout(null);
		theGamePanel.setPreferredSize(new Dimension(1280, 720));
		theGamePanel.addMouseListener(this);
		theGamePanel.addMouseMotionListener(this);
		
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
		theGamePanel.add(theScroll);

		// Text Field
		theField.setBounds(960, 620, 320, 100);
		theField.addActionListener(this);
		theGamePanel.add(theField);

		// Main Menu
		// set fonts for main menu JComponents
		theHost.setFont(fntButton);
		theJoin.setFont(fntButton);
		theHelp.setFont(fntButton);
		theAbout.setFont(fntButton);
		theQuit.setFont(fntButton);
		theTitleScreen.setFont(fntTitle);
		theBack.setFont(fntButton);

		// set bounds of main menu JComponents
		theHost.setBounds(570, 250, 140, 50);
		theJoin.setBounds(570, 325, 140, 50);
		theHelp.setBounds(570, 400, 140, 50);
		theAbout.setBounds(570, 475, 140, 50);
		theQuit.setBounds(570, 550, 140, 50);
		theTitleScreen.setBounds(0, 75, 1280, 80);
		theBack.setBounds(590, 550, 100, 50);

		// add the buttons to the menu panel
		theMenuPanel.add(theHost);
		theMenuPanel.add(theJoin);
		theMenuPanel.add(theHelp);
		theMenuPanel.add(theAbout);
		theMenuPanel.add(theQuit);
		theMenuPanel.add(theTitleScreen);

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
		theTitleScreen.setForeground(Color.white);
		
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
		theStart.setVisible(false); // Hide it initially until 3 players are present
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
		
		// add to the menu panel

		// make buttons invisible

		// add action listener to buttons

		// orginize to the buttons to into and array

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
		// theTimer.start();
		theFrame.pack();
		theFrame.setResizable(false);
		theFrame.setVisible(true);
	}

	public class GamePanel extends JPanel {

		// tile variables
		int tileWidth = 32;
		int tileHeight = 32;
		int GridStartX = 0;
		int GridStartY = 0;
		int ColumnClick;
		int RowClick;

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;

			// background colour
			g2.setColor(new Color(230, 230, 230));
			g2.fillRect(0, 0, 1280, 720);

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
			g2.setColor(Color.GREEN);
			g2.setStroke(new BasicStroke(3));
			g2.drawRect(this.ColumnClick*tileHeight,this.RowClick*tileWidth,tileWidth,tileHeight);
		}
		
		// Method used to pass position of where you clicked
		public void passClickPos(int ColumnClick, int RowClick){
			this.ColumnClick = ColumnClick;
			this.RowClick = RowClick;
		}		
	}

	public class GeneralPanel extends JPanel{
		
		BufferedImage imgBG = null;
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(imgBG, 0, 0, null);
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
		}
	}

	public static void main(String[] args) {
		new HueCueView();
	}
}
// a
