package com.novelbio.analysis.seq.mirna;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 每次用都要new一个新的比较好 
 * 
 * @author zomg0jie
 * 2131207 在家里
 */
public class RNAcofold {
	public static void main(String[] args) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(cmdEXE);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
//		cmdOperate.setGetCmdInErrStream(true);
		cmdOperate.setGetCmdInStdStream(true);
		Thread sss = new Thread(cmdOperate);
		sss.start();
		TxtReadandWrite txtWrite = new TxtReadandWrite(cmdOperate.getInStream());
		TxtReadandWrite txtReadStd = new TxtReadandWrite(cmdOperate.getStreamStd());

		System.out.println("start");
		txtWrite.writefileln(">ggk");
		txtWrite.writefileln("ATCAGAC&GTCTGAT");
		
		txtWrite.writefileln(">ggg");
		txtWrite.writefileln("AAATTATTAGATATACCAAACCAGAGAAAACAAATACATAATCGGAGAAAT" +
				"AC&AAATTATTAGATATACCAAACCAGAGAAAACAAATACATAATCGGAGAAATAC");
		txtWrite.writefileln(">ggg");
		txtWrite.writefileln("AAATTATTAGATATACCAAACCAGAGAAAACAAATACATAATCGGAGAAAT" +
				"AC&AAATTATTAGATATACCAAACCAGAGAAAACAAATACATAATCGGAGAAATAC");
		txtWrite.flush();
		for (String string : txtReadStd.readlines()) {
			System.out.println(string);
		}
		System.out.println("ok");
		txtWrite.writefileln(">hui");
		txtWrite.writefileln("AAATTATTAGATA&TACCAAACCAGAGAAAACAAATACATAATCGGAGAAAT");
		txtWrite.writefileln(">ggk");
		txtWrite.writefileln("ATCAGAC&GTCTGAT");
		txtWrite.flush();
		for (String string : txtReadStd.readlines()) {
			System.out.println(string);
		}
		System.out.println("ok");
		
//		txtWrite.flush();

		txtWrite.close();
//		txtWrite.close();

		
	}
	
	
	
	/**
	>aaaaa&bbbbbb
AUCAGAC&GUCUGAU
(((((((&))))))) ( -7.70)
>aaaaa&bbbbbb
AAAUUAUUAGAUAUACCAAACCAGAGAAAACAAAUACAUAAUCGGAGAAAUAC&AAAUUAUUAGAUAUACCAAACCAGAGAAAACAAAUACAUAAUCGGAGAAAUAC
.........................................((.((.......&.........................................)).))....... ( -3.30)
>aaaaa&bbbbbb
AAAUUAUUAGAUA&UACCAAACCAGAGAAAACAAAUACAUAAUCGGAGAAAU
..(((((......&........................)))))......... ( -1.00)

	*/
	
	
	
	
	static final String cmdEXE = "RNAcofold";
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
		cmdOutStream = cmdOperate.getStreamStd();
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + cmdEXE);
		return lsCmd;
	}
	
}
