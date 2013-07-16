package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.denovo.ClusterSeq;
import com.novelbio.analysis.seq.denovo.N50AndSeqLen;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 修正Trinity之后的结果 */
public class TrinityModify {
	public static void main(String[] args) {
		TrinityModify trinityModify = new TrinityModify();
		trinityModify.setInFileName("/media/winE/NBC/Project/Project_WH/Trinity_tmp2013-07-150307-18080.fasta");
		trinityModify.copeFile();
	}
	
	/** trinity得到的fasta文件 */
	String inFileName;
	String outFileName;
	
	List<String> lsTmpFileName = new ArrayList<String>();
	
	public void setInFileName(String inFileName) {
		this.inFileName = inFileName;
	}
	
	public void cope() {
		String copeFile = copeFile();
		lsTmpFileName.add(copeFile);
		cluster(copeFile);
	}
	
	/** 将trinity的结果整理成用于做聚类的文件，返回文件名 */
	private String copeFile() {
		lsTmpFileName.clear();
		SeqFastaHash seqFastaHash = new SeqFastaHash(inFileName);
		String tmpFileName = FileOperate.changeFileSuffix(inFileName, "_tmp" + DateUtil.getDateAndRandom(), null);
		TxtReadandWrite txtWriteTmp = new TxtReadandWrite(tmpFileName, true);
		for (SeqFasta seqFasta : seqFastaHash.getSeqFastaAll()) {
			seqFasta.setName(seqFasta.getSeqName().split(" ")[0]);
			txtWriteTmp.writefileln(seqFasta.toStringNRfasta());
		}
		txtWriteTmp.close();
		return tmpFileName;
	}
	
	/** 返回完成的聚类对象 */
	private ClusterSeq cluster(String copedFile) {
		String tmpFileNameCluster = FileOperate.changeFileSuffix(inFileName, "_tmpCluster" + DateUtil.getDateAndRandom(), null);
		lsTmpFileName.add(tmpFileNameCluster);
		ClusterSeq clusterSeq = new ClusterSeq();
		clusterSeq.setInFileName(copedFile);
		clusterSeq.setOutFileName(tmpFileNameCluster);
		clusterSeq.setIdentityThrshld(85);
		clusterSeq.setThreadNum(4);
		clusterSeq.run();
		return clusterSeq;
	}
	
	private void removeTmpFile() {
		for (String fileName : lsTmpFileName) {
			FileOperate.DeleteFileFolder(fileName);
		}
	}
	
	/** 返回计算好的N50等信息 */
	public N50AndSeqLen getN50info() {
		N50AndSeqLen n50AndSeqLen = new N50AndSeqLen(inFileName);
		return n50AndSeqLen;
	}
	
	
	
}
