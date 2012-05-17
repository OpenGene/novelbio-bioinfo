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
    * 第几个连续gap，目前仅在SmithWaterman算法中用到
    * 用于连续gap减少罚分
    */
   private int gapNum = 0;;
   /**
    * 从中间开始向两边递增的权重
    */
   private double gradientMid = -1;
   /**
    * 从头开始向尾递增的权重
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
    * 经过校正的分数，用于拼接
    * @param length1 sequence1的长度
    * @param length2 sequence2的长度
    * @param gradientStart 中间位置的比例是多少
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
	 * 添加一个gap
	 */
	public void setGapNum(int gapNum) {
		this.gapNum = gapNum;
	}
	/**
	 * 获得之前几个gap
	 * @return
	 */
	public int getGapNum() {
		return gapNum;
	}
	/**
	 * 
	 * @param cellPrev 
	 * @param space 第一个空位的罚分
	 * @param gapDegrade 空位罚分的递减，按照指数下降的递减，底数为gapDegrade
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
	    * 经过校正的分数，用于拼接
	    * @param length1 sequence1的长度
	    * @param length2 sequence2的长度
	    * @param gradientStar 头部起点的权重
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
	    * 经过校正的分数，用于拼接
	    * @param length1 sequence1的长度
	    * @param length2 sequence2的长度
	    * @param gradientStart 中间位置的比例是多少
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