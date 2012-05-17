package com.novelbio.analysis.seq.blastZJ;

/**
 * @author Paul Reiners
 *
 */
public class Cell {
   private Cell prevCell;
   private double score;
   private int row;
   private int col;
   /**
    * �ڼ�������gap��Ŀǰ����SmithWaterman�㷨���õ�
    * ��������gap���ٷ���
    */
   private int gapNum = 0;;
   /**
    * ���м俪ʼ�����ߵ�����Ȩ��
    */
   private double gradientMid = -1;
   /**
    * ��ͷ��ʼ��β������Ȩ��
    */
   private double gradientAll = -1;
   
   public Cell(int row, int col) {
      this.row = row;
      this.col = col;
   }

   /**
    * @param score
    *           the score to set
    */
   public void setScore(double score) {
      this.score = score;
   }

   /**
    * @return the score
    */
   public double getScore() {
      return score;
   }

   /**
    * @param prevCell
    *           the prevCell to set
    */
   public void setPrevCell(Cell prevCell) {
      this.prevCell = prevCell;
   }

   /**
    * @return the row
    */
   public int getRow() {
      return row;
   }

   /**
    * @return the col
    */
   public int getCol() {
      return col;
   }

   /**
    * @return the prevCell
    */
   public Cell getPrevCell() {
      return prevCell;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "Cell(" + row + ", " + col + "): score=" + score + ", prevCell="
            + prevCell + "]";
   }
   
   /**
    * ����У���ķ���������ƴ��
    * @param length1 sequence1�ĳ���
    * @param length2 sequence2�ĳ���
    * @param gradientStart �м�λ�õı����Ƕ���
    * @return
    */
	public double getGradMidScore(int length1, int length2,double gradientMidStart) {
		if (gradientMid > 0) {
			return gradientMid;
		}
		double cellRowInfo = (double) getRow() / length2;
		if (cellRowInfo < 0.5) {
			cellRowInfo = 1 - cellRowInfo;
		}
		double cellColInfo = (double) getCol() / length1;
		if (cellColInfo < 0.5) {
			cellColInfo = 1 - cellColInfo;
		}
		gradientMid = Math.max(cellColInfo, cellRowInfo);
		gradientMid = ((gradientMid - 0.5) * (1-gradientMidStart))/0.5 + gradientMidStart;
		return gradientMid;
	}
	/**
	 * ���һ��gap
	 */
	public void setGapNum(int gapNum) {
		this.gapNum = gapNum;
	}
	/**
	 * ���֮ǰ����gap
	 * @return
	 */
	public int getGapNum() {
		return gapNum;
	}
	/**
	 * 
	 * @param cellPrev 
	 * @param space ��һ����λ�ķ���
	 * @param gapDegrade ��λ���ֵĵݼ�������ָ���½��ĵݼ�������ΪgapDegrade
	 * @return
	 */
	public double getGapScore(Cell cellPrev, int space,double gapDegrade) {
		if (gapDegrade != 1) {
			return space*Math.pow(gapDegrade,-cellPrev.getGapNum());	
		}
		else
			return space * -cellPrev.getGapNum();
//		
//		if (cellPrev.getGapNum() == 0) {
//			return space;
//		}
//		return 1/Math.pow(space,cellPrev.getGapNum()+1);
	}
	
	   /**
	    * ����У���ķ���������ƴ��
	    * @param length1 sequence1�ĳ���
	    * @param length2 sequence2�ĳ���
	    * @param gradientStar ͷ������Ȩ��
	    * @return
	    */
		public double getGradAllScore(int length1, int length2,double gradientAllStart) {
			if (gradientAll > 0) {
				return gradientAll;
			}
			double cellRowInfo = (double) getRow() / length2;
			double cellColInfo = (double) getCol() / length1;

			gradientAll = Math.max(cellColInfo, cellRowInfo);
			gradientAll = gradientAll * (1-gradientAllStart) + gradientAllStart;
			return gradientAll;
		}
	
	   /**
	    * ����У���ķ���������ƴ��
	    * @param length1 sequence1�ĳ���
	    * @param length2 sequence2�ĳ���
	    * @param gradientStart �м�λ�õı����Ƕ���
	    * @return
	    */
		public static double getTestScore(int length1, int length2,int RowNum,int ColNum,double gradientStart) {
			double cellRowInfo = (double) RowNum / length2;
			if (cellRowInfo < 0.5) {
				cellRowInfo = 1 - cellRowInfo;
			}
			double cellColInfo = (double) ColNum / length1;
			if (cellColInfo < 0.5) {
				cellColInfo = 1 - cellColInfo;
			}
			double gradient = Math.max(cellColInfo, cellRowInfo);
			gradient = ((gradient - 0.5) * (1-gradientStart))/0.5 + gradientStart;
			return gradient;
		}
   
}