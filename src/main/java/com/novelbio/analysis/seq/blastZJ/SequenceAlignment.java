package com.novelbio.analysis.seq.blastZJ;

import com.novelbio.analysis.seq.fasta.SeqFasta;

/**
 * @author Paul Reiners
 * 
 */
public abstract class SequenceAlignment extends DynamicProgramming {

	protected int matchScore = 1;
	protected int mismatchScore = -1;
	protected int spaceScore = -1;
	protected String[] alignments;
	
	Cell lastCell;
	

	public SequenceAlignment(String sequence1, String sequence2) {
		super(sequence1, sequence2);
	}
	/**
	 * 大于0，默认为1
	 * @param matchScore
	 */
	public void setMatchScore(int matchScore) {
		this.matchScore = matchScore;
	}
	/** 小于0，默认-1 */
	public void setMismatchScore(int mismatchScore) {
		this.mismatchScore = mismatchScore;
	}
	/** 小于0，默认-1 */
	public void setSpaceScore(int spaceScore) {
		this.spaceScore = spaceScore;
	}
	protected Object getTraceback() {
		StringBuffer align1Buf = new StringBuffer();
		StringBuffer align2Buf = new StringBuffer();
		Cell currentCell = getTracebackStartingCell();
		while (traceBackIsNotDone(currentCell)) {
			if (currentCell.getRow() - currentCell.getPrevCell().getRow() == 1) {
				align2Buf.insert(0, sequence2.charAt(currentCell.getRow() - 1));
			} else {
				align2Buf.insert(0, '-');
			}
			if (currentCell.getCol() - currentCell.getPrevCell().getCol() == 1) {
				align1Buf.insert(0, sequence1.charAt(currentCell.getCol() - 1));
			} else {
				align1Buf.insert(0, '-');
			}
			currentCell = currentCell.getPrevCell();
			lastCell = currentCell;
		}

		String[] alignments = new String[] { align1Buf.toString(), align2Buf.toString() };

		return alignments;
	}
	
	protected abstract boolean traceBackIsNotDone(Cell currentCell);

	public void runBlast() {
		if (alignments != null) {
			return;
		}
		ensureTableIsFilledIn();
		alignments = (String[]) getTraceback();
	}
	
	public int getAlignmentScore() {
		runBlast();
		
		int score = 0;
		for (int i = 0; i < alignments[0].length(); i++) {
			char c1 = alignments[0].charAt(i);
			char c2 = alignments[1].charAt(i);
			if (c1 == '-' || c2 == '-') {
				score += spaceScore;
			} else if (c1 == c2) {
				score += matchScore;
			} else {
				score += mismatchScore;
			}
		}

		return score;
	}

	public String[] getAlignments() {
		runBlast();
		return alignments;
	}
	public abstract Cell getTracebackStartingCell();
	/** 运行完blast之后才能获得 */
	public Cell getTracebackEndingCell() {
		return lastCell;
	}
}
