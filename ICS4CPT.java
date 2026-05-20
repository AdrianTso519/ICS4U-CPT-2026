//******************************************************************************
// Program Name: Hues and Cues Game
// Authors: Adrian Tso, Hansel Suen, Ethan Wong
// Date: TBD
// School: St. Augustine CHS Computer Science
// Description: An online version of the Hues and Cues board game 
//******************************************************************************

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

public class ICS4CPT implements ActionListener, MouseMotionListener, MouseListener{
	
	// Properties
	JFrame theFrame = new JFrame("CPT");
	GamePanel thePanel = new GamePanel();
	Timer theTimer = new Timer(1000/60, this);
	
	// Game buttons
	JTextArea theArea = new JTextArea();
	JScrollPane theScroll = new JScrollPane(theArea);
	JTextField theField = new JTextField();
	JButton theButton = new JButton("Connect");
	
	// Colour grid Array
	private ColourTile[][] fullColourGrid = new ColourTile[16][30];
	
	// Game state
	int GameState = 0;
	
	// Colour values 
	class ColourTile{
		Color ColorValue;
		ColourTile(int red, int green, int blue){
			ColorValue = new Color(red, green, blue);
		}
	}
	
	// Network Connection Properties
	SuperSocketMaster Socket = null;
	
	
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
			// Establish socket network connection
			// Construct ssm based on if they will be server or client
			// Server
			//Socket = new SuperSocketMaster(6767, this);
			// client
			Socket = new SuperSocketMaster("10.8.49.90", 6112, this);
			// access the connect method
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
	
	public void mousePressed(MouseEvent evt){}
	
	public void mouseClicked(MouseEvent evt){}
	
	// Self made Methods
	public void CSVGrid(String filename){
		try(BufferedReader theReader = new BufferedReader(new FileReader(filename))){
			String line;
			// Continuously get data for colour grid while file has not ended
			while((line = theReader.readLine()) != null){
				String[] colours = line.split(",");
				int row = Integer.parseInt(colours[0]);
				int column = Integer.parseInt(colours[1]);
				int red = Integer.parseInt(colours[2]);
				int green = Integer.parseInt(colours[3]);
				int blue = Integer.parseInt(colours[4]);
				
				// take data from earlier and return it as an rgb value
				// need to fix
				fullColourGrid[row][column] = new ColourTile(red,green,blue);
				
			}
		}catch(IOException e){
			System.out.println("Failed to process CSV File");
		}
	}
	
	// Unused Methods
	
	// Constructor
	public ICS4CPT() {
		// Panel stuff
		thePanel.setLayout(null);
		thePanel.setPreferredSize(new Dimension(1280,720));
		
		theScroll.setBounds(980,0,300,300);
		thePanel.add(theScroll);
		
		// textfield stuff
		theField.setBounds(980,300,300,100);
		theField.addActionListener(this);
		thePanel.add(theField);
		
		// grid assets
		CSVGrid("colors.csv");
		
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
					// access current tile from array
					ColourTile tile = fullColourGrid[row][col];
					
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
}
// ethan is a bum
