package com.novelbio.analysis.emboss.motif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.SeqHashInt;
import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 输入连配好的fasta文件，或序列
 * 用emboss的权重矩阵扫描motif
 * @author zong0jie
 *
 */
public class MotifEmboss implements IntCmdSoft {
	private static final String outSuffix = "_MotifScaningResult";
	/** 连配好的motif */
	Collection<SeqFasta> colAlignmentMotif;
	
	/** 要扫描的序列，与seqFilePath二选一 */
	Collection<SeqFasta> colSeqFasta;
	/** 要扫描的序列，与colSeqFasta二选一 */
	String seqFilePath;
	
	/** motif分析所在的临时文件夹 */
	String motifFilePath;
	/** 输出文件路径 */
	String outFile;
	
	Prophecy prophecy;
	Profit profit;
	
	MotifEmbossScanAlgorithm motifEmbossScanAlgorithm = MotifEmbossScanAlgorithm.Gribskov;
	/** 需要扫描的文件 */
	String seqfastaNeedScan;
	
	List<String> lsCmd = new ArrayList<>();
	
	/** 产生的权重矩阵的文件路径 */
	private String[] weightMatrixFile;
	private  Boolean isNR = null;
	
	/** 输入连配好的motif */
	public void setColAlignedMotifFasta(String motifFilePath) {
		if (!StringOperate.isRealNull(motifFilePath)) {
			this.motifFilePath = motifFilePath;
		}
	}
	/** 输入连配好的motif */
	public void setAlignedMotifSeqHash(SeqHashInt seqHash) {
		colAlignmentMotif = new ArrayList<SeqFasta>();
		ArrayList<String> lsSeqName = seqHash.getLsSeqName();
		for (String string : lsSeqName) {
			SeqFasta seqFasta = seqHash.getSeq(string);
			colAlignmentMotif.add(seqFasta);
		}
		this.motifFilePath = FileOperate.addSep(PathDetail.getTmpPath()) + DateUtil.getDateAndRandom();
	}
	
	/** 输出文件路径 */
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	/** true是，false否
	 * null表示根据序列自动判定
	 * @param isNR
	 */
	public void setIsNR(Boolean isNR) {
		this.isNR = isNR;
	}
	
	/** 输入要扫描的序列 */
	public void setColSeqFasta(Collection<SeqFasta> colSeqFasta) {
		this.colSeqFasta = colSeqFasta;
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

	public void generateMatrix() {
		setParam();
		String motifFile = motifFilePath;
		if (StringOperate.isRealNull(motifFile)) {
			motifFile = writeAlignedMotif();
		}
		weightMatrixFile = generateWeightMatrix(motifFile);
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
	 * <b>之前务必要运行{@link #generateMatrix()}</b><br>
	 * @return 返回 string[2]<br>
	 * 0: 正链扫描结果<br>
	 * 1: 负链扫描结果
	 */
	public void scanMotif() {
		seqfastaNeedScan = writeSeqfastaNeedScan();
		String resultF = FileOperate.changeFileSuffix(outFile, outSuffix, "txt");
		scanAndGetResult(weightMatrixFile[0], seqfastaNeedScan, resultF);
		if (weightMatrixFile.length > 1) {
			String resultR = FileOperate.changeFileSuffix(outFile, outSuffix + "_reverse", "txt");
			scanAndGetResult(weightMatrixFile[1], seqfastaNeedScan, resultR);
		}
	}
	/**
	 * 将aligment文件写入文本，并返回文件名
	 * @param suffix 时间日期随机数
	 * @return
	 */
	private String writeAlignedMotif() {
		String resultFile =  FileOperate.changeFileSuffix(motifFilePath, "_AlignedMofit", "fa");
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
	private String writeSeqfastaNeedScan() {
		if (colSeqFasta == null || colSeqFasta.size() == 0) {
			return seqFilePath;
		}
		String resultFile = FileOperate.changeFileSuffix(motifFilePath, "_seq", "fa");
		TxtReadandWrite txtWrite = new TxtReadandWrite(resultFile, true);
		for (SeqFasta seqFasta : colSeqFasta) {
			txtWrite.writefileln(seqFasta.toStringNRfasta());
		}
		txtWrite.close();
		return resultFile;
	}

	/** 生成打分矩阵，并返回结果 */
	private String[] generateWeightMatrix(String alignedMotif) {
		prophecy.setInAlignment(alignedMotif);
		prophecy.setMatrixAlgorithm(motifEmbossScanAlgorithm);
		String resultFile = FileOperate.changeFileSuffix(alignedMotif, "_weightedMatrix", "fa");
		prophecy.setOutFile(resultFile);
		lsCmd.addAll(prophecy.getCmdExeStr());
		return prophecy.generateProfit();
	}
	
	private String scanAndGetResult(String weightMatrix, String seqFile, String resultFile) {
		profit.setInProfit(weightMatrix);
		profit.setSeqFile(seqFile);
		profit.setOutFile(resultFile);
		lsCmd.addAll(profit.getCmdExeStr());
		profit.scaning();
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

	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}
}
