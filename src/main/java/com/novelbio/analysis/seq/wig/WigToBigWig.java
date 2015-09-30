package com.novelbio.analysis.seq.wig;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class WigToBigWig implements IntCmdSoft {
	String exePath;
	String wigFile;
	String txtMapChrId2Len;
	String bigWigFile;
	public WigToBigWig() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.wigToBigwig);
		exePath = softWareInfo.getExePathRun();
	}
	/** 输入路径 */
	public void setWigFile(String wigFile) {
		this.wigFile = wigFile;
	}
	/** 输入染色体对照表 */
	public void setTxtMapChrId2Len(String txtMapChrId2Len) {
		this.txtMapChrId2Len = txtMapChrId2Len;
	}
	/** 输出文件路径 */
	public void setBigWigFile(String bigWigFile) {
		this.bigWigFile = bigWigFile;
	}
	
	public void convert() {
//		String bigWigFileTmp = FileOperate.changeFileSuffix(bigWigFile, "_tmp", null);
		List<String> lsCmd = getLsCmd(wigFile, txtMapChrId2Len, bigWigFile);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(bigWigFile);
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd(wigFile + " WigToBigWig Error:\n" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
		}
//		FileOperate.moveFile(true, bigWigFileTmp, bigWigFile);
	}
	
	private List<String> getLsCmd(String inFile, String chrMap, String outFile) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "wigToBigWig");
		lsCmd.add(inFile);
		lsCmd.add(chrMap);
		lsCmd.add(outFile);
		return lsCmd;
	}
	
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmdOut = new ArrayList<>();
		String bigWigFileTmp = FileOperate.changeFileSuffix(bigWigFile, "_tmp", null);
		List<String> lsCmd = getLsCmd(wigFile, txtMapChrId2Len, bigWigFileTmp);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsCmdOut.add(cmdOperate.getCmdExeStr());
		return lsCmdOut;
	}
}
