package com.novelbio.analysis.seq.mirna;

import java.util.HashMap;
import java.util.Map;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class MiRNAtargetRNAhybrid extends MiRNAtargetAbs {
	String SpeciesType = "3utr_human";
	
	double targetPvalue = 0.01;
	int targetEnergy = -15;
	
	/** 默认0.01 */
	public void setTargetPvalue(double targetPvalue) {
		this.targetPvalue = targetPvalue;
	}
	/** 默认-15，输入的数会取绝对值再加负号 */
	public void setTargetEnergy(int targetEnergy) {
		this.targetEnergy = -Math.abs(targetEnergy);
	}
	public void setSpeciesType(RNAhybridClass rnaAhybridClass) {
		SpeciesType = rnaAhybridClass.getDetailClassName();
	}
	/**
	 * taxID对应RNAhybrid的对照表，一般来说低等生物对应线虫，哺乳动物对应人类，昆虫对应果蝇
	 * @param txtTaxID_to_RNAhybrid_s_class
	 */
	private String getRNAhybridClass() {
		return "-s " + SpeciesType + " ";
	}
	
	@Override
	public void mirnaPredict() {
		// TODO Auto-generated method stub
		mirnaPredictRun();
		modifyRNAhybridResultToMir2Target();
	}
	
	private void mirnaPredictRun() {
		String cmd = exePath + "RNAhybrid ";
		cmd = cmd + getRNAhybridClass() + "-t " + getInput3UTRseq() + " -q " + getInputMiRNAseq() + "> " + predictResultFile;
		CmdOperate cmdOperate = new CmdOperate(cmd, "miranda_miRNA_predict");
		cmdOperate.run();
	}

	private void modifyRNAhybridResultToMir2Target() {
		TxtReadandWrite txtRead = new TxtReadandWrite(predictResultFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(predictResultFinal, true);
		boolean blockFlag = false;
		String block = null;
		txtOut.writefileln("mirName\ttargetGene\tPvalue\tEnergy");
		for (String string : txtRead.readlines()) {
			if (string.startsWith("target:") && block != null) {
				blockFlag = true;
			}
			if (blockFlag && block != null) {
				String[] tmpResult = getBlockRNAhybrid(block);
				writeStr(tmpResult, txtOut);
				blockFlag = false;
				block = "";
			}
			block = block + TxtReadandWrite.ENTER_LINUX + string;
		}
		txtRead.close();
		txtOut.close();
	
	}
	/**
	 * 给定RNAhybrid的一段，返回所需的信息
	 * @param block
	 * @return string[] 0: miRNA 1:geneID 2:pvalue 3: mef
	 */
	private String[] getBlockRNAhybrid(String block) {
		String[] result = new String[4];
		String[] ss = block.split(TxtReadandWrite.ENTER_LINUX);
		for (String string : ss) {
			if (string.startsWith("miRNA :")) {
				result[0] = string.replace("miRNA :", "").trim();
			}
			else if (string.startsWith("target:")) {
				result[1] = string.replace("target:", "").trim();
			}
			else if (string.startsWith("p-value:")) {
				result[2] = string.replace("p-value:", "").trim();
			}
			else if (string.startsWith("mfe:")) {
				result[3] = string.replace("mfe:", "").replace("kcal/mol", "").trim();
			}
		}
		if (Double.parseDouble(result[2]) > targetPvalue || Double.parseDouble(result[3]) > -Math.abs(targetEnergy)) {
			return null;
		}
		return result;
	}
	
	private void writeStr(String[] tmpResult, TxtReadandWrite txtWrite){
		if (tmpResult == null) {
			return;
		}
		txtWrite.writefileln(tmpResult);
	}

}

