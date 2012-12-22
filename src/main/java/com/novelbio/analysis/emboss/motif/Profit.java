package com.novelbio.analysis.emboss.motif;

import com.novelbio.analysis.seq.sam.SamToBed;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * profit scans one or more sequences with a simple frequency matrix 
 * and writes an output file with any high-scoring matches. All possible 
 * ungapped alignments of each sequence to the matrix are scored and 
 * any matches with a score higher than the specified threshold are 
 * written to the output file. The output file includes the name of any 
 * matching sequence found, the start position in the sequence of the 
 * match and the percentage of the maximum possible score.<br><br>
 * <b>Ĭ�Ͻ�����ת��ΪСдȻ��ɨ��<b>
 * @author zong0jie
 *
 */
public class Profit {

//	profit  -infile $infile -sequence $sequence -outfile $outfile -sreverse2  -snucleotide2  -sprotein2 -slower2	
	
	String ExePath = "";
	String inProfit;
	String seqFile;
	
	/** true:Nr false:AA */
	boolean isNr;
	
	/**
	 * @param isNR true �������У���������Ĭ��ͬʱɨ��������
	 * false ��������
	 */
	public Profit(boolean isNR) {
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
	
	/** ����Ĵ�־��� */
	public void setInProfit(String inProfit) {
		this.inProfit = inProfit;
	}
	
	public void setSeqFile(String seqFile) {
		this.seqFile = seqFile;
	}
	
	/** ��ô�־����·�� */
	private String getInProfit() {
		return " -infile " + CmdOperate.addQuot(inProfit) + " ";
	}
	
	/** ���Ҫɨ���������Ϣ */
	private String getSeqFile() {
		return " -sequence " + CmdOperate.addQuot(seqFile) + " ";
	}
	
	/** �Ǻ��ỹ�ǵ��� */
	private String getSeqType() {
		if (isNr) {
			return " -sreverse2  -snucleotide2 ";
		} else {
			return " -sprotein2 ";
		}
	}
	
	public void scaning(String outFile) {
		String cmd = ExePath + "profit -slower2 " + getInProfit() + getSeqFile() + getSeqType();
		CmdOperate cmdOperate = new CmdOperate(cmd,"emboss_profile");
		cmdOperate.run();
	}

}
