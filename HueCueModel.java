//******************************************************************************
// Program Name: 	Hues and Cues Model
// Authors: 		Adrian Tso, Hansel Suen, Ethan Wong
// Date: 			June 9th 2026
// School: 			St. Augustine CHS Computer Science
// Description: 	An online version of the Hues and Cues board game 
//******************************************************************************

import java.io.*;
import java.awt.*;

public class HueCueModel{
	
	// Properties
		
	// Colour grid Array
	private ColourTile[][] fullColourGrid = new ColourTile[16][30];
	
	// Game state
	int GameState = 0;
	
	// Colour values 
	public class ColourTile{
		Color ColorValue;
		ColourTile(int red, int green, int blue){
			ColorValue = new Color(red, green, blue);
		}
	}

	public int[] generateTargetTile(){
		 int[] RandomRowCol = new int[2]; 
		// Random Row
		RandomRowCol[0] = (int)(Math.random() * 16) + 1;
		RandomRowCol[1] = (int)(Math.random() * 30) + 1;
		return RandomRowCol;
	}
	
	public ColourTile getTile(int row, int col){
		return fullColourGrid[row][col];
	}
	
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
				fullColourGrid[row][column] = new ColourTile(red,green,blue);
				
			}
		}catch(IOException e){
			System.out.println("Failed to process CSV File");
		}
	}
}
