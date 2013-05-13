package com.novelbio.analysis.emboss.motif;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Protectable;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * 输入连配好的fasta文件，或序列
 * 用emboss的权重矩阵扫描motif
 * @author zong0jie
 *
 */
public class MotifEmboss {
	public static void main(String[] args) {
		String fileName = "TSS_36_UP.fasta";
		SeqHash seqHashMotif = new SeqHash("/home/zong0jie/桌面/20121224/motif.fasta");
		SeqHash seqHash = new SeqHash("/home/zong0jie/桌面/20121224/Tss/" + fileName);
		MotifEmboss motifEmboss = new MotifEmboss();
		motifEmboss.motifPath = "/home/zong0jie/Desktop/test/";
		motifEmboss.setAlignedMotifSeqHash(seqHashMotif);
		motifEmboss.setSeqHash(seqHash);
		motifEmboss.setMotifEmbossScanAlgorithm(MotifEmbossScanAlgorithm.Frequency);
		String[] result = motifEmboss.scanMotif();
		FileOperate.moveFile(result[0], "/home/zong0jie/桌面/20121224/motifResult", fileName + "motif.txt", true);
		FileOperate.moveFile(result[1], "/home/zong0jie/桌面/20121224/motifResult", fileName + "motif_reverse.txt", true);
	}
	
	SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.emboss);
	
	/** 连配好的motif */
	Collection<SeqFasta> colAlignmentMotif;
	/** 要扫描的序列 */
	Collection<SeqFasta> colSeqFasta;
	/** motif分析所在的临时文件夹 */
	String motifPath;
	
	Prophecy prophecy;
	Profit profit;
	
	MotifEmbossScanAlgorithm motifEmbossScanAlgorithm = MotifEmbossScanAlgorithm.Gribskov;
	
	/** 输入连配好的motif */
	public void setColAlignedMotifFasta(Collection<SeqFasta> colAlignmentMotif) {
		this.colAlignmentMotif = colAlignmentMotif;
	}
	
	/** 输入连配好的motif */
	public void setAlignedMotifSeqHash(SeqHash seqHash) {
		colAlignmentMotif = new ArrayList<SeqFasta>();
		addSeq(colAlignmentMotif, seqHash);
	}
	
	/** 输入要扫描的序列 */
	public void setColSeqFasta(Collection<SeqFasta> colSeqFasta) {
		this.colSeqFasta = colSeqFasta;
	}
	
	/** 输入要扫描的序列 */
	public void setSeqHash(SeqHash seqHash) {
		colSeqFasta = new ArrayList<SeqFasta>();
		addSeq(colSeqFasta, seqHash);
	}
	
	public void setMotifEmbossScanAlgorithm(MotifEmbossScanAlgorithm motifEmbossScanAlgorithm) {
		this.motifEmbossScanAlgorithm = motifEmbossScanAlgorithm;
	}
	
	/**
	 * @param colSeq motif
	 * @param seqHash 待扫描的序列
	 */
	private void addSeq(Collection<SeqFasta> colSeq, SeqHash seqHash) {
		ArrayList<String> lsSeqName = seqHash.getLsSeqName();
		for (String string : lsSeqName) {
			SeqFasta seqFasta = seqHash.getSeq(string);
			colSeq.add(seqFasta);
		}
	}
	
	/**
	 * 返回motif分析得到的文件名
	 * @return
	 */
	private String[] scanMotif() {
		setParam();
		String suffix = "_" +DateUtil.getDateAndRandom();
		String alignedMotif = writeAlignedMotif(suffix);
		String seqfastaNeedScan = writeSeqfastaNeedScan(suffix);
		String[] weightMatrixFile = generateWeightMatrix(alignedMotif, suffix);
		String[] resultMotif = new String[weightMatrixFile.length];
		resultMotif[0] = scanAndGetResult(weightMatrixFile[0], seqfastaNeedScan, suffix);
		if (resultMotif.length > 1) {
			resultMotif[1] = scanAndGetResult(weightMatrixFile[1], seqfastaNeedScan, "_reserve" + suffix);
		}
		return resultMotif;
	}
	/**
	 * 将aligment文件写入文本，并返回文件名
	 * @param suffix 时间日期随机数
	 * @return
	 */
	private String writeAlignedMotif(String suffix) {
		String resultFile = FileOperate.addSep(motifPath) + "AlignedMofit" + suffix + ".fa";
		TxtReadandWrite txtWrite = new TxtReadandWrite(resultFile, true);
		for (SeqFasta seqFasta : colAlignmentMotif) {
			txtWrite.writefileln(seqFasta.toStringNRfasta());
		}
		txtWrite.close();
		return resultFile;
	}
	
	/**
	 * 将待扫描的文件写入文本，并返回文件名
	 * @param suffix 时间日期随机数
	 * @return
	 */
	private String writeSeqfastaNeedScan(String suffix) {
		String resultFile = FileOperate.addSep(motifPath) + "Sequence" + suffix + ".fa";
		TxtReadandWrite txtWrite = new TxtReadandWrite(resultFile, true);
		for (SeqFasta seqFasta : colSeqFasta) {
			txtWrite.writefileln(seqFasta.toStringNRfasta());
		}
		txtWrite.close();
		return resultFile;
	}
	
	/** 生成打分矩阵，并返回结果 */
	private String[] generateWeightMatrix(String alignedMotif, String suffix) {
		prophecy.setExePath(softWareInfo.getExePath());
		prophecy.setInAlignment(alignedMotif);
		prophecy.setMatrixAlgorithm(motifEmbossScanAlgorithm);
		String resultFile = FileOperate.addSep(motifPath) + "weightedMatrix" + suffix + ".fa";
		return prophecy.generateProfit(resultFile);
	}
	
	private void setParam() {
		boolean isNr = true;
		int i = 100;
		for (SeqFasta seqFasta : colSeqFasta) {
			//判定前100条序列看是核酸还是蛋白
			if (i >= 100) {
				break;
			}
			if (seqFasta.getSeqType() == SeqFasta.SEQ_PRO) {
				isNr = false;
				break;
			}
			i++;
		}
		prophecy = new Prophecy(isNr);
		profit = new Profit(isNr);
	}
	
	private String scanAndGetResult(String weightMatrix, String seqFile, String suffix) {
		profit.setExePath(softWareInfo.getExePath());
		profit.setInProfit(weightMatrix);
		profit.setSeqFile(seqFile);
		String resultFile = FileOperate.addSep(motifPath) + "MotifScaningResult" + suffix + ".fa";
		profit.scaning(resultFile);
		return resultFile;
	}
	
	
	/** emboss的prophecy软件用来产生权重矩阵的算法 */
	public static enum MotifEmbossScanAlgorithm {
		Frequency, 
		/**  For Gribskov the scoring scheme is based on a notion of distance 
		 * between a sequence and an ancestral or generalized sequence. 
		 */
		Gribskov, 
		/**  For Henikoff it is based on weights of the diversity observed at each position in
		 *  the alignment, rather than on a sequence distance measure. 
		 */
		Henikoff
	}
}
