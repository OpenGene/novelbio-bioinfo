package com.novelbio.analysis.microarray;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.NovelBioConst;

public class AffyNormalization {
	public static final int NORM_RMA = 10;
	public static final int NORM_GCRMA = 20;
	public static final int NORM_MAS5 =30;
	
	String workSpace;
	String rawScript = "";
	String outScript = "";
	/** 标准化后的数据 */
	String outFileName = "";
	int normalizedType = NORM_RMA;
	String readFile = "";
	ArrayList<String> lsRawCelFile = new ArrayList<String>();
	
	public AffyNormalization() {
		rawScript = "/media/winE/Bioinformatics/R/Protocol/Microarray/Affymetirx芯片分析Java.txt";
		setWorkSpace();
		setOutScriptPath();
	}

	private void setWorkSpace() {
		workSpace = FileOperate.getProjectPath() + "Tmp/";
	}
	private void setOutScriptPath() {
		outScript = workSpace + "AffyNorm_" + DateTime.getDateAndRandom() + ".R";
	}
	public void setLsRawCelFile(ArrayList<String> lsRawCelFile) {
		this.lsRawCelFile = lsRawCelFile;
	}
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}
	/** NORM_RMA 等 */
	public void setNormalizedType(int normalizedType) {
		this.normalizedType = normalizedType;
	}
	/** 仅供测试 */
	public String getOutScript() {
		generateScript();
		return outScript;
	}
	protected void generateScript() {
		TxtReadandWrite txtReadScript = new TxtReadandWrite(rawScript, false);
		TxtReadandWrite txtOutScript = new TxtReadandWrite(outScript, true);
		for (String content : txtReadScript.readlines()) {
			if (content.startsWith("#workspace"))
				txtOutScript.writefileln(getWorkSpace(content));
			else if (content.startsWith("#filename"))
				txtOutScript.writefileln(getFileName(content));
			else if (content.startsWith("#NormalizedMethod"))
				txtOutScript.writefileln(getMethodType(content));
			else if (content.startsWith("#readCelFile"))
				txtOutScript.writefileln(getRawDataFile(content));
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
	private String getFileName(String content) {
		String fileRawdata = content.split(SepSign.SEP_ID)[1];
		fileRawdata = fileRawdata.replace("{$filename}", outFileName);
		return fileRawdata;
	}
	private String getRawDataFile(String content) {
		String celFileName = "\"" + lsRawCelFile.get(0) + "\"";
		for (int i = 1; i < lsRawCelFile.size(); i++) {
			celFileName = celFileName + ", \"" + lsRawCelFile.get(i) + "\"";
		}
		String fileRawdata = content.split(SepSign.SEP_ID)[1];
		fileRawdata = fileRawdata.replace("{$RawCelFile}", celFileName);
		return fileRawdata;
	}
	private String getMethodType(String content) {
		String methodType[] = content.split(SepSign.SEP_ID)[1].split(SepSign.SEP_INFO);
		HashMap<Integer, String> mapMethodID2Script = new HashMap<Integer, String>();
		for (String string : methodType) {
			String[] tmpMethod = string.split(SepSign.SEP_INFO_SAMEDB);
			mapMethodID2Script.put(Integer.parseInt(tmpMethod[0]), tmpMethod[1]);
		}		
		return mapMethodID2Script.get(normalizedType);
	}
	/**
	 * 调用Rrunning并写入Cmd的名字,
	 * 例如：
	 * Rrunning("DEseq")
	 */
	public void run() {
		generateScript();
		Rrunning("Normalize");
	}
	protected void Rrunning(String cmdName) {
		String cmd = NovelBioConst.R_SCRIPT + outScript;
		CmdOperate cmdOperate = new CmdOperate(cmd, cmdName);
		cmdOperate.run();
	}
	
	public static HashMap<String, Integer> getMapNormStr2ID() {
		HashMap<String, Integer> mapNormStr2ID = new HashMap<String, Integer>();
		mapNormStr2ID.put("RMA--Log2", NORM_RMA);
		mapNormStr2ID.put("GCRMA--Log2", NORM_GCRMA);
		mapNormStr2ID.put("MAS5--NoLog2", NORM_MAS5);
		return mapNormStr2ID;
	}
}
