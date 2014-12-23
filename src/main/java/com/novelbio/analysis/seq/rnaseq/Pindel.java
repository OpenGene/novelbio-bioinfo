package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.web.model.project.NBCFile;

public class Pindel implements IntCmdSoft {

	String exePath;
	/** 输入文件*/
	String inputFile;
	/** 输入的配置文件*/
	String configFile;
	/** only report inserted (NT) sequences in deletions greater than this size (default 50)*/
	int minNTSize;
	/** only report inversions greater than this number of bases (default 50)*/
	int minInversionSize;
	/** the number of threads Pindel will use (default 1)*/
	int numberOfThreads;
	/** 设置输出文件路径包括文件名称前缀*/
	String outputPrefix;
	
	public void setInputFile(String inputFile) {
//		FileOperate.checkFileExistAndBigThanSize(inputFile, 0);
		this.inputFile = inputFile;
	}
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	public void setMinInversionSize(int minInversionSize) {
		this.minInversionSize = minInversionSize;
	}
	public void setMinNTSize(int minNTSize) {
		this.minNTSize = minNTSize;
	}
	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}
	public void setOutputPrefix(String outputPrefix) {
		this.outputPrefix = outputPrefix;
	}
	
	public Pindel() {
		 SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.pindel);
		 this.exePath = softWareInfo.getExePathRun();
	}
	public void run() {
		CmdOperate cmdOperate = new CmdOperate(getCmdExeStr());
		cmdOperate.runWithExp("Pindel error:");
	}
	List<String> lsCmd = new ArrayList<>();
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "pindel");
		ArrayOperate.addArrayToList(lsCmd, getInputFile(inputFile));
		ArrayOperate.addArrayToList(lsCmd, getConfigFile());
		ArrayOperate.addArrayToList(lsCmd, getMinInversionSize());
		ArrayOperate.addArrayToList(lsCmd, getMinNTSize());
		ArrayOperate.addArrayToList(lsCmd, getNumberOfThreads());
		ArrayOperate.addArrayToList(lsCmd, getOutputPrefixp());
		System.out.println("lsCmd is ==> " + lsCmd.toString());
		return lsCmd;
	}
	private String[] getInputFile(String inputFile) {
		return new String[] {  "-f",inputFile };
	}
	private String[] getConfigFile() {
		return new String[] { "-i", configFile};
	}
	private String[] getMinInversionSize() {
		return new String[] { "-v", minInversionSize + "" };
	}
	private String[] getMinNTSize() {
		return new String[] { "-n", minNTSize + "" };
	}
	private String[] getNumberOfThreads() {
		return new String[] { "-T", numberOfThreads + "" };
	}
	private String[] getOutputPrefixp() {
		return new String[] { "-o", outputPrefix};
	}
	protected String creatConfigFile(List<String> lsInputFile,List<String> lsPrefix, int insertSize,String outConfigFile){
		TxtReadandWrite txtWrite = new TxtReadandWrite(outConfigFile, true);
		for (int i = 0; i < lsInputFile.size(); i++) {
			String inputFileName = NBCFile.findInstance(lsInputFile.get(i)).getRealPathAndName();
			String fileName = NBCFile.findInstance(lsInputFile.get(i)).getFileName();
			System.out.println("inputFileName" + inputFileName);
			System.out.println("fileName" + fileName);
			txtWrite.writefileln(inputFileName + "\t" + insertSize + "\t" + lsPrefix.get(i));
		}
		txtWrite.close();
		return  outConfigFile;
	}
}
