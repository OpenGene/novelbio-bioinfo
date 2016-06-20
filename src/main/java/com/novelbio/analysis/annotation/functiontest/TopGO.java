package com.novelbio.analysis.annotation.functiontest;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.service.SpringFactoryBioinfo;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class TopGO implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(TopGO.class);
	Configuration freeMarkerConfiguration = (Configuration)SpringFactoryBioinfo.getFactory().getBean("freemarkNBC");
	
	String workSpace;
	String exeScript = "";
	
	int firstSigNodes = 10;
	String useInfo = "def";
	/** topgo的信息 */
	GOtype goType = GOtype.BP;
	GoAlgorithm goAlgorithm;

	String CalGeneIDFile;
	String BGGeneFile;
	
	/** 输出GO结果数据 */
	String rawGoResultFile = "";
	String goMapPdfPrefix = "";
	
	/** 输出GO的信息，用来做Go2Gene表的 */
	String GOInfoFile = "";
	/** 展示多少个GOTerm */
	int displayGoNum = 300;

	/** 需要做分析的基因 */
	Collection<String> lsGeneID;
	ArrayList<String> lsBG;
	
	/** R脚本的具体内容 */
	String scriptContent;
	
	ArrayList<String[]> lsResult;
	ArrayListMultimap<String, String> mapGOID2LsGeneID;
	public TopGO(GoAlgorithm goAlgorithm, GOtype goType) {
		this.goAlgorithm = goAlgorithm;
		this.goType = goType;
		setWorkSpace();
		setExeScriptPath();
		setRawGoResultFile();
		setGOInfoFile();
		setCalGeneIDFilePath();
	}
	private void setWorkSpace() {
		workSpace = PathDetail.getRworkspaceTmp();
	}
	private void setExeScriptPath() {
		exeScript = PathDetail.getRworkspaceTmp() + "TopGO." + goType.getOneWord() + "." + goAlgorithm.toString() + "."  + DateUtil.getDateAndRandom() + ".R";
	}
	/** 输入文件 */
	private void setRawGoResultFile() {
		this.rawGoResultFile = workSpace + "TopGOResult."  + goType.getOneWord() + "." + goAlgorithm.toString() + "." + DateUtil.getDateAndRandom() + ".txt";
		this.goMapPdfPrefix = workSpace + "TopGOResult."  + goType.getOneWord() + "." + goAlgorithm.toString() + "." + DateUtil.getDateAndRandom();
	}
	/** 输入文件 */
	private void setGOInfoFile() {
		this.GOInfoFile = workSpace + "TopGOInfo."  + goType.getOneWord() + "." + goAlgorithm.toString() + "." + DateUtil.getDateAndRandom() + ".txt";
		this.BGGeneFile = workSpace + "TopGOBG."  + goType.getOneWord() + "." + goAlgorithm.toString() + "." + DateUtil.getDateAndRandom() + ".txt";
	}
	private void setCalGeneIDFilePath() {
		CalGeneIDFile = PathDetail.getRworkspaceTmp() + "TopGO.CalGeneIDFile." + goType.getOneWord() + "." + goAlgorithm.toString() + "." + DateUtil.getDateAndRandom() + ".txt";
	}
	
	/** 待检验的基因 */
	public void setLsGene(Collection<String> lsGeneID) {
		this.lsGeneID = lsGeneID;
	}
	public void setDisplayGoNum(int displayGoNum) {
		this.displayGoNum = displayGoNum;
	}
	/** 背景 */
	public void setLsBG(ArrayList<String> lsBG) {
		this.lsBG = lsBG;
	}
	/** Go2Term.GO_BP 等 */
	public void setGoType(GOtype goType) {
		if (goType == GOtype.ALL) {
			this.goType = GOtype.BP;
		}
		this.goType = goType;
	}
	
	/** 仅供测试 */
	public String getOutScript() {
		generateScript();
		//将待分析的基因写入文本
		fillCalGeneID_And_BG_File();
		return exeScript;
	}
	protected void generateScript() {
		Map<String,Object> mapData = new HashMap<String, Object>();
		mapData.put("workspace", workSpace.replace("\\", "/"));
		mapData.put("GOtype", goType.getTwoWord());
		mapData.put("GONum",  displayGoNum + "");
		
		mapData.put("firstSigNodes",  firstSigNodes + "");
		mapData.put("useInfo",  useInfo + "");

		
		mapData.put("GoResultFile",  FileHadoop.convertToLocalPath(rawGoResultFile.replace("\\", "/")));
		mapData.put("GoMapPdfPrefix",  FileHadoop.convertToLocalPath(goMapPdfPrefix.replace("\\", "/")));

		mapData.put("GOInfoFile",  FileHadoop.convertToLocalPath(GOInfoFile.replace("\\", "/")));
		
		mapData.put("CalGeneIDFile", FileHadoop.convertToLocalPath(CalGeneIDFile.replace("\\", "/")));
		
		mapData.put("BGGeneFile", FileHadoop.convertToLocalPath(BGGeneFile.replace("\\", "/")));
		mapData.put("GOAlgorithm", getAlgorithm());
		
		try {
			Template template = freeMarkerConfiguration.getTemplate("/R/TopGO.ftl");
			StringWriter sw = new StringWriter();
			TxtReadandWrite txtReadandWrite = new TxtReadandWrite(exeScript, true);
			// 处理并把结果输出到字符串中
			template.process(mapData, sw);
			scriptContent = sw.toString();
			txtReadandWrite.writefile(scriptContent);
			txtReadandWrite.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("渲染出错啦! " + e.getMessage());
		}
	}
	
	public String getTopGoPdfFile() {
		return goMapPdfPrefix + "_" + getAlgorithm() + "_" + firstSigNodes + "_" + useInfo + ".pdf";
	}
	public String getTopGoScript() {
		return exeScript;
	}
	private String getAlgorithm() {
		String resultAlgorithm = GoAlgorithm.elim.toString();
		if (goAlgorithm != null) {
			resultAlgorithm = goAlgorithm.toString();
		}
		return resultAlgorithm;
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
		txtWrite.close();
		
		TxtReadandWrite txtTopGoBG = new TxtReadandWrite(BGGeneFile, true);
		for (String string : lsBG) {
			txtTopGoBG.writefileln(string);
		}
		txtTopGoBG.close();
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
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(PathDetail.getRscript());
		lsCmd.add(exeScript);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.runWithExp();
	}
	private void readResult() {
		TxtReadandWrite txtRGo2Gene = new TxtReadandWrite(rawGoResultFile, false);
		lsResult = txtRGo2Gene.ExcelRead(2, 2, txtRGo2Gene.ExcelRows(), txtRGo2Gene.ExcelColumns(), 0);
		//去除"号
		for (String[] strings : lsResult) {
			for (int i = 0; i < strings.length; i++) {
				strings[i] = strings[i].replace("\"", "");
			}
		}
		txtRGo2Gene.close();
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
		mapGOID2LsGeneID = ArrayListMultimap.create();
		String goID = "";
		for (String content : txtRGo2Gene.readlines()) {
			content = content.trim();
			if (content.startsWith("#")) {
				goID = content.replace("#", "").trim();
				mapGOID2LsGeneID.put(content.replace("#", "").trim(), content.trim());
				continue;
			}
			if (content.equals("")) {
				continue;
			}
			mapGOID2LsGeneID.put(goID, content);
		}
		txtRGo2Gene.close();
	}
	/** 删除中间文件 */
	private void clean() {
//		FileOperate.deleteFileFolder(exeScript);
		FileOperate.deleteFileFolder(BGGeneFile);
		FileOperate.deleteFileFolder(CalGeneIDFile);
		FileOperate.deleteFileFolder(rawGoResultFile);
		FileOperate.deleteFileFolder(GOInfoFile);
	}
	
	/** 返回获得的结果文件 */
	public ArrayList<String[]> getLsTestResult() {
		return lsResult;
	}
	/** 返回获得的Go2Gene列表 */
	public ArrayListMultimap<String, String> getGo2GeneUniIDAll() {
		return mapGOID2LsGeneID;
	}
	
	public static enum GoAlgorithm {
		elim, classic, weight, weight01, parentchild, novelgo;
		static HashMap<String, GoAlgorithm> mapStr2GoAlgrithm = new LinkedHashMap<String, GoAlgorithm>();
		static {
			mapStr2GoAlgrithm.put("novelgo", novelgo);
			mapStr2GoAlgrithm.put("parentchild", parentchild);
			mapStr2GoAlgrithm.put("classic", classic);
			mapStr2GoAlgrithm.put("weight", weight);
			mapStr2GoAlgrithm.put("weight01", weight01);
			mapStr2GoAlgrithm.put("elim", elim);
		}
		public static HashMap<String, GoAlgorithm> getMapStr2GoAlgrithm() {
			return mapStr2GoAlgrithm;
		}
	}
	
	/** 务必要run完之后才能获得 */
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsScript = new ArrayList<>();
		lsScript.add(scriptContent);
		return lsScript;
	}
	
}
