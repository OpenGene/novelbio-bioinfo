package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.NovelBioConst;

public class TopGO {
	String workSpace;
	String tmplateScript = "";
	String exeScript = "";
	
	/** topgo的信息 */
	String GoType = "BP";
	String CalGeneIDFile;
	String BGGeneFile;
	
	/** 输出GO结果数据 */
	String rawGoResultFile = "";
	/** 输出GO的信息，用来做Go2Gene表的 */
	String GOInfoFile = "";
	/** 展示多少个GOTerm */
	int displayGoNum = 300;

	/** 需要做分析的基因 */
	ArrayList<String> lsGeneID;
	ArrayList<String[]> lsBG;
	
	
	ArrayList<String[]> lsResult;
	HashMap<String,ArrayList<String>> mapGOID2LsGeneID;
	public TopGO() {
		tmplateScript = NovelBioConst.getRworkspace() + "topGOJava.txt";
		setWorkSpace();
		setExeScriptPath();
		setRawGoResultFile();
		setGOInfoFile();
		setCalGeneIDFilePath();
	}
	private void setWorkSpace() {
		workSpace = NovelBioConst.getRworkspaceTmp();
	}
	private void setExeScriptPath() {
		exeScript = NovelBioConst.getRworkspaceTmp() + "TopGO_" + DateTime.getDateAndRandom() + ".R";
	}
	/** 输入文件 */
	private void setRawGoResultFile() {
		this.rawGoResultFile = workSpace + "TopGOResult_" + DateTime.getDateAndRandom() + ".txt";
	}
	/** 输入文件 */
	private void setGOInfoFile() {
		this.GOInfoFile = workSpace + "TopGOInfo_" + DateTime.getDateAndRandom() + ".txt";
		this.BGGeneFile = workSpace + "TopGOBG_" + DateTime.getDateAndRandom() + ".txt";
	}
	private void setCalGeneIDFilePath() {
		CalGeneIDFile = NovelBioConst.getRworkspaceTmp() + "TopGO_CalGeneIDFile" + DateTime.getDateAndRandom() + ".txt";
	}
	
	/** 待检验的基因 */
	public void setLsGene(ArrayList<String> lsGeneID) {
		this.lsGeneID = lsGeneID;
	}
	public void setDisplayGoNum(int displayGoNum) {
		this.displayGoNum = displayGoNum;
	}
	/** 背景 */
	public void setLsBG(ArrayList<String[]> lsBG) {
		this.lsBG = lsBG;
	}
	/** Go2Term.GO_BP 等 */
	public void setGoType(String GoType) {
		if (GoType.equals(Go2Term.GO_BP)) 
			this.GoType = "BP";
		else if (GoType.equals(Go2Term.GO_MF)) 
			this.GoType = "MF";
		else if (GoType.equals(Go2Term.GO_CC)) 
			this.GoType = "CC";
	}
	/** 仅供测试 */
	public String getOutScript() {
		generateScript();
		//将待分析的基因写入文本
		fillCalGeneID_And_BG_File();
		return exeScript;
	}

	protected void generateScript() {
		TxtReadandWrite txtReadScript = new TxtReadandWrite(tmplateScript, false);
		TxtReadandWrite txtOutScript = new TxtReadandWrite(exeScript, true);
		for (String content : txtReadScript.readlines()) {
			if (content.startsWith("#workspace"))
				txtOutScript.writefileln(getWorkSpace(content));
			else if (content.startsWith("#GOtype"))
				txtOutScript.writefileln(getGOtype(content));
			else if (content.startsWith("#GONum"))
				txtOutScript.writefileln(getGONum(content));
			else if (content.startsWith("#GoResult"))
				txtOutScript.writefileln(getGoResult(content));
			else if (content.startsWith("#GOInfoFile"))
				txtOutScript.writefileln(getGOInfoFile(content));
			else if (content.startsWith("#CalculateGeneID"))
				txtOutScript.writefileln(getCalculateGeneID(content));
			else if (content.startsWith("#BGGeneID"))
				txtOutScript.writefileln(getBGGeneFile(content));
			else {
				txtOutScript.writefileln(content);
			}
		}
		txtOutScript.close();
	}

