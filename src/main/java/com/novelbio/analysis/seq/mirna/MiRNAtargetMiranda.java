package com.novelbio.analysis.seq.mirna;

import org.apache.log4j.Logger;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 预测miRNA的靶基因miranda
 * @author zong0jie
 *
 */
public class MiRNAtargetMiranda extends MiRNAtargetAbs{
	Logger logger = Logger.getLogger(MiRNAtargetMiranda.class);
	
	int targetScore = 150;
	int targetEnergy = -15;
	
	/** 默认150 */
	public void setTargetScore(int targetScore) {
		this.targetScore = targetScore;
	}
	private String getTargetScore() {
		return "-sc " + targetScore + " ";
	}
	/** 默认-15，输入的数会取绝对值再加负号 */
	public void setTargetEnergy(int targetEnergy) {
		this.targetEnergy = -Math.abs(targetEnergy);
	}
	private String getTargetEnergy() {
		return "-en " + targetEnergy + " ";
	}
	
	public void mirnaPredict() {
		try {
			mirnaPredictExp();
		} catch (InterruptedException e) {
			logger.error("出错");
			e.printStackTrace();
		}
	}
	private void mirnaPredictExp() throws InterruptedException {
		String cmd = exePath + "miranda ";
		cmd = cmd + getInputMiRNAseq() + getInput3UTRseq() + " " + getTargetScore() + getTargetEnergy() + "-out " + getPredictResultFile();
		CmdOperate cmdOperate = new CmdOperate(cmd, "miranda_miRNA_predict");
		cmdOperate.run();
		Thread.sleep(2000);
		modifyMirandaResult_To_Mir2Target();
	}
	/**
	 * 给定miRanda的输出文件，将其整理成需要的格式
	 * @param miRandaOut
	 * "mirName","targetGene", "score","Energy"
	 */
	private void modifyMirandaResult_To_Mir2Target() {
		TxtReadandWrite txtRead = new TxtReadandWrite(predictResultFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(predictResultFinal, true);
		txtOut.writefileln(new String[]{"mirName","targetGene", "score","Energy"});
		boolean start = true;//标记扫描的起点
		String[] pair = null;
		for (String string : txtRead.readlines()) {
			if (start) {
				pair = new String[4];//0: mir 1:Target 2: energy
			}
			if (string.contains("Performing Scan")) {
				string = string.replace("Performing Scan:", "");
				String[] tmp = string.split("vs");
				pair[0] = tmp[0].trim(); pair[1] = tmp[1].trim();
				start = false;
			}
			//标记一个写完
			if (string.contains("Complete")) {
				if (pair[0] != null) {
					txtOut.writefileln(pair);
				}
				start = true;
			}
			//没找到就查找下一条
			if (string.contains("No Hits Found above Threshold")) {
				start = true;
				continue;
			}
			if (string.startsWith(">") && !string.startsWith(">>")) {
				String[] tmpScore = string.split("\t");
				if (pair[2] == null) {
					pair[2] = tmpScore[2]; pair[3] = tmpScore[3];
				}
				else {
					double score = Double.parseDouble(tmpScore[2]);
					if (score > Double.parseDouble(pair[2])) {
						pair[2] = tmpScore[2]; pair[3] = tmpScore[3];
					}
				}
			}
		}
		txtRead.close();
		txtOut.close();
	}
}
