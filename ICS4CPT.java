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
	
	
	
	// Methods
	public void actionPerformed(ActionEvent evt){
		
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
		thePanel.setPreferredSize(new Dimension(1280, 720));
		theFrame.setContentPane(thePanel);
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.pack();
		theFrame.setResizable(false);
		theFrame.setVisible(true);
		theTimer.start();
	}
	
	
	// Main Method
	public static void main(String [] args){
		new ICS4CPT();
	}
	
	
}
