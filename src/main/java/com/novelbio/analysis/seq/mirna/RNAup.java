package com.novelbio.analysis.seq.mirna;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class RNAup {
	public static void main(String[] args) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(cmdEXE);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
//		cmdOperate.setGetCmdInErrStream(true);
		cmdOperate.setGetCmdInStdStream(true);
		Thread sss = new Thread(cmdOperate);
		sss.start();
		TxtReadandWrite txtWrite = new TxtReadandWrite(cmdOperate.getInStream());
		System.out.println("start");
		txtWrite.writefileln(">aaaaa&bbbbbb");
		txtWrite.writefileln("ATCAGAC&GTCTGAT");
		
		txtWrite.writefileln(">aaaaa&bbbbbb");
		txtWrite.writefileln("AAATTATTAGATATACCAAACCAGAGAAAACAAATACATAATCGGAGAAAT" +
				"AC&AAATTATTAGATATACCAAACCAGAGAAAACAAATACATAATCGGAGAAATAC");
		txtWrite.writefileln(">aaaaa&bbbbbb");
		txtWrite.writefileln("AAATTATTAGATA&TACCAAACCAGAGAAAACAAATACATAATCGGAGAAAT");
//		txtWrite.flush();

		txtWrite.close();
//		txtWrite.close();
		TxtReadandWrite txtReadStd = new TxtReadandWrite(cmdOperate.getStreamStd());
		for (String string : txtReadStd.readlines()) {
			System.out.println(string);
		}
		
	}
	
	/**
>aaaaa&bbbbbb               总能量  打开能量 稳定能量 
(((((((&)))))))   1,7   :   1,7   (-7.70 = -7.70 + 0.00)
AUCAGAC&GUCUGAU
RNAup output in file: aaaaa&bbbbbb__w25_u1.out
>aaaaa&bbbbbb
((((.&)))).  42,46  :  42,46  (-2.68 = -3.30 + 0.62)
UCGGA&UCGGA
RNAup output in file: aaaaa&bbbbbb__w25_u1.out
>aaaaa&bbbbbb
(((((&)))))  25,29  :   3,7   (-0.62 = -1.00 + 0.38)
AUAAU&AUUAU
RNAup output in file: RNA_w25_u1.out

	
	 */
	
	
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
		cmdOutStream = cmdOperate.getStreamStd();
		
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + cmdEXE);
		return lsCmd;
	}
	
}
