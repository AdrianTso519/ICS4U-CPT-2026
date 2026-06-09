//******************************************************************************
// Program Name: 	Hues and Cues Model
// Authors: 		Adrian Tso, Hansel Suen, Ethan Wong
// Date: 			June 9th 2026
// School: 			St. Augustine CHS Computer Science
// Description: 	An online version of the Hues and Cues board game 
//******************************************************************************

/**
 * HueCueModel handles the game logic, state management, 
 * and scoring calculations for the Hues and Cues game.
 * * <h1>HueCueModel</h1>
 * HueCueModel handles the game logic, state management,<p>
 * and scoring calculations for the Hues and Cues game.<p>
 * This class is meant to be used in the Hues & Cues View program
 * @author  Adrian Tso, Hansel Suen, Ethan Wong
 * @version 1.0
 * @since   2026-06-09 
 */
import java.io.*;
import java.awt.*;

public class HueCueModel{
	
	// Properties
	/** The display name of the current user. */
	String username = "Host";
	/** 
	 * Maximum number of rounds allowed before the game concludes. <p>
	 * It has a default value of 3.
	 */
	int intMaxRounds = 3;
	/** The current round number. */
	int intRounds = 0;
	/** The current user's total score. */
	int intMyScore = 0;
	/** * The current phase of the game: <p>
     * 1: Cue Giver provides cue, 2: Guessing, 3: Second cue, 4: Final guess, 5: Scoring. 
     */
	int intGameState = 0;
	/** Total number of players currently connected. */
	int intPlayerCount = 1;
	/** The player ID assigned to the current player (1-6). */
	int intPlayerNumber = 1;
	/** The index (player number) of the player who is currently the cue giver. */
	int intCueGiver = 0;
	int intCueCount = 1;
	int intHelpCnt = 0;
	boolean blnCueGiver = false;
	boolean blnCueGiven = true;
	/** Array storing the usernames of up to 6 players. */
	String strUserName[] = new String[7];
	/** Array storing the scores for up to 6 players. */
	int intUserScore[] = new int[7];
	/** Stores the coordinates of the target tile [row, col]. */
	int intRandomTile[] = new int[2];
	/** Stores the [row, col] coordinates of each player's latest guess. */
	int intUserClicks[][] = new int[7][2];
		
	// Colour grid Array
	/** The full 16x30 grid of tiles representing the game board. */
	private ColourTile[][] fullColourGrid = new ColourTile[16][30];
	
	// Colour values 
	public class ColourTile{
		Color ColorValue;
		ColourTile(int red, int green, int blue){
			ColorValue = new Color(red, green, blue);
		}
	}
	
	/**
	* Saves a player's clicked tile coordinates according to their player number.
	* @param intUserNumber  The index/ID of the player (1-6).
	* @param intUserClickX  The column coordinate of the player's guess.
	* @param intUserClickY  The row coordinate of the player's guess.
	*/
	public void savePlayerPos(int intUserNumber, int intUserClickX, int intUserClickY){
		for(int intCount = 1; intCount <= 6; intCount++){
			if(intCount == intUserNumber){
				intUserClicks[intCount][0] = intUserClickX;
				intUserClicks[intCount][1] = intUserClickY;
				break;
			}
		}
	}
	
	/**
	* Gets the winner (Player with the highest score) of the game. <p>
	* If one more player got the highest score, <p>
	* then the player with the lowest player number (index) will be returned.
	* @return The player number (index) of the winner.
	*/
	public int getWinner() {
		int intBestPlayer = 1;
		int intBestScore = intUserScore[1];

		for(int intCount = 1; intCount <= intPlayerCount; intCount++){
			// Saves the highest score among all players, then return the player number
			if(intUserScore[intCount] > intBestScore){
				intBestScore = intUserScore[intCount];
				intBestPlayer = intCount;
			}
		}

		return intBestPlayer;
	}
	
	/**
	* Generates a random target tile coordinate on the game board.
	* @return An integer array where index 0 is the row (1-16) and index 1 is the column (1-30).
	*/
	public int[] generateTargetTile(){
		 int[] RandomRowCol = new int[2]; 
		// Random Row
		RandomRowCol[0] = (int)(Math.random() * 16) + 1;
		RandomRowCol[1] = (int)(Math.random() * 30) + 1;
		return RandomRowCol;
	}
	
