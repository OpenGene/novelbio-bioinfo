package com.novelbio.analysis.seq.denovo;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class SSRPredict implements IntCmdSoft {

	//输入文件：需要进行SSR预测的序列文件，fasta格式文件
		String inputFile;
		String exePath = "";	
		String misainiFile;
		
		public SSRPredict() {
			SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.misa);
			this.exePath = softWareInfo.getExePathRun();
		}
		
		public void setInputFile(String inputFile) {
			this.inputFile = inputFile;
		}
		

		
		public void setMisainiFile(String misainiFile) {
			FileOperate.checkFileExistAndBigThanSize(misainiFile, 0);
			this.misainiFile = misainiFile;
		}

		public void run() {
			List<String> lsCmd = getLsCmd();
			CmdOperate cmdOperate = new CmdOperate(lsCmd);
			cmdOperate.runWithExp("MISA error:");
		}
		
		private List<String> getLsCmd() {
			List<String> lsCmd = new ArrayList<>();
			lsCmd.add(exePath + "misa.pl");
			ArrayOperate.addArrayToList(lsCmd, getInputFile(inputFile));
			return lsCmd;
		}
		private String[] getInputFile(String inputFile) {
			return new String[]{inputFile};
		}
		@Override
		public List<String> getCmdExeStr() {
			List<String> lsResult = new ArrayList<>();
			List<String> lsCmd = getLsCmd();
			CmdOperate cmdOperate = new CmdOperate(lsCmd);
			lsResult.add(cmdOperate.getCmdExeStr());
			return lsResult;
		}
}
