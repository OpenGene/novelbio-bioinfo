package com.novelbio.analysis.comparegenomics;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * 基于muscle的多序列比对软件
 * @author novelbio
 *
 */
public class AlignmentMuscle implements IntCmdSoft{

	String exePath = "";	
	
	String inputFasta;
	boolean isProfileAlign;
	String inputFasta2;
	
	String outputFasta;
	/** 最大迭代次数，算法默认为16 */
	int maxiters = -1;
	/** 最长运行时间 */
	int maxhours = -1;
	
	/** 是否需要优化比对结果，该选项必须不能为 isProfileAlign，同时输入文件必须已经被联配过 */
	boolean isRefine = false;
	
	public AlignmentMuscle() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.muscle);
		this.exePath = softWareInfo.getExePathRun();
	}
	
	public void setInputFasta(String inputFasta) {
		this.inputFasta = inputFasta;
		this.isProfileAlign = false;
	}
	
	/**
	 * Profile-profile alignment<br><br>
	 * Profile-profile alignment takes two existing MSAs ("profiles") and aligns them to each other, keeping the columns in each MSA intact. The final alignment is made by inserting columns of gaps into the MSAs as needed. The alignments of sequences in each input MSAs are thus fully preserved in the output alignment.
	 * <br><br>
	 * One or both of the input alignments may be single sequences.
	 * <br><br>
	 * Example<br><br>
	 * muscle -profile -in1 one.afa -in2 two.afa -out both.afa
	 * <br><br>
	 * Profile-profile alignment is not for homolog recognition<br>
	 * MUSCLE does not compute a similarity measure or measure of statistical significance (such as an E-value), so this option is not useful for discriminating homologs from unrelated sequences.
	 */
	public void setInputFastaProfileAlign(String inputFasta1, String inputFasta2) {
		this.inputFasta = inputFasta1;
		this.inputFasta2 = inputFasta2;
		this.isProfileAlign = true;
	}
	
	public void setOutputFasta(String outputFasta) {
		this.outputFasta = outputFasta;
	}
	
	/** 是否需要优化比对结果，该选项必须不能为 isProfileAlign，同时输入文件必须已经被联配过 */
	public void setRefine(boolean isRefine) {
		this.isRefine = isRefine;
	}
	
	public void runAlignment() {
		List<String> lsCmd = getLsCmd(getTmpOutFile());
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setRedirectInToTmp(true);
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamInput(inputFasta);
		if (isProfileAlign) {
			cmdOperate.addCmdParamInput(inputFasta2);
		}
		cmdOperate.addCmdParamOutput(getTmpOutFile());
		
		cmdOperate.runWithExp();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("run trinity error:", cmdOperate);
		}
		FileOperate.moveFile(true, getTmpOutFile(), outputFasta);
	}
	
	private String getTmpOutFile() {
		return FileOperate.changeFileSuffix(outputFasta, "_tmp", null);
	}
	
	private List<String> getLsCmd(String outPath) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "muscle");
		ArrayOperate.addArrayToList(lsCmd, getInput());
		ArrayOperate.addArrayToList(lsCmd, getOutput(outPath));
		ArrayOperate.addArrayToList(lsCmd, getMaxiters());
		ArrayOperate.addArrayToList(lsCmd, getMaxhours());
		ArrayOperate.addArrayToList(lsCmd, getRefine());
		return lsCmd;
	}
	
	private String[] getInput() {
		if (isProfileAlign) {
			return new String[]{"-profile", "-in1", inputFasta, "-in2", inputFasta2};
		} else {
			return new String[]{"-in", inputFasta};			
		}
	}
	
	private String[] getOutput(String output) {
		return new String[]{"-out", output};
	}
	
	private String[] getRefine() {
		if (isRefine && !isProfileAlign) {
			return new String[]{"-refine"};
		} else {
			return null;
		}
	}
	
	private String[] getMaxiters() {
		if (maxiters <= 0) {
			return null;
		} else if (maxiters > 100) {
			maxhours = 100;
		}
		return new String[]{"--maxiters", maxiters + ""};
	}
	
	private String[] getMaxhours() {
		if (maxhours <= 0) {
			return null;
		} else if (maxhours > 50) {
			maxhours = 50;
		}
		return new String[]{"--maxhours", maxiters + ""};
	}

	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmd = getLsCmd(getTmpOutFile());
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		String cmd = cmdOperate.getCmdExeStr();
		List<String> lsCmdOut = new ArrayList<>();
		lsCmdOut.add(cmd);
		return lsCmdOut;
	}

}