	/**
	 * Registers a new player by storing their name in the first available slot 
	 * and broadcasting the updated player count to the network.
	 * @param strClientName	The name of the player joining the game.
	 * @param Socket  		SuperSocketMaster is used to broadcast updates.
	 */
	public void storeUserName(String strClientName, SuperSocketMaster Socket){
		// Scans through the 6 player numbers
		for(int intCount = 1; intCount <= 6; intCount++){
			// Stores in the first empty one
			if(strUserName[intCount] == null){
				strUserName[intCount] = strClientName;
				intPlayerCount++;
				
				Socket.sendText("<COUNT> "+intPlayerCount);
				System.out.println(intCount+" "+strUserName[intCount]);
				
				break;
			}
		}
	}
	/**
	* Removes a player from the game when they disconnect and updates the player count.<p>
	* This method searches for the specified username in the player arrat, <p>
	* clears their slot, decrements the total player count, <p>
	* and broadcasts the new count to all connected clients via the socket.
	* @param strClientName The name of the player to be removed.
	* @param Socket        The SuperSocketMaster instance used to broadcast the updated count.
	*/
	public void removeUserName(String strClientName, SuperSocketMaster Socket){
		System.out.println("Removing: " + strClientName);
		// Scans through the 6 player numbers
		for(int intCount = 1; intCount <= 6; intCount++){
			// Removes the username if it matches the one left
			if (strUserName[intCount] != null && strUserName[intCount].equals(strClientName)){
				strUserName[intCount] = null;
				intPlayerCount--;
				
				Socket.sendText("<COUNT> "+intPlayerCount);
				
				break;
			}
		}
	}
	
	/**
	* Compiles and broadcasts a CSV-formatted string containing all active usernames and resets all player scores to zero.<p>
	* This method goes through the current player slots, appends each username (or a comma if the slot is empty) to a message, <p>
	* and sends the final string to all connected clients. <p>
	* It also resets the global score array to 0.
	* @param Socket	SuperSocketMaster is used to broadcast the player data string.
	*/
	public void sendPlayerData(SuperSocketMaster Socket){
		// Arranges all usernames + player number into one single csv message for clients to deal with
		String strLine = "<PLAYERS> ";
		
		for(int intCount = 1; intCount <= 6; intCount++){
			if(strUserName[intCount] == null){
				strLine += ",";
			}else{
				strLine += strUserName[intCount] + ",";
			}
			intUserScore[intCount] = 0;
		}
		
		Socket.sendText(strLine);
		System.out.println(strLine);
	}
	
	/**
	* Processes a CSV-formatted string of player names to update the local player list. <p>
	* This method splits the incoming string into individual usernames, <p>
	* filling in the local player name array, 
	* and identifies the current client's player number by matching their username against the array. <p>
	* It also resets the global score array to 0.
	* @param strLine	The CSV-formatted string containing player username broadcasted by the host.
	*/
	public void loadPlayerData(String strLine){
		// Split the csv message 
		// Saves individual messages as player number & username
		String[] strPlayers = strLine.split(",", -1);
		
		for(int intCount = 1; intCount <= 6; intCount++){
			if(strPlayers[intCount - 1].equals("")){
				strUserName[intCount] = null;
			} else {
				strUserName[intCount] = strPlayers[intCount - 1];
			}
			
			if(strPlayers[intCount].equals(username)){
				intPlayerNumber = intCount+1;
				System.out.println("I am player "+intPlayerNumber);
			}
			intUserScore[intCount] = 0;
		}
	}
	
	/**
	* Updates the score for a specific player in the local array.
	* @param intUserNumber	The index of the player (1-6) whose score is being updated.
	* @param intUserPoint	The new point total to assign to the player.
	*/
	public void loadUserScore(int intUserNumber, int intUserPoint){
		// load the user score into an array
		intUserScore[intUserNumber] = intUserPoint;
	}
	
	/**
	* Advances the game to the next player's turn to play as the cue giver. <p>
	* This method increments the cue giver index to the next player. 
	* If the index exceeds the total number of players in the game, <p>
	* it automatically goes back to player 1. <p>
	* It also resets the game state and broadcasts the new cue giver's ID to all clients.
	* @param Socket	SuperSocketMaster is used to broadcast the new cue giver ID.
	*/
	public void nextRound(SuperSocketMaster Socket){
		// Move on to the next round if all players went as the cue giver for the round
		intGameState = 0;
		intCueGiver++;
		if(intCueGiver > intPlayerCount){
			// Reset the cue giver to player 1
			intCueGiver = 1;
		}
		Socket.sendText("<CUER>"+this.intCueGiver);
	}
	
