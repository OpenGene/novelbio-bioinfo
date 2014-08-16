package baobao.core;

import baobao.util.Coordinate;

/*
 * ScoreFour Class
 * 
 * This class handles execution of the game.  
 * The constructor accepts two player instances, which will make moves in the game.
 * The ScoreFour class will deal with those moves and determine whether a move is legal
 * (see isValidCoordinate, isValidPlacement and place methods), and after each move
 * whether the game is over (see isGameOVer).
 * 
 * In the comments below, i is the row number, j is the column number, and k is the height.
 */
public class ScoreFour {
		protected char board[][][];
		private int i;
		private int j;
		private int k;
		private char b;
		private char w;
		
		

	/**
	 * #1
	 * 
	 * Initialize all instance variables (e.g. players, board ... )
	 * @param player1 - first player
	 * @param player2 - second player
	 */
	public ScoreFour(Player player1, Player player2) {
		 board = new char[4][4][4];
          
		}
	
	/**
	 * #2
	 * 
	 * Check if the coordinates are valid
	 * 
	 * Coordinates are valid if and only if they are:
	 * 		* greater than or equal to 0
	 * and	* less than boardSize
	 * 
	 * @param i - i coordinate
	 * @param j - j coordinate
	 * @param k - k coordinate
	 * @return true - if coordinates are valid
	 * @return false - if coordinates are invalid
	 */
	protected boolean isValidCoordinate(int i, int j, int k) {
		if((i>=0&&i<4)&&(j>=0&&j<4)&&(k>=0&&k<4))
			return true;
		else
			return false;	
	}
	
	/**
	 * #3
	 * 
	 * Get the value of the board at (i, j, k).
	 * 
	 * The value of the board is a single char: use 'w' for WHITE, representing player 1, 
	 * and 'b' for BLACK, representing player 2.
	 * 
	 * return -1 if the coordinates are invalid
	 * return 0 (without quotes) if the board is empty at the given coordinates.
	 * 
	 * NOTE: All other methods require this method to be working for testing.
	 * 
	 * @param i - i coordinate
	 * @param j - j coordinate
	 * @param k - k coordinate
	 * @return value
	 */
	protected char getValue(int i, int j, int k) {
		if(!isValidCoordinate(i,j,k))
		{	return (char) -1;}
	
		else   if(board[i][j][k]==0)
			return 0;
			return board[i][j][k];	
	}
	
	/**
	 * #3
	 * 
	 * Set the value of the board at (i, j, k)
	 * 
	 * if the coordinates are invalid, do nothing
	 * 
	 * NOTE: All other methods require this method to be working for testing.
	 * 
	 * @param i - i coordinate
	 * @param j - j coordinate
	 * @param k - k coordinate
	 * @param value - the new value
	 */
	protected void setValue(int i, int j, int k, char value) {	
		if(!isValidCoordinate(i,j,k))
			return;
		board[i][j][k]=b;
	}
	
	/**
	 * #4
	 * 
	 * Check if the placement is Valid
	 * 
	 * The placement is valid if and only if:
	 * 		* the coordinates are valid
	 * and	* the column is not full
	 * 
	 * @param placement - the position of the placement
	 * @return true - if the placement is valid
	 * @return false - if the placement is invalid
	 */
	protected boolean isValidPlacement(Coordinate placement) {
		if(isValidCoordinate(i,j,k))
		{if(k==0)
			if(getValue(i,j,k)==0)
			return true;
			}
		{if(k>0)
			if(getValue(i,j,k-1)!=0)
				if(getValue(i,j,k)==0)
					return true;
		}
                 return false;
	}
	
	/**
	 * #5
	 * 
	 * Place a value at the coordinate
	 * 
	 * If the position is invalid, ignore it.
	 *  
	 * @param position - the position of the placement
	 * @param value - the value ('b'/'w')
	 */
	protected void place(Coordinate placement, char value) {
		if(!isValidPlacement(placement))
			return;
		board[i][j][k]= b;
	}
	
	/**
	 * #6
	 * 
	 * Check if the game is over.
	 * 
	 * The game is over if and only if:
	 * 		* 64 moves have been attempted (valid or invalid) and neither player has 
	 * 		won;
	 * or		* a player has won.
|	 * 
	 * A player has won if and only if:
	 * 		* they have boardSize pieces in a row (horizontal, vertical or diagonal)
	 * 	
	 * @return
	 */
	protected boolean isGameOver() {
		for (int i=0;i<4;i++)
		{for(int j=0;j<4;j++)
		  {for(int k=0;k<4;k++)
			  if (board[i][j][k]!=0)
				  return true;//game over in draw
		  }
	    for(int x=0;x<4;x++){
	    	for(int y=0;y<4;y++)
	    	{int z=0;
	    	if(board[x][y][z]==board[x][y][z+1])
	    	{if(board[x][y][z+1]==board[x][y][z+2]){
	    		if(board[x][y][z+2]==board[x][y][z+3])
	    		{
	    			if((board[x][y][z]==b)||(board[x][y][z]==w))
	    				return true;//position 1 win in orthogonal height ;
	    		}
	    		}
	    	}
	    		
	    	}
	    }
	    for(int z=0;z<4;z++){
	    	for(int y=0;y<4;y++)
	    	{int x=0;
	    	if(board[x][y][z]==board[x+1][y][z])
	    	{if(board[x+2][y][z]==board[x+3][y][z]){
	    		if(board[x][y][z]==board[x+3][y][z])
	    		{
	    			if((board[x][y][z]==b)||board[x][y][z]==w)
	    				return true;//position 2 win in orthogonal width;
	    		}
	    		}
	    	}
	    		
	    	}
	    }
	    for(int x=0;x<4;x++){
	    	for(int z=0;z<4;z++)
	    	{int y=0;
	    	if(board[x][y][z]==board[x][y+1][z])
	    	{if(board[x][y+1][z]==board[x][y+2][z]){
	    		if(board[x][y+2][z]==board[x][y+3][z])
	    		{
	    			if((board[x][y][z]==b)||(board[x][y][z]==w))
	    				return true;//position 3 win in orthogonal length;
	    		}
	    		}
	    	}
	    		
	    	}
	    }
	    {int x=0;
	    int y=0;
	    int z=0;
	    {if(board[x][y][z]==board[x+1][y+1][z])
	    {if(board[x+1][y+1][z]==board[x+2][y+2][z])
	    {if(board[x+2][y+2][z]==board[x+3][y+3][z])
	    {if((board[x][y][z]==b)||board[x][y][z]==w)//win in diagonal line in one plane
	    	return true;
	    	
	    }
	    }
	    	
	    }
	    {if(board[x+3][y][z]==board[x+2][y+1][z])
	    {if(board[x+2][y+1][z]==board[x+1][y+2][z])
	    {if(board[x+1][y+2][z]==board[x+3][y+3][z])
	    {if((board[x+3][y][z]==b)||board[x+3][y][z]==w)
	    	return true;//win in diagonal line in one plane
	    	
	    }
	    }
	    	
	    }
	    
	    }
	    }
	    }
	    
	    }
	   
		
		return false;
	}
	
	/**
	 * Run the game and return the winning player
	 * 		1. Notify players of new opponent (use the opponent's class' canonical name)
	 * 		2. Run the game until the game is over
	 * 		3. Notify players of outcome (possible outcomes are WIN, DRAW, LOSS
	 * 		4. Return winning player (return null for a draw)
	 * 
	 * 
	 * @return winning player
	 * @return null - if there's a draw
	 */
	public Player run() {
		// TODO fill in this method
		return null;
	}
}

