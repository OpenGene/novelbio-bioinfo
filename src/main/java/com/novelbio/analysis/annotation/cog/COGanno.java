package com.novelbio.analysis.annotation.cog;

import com.novelbio.analysis.annotation.blast.BlastNBC;
import com.novelbio.analysis.annotation.blast.BlastType;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.generalConf.PathDetailNBC;

public class COGanno {
	BlastNBC blastNBC = new BlastNBC();
	String cogFastaFile;
	String seqFastaFile;
	int threadNum = 4;
	
	String blastTmpFile;
	
	/** 和COG做blast的临时文件 */
	public void setBlastTmpFile(String blastTmpFile) {
		this.blastTmpFile = blastTmpFile;
	}
	
	
	private String getSeqToCOG() {
		
	}
	
	
	private String blastSeqToCOG() {
		if (blastTmpFile == null || blastTmpFile.trim().equals("")) {
			blastTmpFile = PathDetail.getTmpPath() + "blastToCOG" + DateUtil.getDateAndRandom();
		}
		
		BlastType blastType = BlastType.blastp;
		int seqQueryType = SeqHash.getSeqType(seqFastaFile);
		if (seqQueryType != SeqFasta.SEQ_PRO) {
			blastType = BlastType.blastx;
		}
		blastNBC.setResultFile(blastTmpFile);
		blastNBC.setBlastType(blastType);
		blastNBC.setEvalue(1e-10);
		blastNBC.setCpuNum(threadNum);
		blastNBC.setQueryFastaFile(seqFastaFile);
		blastNBC.setDatabaseSeq(cogFastaFile);
		blastNBC.setResultSeqNum(1);
		boolean isSucess = blastNBC.blast();
		if (isSucess) {
			return blastTmpFile;
		} else {
			return null;
		}
	}
}
