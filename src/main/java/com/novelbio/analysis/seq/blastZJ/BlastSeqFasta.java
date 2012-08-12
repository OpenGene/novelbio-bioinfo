package com.novelbio.analysis.seq.blastZJ;

import com.novelbio.analysis.seq.fasta.SeqFasta;

/**
 * 
 * @author zong0jie
 * 按照evalue从小到大排序
 */
public class BlastSeqFasta {
	public static void main(String[] args) {
		SeqFasta seqFasta = new SeqFasta("satdgsegestaa");
		SeqFasta seqFasta2 = new SeqFasta("sefsefsfesatdgseesegestsefsef");
		BlastSeqFasta blastSeqFasta = new BlastSeqFasta(seqFasta, seqFasta2);
		blastSeqFasta.blast();
		System.out.println(blastSeqFasta.alignmentQuery);
		System.out.println(blastSeqFasta.alignmentSubject);
	}
	
	SeqFasta seqFastaQuery;
	SeqFasta seqFastaSubject;
	
	String alignmentQuery;
	String alignmentSubject;
	
	///parameter///
	int mismatchScore = -1;
	int spaceScore = -1;
	double gapDegrade = 1.1;
	///
	boolean blast = false;
	////result ///
	double identities;
	int matchNum;
	int misMathchNum;
	int gapNumQuery;
	int gapNumSubject;
	
	double score;
	/** 从0开始记数，可直接使用substring方法 */
	int startQuery;
	/** 从0开始记数，可直接使用substring方法 */
	int startSubject;
	/** 从1开始记数，可直接使用substring方法 */
	int endQuery;
	/** 从1开始记数，可直接使用substring方法 */
	int endSubject;
	
	public BlastSeqFasta(SeqFasta seqFastaQuery, SeqFasta seqFastaSubject) {
		this.seqFastaQuery = seqFastaQuery;
		this.seqFastaSubject = seqFastaSubject;
	}
	public BlastSeqFasta(String seqFastaQuery, String seqFastaSubject) {
		this.seqFastaQuery = new SeqFasta(seqFastaQuery);
		this.seqFastaSubject = new SeqFasta(seqFastaSubject);
	}
	/** 默认-1 */
	public void setMismatchScore(int mismatchScore) {
		this.mismatchScore = mismatchScore;
	}
	/** 默认1.1，越大下降越快 */
	public void setGapDegrade(double gapDegrade) {
		this.gapDegrade = gapDegrade;
		
	}
	/** 默认-1 */
	public void setSpaceScore(int spaceScore) {
		this.spaceScore = spaceScore;
	}
	
	public void blast() {
		if (blast) return;
		
		blast = true;
		SmithWaterman smithWaterman = new SmithWaterman(seqFastaQuery.toString().toUpperCase(), seqFastaSubject.toString().toUpperCase());
		setBlastparam(smithWaterman);
		smithWaterman.runBlast();
		alignmentQuery = smithWaterman.getAlignments()[0];
		alignmentSubject = smithWaterman.getAlignments()[1];
		
		Cell cellStart = smithWaterman.getTracebackEndingCell();
		Cell cellEnd = smithWaterman.getTracebackStartingCell();
		score = cellEnd.getScore();
		
		startQuery = cellStart.getCol(); startSubject = cellStart.getRow();
		endQuery = cellEnd.getCol(); endSubject = cellEnd.getRow();
		setInfo();
	}
	private void setBlastparam(SmithWaterman smithWaterman) {
		smithWaterman.setMismatchScore(mismatchScore);
		smithWaterman.setSpaceScore(spaceScore);
		smithWaterman.setGapDegrade(gapDegrade);
	}
	private void setInfo() {
		for (int i = 0; i < alignmentQuery.length(); i++) {
			char c1 = alignmentQuery.charAt(i);
			char c2 = alignmentSubject.charAt(i);
			if (c1 == '-') {
				gapNumQuery++;
			}
			else if (c2 == '-') {
				gapNumSubject++;
			} else if (c1 == c2) {
				matchNum++;
			} else {
				misMathchNum++;
			}
		}
	}
	
	public String getAlignmentQuery() {
		return alignmentQuery;
	}
	public String getAlignmentSubject() {
		return alignmentSubject;
	}

	public int getGapNumQuery() {
		return gapNumQuery;
	}
	public int getGapNumSubject() {
		return gapNumSubject;
	}
	public double getIdentities() {
		return identities;
	}
	public int getMatchNum() {
		return matchNum;
	}
	public int getMisMathchNum() {
		return misMathchNum;
	}
	public double getScore() {
		return score;
	}
	public SeqFasta getSeqFastaQuery() {
		return seqFastaQuery;
	}
	public SeqFasta getSeqFastaSubject() {
		return seqFastaSubject;
	}
	/** 从0开始记数，可直接使用substring方法 */
	public int getStartQuery() {
		return startQuery;
	}
	/** 从0开始记数，可直接使用substring方法 */
	public int getStartSubject() {
		return startSubject;
	}
	/** 从1开始记数，可直接使用substring方法 */
	public int getEndQuery() {
		return endQuery;
	}
	/** 从1开始记数，可直接使用substring方法 */
	public int getEndSubject() {
		return endSubject;
	}
}
