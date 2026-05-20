//******************************************************************************
// Program Name: Hues and Cues Game
// Authors: Adrian Tso, Hansel Suen, Ethan Wong
// Date: June 9th 2026
// School: St. Augustine CHS Computer Science
// Description: An online version of the Hues and Cues board game 
//******************************************************************************

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

public class HueCueView implements ActionListener, MouseMotionListener, MouseListener{
	
	// Properties
	JFrame theFrame = new JFrame("CPT");
	GamePanel thePanel;
	Timer theTimer = new Timer(1000/60, this);
	
	// Game buttons
	JTextArea theArea = new JTextArea();
	JScrollPane theScroll = new JScrollPane(theArea);
	JTextField theField = new JTextField();
	JButton theButton = new JButton("Connect");
	
	// Network Connection Properties
	SuperSocketMaster Socket = null;

	HueCueModel model;
	
	// Mandatory Methods
	public void actionPerformed(ActionEvent evt){
		// Field triggered
		if(evt.getSource() == theField){
			System.out.println("Field event triggered");
			Socket.sendText(theField.getText());
			theField.setText("");
		// Button Triggered
		}else if(evt.getSource() == theButton){
			System.out.println("button event triggered");
			Socket = new SuperSocketMaster("10.8.49.90", 6112, this);
			Socket.connect();
			theButton.setEnabled(false);
		// Socket triggered
		}else if(evt.getSource() == Socket){
			System.out.println("Socket event triggered");
			String strLine = Socket.readText();
			theArea.append(strLine + "\n");
		}
	}
	
	public void mouseMoved(MouseEvent evt){}
	public void mouseDragged(MouseEvent evt){}
	public void mouseExited(MouseEvent evt){}
	public void mouseEntered(MouseEvent evt){}
	public void mouseReleased(MouseEvent evt){}
	
	public void mousePressed(MouseEvent evt){
		// Get pixel coordinates of the mouse
		int MouseX = evt.getX();
		int MouseY = evt.getY();
		
		// Get grid dimensions
		int TileWidth = thePanel.tileWidth;
		int TileHeight = thePanel.tileHeight;
		int StartX = thePanel.GridStartX;
		int StartY = thePanel.GridStartY;
		
		// Convert coordinates to array (rows and columns)
		int ColumnClick = (MouseX - StartX) / TileWidth;
		int RowClick = (MouseY - StartY) / TileHeight;
		
		// Check to make sure click is inside grid boundaries
		if(RowClick >= 0 && RowClick < 16 && ColumnClick >= 0 && ColumnClick < 30){
			
			// FIXED: use model instead of View array
			HueCueModel.ColourTile clickedTile = model.getTile(RowClick, ColumnClick);
			
			System.out.println("Clicked Grid Cell is Row: " + (RowClick+1) + " Column: " + (ColumnClick+1));
			
			// print tile clicked to terminal
			if(clickedTile != null){
				Color c = clickedTile.ColorValue;
				System.out.println("Tile RGB: (" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ")");
			} else{
				System.out.println("Clicked an empty/null tile slot.");
			}
		}
	}
	
	public void mouseClicked(MouseEvent evt){}
	
	// Constructor
	public HueCueView() {

		// CONNECT MODEL (ADDED ONLY)
		model = new HueCueModel();
		model.CSVGrid("colors.csv");

		// Panel stuff
		thePanel = new GamePanel();
		thePanel.setLayout(null);
		thePanel.setPreferredSize(new Dimension(1280,720));
		thePanel.addMouseListener(this);
		thePanel.addMouseMotionListener(this);
		
		theScroll.setBounds(960,0,320,620);
		thePanel.add(theScroll);
		
		// textfield stuff
		theField.setBounds(960,620,320,100);
		theField.addActionListener(this);
		thePanel.add(theField);
		
		// frame stuff
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setContentPane(thePanel);
		theFrame.pack();
		theFrame.setVisible(true);
	}
	
	class GamePanel extends JPanel{
	
		int tileWidth = 32;
		int tileHeight = 32;
		int GridStartX = 0;
		int GridStartY = 0;
	
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			
			// background colour
			g2.setColor(new Color(230, 230,230));
			g2.fillRect(0,0,1280,720);
		
			// for loops to get through all 480 slots
			for(int row = 0; row < 16; row++){
				for(int col = 0; col < 30; col++){
					
					// FIXED: use model instead of direct array
					HueCueModel.ColourTile tile = model.getTile(row, col);
					
					// safety check
					if(tile == null) continue;
					
					// math coordinate position
					int x = GridStartX + (col * tileWidth);
					int y = GridStartY + (row * tileHeight);
					
					// draw rectangle blocks
					g2.setColor(tile.ColorValue);
					g2.fillRect(x,y,tileWidth,tileHeight);
					
					// draw borders
					g2.setColor(new Color(40, 40, 40));
					g2.drawRect(x, y, tileWidth, tileHeight);
				}
			}
		}
	}
	public static void main(String[] args){
		new HueCueView();
	}
}
