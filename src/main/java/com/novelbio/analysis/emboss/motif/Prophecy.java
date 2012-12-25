package com.novelbio.analysis.emboss.motif;

import com.novelbio.analysis.emboss.motif.MotifEmboss.MotifEmbossScanAlgorithm;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * emboss��alignment����Ȩ�ؾ���Ȼ���Profit��ɨ��motif��
 * @author zong0jie
 * <b>Ĭ�Ͻ�����ת��ΪСдȻ��ɨ��<b>
 */
public class Prophecy {
//	prophecy -sequence "$filename" -datafile Epprofile  -type G -name novelbio -threshold 75 -outfile "$outFileName"
	
	String ExePath = "";
	String inAlignment;
	String profitName = "novelbio";
	MotifEmbossScanAlgorithm motifEmbossScanAlgorithm = MotifEmbossScanAlgorithm.Gribskov;
	
	boolean isNr;
	/**
	 * @param isNR true �������У���������Ĭ��ͬʱɨ��������
	 * false ��������
	 */
	public Prophecy(boolean isNR) {
		this.isNr = isNR;
	}
	
	/**
	 * �趨samtools���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.ExePath = "";
		} else {
			this.ExePath = FileOperate.addSep(exePath);
		}
	}
	
	/** ����������ļ� */
	public void setInAlignment(String inAlignment) {
		this.inAlignment = inAlignment;
	}
	
	public void setMatrixAlgorithm(MotifEmbossScanAlgorithm motifEmbossScanAlgorithm) {
		this.motifEmbossScanAlgorithm = motifEmbossScanAlgorithm;
	}
	
	private String getMatrixAlgorithm() {
		String result = " -type ";
		if (motifEmbossScanAlgorithm == MotifEmbossScanAlgorithm.Frequency) {
			result = result + "F ";
		} else if (motifEmbossScanAlgorithm == MotifEmbossScanAlgorithm.Gribskov) {
			result = result + "G -datafile Epprofile -open 3.0 -extension 0.3 ";
		} else if (motifEmbossScanAlgorithm == MotifEmbossScanAlgorithm.Henikoff) {
			result = result + "H -datafile EBLOSUM62 -open 3.0 -extension 0.3 ";
		}
		return result;
	}
	
	private String getThreshold() {
		return " -threshold 75 ";
	}
	
	private String getInAlignment() {
		return " -sequence " + CmdOperate.addQuot(inAlignment);
	}
	
	private String getName() {
		return " -name " + profitName + " ";
	}
	
	/** �Ǻ��ỹ�ǵ��� */
	private String getSeqType() {
		if (isNr) {
			return " -snucleotide1 ";
		} else {
			return " -sprotein1 ";
		}
	}
	/** ���ز����ľ��� */
	public String[] generateProfit(String outFile) {
		String[] result = new String[1];
		if (isNr) {
			result = new String[2];
		}
		result[0] = outFile;
		String cmd = ExePath + "prophecy -slower1 " + getInAlignment() + getMatrixAlgorithm()
				+ getSeqType() + getName() + getThreshold() + " -outfile " + CmdOperate.addQuot(outFile);
		CmdOperate cmdOperate = new CmdOperate(cmd,"emboss_profit");
		cmdOperate.run();
		if (isNr) {
			result[1] = FileOperate.changeFileSuffix(outFile, "_reverse", null);
			cmd = ExePath + "prophecy -slower1 -sreverse1 " + getInAlignment() + getMatrixAlgorithm() + getSeqType()
					+ getName() + getThreshold() + " -outfile " + CmdOperate.addQuot(result[1]);
			cmdOperate = new CmdOperate(cmd,"emboss_profit_reverce");
			cmdOperate.run();
		}
	
		
		return result;
	}


}