	/**
	* Advances the game to the next game state and broadcasts it to all clients.<p>
	* The method increments the game state variable. <p>
	* If the state reaches 6, it resets to 1 to begin a new round cycle. <p>
	* The states are defined as follows:<p>
	* 1: Cue Giver provides the first cue<p>
	* 2: Players provide their first hue guesses<p>
	* 3: Cue Giver provides the second cue<p>
	* 4: Players provide their final hue guesses<p>
	* 5: Scoring calculation phase<p>
	* @param Socket	SuperSocketMaster is used to broadcast the new state value to all clients.
	*/
	public void nextState(SuperSocketMaster Socket){
		// Increase the game state by 1
		intGameState += 1;
		if(intGameState == 6){
			intGameState = 1;
		}
			// 1 cue give
			// 2 guess 1
			// 3 cue give 2
			// 4 guess 2
			// 5 score
		Socket.sendText("<STATE>"+this.intGameState);
	}
	
	/**
	* Returns the ColourTile object at the specified board coordinates.
	* @param row The row index of the tile.
	* @param col The column index of the tile.
	* @return The ColourTile object located at the given coordinates.
	*/
	public ColourTile getTile(int row, int col){
		// Get the colour tile based on the row and col 
		return fullColourGrid[row][col];
	}
	
	/**
	* Calculates the points a player got based on the closeness of their guess to the target tile.<p>
	* The scoring rules are based on a square grid system:<p>
	* 3 points: Exact match (target tile).<p>
	* 2 points: Within the 3x3 square centered on the target.<p>
	* 1 point: Within the 5x5 square centered on the target.<p>
	* 0 points: Outside the 5x5 square.<p>
	* @param RandomRowCol	An integer array where index 0 is the row and index 1 is the column of the target tile.
	* @param RowClick    	The row index of the player's guess.
	* @param ColumnClick 	The column index of the player's guess.
	* @return The points earned (0, 1, 2, or 3).
	*/
	public int getScore(int[] RandomRowCol, int RowClick, int ColumnClick){
		RowClick += 1;
		ColumnClick += 1;
		
		// If your tile is within the 5x5 square around the target tile
		if(RandomRowCol[0] - 2 <= RowClick && RowClick <= RandomRowCol[0] + 2 && RandomRowCol[1] - 2 <= ColumnClick && ColumnClick <= RandomRowCol[1] + 2){

			if(RandomRowCol[0] == RowClick && RandomRowCol[1] == ColumnClick){
				// 3 points if it is the exact tile
				return 3;
			}else if(RandomRowCol[0] - 1 <= RowClick && RowClick <= RandomRowCol[0] + 1 && RandomRowCol[1] - 1 <= ColumnClick && ColumnClick <= RandomRowCol[1] + 1){
				// 2 points if it is within the 3x3 square around the target tile
				return 2;
			}else{
				// 1 point if it is within the 5x5 square around the target tile
				return 1;
			}
		}else{
			// If it's not within the 5x5 square, then no points are earned
			return 0;
		}
	}

	/**
	* Reads a CSV file to initialize the fullColourGrid with ColourTile data.<p>
	* The expected format of each line in the CSV file is: row, column, red, green, blue.<p>
	* If the file is not found or cannot be read, an error message is printed to the terminal.
	* @param filename	The path to the CSV file that contains grid and colour data.
	*/
	public void CSVGrid(String filename) {
		try (BufferedReader theReader = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("/" + filename)))) {

			String line;

			while ((line = theReader.readLine()) != null) {
				String[] colours = line.split(",");

				int row = Integer.parseInt(colours[0]);
				int column = Integer.parseInt(colours[1]);
				int red = Integer.parseInt(colours[2]);
				int green = Integer.parseInt(colours[3]);
				int blue = Integer.parseInt(colours[4]);

				fullColourGrid[row][column] = new ColourTile(red, green, blue);
			}

		} catch (Exception e) {
			System.out.println("Failed to process CSV File: " + filename);
			e.printStackTrace();
		}
	}
	
	// Constructor
	/**Constructs a new HueCueModel object.*/
	public HueCueModel(){
		
	}
}
