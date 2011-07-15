package com.novelbio.analysis.seq.blastZJ;

/**
 * @author Paul Reiners
 * 
 */
public class SmithWaterman extends SequenceAlignment {

	private Cell highScoreCell;
	double gapDegrade = 1;
	/**
	 * 
	 * @param sequence1
	 * @param sequence2
	 * @param gradiet �ݶȣ������ƴ��ʱ���ã�����˵����ߵ�Խ�ӽ����еı�Եʱ���÷�Խ�ߣ�Խ�������е��в����÷�Խ��
	 * ��ô��͵㣬Ҳ�������е��е��ּ�Ϊgradiet
	 */
	public SmithWaterman(String sequence1, String sequence2, double gapDegrade) {
		super(sequence1, sequence2);
		this.gapDegrade = gapDegrade;
	}

	public SmithWaterman(String sequence1, String sequence2, int match,
			int mismatch, int gap, double gapDegrade) {
		super(sequence1, sequence2, match, mismatch, gap);
		this.gapDegrade = gapDegrade;
	}

	protected void initialize() {
		super.initialize();

		highScoreCell = scoreTable[0][0];
	}

	protected void fillInCell(Cell currentCell, Cell cellAbove, Cell cellToLeft,
         Cell cellAboveLeft) {
      double rowSpaceScore = cellAbove.getScore() + currentCell.getGapScore(cellAbove, space,gapDegrade);
      double colSpaceScore = cellToLeft.getScore() + currentCell.getGapScore(cellToLeft, space,gapDegrade);
      double matchOrMismatchScore = cellAboveLeft.getScore();
      if (sequence2.charAt(currentCell.getRow() - 1) == sequence1.charAt(currentCell.getCol() - 1)) {
         matchOrMismatchScore += match;
      } else {
         matchOrMismatchScore += mismatch;
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
      //�����еķ���������һ���ݶȣ�Ҳ���ǿ�������β����Ȩ�ػ�ߣ��ɴ�������
      if (currentCell.getScore()*currentCell.getGradAllScore(sequence1.length(), sequence2.length(), 0.4) > highScoreCell.getScore()*highScoreCell.getGradAllScore(sequence1.length(), sequence2.length(), 0.4)) 
      {
         highScoreCell = currentCell;
      }
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

	@Override
	protected boolean traceBackIsNotDone(Cell currentCell) {
		return currentCell.getScore() != 0;
	}

	@Override
	protected Cell getTracebackStartingCell() {
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
