package com.novelbio.analysis.annotation.blast;

import java.util.HashMap;

import com.novelbio.analysis.seq.fasta.SeqFasta;

public enum BlastType {

	tblastn, tblastx, blastn, blastx, blastp;
	/**
	 * @param QfastaType 输入的fasta类型，是核酸还是蛋白
	 * @param SfastaType 输出的fasta类型，是核酸还是蛋白
	 * @return blast类型的哈希表
	 * key：说明
	 * value：具体的blast类型，如BLAST_TBLASTN_AA2NR_WITH_AA等，可直接用于设置本类的参数
	 */
	public static HashMap<String, BlastType> getMapBlastType(int QfastaType, int SfastaType) {
		HashMap<String, BlastType> hashBlastType = new HashMap<String, BlastType>();
		if (QfastaType == SeqFasta.SEQ_PRO) {
			if (SfastaType == SeqFasta.SEQ_PRO) {
				hashBlastType.put("BLASTP_AA2AA_WITH_AA", blastp);
			}
			else if (SfastaType == SeqFasta.SEQ_DNA || SfastaType == SeqFasta.SEQ_RNA) {
				hashBlastType.put("TBLASTN_AA2NR_WITH_AA", tblastn);
			}
			else if (SfastaType == SeqFasta.SEQ_UNKNOWN) {
				hashBlastType.put("TBLASTN_AA2NR_WITH_AA", tblastn);
				hashBlastType.put("BLASTP_AA2AA_WITH_AA", blastp);
			}
		}
		else if (QfastaType == SeqFasta.SEQ_DNA || QfastaType == SeqFasta.SEQ_RNA) {
			if (SfastaType == SeqFasta.SEQ_PRO) {
				hashBlastType.put("BLASTX_NR2AA_WITH_AA", blastx);
			}
			else if (SfastaType == SeqFasta.SEQ_DNA || SfastaType == SeqFasta.SEQ_RNA) {
				hashBlastType.put("TBLASTX_NR2NR_WITH_AA", tblastx);
				hashBlastType.put("BLASTN_NR2NR_WITH_NR", blastn);
			}
			else if (SfastaType == SeqFasta.SEQ_UNKNOWN) {
				hashBlastType.put("TBLASTX_NR2NR_WITH_AA", tblastx);
				hashBlastType.put("BLASTN_NR2NR_WITH_NR", blastn);
				hashBlastType.put("BLASTX_NR2AA_WITH_AA", blastx);
			}
		}
		else if (QfastaType == SeqFasta.SEQ_UNKNOWN) {
			if (SfastaType == SeqFasta.SEQ_PRO) {
				hashBlastType.put("BLASTX_NR2AA_WITH_AA", blastx);
				hashBlastType.put("BLASTP_AA2AA_WITH_AA", blastp);
			}
			else if (SfastaType == SeqFasta.SEQ_DNA || SfastaType == SeqFasta.SEQ_RNA) {
				hashBlastType.put("TBLASTN_AA2NR_WITH_AA", tblastn);
				hashBlastType.put("TBLASTX_NR2NR_WITH_AA", tblastx);
				hashBlastType.put("BLASTN_NR2NR_WITH_NR", blastn);
			}
			else if (SfastaType == SeqFasta.SEQ_UNKNOWN) {
				hashBlastType.put("TBLASTN_AA2NR_WITH_AA", tblastn);
				hashBlastType.put("TBLASTX_NR2NR_WITH_AA", tblastx);
				hashBlastType.put("BLASTN_NR2NR_WITH_NR", blastn);
				hashBlastType.put("BLASTX_NR2AA_WITH_AA", blastx);
				hashBlastType.put("BLASTP_AA2AA_WITH_AA", blastp);
			}
		}
		return hashBlastType;
	}

}
