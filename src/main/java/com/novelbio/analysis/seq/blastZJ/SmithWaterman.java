package com.novelbio.analysis.seq.blastZJ;

import com.novelbio.analysis.seq.fasta.SeqFasta;

/**
 * @author Paul Reiners
 * 
 */
public class SmithWaterman extends SequenceAlignment {
	private Cell highScoreCell;
	double gapDegrade = 1.2;
	/** �Ƿ�����ƴ��β�� */
	boolean isTailPriority = false;
	
	/**
	 * @param sequence1
	 * @param sequence2
	 */
	public SmithWaterman(String sequence1, String sequence2) {
		super(sequence1, sequence2);
	}

	/**
	* @param gapDegrade ��λ���ֵĵݼ�������ָ���½��ĵݼ�������ΪgapDegrade
	*/
	public void setGapDegrade(double gapDegrade) {
		this.gapDegrade = gapDegrade;
	}
	protected void initialize() {
		super.initialize();
		highScoreCell = scoreTable[0][0];
	}
	/** �趨����ƴ��β�� */
	public void setTailPriroity() {
		isTailPriority = true;
	}
	protected void fillInCell(Cell currentCell, Cell cellAbove, Cell cellToLeft, Cell cellAboveLeft) {
      double rowSpaceScore = cellAbove.getScore() + currentCell.getGapScore(cellAbove, spaceScore,gapDegrade);
      double colSpaceScore = cellToLeft.getScore() + currentCell.getGapScore(cellToLeft, spaceScore,gapDegrade);
      double matchOrMismatchScore = cellAboveLeft.getScore();
      if (sequence2.charAt(currentCell.getRow() - 1) == sequence1.charAt(currentCell.getCol() - 1)) {
         matchOrMismatchScore += matchScore;
      } else {
         matchOrMismatchScore += mismatchScore;
      }
      if (rowSpaceScore >= colSpaceScore) {
         if (matchOrMismatchScore >= rowSpaceScore) {
            if (matchOrMismatchScore > 0) {
               currentCell.setScore(matchOrMismatchScore);
               currentCell.setPrevCell(cellAboveLeft);
            }
         } else {
            if (rowSpaceScore > 0) {
               currentCell.setScore(rowSpaceScore);
               currentCell.setGapNum(cellAbove.getGapNum()+1);
               currentCell.setPrevCell(cellAbove);
            }
         }
      } else {
         if (matchOrMismatchScore >= colSpaceScore) {
            if (matchOrMismatchScore > 0) {
               currentCell.setScore(matchOrMismatchScore);
               currentCell.setPrevCell(cellAboveLeft);
            }
         } else {
            if (colSpaceScore > 0) {
               currentCell.setScore(colSpaceScore);
               currentCell.setGapNum(cellToLeft.getGapNum()+1);
               currentCell.setPrevCell(cellToLeft);
            }
         }
      }
      setHighScore(currentCell);
	}
	private void setHighScore(Cell scoreCell) {
		if (!isTailPriority) {
			if (scoreCell.getScore() > highScoreCell.getScore()) { 
				highScoreCell = scoreCell; 
			}
		}
		else {
		      //�����еķ���������һ���ݶȣ�Ҳ���ǿ�������β����Ȩ�ػ�ߣ��ɴ�������
		      if (scoreCell.getScore()*scoreCell.getGradAllScore(sequence1.length(), sequence2.length(), 0.4) 
		    		  > 
		      highScoreCell.getScore()*highScoreCell.getGradAllScore(sequence1.length(), sequence2.length(), 0.4)) 
		      {
		         highScoreCell = scoreCell;
		      }
		}
	}


	@Override
	protected boolean traceBackIsNotDone(Cell currentCell) {
		return currentCell.getScore() != 0;
	}
	/** Ҳ������߷�����cell */
	@Override
	public Cell getTracebackStartingCell() {
		return highScoreCell;
	}

	@Override
	protected Cell getInitialPointer(int row, int col) {
		return null;
	}

	@Override
	protected int getInitialScore(int row, int col) {
		return 0;
	}
}
