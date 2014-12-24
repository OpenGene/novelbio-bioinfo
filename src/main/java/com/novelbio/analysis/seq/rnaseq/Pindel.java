package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class Pindel implements IntCmdSoft {

	String exePath;
	/** 输入参考序列文件*/
	String reference;
	/** 输入文件*/
	List<String> lsInputFile;
	/** 输入文件*/
	List<String> lsPrefix;
	/** 文库的插入片段长度*/
	int insertSize;
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
	List<String> lsFile = new ArrayList<String>();
	
	public void setReference(String reference) {
		this.reference = reference;
	}
	public void setInputFile(List<String> lsInputFile) {
		this.lsInputFile = lsInputFile;
	}
	public void setLsPrefix(List<String> lsPrefix) {
		this.lsPrefix = lsPrefix;
	}
	public void setInsertSize(int insertSize) {
		this.insertSize = insertSize;
	}
	public void setConfigFile(String configFile) {
		this.configFile = creatConfigFile();
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
	
//	List<String> lsCmd = new ArrayList<>();
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "pindel");
		ArrayOperate.addArrayToList(lsCmd, getReference());
		ArrayOperate.addArrayToList(lsCmd, getConfigFile());
		ArrayOperate.addArrayToList(lsCmd, getMinInversionSize());
		ArrayOperate.addArrayToList(lsCmd, getMinNTSize());
		ArrayOperate.addArrayToList(lsCmd, getNumberOfThreads());
		ArrayOperate.addArrayToList(lsCmd, getOutputPrefixp());
		System.out.println("lsCmd is ==> " + lsCmd.toString());
		return lsCmd;
	}
//	private String[] getInputFile(String inputFile) {
//		return new String[] {  "-f",inputFile };
//	}
	private String[] getReference() {
		return new String[] { "-f", reference};
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
	protected String creatConfigFile(){
		String outConfigFile = PathDetail.getTmpPathRandom() + "/" + "config.txt";
		TxtReadandWrite txtWrite = new TxtReadandWrite(outConfigFile, true);
		for (int i = 0; i < lsInputFile.size(); i++) {
			txtWrite.writefileln(lsInputFile.get(i) + "\t" + insertSize + "\t" + lsPrefix.get(i));
		}
		txtWrite.close();
		return  outConfigFile;
	}
}
