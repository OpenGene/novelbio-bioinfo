package com.novelbio.bioinfo.mirna;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.IntCmdSoft;
import com.novelbio.bioinfo.gff.GffGene.GeneStructure;
import com.novelbio.bioinfo.gffchr.GffChrAbs;
import com.novelbio.bioinfo.gffchr.GffChrSeq;
import com.novelbio.bioinfo.mirna.rnahybrid.HybridUnit;
import com.novelbio.bioinfo.mirna.rnahybrid.RNAhybrid;
import com.novelbio.bioinfo.mirna.rnahybrid.RNAmiranda;
import com.novelbio.bioinfo.mirna.rnahybrid.RNAhybrid.HybridRNAUnit;
import com.novelbio.bioinfo.mirna.rnahybrid.RNAhybrid.RNAhybridClass;
import com.novelbio.bioinfo.mirna.rnahybrid.RNAmiranda.MirandaPair;
import com.novelbio.bioinfo.tools.compare.CombineTab;

/** 动物和植物的mir靶基因预测 */
public class MirTargetMammal implements IntCmdSoft {
	GffChrSeq gffChrSeq = new GffChrSeq();
	/** 将初步文件经过整理后产生的结果文件 */
	String predictResultFinal;
	
	String inputUTR3seq = "";
	String inputMiRNAseq = "";
	RNAhybridClass rnaHybridClass;
	double pvalue;
	int score;
	int energy;
	
	List<String> lsCmd = new ArrayList<>();
	
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
		lsCmd.clear();
		String mirandaOut = predictMiranda();
		String rnaHybridOut = predictRNAhybrid();
		List<String[]> lsOverlap = overLap(mirandaOut, rnaHybridOut);
		TxtReadandWrite txtWrite = new TxtReadandWrite(predictResultFinal, true);
		txtWrite.ExcelWrite(lsOverlap);
		txtWrite.close();
	}
	

	private String predictMiranda() {
		String mirandaFile = FileOperate.changeFileSuffix(predictResultFinal, ".miranda", "gz");
		RNAmiranda rnAmiranda = new RNAmiranda();
		rnAmiranda.setMiRNAseq(inputMiRNAseq);
		rnAmiranda.setUtr3File(getInput3UTRseq());
		if (score > 0) {
			rnAmiranda.setTargetScore(score);
		}
		if (energy > 0) {
			rnAmiranda.setTargetEnergy(energy);
		}

		rnAmiranda.setPredictResultFile(mirandaFile);
		String mirandaOut = FileOperate.changeFileSuffix(predictResultFinal, ".miranda_modify", null);
		if (!FileOperate.isFileExistAndBigThanSize(mirandaFile, 0)) {
			lsCmd.addAll(rnAmiranda.getCmdExeStr());
			rnAmiranda.mirnaPredict();
		}
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(mirandaOut, true);
		boolean title = false;
		for (MirandaPair mirandaPair : rnAmiranda.readPerlines()) {
			HybridUnit hybridUnit = mirandaPair.getMirandaUnitMaxEnergySeed();
			if (hybridUnit == null) {
				continue;
			}
			if (!title) {
				txtWrite.writefileln(hybridUnit.getTitle());
				title = true;
			}

			txtWrite.writefileln(hybridUnit.toResultTab());
		}
		txtWrite.close();
		return mirandaOut;
	}
	
	private String predictRNAhybrid() {
		String rnaHybridFile = FileOperate.changeFileSuffix(predictResultFinal, ".rnahybrid", "gz");

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
		rnAhybrid.setPredictResultFile(rnaHybridFile);
		String rnaHybridOut = FileOperate.changeFileSuffix(predictResultFinal, ".rnahybrid_modify", null);
		if (!FileOperate.isFileExistAndBigThanSize(rnaHybridFile, 0)) {
			lsCmd.addAll(rnAhybrid.getCmdExeStr());
			rnAhybrid.mirnaPredictRun();
		}
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(rnaHybridOut, true);
		boolean title = false;
		for (HybridRNAUnit hybridUnit : rnAhybrid.readPerlines()) {
			if (!title) {
				txtWrite.writefileln(hybridUnit.getTitle());
				title = true;
			}
			if (hybridUnit.getPvalue() > pvalue || !hybridUnit.isSeedPerfectMatch()) {
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
		combineTab.setColExtractDetail(txtInputFileMiranda, "mirnada", 3,4,5,6);
		combineTab.setColExtractDetail(txtInputFileMiRNAhybrid, "rnaHybrid", 3,4,5,6);
		combineTab.setColCompareOverlapID(1, 2);
		ArrayList<String[]> lsCombine = combineTab.getResultLsIntersection();
		try {
			combineTab.renderScriptAndDrawImage(FileOperate.changeFileSuffix(predictResultFinal, ".Ven", "png"), "", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lsCombine;
	}
	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}

}
