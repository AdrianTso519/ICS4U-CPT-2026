//******************************************************************************
// Program Name: Hues and Cues Game
// Authors: Adrian Tso, Hansel Suen, Ethan Wong
// Date: TBD
// School: St. Augustine CHS Computer Science
// Description: An online version of the Hues and Cues board game 
//******************************************************************************


import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

public class ICS4CPT implements ActionListener, MouseMotionListener, MouseListener{
	
	// Properties
	JFrame theFrame = new JFrame("CPT");
	JPanel thePanel = new JPanel();
	Timer theTimer = new Timer(1000/60, this);
	
	// Game Properties
	JTextArea theArea = new JTextArea();
	JScrollPane theScroll = new JScrollPane(theArea);
	JTextField theField = new JTextField();
	JButton theButton = new JButton("Connect");
	
	// Network Connection Properties
	SuperSocketMaster Socket = null;
	
	
	// Methods
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
	
	public void mouseMoved(MouseEvent evt){
		
	}
	
	public void mouseDragged(MouseEvent evt){
		
	}
	
	public void mouseExited(MouseEvent evt){
		
	}
	
	public void mouseEntered(MouseEvent evt){
		
	}
	
	public void mouseReleased(MouseEvent evt){
		
	}
	
	public void mousePressed(MouseEvent evt){
		
	}
	
	public void mouseClicked(MouseEvent evt){
		
	}
	
	// Unused Methods
	
	// Constructor
	public ICS4CPT() {
		thePanel.setLayout(null);
		thePanel.setPreferredSize(new Dimension(1280,720));
		
		theScroll.setBounds(980,0,300,300);
		thePanel.add(theScroll);
		
		theField.setBounds(980,300,300,100);
		theField.addActionListener(this);
		thePanel.add(theField);
		
		theButton.setBounds(980,400,300,100);
		theButton.addActionListener(this);
		thePanel.add(theButton);
		
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setContentPane(thePanel);
		theFrame.pack();
		theFrame.setVisible(true);
	}
	
	
	// Main Method
	public static void main(String [] args){
		new ICS4CPT();
	}
	
	
}
