package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class RNAmiranda {
	private static final Logger logger = Logger.getLogger(RNAmiranda.class);
	
	public static void main(String[] args) {
		RNAmiranda rnAmiranda = new RNAmiranda();
		rnAmiranda.setInputMiRNAseq("/media/winD/plant_miRNA_predict/AraDemo.fa");
		rnAmiranda.setInputUTR3seq("/media/winD/plant_miRNA_predict/seq.fa");
		rnAmiranda.setPredictResultFile("/media/winD/plant_miRNA_predict/result.fa");
		rnAmiranda.mirnaPredict();
	}
	
	String exePath = "";
	int targetScore = 150;
	int targetEnergy = -15;
	
	String inputUTR3seq;
	String inputMiRNAseq;
	String predictResultFile;
	
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals(""))
			this.exePath = "";
		else
			this.exePath = FileOperate.addSep(exePath);
	}
	public void setInputMiRNAseq(String inputMiRNAseq) {
		this.inputMiRNAseq = inputMiRNAseq;
	}
	public void setInputUTR3seq(String inputUTR3seq) {
		this.inputUTR3seq = inputUTR3seq;
	}
	public void setPredictResultFile(String predictResultFile) {
		this.predictResultFile = predictResultFile;
	}
	/** 默认150 */
	public void setTargetScore(int targetScore) {
		this.targetScore = targetScore;
	}

	/** 默认-15，输入的数会取绝对值再加负号 */
	public void setTargetEnergy(int targetEnergy) {
		this.targetEnergy = -Math.abs(targetEnergy);
	}

	public String mirnaPredict() {
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("miranda error:" + cmdOperate.getCmdExeStrReal());
		}
		return predictResultFile;
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "miranda");
		lsCmd.add(inputMiRNAseq);
		lsCmd.add(inputUTR3seq);
		ArrayOperate.addArrayToList(lsCmd, getTargetScore());
		ArrayOperate.addArrayToList(lsCmd, getTargetEnergy());
		ArrayOperate.addArrayToList(lsCmd, getPredictResult());
		return lsCmd;
	}
	
	private String[] getTargetScore() {
		return new String[]{"-sc", targetScore + ""};
	}
	private String[] getTargetEnergy() {
		return new String[]{"-en", targetEnergy + ""};
	}
	private String[] getPredictResult() {
		return new String[]{"-out", predictResultFile};
	}
	
	
	
	/**
	 * Read Sequence:AT1G01020.2 (1085 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01020.2
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Score for this Scan:
No Hits Found above Threshold
Complete

Read Sequence:AT1G01030.1 (1905 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01030.1
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Score for this Scan:
No Hits Found above Threshold
Complete

Read Sequence:AT1G01040.1 (6251 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01040.1
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

   Forward:	Score: 161.000000  Q:2 to 23  R:5393 to 5422 Align Len (24) (66.67%) (75.00%)

   Query:    3' tttacCTCCTAGTT---CAACCCAAACCCa 5'
                     |:|| |:||    || |||||||| 
   Ref:      5' aaaccGGGGTTTAACTCTTTTGGTTTGGGa 3'

   Energy:  -18.629999 kCal/Mol

Scores for this hit:
>ath-miR156c	AT1G01040.1	161.00	-18.63	2 23	5393 5422	24	66.67%	75.00%

Score for this Scan:
Seq1,Seq2,Tot Score,Tot Energy,Max Score,Max Energy,Strand,Len1,Len2,Positions
>>ath-miR156c	AT1G01040.1	161.00	-18.63	161.00	-18.63	39	27	6251	 5393
Complete

Read Sequence:AT1G01040.2 (5877 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01040.2
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

   Forward:	Score: 161.000000  Q:2 to 23  R:5126 to 5155 Align Len (24) (66.67%) (75.00%)

   Query:    3' tttacCTCCTAGTT---CAACCCAAACCCa 5'
                     |:|| |:||    || |||||||| 
   Ref:      5' aaaccGGGGTTTAACTCTTTTGGTTTGGGa 3'

   Energy:  -18.629999 kCal/Mol

Scores for this hit:
>ath-miR156c	AT1G01040.2	161.00	-18.63	2 23	5126 5155	24	66.67%	75.00%

Score for this Scan:
Seq1,Seq2,Tot Score,Tot Energy,Max Score,Max Energy,Strand,Len1,Len2,Positions
>>ath-miR156c	AT1G01040.2	161.00	-18.63	161.00	-18.63	40	27	5877	 5126
Complete

Read Sequence:AT1G01046.1 (207 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01046.1
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Score for this Scan:
No Hits Found above Threshold
Complete

Read Sequence:AT1G01050.1 (976 nt)
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Performing Scan: ath-miR156c vs AT1G01050.1
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
Score for this Scan:
No Hits Found above Threshold
Complete

	 */
	
}
