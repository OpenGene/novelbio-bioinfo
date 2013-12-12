package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.rnahybrid.HybridUnit;
import com.novelbio.analysis.seq.rnahybrid.RNAhybrid;
import com.novelbio.analysis.seq.rnahybrid.RNAhybrid.RNAhybridClass;
import com.novelbio.analysis.seq.rnahybrid.RNAmiranda;
import com.novelbio.analysis.seq.rnahybrid.RNAmiranda.MirandaPair;
import com.novelbio.analysis.tools.compare.CombineTab;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 动物和植物的mir靶基因预测 */
public class MirTargetMammal {

	GffChrSeq gffChrSeq = new GffChrSeq();
	/** 将初步文件经过整理后产生的结果文件 */
	String predictResultFinal;
	
	String inputUTR3seq = "";
	String inputMiRNAseq = "";
	RNAhybridClass rnaHybridClass;
	double pvalue;
	int score;
	int energy;
	
	public void setSpeciesType(RNAhybridClass rnaHybridClass) {
		this.rnaHybridClass = rnaHybridClass;
	}
	/** 本方法和setInputUTR3File二选一 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrSeq.setGffChrAbs(gffChrAbs);
	}
	/** 设定UTR3的序列，没有的话就从gffChrSeq中提取 */
	public void setInputUTR3File(String inputUTR3seq) {
		this.inputUTR3seq = inputUTR3seq;
	}
	public void setInputMiRNAseq(String inputMiRNAseq) {
		this.inputMiRNAseq = inputMiRNAseq;
	}
	public void setOutFile(String rnahybridResultOut) {
		this.predictResultFinal = rnahybridResultOut;
	}
	public String getPredictResultFinal() {
		return predictResultFinal;
	}
	public void setTargetPvalue(double targetPvalue) {
		this.pvalue = targetPvalue;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public void setEnergy(int energy) {
		this.energy = energy;
	}
	/**
	 * @return inputMiRNAseq + " ";
	 */
	protected String getInputMiRNAseq() {
		return inputMiRNAseq + " ";
	}
	/**
	 * 将所有含有3UTR的基因的3UTR序列写入文本
	 * @param mirandaResultOut + " "
	 */
	private String getInput3UTRseq() {
		try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
		
		if (FileOperate.isFileExistAndBigThanSize(inputUTR3seq, 1)) {
			return inputUTR3seq;
		}
		if (inputUTR3seq == null || inputUTR3seq.equals("")) {
			inputUTR3seq = FileOperate.getPathName(predictResultFinal) + "Utr3.fa";
		}
		gffChrSeq.setIsSaveToFile(true);
		gffChrSeq.setOutPutFile(inputUTR3seq);
		gffChrSeq.setGeneStructure(GeneStructure.UTR3);
		gffChrSeq.setGetAllIso(false);
		gffChrSeq.setGetSeqGenomWide();
		//提取序列
		gffChrSeq.run();
		return inputUTR3seq;
	}
	
	public void predict() {
		String mirandaOut = predictMiranda();
		String rnaHybridOut = predictRNAhybrid();
		List<String[]> lsOverlap = overLap(mirandaOut, rnaHybridOut);
		TxtReadandWrite txtWrite = new TxtReadandWrite(predictResultFinal, true);
		txtWrite.ExcelWrite(lsOverlap);
		txtWrite.close();
	}
	

	private String predictMiranda() {
		RNAmiranda rnAmiranda = new RNAmiranda();
		rnAmiranda.setMiRNAseq(inputMiRNAseq);
		rnAmiranda.setUtr3File(getInput3UTRseq());
		if (score > 0) {
			rnAmiranda.setTargetScore(score);
		}
		if (energy > 0) {
			rnAmiranda.setTargetEnergy(energy);
		}
		
	
		
		rnAmiranda.setPredictResultFile(FileOperate.changeFileSuffix(predictResultFinal, "_miranda", null));
		String mirandaOut = FileOperate.changeFileSuffix(predictResultFinal, "_miranda_modify", null);
		TxtReadandWrite txtWrite = new TxtReadandWrite(mirandaOut, true);
		boolean title = false;
		for (MirandaPair mirandaPair : rnAmiranda.readPerlines()) {
			HybridUnit hybridUnit = mirandaPair.getMirandaUnitMaxEnergySeed();
			if (!title) {
				txtWrite.writefileln(hybridUnit.getTitle());
				title = true;
			}
			if (hybridUnit == null) {
				continue;
			}
			txtWrite.writefileln(hybridUnit.toResultTab());
		}
		txtWrite.close();
		return mirandaOut;
	}
	
	private String predictRNAhybrid() {
		RNAhybrid rnAhybrid = new RNAhybrid();
		rnAhybrid.setMiRNAseq(inputMiRNAseq);
		rnAhybrid.setSpeciesType(rnaHybridClass);
		rnAhybrid.setUtr3File(getInput3UTRseq());
		if (energy > 0) {
			rnAhybrid.setTargetEnergy(energy);
		}
		if (pvalue > 0) {
			rnAhybrid.setTargetPvalue(pvalue);
		}
		rnAhybrid.setPredictResultFile(FileOperate.changeFileSuffix(predictResultFinal, "_rnahybrid", null));
		String rnaHybridOut = FileOperate.changeFileSuffix(predictResultFinal, "_rnahybrid_modify", null);
		TxtReadandWrite txtWrite = new TxtReadandWrite(rnaHybridOut, true);
		boolean title = false;
		for (HybridUnit hybridUnit : rnAhybrid.readPerlines()) {
			if (!title) {
				txtWrite.writefileln(hybridUnit.getTitle());
				title = true;
			}
			if (!hybridUnit.isSeedPerfectMatch()) {
				continue;
			}
			txtWrite.writefileln(hybridUnit.toResultTab());
		}
		txtWrite.close();
		return rnaHybridOut;
	}
	
	
	private List<String[]> overLap(String txtInputFileMiranda, String txtInputFileMiRNAhybrid) {
		CombineTab combineTab = new CombineTab();
		combineTab.setStrNull(null);
		combineTab.setColExtractDetail(txtInputFileMiranda, "mirnada", 2,3,4,5,6);
		combineTab.setColExtractDetail(txtInputFileMiRNAhybrid, "rnaHybrid", 3,4,5,6);
		combineTab.setColCompareOverlapID(1, 2);
		ArrayList<String[]> lsCombine = combineTab.getResultLsIntersection();
		try {
			combineTab.renderScriptAndDrawImage(FileOperate.changeFileSuffix(predictResultFinal, "_Ven", "tiff"), "", "");
		} catch (Exception e) {}
		return lsCombine;
	}


}
