package com.novelbio.analysis.seq.blastZJ;

/**
 * @author Paul Reiners
 * 
 */
public class NeedlemanWunsch extends SequenceAlignment {

   public NeedlemanWunsch(String sequence1, String sequence2) {
      super(sequence1, sequence2);
   }

   public NeedlemanWunsch(String sequence1, String sequence2, int match,
         int mismatch, int gap) {
      super(sequence1, sequence2);
   }

   protected void fillInCell(Cell currentCell, Cell cellAbove, Cell cellToLeft,
         Cell cellAboveLeft) {
	   double rowSpaceScore = cellAbove.getScore() + spaceScore;
	   double colSpaceScore = cellToLeft.getScore() + spaceScore;
	   double matchOrMismatchScore = cellAboveLeft.getScore();
      if (sequence2.charAt(currentCell.getRow() - 1) == sequence1
            .charAt(currentCell.getCol() - 1)) {
         matchOrMismatchScore += matchScore;
      } else {
         matchOrMismatchScore += mismatchScore;
      }
      if (rowSpaceScore >= colSpaceScore) {
         if (matchOrMismatchScore >= rowSpaceScore) {
            currentCell.setScore(matchOrMismatchScore);
            currentCell.setPrevCell(cellAboveLeft);
         } else {
            currentCell.setScore(rowSpaceScore);
            currentCell.setPrevCell(cellAbove);
         }
      } else {
         if (matchOrMismatchScore >= colSpaceScore) {
            currentCell.setScore(matchOrMismatchScore);
            currentCell.setPrevCell(cellAboveLeft);
         } else {
            currentCell.setScore(colSpaceScore);
            currentCell.setPrevCell(cellToLeft);
         }
      }
   }

   @Override
   protected boolean traceBackIsNotDone(Cell currentCell) {
      return currentCell.getPrevCell() != null;
   }

   @Override
   public Cell getTracebackStartingCell() {
      return scoreTable[scoreTable.length - 1][scoreTable[0].length - 1];
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "[NeedlemanWunsch: sequence1=" + sequence1 + ", sequence2="
            + sequence2 + "]";
   }

   protected Cell getInitialPointer(int row, int col) {
      if (row == 0 && col != 0) {
         return scoreTable[row][col - 1];
      } else if (col == 0 && row != 0) {
         return scoreTable[row - 1][col];
      } else {
         return null;
      }
   }

   protected int getInitialScore(int row, int col) {
      if (row == 0 && col != 0) {
         return col * spaceScore;
      } else if (col == 0 && row != 0) {
         return row * spaceScore;
      } else {
         return 0;
      }
   }
}
