package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.mirna.MiRNAtargetMiranda;
import com.novelbio.analysis.seq.mirna.MiRNAtargetRNAhybrid;
import com.novelbio.analysis.seq.mirna.MiRNAtargetRNAhybrid.RNAhybridClass;
import com.novelbio.analysis.tools.compare.CombineTab;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * 同时用miranda和RNAhybrid分析，结果取并集
 * @author zong0jie
 *
 */
public class CtrlMiRNAtargetPredict {
	MiRNAtargetMiranda miranda = new MiRNAtargetMiranda();
	MiRNAtargetRNAhybrid miRNAtargetRNAhybrid = new MiRNAtargetRNAhybrid();
	String txtMirTargetOverlap;
	/** 本方法和setInputUTR3File二选一 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		miranda.setGffChrAbs(gffChrAbs);
		String UTR3file = miranda.getInput3UTRseq();
		miRNAtargetRNAhybrid.setInputUTR3File(UTR3file);
	}
	/** 设定UTR3的序列，没有的话就从gffChrSeq中提取 */
	public void setInputUTR3File(String inputUTR3seq) {
		if (!FileOperate.isFileExistAndBigThanSize(inputUTR3seq, 10)) {
			return;
		}
		miranda.setInputUTR3File(inputUTR3seq);
		miRNAtargetRNAhybrid.setInputUTR3File(inputUTR3seq);
	}
	public void setInputMiRNAseq(String inputMiRNAseq) {
		miranda.setInputMiRNAseq(inputMiRNAseq);
		miRNAtargetRNAhybrid.setInputMiRNAseq(inputMiRNAseq);
	}
	/**
	 * 最后结果交集文件
	 * @param txtMirTargetOverlap
	 */
	public void setMirTargetOverlap(String txtMirTargetOverlap) {
		this.txtMirTargetOverlap = txtMirTargetOverlap;
		miranda.setOutFile(FileOperate.changeFilePrefix(txtMirTargetOverlap, "_miranda", null));
		miRNAtargetRNAhybrid.setOutFile(FileOperate.changeFilePrefix(txtMirTargetOverlap, "_miRNAtarget", null));
	}
	/** RNAhybrid的物种类型 */
	public void setSpeciesType(RNAhybridClass rAhybridClass) {
		miRNAtargetRNAhybrid.setSpeciesType(rAhybridClass);
	}
	/** 默认0.01 */
	public void setTargetPvalue(double targetPvalue) {
		miRNAtargetRNAhybrid.setTargetPvalue(targetPvalue);
	}
	/** 默认150 */
	public void setTargetScore(int targetScore) {
		miranda.setTargetScore(targetScore);
	}
	/** 默认-15，输入的数会取绝对值再加负号 */
	public void setTargetEnergy(int targetEnergy) {
		miranda.setTargetEnergy(targetEnergy);
		miRNAtargetRNAhybrid.setTargetEnergy(targetEnergy);
	}
	
	public void predict() {
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.miranada);
		miranda.setExePath(softWareInfo.getExePath());
		softWareInfo.setName(SoftWare.RNAhybrid);
		miRNAtargetRNAhybrid.setExePath(softWareInfo.getExePath());
		miranda.mirnaPredict();
		miRNAtargetRNAhybrid.mirnaPredict();
		ArrayList<String[]> lsOverlapTarget = overLap(miranda.getPredictResultFile(), miRNAtargetRNAhybrid.getPredictResultFinal());
		TxtReadandWrite txtOut = new TxtReadandWrite(txtMirTargetOverlap, true);
		txtOut.ExcelWrite(lsOverlapTarget, "\t", 1, 1);
	}
	
	private ArrayList<String[]> overLap(String txtInputFileMiranda, String txtInputFileMiRNAhybrid) {
		CombineTab combineTab = new CombineTab();
		combineTab.setStrNull(null);
		combineTab.setColExtractDetai(txtInputFileMiranda, "mirnada", 2,3,4);
		combineTab.setColExtractDetai(txtInputFileMiRNAhybrid, "mirnada", 2,3,4);
		combineTab.setColCompareOverlapID(1);
		ArrayList<String[]> lsCombine = combineTab.getResultLsIntersection();
		return lsCombine;
	}
}