	private String getWorkSpace(String content) {
		String RworkSpace = content.split(SepSign.SEP_ID)[1];
		RworkSpace = RworkSpace.replace("{$workspace}", workSpace);
		return RworkSpace;
	}
	private String getGOtype(String content) {
		String gotype = content.split(SepSign.SEP_ID)[1];
		gotype = gotype.replace("{$GOType}", GoType);
		return gotype;
	}
	private String getGONum(String content) {
		String GONum = content.split(SepSign.SEP_ID)[1];
		GONum = GONum.replace("{$GONum}", displayGoNum + "");
		return GONum;
	}
	private String getGoResult(String content) {
		String GoResultFile = content.split(SepSign.SEP_ID)[1];
		GoResultFile = GoResultFile.replace("{$GoResultFile}", rawGoResultFile);
		return GoResultFile;
	}
	private String getGOInfoFile(String content) {
		String GOInfo = content.split(SepSign.SEP_ID)[1];
		GOInfo = GOInfo.replace("{$GOInfoFile}", GOInfoFile + "");
		return GOInfo;
	}
	private String getCalculateGeneID(String content) {
		String GOInfo = content.split(SepSign.SEP_ID)[1];
		GOInfo = GOInfo.replace("{$CalGeneIDFile}", CalGeneIDFile + "");
		return GOInfo;
	}
	private String getBGGeneFile(String content) {
		String GOInfo = content.split(SepSign.SEP_ID)[1];
		GOInfo = GOInfo.replace("{$BGGeneFile}", BGGeneFile + "");
		return GOInfo;
	}
	private void fillCalGeneID_And_BG_File() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(CalGeneIDFile, true);
		String[] tmpOut = new String[lsGeneID.size()];
		int i = 0;
		for (String geneID : lsGeneID) {
			tmpOut[i] = geneID;
			i ++;
		}
		txtWrite.Rwritefile(tmpOut);
		
		TxtReadandWrite txtTopGoBG = new TxtReadandWrite(BGGeneFile, true);
		txtTopGoBG.ExcelWrite(lsBG);
	}
	
	/**
	 * 调用Rrunning并写入Cmd的名字,
	 * 例如：
	 * Rrunning("DEseq")
	 */
	public void run() {
		fillCalGeneID_And_BG_File();
		generateScript();
		Rrunning("TopGO");
		readResult();
		readGo2GeneAll();
		clean();
	}
	protected void Rrunning(String cmdName) {
		String cmd = NovelBioConst.R_SCRIPT + exeScript;
		CmdOperate cmdOperate = new CmdOperate(cmd, cmdName);
		cmdOperate.run();
	}
	private void readResult() {
		TxtReadandWrite txtRGo2Gene = new TxtReadandWrite(rawGoResultFile, false);
		lsResult = txtRGo2Gene.ExcelRead("\t", 2, 2, txtRGo2Gene.ExcelRows(), txtRGo2Gene.ExcelColumns("\t"), 0);
		//去除"号
		for (String[] strings : lsResult) {
			for (int i = 0; i < strings.length; i++) {
				strings[i] = strings[i].replace("\"", "");
			}
		}
	}
	/** 返回获得的Go2Gene列表 */
	private void readGo2GeneAll() {
		try {
			getGo2GeneAllTry();
		} catch (Exception e) {
			try {
				Thread.sleep(2000);
				getGo2GeneAllTry();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	/**
	 * 输入每个GO对应的全部基因文件，由R产生<br>
	 * 读取RGoInfo文件，将里面的GO2Gene的信息保存为<br>
	 * hash--GOID-lsGeneID
	 * @return
	 * @throws Exception 
	 */
	private void getGo2GeneAllTry() {
		TxtReadandWrite txtRGo2Gene = new TxtReadandWrite(GOInfoFile, false);
		mapGOID2LsGeneID = new HashMap<String, ArrayList<String>>();
		ArrayList<String> lsGOGene = null;
		for (String content : txtRGo2Gene.readlines()) {
			if (content.startsWith("#")) {
				lsGOGene = new ArrayList<String>();
				mapGOID2LsGeneID.put(content.replace("#", "").trim(), lsGOGene);
				continue;
			}
			if (content.trim().equals("")) {
				continue;
			}
			lsGOGene.add(content.trim());
		}
	}
	/** 删除中间文件 */
	private void clean() {
		FileOperate.DeleteFileFolder(exeScript);
		FileOperate.DeleteFileFolder(BGGeneFile);
		FileOperate.DeleteFileFolder(CalGeneIDFile);
		FileOperate.DeleteFileFolder(rawGoResultFile);
		FileOperate.DeleteFileFolder(GOInfoFile);
	}
	
	/** 返回获得的结果文件 */
	public ArrayList<String[]> getLsTestResult() {
		return lsResult;
	}
	/** 返回获得的Go2Gene列表 */
	public HashMap<String,ArrayList<String>> getGo2GeneAll() {
		return mapGOID2LsGeneID;
	}
	
}
