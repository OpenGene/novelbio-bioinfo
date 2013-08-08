package com.novelbio.analysis.emboss.motif;

import java.util.ArrayList;
import java.util.Collection;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.SeqHashInt;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.generalConf.PathNBCDetail;

/**
 * 输入连配好的fasta文件，或序列
 * 用emboss的权重矩阵扫描motif
 * @author zong0jie
 *
 */
public class MotifEmboss {
//	public static void main(String[] args) {
//		String fileName = "TSS_36_UP.fasta";
//		SeqHash seqHashMotif = new SeqHash("/home/zong0jie/桌面/20121224/motif.fasta");
//		SeqHash seqHash = new SeqHash("/home/zong0jie/桌面/20121224/Tss/" + fileName);
//		MotifEmboss motifEmboss = new MotifEmboss();
//		motifEmboss.motifPath = "/home/zong0jie/Desktop/test/";
//		motifEmboss.setAlignedMotifSeqHash(seqHashMotif);
//		motifEmboss.setSeqHash(seqHash);
//		motifEmboss.setMotifEmbossScanAlgorithm(MotifEmbossScanAlgorithm.Frequency);
//		String[] result = motifEmboss.scanMotif();
//		FileOperate.moveFile(result[0], "/home/zong0jie/桌面/20121224/motifResult", fileName + "motif.txt", true);
//		FileOperate.moveFile(result[1], "/home/zong0jie/桌面/20121224/motifResult", fileName + "motif_reverse.txt", true);
//	}
	
	SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.emboss);
	
	/** 连配好的motif */
	Collection<SeqFasta> colAlignmentMotif;
	
	/** 要扫描的序列，与seqFilePath二选一 */
	Collection<SeqFasta> colSeqFasta;
	/** 要扫描的序列，与colSeqFasta二选一 */
	String seqFilePath;
	
	/** motif分析所在的临时文件夹 */
	String motifPath = PathDetail.getTmpPath();
	
	Prophecy prophecy;
	Profit profit;
	
	MotifEmbossScanAlgorithm motifEmbossScanAlgorithm = MotifEmbossScanAlgorithm.Gribskov;
	/** 需要扫描的文件 */
	String seqfastaNeedScan;
	
	/** 产生的权重矩阵的文件路径 */
	private String[] weightMatrixFile;
	private  Boolean isNR = null;
	
	/** 输入连配好的motif */
	public void setColAlignedMotifFasta(Collection<SeqFasta> colAlignmentMotif) {
		this.colAlignmentMotif = colAlignmentMotif;
	}
	
	/** true是，false否
	 * null表示根据序列自动判定
	 * @param isNR
	 */
	public void setIsNR(Boolean isNR) {
		this.isNR = isNR;
	}
	
	/** 输入连配好的motif */
	public void setAlignedMotifSeqHash(SeqHashInt seqHash) {
		colAlignmentMotif = new ArrayList<SeqFasta>();
		addSeq(colAlignmentMotif, seqHash);
		seqFilePath = null;
	}
	
	/** 输入要扫描的序列 */
	public void setColSeqFasta(Collection<SeqFasta> colSeqFasta) {
		this.colSeqFasta = colSeqFasta;
	}
	
	/** 输入要扫描的序列 */
	public void setSeqHash(SeqHashInt seqHash) {
		colSeqFasta = new ArrayList<SeqFasta>();
		addSeq(colSeqFasta, seqHash);
		seqFilePath = null;
	}
	/** 输入要扫描的序列的文件名，务必是fasta格式，
	 * 与{@link #setColSeqFasta(Collection)} 只能选一个
	 */
	public void setSeqFilePath(String seqFilePath) {
		this.seqFilePath = seqFilePath;
		colSeqFasta = new ArrayList<SeqFasta>();
	}
	
	public void setMotifEmbossScanAlgorithm(MotifEmbossScanAlgorithm motifEmbossScanAlgorithm) {
		this.motifEmbossScanAlgorithm = motifEmbossScanAlgorithm;
	}
	
	/**
	 * @param colSeq motif
	 * @param seqHash 待扫描的序列
	 */
	private void addSeq(Collection<SeqFasta> colSeq, SeqHashInt seqHash) {
		ArrayList<String> lsSeqName = seqHash.getLsSeqName();
		for (String string : lsSeqName) {
			SeqFasta seqFasta = seqHash.getSeq(string);
			colSeq.add(seqFasta);
		}
	}
	
	public void generateMatrix() {
		setParam();
		String suffix = "_" +DateUtil.getDateAndRandom();
		String alignedMotif = writeAlignedMotif(suffix);
		weightMatrixFile = generateWeightMatrix(alignedMotif, suffix);
	}
	/**
	 * <b>之前务必要运行{@link #generateMatrix()}</b><br>
	 * @return 返回 string[2]<br>
	 * 0: 正链扫描结果<br>
	 * 1: 负链扫描结果
	 */
	public String[] scanMotif() {
		String suffix = "_" +DateUtil.getDateAndRandom();
		seqfastaNeedScan = writeSeqfastaNeedScan(suffix);
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
	
	private void setParam() {
		if (isNR == null) {
			isNR = getIsNR();
		}
		prophecy = new Prophecy(isNR);
		profit = new Profit(isNR);
	}
	
	/** 设定是DNA还是Protein */
	private boolean getIsNR() {
		boolean isNr = true;
		if (colSeqFasta != null && colSeqFasta.size() > 0) {
			int i = 100;
			for (SeqFasta seqFasta : colSeqFasta) {
				//判定前100条序列看是核酸还是蛋白
				if (i >= 100) break;
				
				if (seqFasta.getSeqType() == SeqFasta.SEQ_PRO) {
					isNr = false;
					break;
				}
				i++;
			}
		} else {
			int seqType = SeqHash.getSeqType(seqFilePath);
			isNr = (seqType == SeqFasta.SEQ_DNA);
		}
		return isNr;
	}
	
	/**
	 * 将待扫描的文件写入文本，并返回文件名
	 * @param suffix 时间日期随机数
	 * @return
	 */
	private String writeSeqfastaNeedScan(String suffix) {
		if (colSeqFasta == null || colSeqFasta.size() == 0) {
			return seqFilePath;
		}
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
