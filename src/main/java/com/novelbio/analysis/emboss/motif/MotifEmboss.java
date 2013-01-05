package com.novelbio.analysis.emboss.motif;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Protectable;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * ��������õ�fasta�ļ���������
 * ��emboss��Ȩ�ؾ���ɨ��motif
 * @author zong0jie
 *
 */
public class MotifEmboss {
	public static void main(String[] args) {
		String fileName = "TSS_36_UP.fasta";
		SeqHash seqHashMotif = new SeqHash("/home/zong0jie/����/20121224/motif.fasta");
		SeqHash seqHash = new SeqHash("/home/zong0jie/����/20121224/Tss/" + fileName);
		MotifEmboss motifEmboss = new MotifEmboss();
		motifEmboss.motifPath = "/home/zong0jie/Desktop/test/";
		motifEmboss.setAlignedMotifSeqHash(seqHashMotif);
		motifEmboss.setSeqHash(seqHash);
		motifEmboss.setMotifEmbossScanAlgorithm(MotifEmbossScanAlgorithm.Frequency);
		String[] result = motifEmboss.scanMotif();
		FileOperate.moveFile(result[0], "/home/zong0jie/����/20121224/motifResult", fileName + "motif.txt", true);
		FileOperate.moveFile(result[1], "/home/zong0jie/����/20121224/motifResult", fileName + "motif_reverse.txt", true);
	}
	
	SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.emboss);
	
	/** ����õ�motif */
	Collection<SeqFasta> colAlignmentMotif;
	/** Ҫɨ������� */
	Collection<SeqFasta> colSeqFasta;
	/** motif�������ڵ���ʱ�ļ��� */
	String motifPath;
	
	Prophecy prophecy;
	Profit profit;
	
	MotifEmbossScanAlgorithm motifEmbossScanAlgorithm = MotifEmbossScanAlgorithm.Gribskov;
	
	/** ��������õ�motif */
	public void setColAlignedMotifFasta(Collection<SeqFasta> colAlignmentMotif) {
		this.colAlignmentMotif = colAlignmentMotif;
	}
	
	/** ��������õ�motif */
	public void setAlignedMotifSeqHash(SeqHash seqHash) {
		colAlignmentMotif = new ArrayList<SeqFasta>();
		addSeq(colAlignmentMotif, seqHash);
	}
	
	/** ����Ҫɨ������� */
	public void setColSeqFasta(Collection<SeqFasta> colSeqFasta) {
		this.colSeqFasta = colSeqFasta;
	}
	
	/** ����Ҫɨ������� */
	public void setSeqHash(SeqHash seqHash) {
		colSeqFasta = new ArrayList<SeqFasta>();
		addSeq(colSeqFasta, seqHash);
	}
	
	public void setMotifEmbossScanAlgorithm(MotifEmbossScanAlgorithm motifEmbossScanAlgorithm) {
		this.motifEmbossScanAlgorithm = motifEmbossScanAlgorithm;
	}
	
	private void addSeq(Collection<SeqFasta> colSeq, SeqHash seqHash) {
		ArrayList<String> lsSeqName = seqHash.getLsSeqName();
		for (String string : lsSeqName) {
			SeqFasta seqFasta = seqHash.getSeq(string);
			colSeq.add(seqFasta);
		}
	}
	
	private String[] scanMotif() {
		setParam();
		String suffix = "_" +DateTime.getDateAndRandom();
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
	 * ��aligment�ļ�д���ı����������ļ���
	 * @param suffix ʱ�����������
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
	 * ����ɨ����ļ�д���ı����������ļ���
	 * @param suffix ʱ�����������
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
	
	/** ���ɴ�־��󣬲����ؽ�� */
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
			//�ж�ǰ100�����п��Ǻ��ỹ�ǵ���
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
	
	
	/** emboss��prophecy�����������Ȩ�ؾ�����㷨 */
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
