package com.novelbio.analysis.blastzj;


/**
 * LCS算法
 * @author Paul Reiners
 * 
 */
public class LongestCommonSubsequence extends DynamicProgramming {

   public LongestCommonSubsequence(String sequence1, String sequence2) {
      super(sequence1, sequence2);
   }

   public String getLongestCommonSubsequence() {
      if (!isInitialized) {
         initialize();
      }
      if (!tableIsFilledIn) {
         fillIn();
      }

      return (String) getTraceback();
   }

   @Override
   protected void fillInCell(Cell currentCell, Cell cellAbove, Cell cellToLeft,
         Cell cellAboveLeft) {
      double aboveScore = cellAbove.getScore();
      double leftScore = cellToLeft.getScore();
      double matchScore;
      //如果前一行和前一列的
      if (sequence1.charAt(currentCell.getCol() - 1) == sequence2.charAt(currentCell.getRow() - 1)) {
         matchScore = cellAboveLeft.getScore() + 1;
      } else {
         matchScore = cellAboveLeft.getScore();
      }
      double cellScore;
      Cell cellPointer;
      if (matchScore >= aboveScore) {
         if (matchScore >= leftScore) {
            // matchScore >= aboveScore and matchScore >= leftScore
            cellScore = matchScore;
            cellPointer = cellAboveLeft;
         } else {
            // leftScore > matchScore >= aboveScore
            cellScore = leftScore;
            cellPointer = cellToLeft;
         }
      } else {
         if (aboveScore >= leftScore) {
            // aboveScore > matchScore and aboveScore >= leftScore
            cellScore = aboveScore;
            cellPointer = cellAbove;
         } else {
            // leftScore > aboveScore > matchScore
            cellScore = leftScore;
            cellPointer = cellToLeft;
         }
      }
      currentCell.setScore(cellScore);
      currentCell.setPrevCell(cellPointer);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.ibm.compbio.seqalign.DynamicProgramming#getInitialPointer(int,
    *      int)
    */
   @Override
   protected Cell getInitialPointer(int row, int col) {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.ibm.compbio.seqalign.DynamicProgramming#getInitialScore(int, int)
    */
   @Override
   protected int getInitialScore(int row, int col) {
      return 0;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.ibm.compbio.seqalign.DynamicProgramming#getTraceback()
    */
   @Override
   protected Object getTraceback() {
      StringBuffer lCSBuf = new StringBuffer();
      Cell currentCell = scoreTable[scoreTable.length - 1][scoreTable[0].length - 1];
      while (currentCell.getScore() > 0) {
         Cell prevCell = currentCell.getPrevCell();
         if ((currentCell.getRow() - prevCell.getRow() == 1  && currentCell.getCol() - prevCell.getCol() == 1)
               && currentCell.getScore() == prevCell.getScore() + 1) {
            lCSBuf.insert(0, sequence1.charAt(currentCell.getCol() - 1));
         }
         else {
        	  lCSBuf.insert(0, "-");
		}
         //这里可以在else里面插入一个横杠
         currentCell = prevCell;
      }

      return lCSBuf.toString();
   }
}
