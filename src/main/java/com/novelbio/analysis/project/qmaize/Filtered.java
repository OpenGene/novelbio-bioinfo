package com.novelbio.analysis.project.qmaize;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class Filtered {
	
	public static void main(String[] args) {
		filtered();
	}
	
	public static void filtered() {
		String parentFile = "/media/winE/NBC/Project/Project_Q_Lab/ÇúÓñÃ×/";
		String seqFile = parentFile +  "0.fq.gz";
		TxtReadandWrite txtStatistic = new TxtReadandWrite(FileOperate.changeFileSuffix(seqFile, "_MapStatistic", "txt"), true);
		
		
		FastQ fastQ = new FastQ(seqFile , FastQ.QUALITY_MIDIAN);  
		fastQ.setReadsLenMin(40);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		FastQ fastQ2 = fastQ.filterReads(FileOperate.changeFileSuffix(seqFile, "_filtered", "fq"));
		txtStatistic.writefileln(seqFile+"\t" + fastQ.getSeqNum() + fastQ2.getSeqNum());
		
		seqFile = parentFile +  "1.fq.gz";
		fastQ = new FastQ(seqFile , FastQ.QUALITY_MIDIAN);
		fastQ.setReadsLenMin(40);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ2 = fastQ.filterReads(FileOperate.changeFileSuffix(seqFile, "_filtered", "fq"));
		txtStatistic.writefileln(seqFile+"\t" + fastQ.getSeqNum() + fastQ2.getSeqNum());
		
		seqFile = parentFile +  "2.fq.gz";
		fastQ = new FastQ(seqFile , FastQ.QUALITY_MIDIAN);
		fastQ.setReadsLenMin(40);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ2 = fastQ.filterReads(FileOperate.changeFileSuffix(seqFile, "_filtered", "fq"));
		txtStatistic.writefileln(seqFile+"\t" + fastQ.getSeqNum() + fastQ2.getSeqNum());
		
		seqFile = parentFile +  "3.fq.gz";
		fastQ = new FastQ(seqFile , FastQ.QUALITY_MIDIAN);
		fastQ.setReadsLenMin(40);
		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
		fastQ2 = fastQ.filterReads(FileOperate.changeFileSuffix(seqFile, "_filtered", "fq"));
		txtStatistic.writefileln(seqFile+"\t" + fastQ.getSeqNum() + fastQ2.getSeqNum());
		txtStatistic.close();
	}
}
