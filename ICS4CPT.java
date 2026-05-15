import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

public class ICS4CPT implements ActionListener{
	
	// Properties
	JFrame theFrame = new JFrame("CPT");
	JPanel thePanel = new JPanel();
	Timer theTimer = new Timer(1000/60, this);
	
	// Game Properties
	
	
	// Methods
	public void actionPerformed(ActionEvent evt){
		
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
