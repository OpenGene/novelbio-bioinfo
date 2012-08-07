package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.GffChrSeq;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public abstract class MiRNAtargetAbs {

	GffChrSeq gffChrSeq = new GffChrSeq();
	String exePath = "";
	/** Ԥ����������ĳ���ԭʼ�ļ� */
	String predictResultFile;
	/** �������ļ��������������Ľ���ļ� */
	String predictResultFinal;
	
	String inputUTR3seq = "";
	String inputMiRNAseq = "";
	
	/** ��������setInputUTR3File��ѡһ */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrSeq.setGffChrAbs(gffChrAbs);
	}
	/** �趨UTR3�����У�û�еĻ��ʹ�gffChrSeq����ȡ */
	public void setInputUTR3File(String inputUTR3seq) {
		this.inputUTR3seq = inputUTR3seq;
	}
	public void setInputMiRNAseq(String inputMiRNAseq) {
		this.inputMiRNAseq = inputMiRNAseq;
	}
	public void setOutFile(String rnahybridResultOut) {
		this.predictResultFinal = rnahybridResultOut;
		this.predictResultFile = FileOperate.changeFileSuffix(rnahybridResultOut, "_raw", null);
	}
	public String getPredictResultFile() {
		return predictResultFile;
	}
	public String getPredictResultFinal() {
		return predictResultFinal;
	}
	/**
	 * @return inputMiRNAseq + " ";
	 */
	protected String getInputMiRNAseq() {
		return inputMiRNAseq + " ";
	}
	/**
	 * �����к���3UTR�Ļ����3UTR����д���ı�
	 * @param mirandaResultOut + " "
	 */
	public String getInput3UTRseq() {
		try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
		
		if (FileOperate.isFileExistAndBigThanSize(inputUTR3seq, 1)) {
			return inputUTR3seq;
		}
		if (inputUTR3seq == null || inputUTR3seq.equals("")) {
			inputUTR3seq = FileOperate.changeFileSuffix(predictResultFinal, "_3UTR", "fasta");
		}
		gffChrSeq.setSaveToFile(true);
		gffChrSeq.setOutPutFile(inputUTR3seq);
		gffChrSeq.setGeneStructure(GeneStructure.UTR3);
		gffChrSeq.setGetAllIso(false);
		gffChrSeq.setGetSeqIsoGenomWide();
		//��ȡ����
		gffChrSeq.run();
		return inputUTR3seq;
	}
	
	public void setExePath(String exePath) {
		if (exePath != null && !exePath.trim().equals("")) {
			this.exePath = FileOperate.addSep(exePath);
		}
	}
	
	public abstract void mirnaPredict();
}
