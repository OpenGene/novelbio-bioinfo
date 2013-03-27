package com.novelbio.analysis.microarray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;

public class AffyNormalization {
	public static final int NORM_RMA = 10;
	public static final int NORM_GCRMA = 20;
	public static final int NORM_MAS5 =30;
	
	/** 常规芯片譬如表达谱芯片 */
	public static final String arrayType_normAffy = "normaffy";
	/** 外显子芯片等 */
	public static final String arrayType_exonAffy = "exonaffy";
	
	
	String workSpace;
	String rawScript = "";
	String outScript = "";
	/** 标准化后的数据 */
	String outFileName = "";
	int normalizedType = NORM_RMA;
	String arrayType = arrayType_normAffy;
	String readFile = "";
	ArrayList<String> lsRawCelFile = new ArrayList<String>();
	
	public AffyNormalization() {
		rawScript = PathDetail.getRworkspace() + "Affymetirx芯片分析Java.txt";
		setWorkSpace();
		setOutScriptPath();
	}

	private void setWorkSpace() {
		workSpace = PathDetail.getRworkspaceTmp();
	}
	private void setOutScriptPath() {
		outScript = PathDetail.getRworkspaceTmp() + "AffyNorm_" + DateUtil.getDateAndRandom() + ".R";
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
	/** arrayType_exonAffy 等 */
	public void setArrayType(String arrayType) {
		this.arrayType = arrayType;
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
		RworkSpace = RworkSpace.replace("{$workspace}", workSpace.replace("\\", "/"));
		return RworkSpace;
	}
	private String getFileName(String content) {
		String fileRawdata = content.split(SepSign.SEP_ID)[1];
		fileRawdata = fileRawdata.replace("{$filename}", outFileName.replace("\\", "/"));
		return fileRawdata;
	}
	private String getRawDataFile(String content) {
		String celFileName = CmdOperate.addQuot(lsRawCelFile.get(0));
		for (int i = 1; i < lsRawCelFile.size(); i++) {
			celFileName = celFileName + "," + CmdOperate.addQuot(lsRawCelFile.get(i));
		}
		
		String[] arrayType = content.split(SepSign.SEP_ID)[1].split(SepSign.SEP_INFO);
		HashMap<String, String> mapAffayType2Script = new HashMap<String, String>();
		for (String string : arrayType) {
			String[] tmpArray = string.split(SepSign.SEP_INFO_SAMEDB);
			mapAffayType2Script.put(tmpArray[0], tmpArray[1]);
		}		
		String fileRawdata = mapAffayType2Script.get(this.arrayType);
		fileRawdata = fileRawdata.replace("{$RawCelFile}", celFileName.replace("\\", "/"));
		return fileRawdata;
	}
	private String getMethodType(String content) {
		String[] methodType = content.split(SepSign.SEP_ID)[1].split(SepSign.SEP_INFO);
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
		clean();
	}
	protected void Rrunning(String cmdName) {
		String cmd = PathDetail.getRscript() + outScript.replace("\\", "/");
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.run();
	}
	
	public static HashMap<String, Integer> getMapNormStr2ID() {
		HashMap<String, Integer> mapNormStr2ID = new LinkedHashMap<String, Integer>();
		mapNormStr2ID.put("RMA--Log2", NORM_RMA);
		mapNormStr2ID.put("GCRMA--Log2", NORM_GCRMA);
		mapNormStr2ID.put("MAS5--NoLog2", NORM_MAS5);
		return mapNormStr2ID;
	}
	public static HashMap<String, String> getMapArrayTpye() {
		HashMap<String, String> mapArrayType = new LinkedHashMap<String, String>();
		mapArrayType.put("Normal Array", arrayType_normAffy);
		mapArrayType.put("Exon Array", arrayType_normAffy);
		return mapArrayType;
	}
	/** 删除中间文件 */
	private void clean() {
		FileOperate.DeleteFileFolder(outScript);
	}
}
