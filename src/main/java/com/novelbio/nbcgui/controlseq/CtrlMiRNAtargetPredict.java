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
import com.novelbio.database.model.species.Species;

/**
 * ͬʱ��miranda��RNAhybrid���������ȡ����
 * @author zong0jie
 *
 */
public class CtrlMiRNAtargetPredict {
	public static void main(String[] args) {
		CtrlMiRNAtargetPredict ctrlMiRNAtargetPredict = new CtrlMiRNAtargetPredict();
		Species species = new Species();
		species.setTaxID(9606);
		
//		GffChrAbs gffChrAbs = new GffChrAbs(species.getGffFile()[0], species.getGffFile()[1], species.getChrRegxAndPath()[1], null, 0);
		
//		ctrlMiRNAtargetPredict.setGffChrAbs(gffChrAbs);
		ctrlMiRNAtargetPredict.setInputMiRNAseq("/home/zong0jie/Desktop/platformtest/predictTarget/mature_human_Final.fa");
		ctrlMiRNAtargetPredict.setInputUTR3File("/home/zong0jie/Desktop/platformtest/predictTarget/pig3UTR");
		ctrlMiRNAtargetPredict.setMirTargetOverlap("/home/zong0jie/Desktop/platformtest/predictTarget/outResult");
		ctrlMiRNAtargetPredict.setSpeciesType(RNAhybridClass.human);
		ctrlMiRNAtargetPredict.setTargetEnergy(15);
		ctrlMiRNAtargetPredict.setTargetPvalue(0.05);
		ctrlMiRNAtargetPredict.setTargetScore(150);
		ctrlMiRNAtargetPredict.predict();
	}
	MiRNAtargetMiranda miranda = new MiRNAtargetMiranda();
	MiRNAtargetRNAhybrid miRNAtargetRNAhybrid = new MiRNAtargetRNAhybrid();
	String txtMirTargetOverlap;
	/** ��������setInputUTR3File��ѡһ */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		miranda.setGffChrAbs(gffChrAbs);
	}
	/** �趨UTR3�����У�û�еĻ��ʹ�gffChrSeq����ȡ */
	public void setInputUTR3File(String inputUTR3seq) {
		if (!FileOperate.isFileExistAndBigThanSize(inputUTR3seq, 10)) {
			return;
		}
		miranda.setInputUTR3File(inputUTR3seq);
	}
	public void setInputMiRNAseq(String inputMiRNAseq) {
		miranda.setInputMiRNAseq(inputMiRNAseq);
		miRNAtargetRNAhybrid.setInputMiRNAseq(inputMiRNAseq);
	}
	/**
	 * ����������ļ�
	 * @param txtMirTargetOverlap
	 */
	public void setMirTargetOverlap(String txtMirTargetOverlap) {
		this.txtMirTargetOverlap = txtMirTargetOverlap;
		miranda.setOutFile(FileOperate.changeFilePrefix(txtMirTargetOverlap, "miranda_", null));
		miRNAtargetRNAhybrid.setOutFile(FileOperate.changeFilePrefix(txtMirTargetOverlap, "miRNAtarget_", null));
	}
	/** RNAhybrid���������� */
	public void setSpeciesType(RNAhybridClass rAhybridClass) {
		miRNAtargetRNAhybrid.setSpeciesType(rAhybridClass);
	}
	/** Ĭ��0.01 */
	public void setTargetPvalue(double targetPvalue) {
		miRNAtargetRNAhybrid.setTargetPvalue(targetPvalue);
	}
	/** Ĭ��150 */
	public void setTargetScore(int targetScore) {
		miranda.setTargetScore(targetScore);
	}
	/** Ĭ��-15�����������ȡ����ֵ�ټӸ��� */
	public void setTargetEnergy(int targetEnergy) {
		miranda.setTargetEnergy(targetEnergy);
		miRNAtargetRNAhybrid.setTargetEnergy(targetEnergy);
	}
	
	public void predict() {
		String UTR3file = miranda.getInput3UTRseq();
		miRNAtargetRNAhybrid.setInputUTR3File(UTR3file);
		
		SoftWareInfo softWareInfo = new SoftWareInfo();
		softWareInfo.setName(SoftWare.miranada);
		miranda.setExePath(softWareInfo.getExePath());
		softWareInfo.setName(SoftWare.RNAhybrid);
		miRNAtargetRNAhybrid.setExePath(softWareInfo.getExePath());
		miRNAtargetRNAhybrid.mirnaPredict();
		miranda.mirnaPredict();
		
		ArrayList<String[]> lsOverlapTarget = overLap(miranda.getPredictResultFinal(), miRNAtargetRNAhybrid.getPredictResultFinal());
		TxtReadandWrite txtOut = new TxtReadandWrite(txtMirTargetOverlap, true);
		txtOut.ExcelWrite(lsOverlapTarget);
	}
	
	private ArrayList<String[]> overLap(String txtInputFileMiranda, String txtInputFileMiRNAhybrid) {
		CombineTab combineTab = new CombineTab();
		combineTab.setStrNull(null);
		combineTab.setColExtractDetai(txtInputFileMiranda, "mirnada", 2,3,4);
		combineTab.setColExtractDetai(txtInputFileMiRNAhybrid, "rnaHybrid", 3,4);
		combineTab.setColCompareOverlapID(1, 2);
		ArrayList<String[]> lsCombine = combineTab.getResultLsIntersection();
		return lsCombine;
	}
}
