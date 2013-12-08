package com.novelbio.analysis.seq.mirna;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

/** 每次用都要new一个新的比较好 
 * 
 * @author zomg0jie
 * 2131207 在家里
 */
public class RNAcofold {
	static final String cmdEXE = "RNAup";
	String exePath;
	
	InputStream cmdOutStream;
	OutputStream cmdInStream;
	
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals(""))
			this.exePath = "";
		else
			this.exePath = FileOperate.addSep(exePath);
	}
	
	public void run() {
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		cmdOperate.setGetCmdInStdStream(true);
		cmdOutStream = cmdOperate.getStdStream();
		
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + cmdEXE);
		return lsCmd;
	}
	
}
