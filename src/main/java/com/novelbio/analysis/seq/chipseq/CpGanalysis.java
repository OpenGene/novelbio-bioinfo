package com.novelbio.analysis.seq.chipseq;

import com.novelbio.analysis.seq.fasta.SeqFasta;

public class CpGanalysis {
	int CG;
	int CHG;
	int CHH;

	
	/** 输入待判断的序列，注意输入的序列要左右扩展2bp，
	 * 计算的位点数量会叠加 */
	public void addSequence(SeqFasta seq, boolean isStrandSpecific) {
		addSequence(seq.toString(), isStrandSpecific);
	}
	
	/** 输入待判断的序列，注意输入的序列要左右扩展2bp，
	 * 计算的位点数量会叠加 */
	public void addSequence(String seq, boolean isStrandSpecific) {
		analysisSeq(seq);
		if (!isStrandSpecific) {
			seq = SeqFasta.reverseComplement(seq);
			analysisSeq(seq);
		}
	}
	
	/** 只分析开头第二个碱基到结尾第二个碱基中间的c，因为输入的时候有扩展了2bp */
	private void analysisSeq(String seq) {
		char[] ss = seq.toLowerCase().toCharArray();
		for (int i = 2; i < ss.length - 2; i++) {
			char base0 = ss[i];
			char base1 = ss[i+1];
			char base2 = ss[i+2];
			if (base0 != 'c') continue;
			
			if (base1 == 'g') {
				CG++;
			} else if (base1 != 'g' && base2 == 'g') {
				CHG++;
			} else if (base1 != 'g' && base2 != 'g') {
				CHH++;
			}
		}
	}
	
	/** CG位点的数目 */
	public int getCGNum() {
		return CG;
	}
	/** CHG位点的数目 */
	public int getCHGNum() {
		return CHG;
	}
	/** CHH位点的数目 */
	public int getCHHNum() {
		return CHH;
	}
	
	public void addCpGInfo(CpGanalysis cpGanalysis) {
		this.CG += cpGanalysis.CG;
		this.CHG += cpGanalysis.CHG;
		this.CHH += cpGanalysis.CHH;
	}
	
}
