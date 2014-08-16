package baobao.core;
import baobao.util.Coordinate;
/*
 * Player is an interface!  You must implement this interface with another
 * class.
 * 
 * Put that class in a package whose name is your unikey.
 * Your unikey is the username you use when you log in to PASTA.
 * 
 * YOU MUST NOT MODIFY THIS INTERFACE
 * 
 */
public interface Player {
	/**
	 * This method asks the player for their next move.
	 * 
	 * If you fail to return a move within the required time, your move is forfeit.
	 * 
	 * @param board - the current board state
	 * @param colour - the colour of piece they will be placing
	 * @return the move your player will make
	 * 
	 * NOTE: This method must execute within the required time.
	 * Timing requirements will be posted on the assessment details and 
	 * may be subject to changes depending on load requirements on the server.
	 */
	public Coordinate getNextMove(char[][][] board, char colour);
	
	/**
	 * This method notifies the player of a new opponent.
	 * 
	 * @param opponentName - the opponent name
	 * 
	 * NOTE: This method must execute within the required time.
	 * Timing requirements will be posted on the assessment details and 
	 * may be subject to changes depending on load requirements on the server.
	 */
	public void notifyNewOpponent(String opponentName);
	
	/**
	 * This method notifies the player of the outcome of a game
	 * 
	 * The possible outcomes are : WIN, LOSS, DRAW
	 * 
	 * @param outcome - outcome of the game
	 * 
	 * NOTE: This method must execute within the required time.
	 * Timing requirements will be posted on the assessment details and 
	 * may be subject to changes depending on load requirements on the server.
	 */
	public void notifyOutcome(String outcome);
}


