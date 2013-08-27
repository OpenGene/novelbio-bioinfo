package com.novelbio.analysis.microarray;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.novelbio.base.PathDetail;
import com.novelbio.base.SepSign;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.service.SpringFactory;
import com.novelbio.generalConf.PathDetailNBC;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class AffyNormalization {
	public static final int NORM_RMA = 10;
	public static final int NORM_GCRMA = 20;
	public static final int NORM_MAS5 =30;
	
	/** 常规芯片譬如表达谱芯片 */
	public static final String arrayType_normAffy = "normaffy";
	/** 外显子芯片等 */
	public static final String arrayType_exonAffy = "exonaffy";
	
	Configuration freeMarkerConfiguration = (Configuration)SpringFactory.getFactory().getBean("freemarkNBC");
	
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
		setOutScriptPath();
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
		Map<String,Object> mapData = new HashMap<String, Object>();
		mapData.put("workspace", PathDetail.getRworkspaceTmp());
		mapData.put("fileOutName", outFileName);
		mapData.put("isNorm", isNormType());
		mapData.put("RawCelFile", getRawDataFile());
		mapData.put("normalizedType", getMethodType());

		try {
			Template template = freeMarkerConfiguration.getTemplate("/R/AffyCellNormalize.ftl");
			StringWriter sw = new StringWriter();
			TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outScript, true);
			// 处理并把结果输出到字符串中
			template.process(mapData, sw);
			txtReadandWrite.writefile(sw.toString());
			txtReadandWrite.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean isNormType() {
		if (arrayType == arrayType_normAffy) {
			return true;
		} else {
			return false;
		}
	}
	
	private String getRawDataFile() {
		String celFileName = CmdOperate.addQuot(lsRawCelFile.get(0));
		for (int i = 1; i < lsRawCelFile.size(); i++) {
			celFileName = celFileName + "," + CmdOperate.addQuot(lsRawCelFile.get(i));
		}
		celFileName = celFileName.replace("\\", "/");
		return celFileName;
	}
	
	private String getMethodType() {
		String normType = "";
		if (normalizedType == NORM_GCRMA) {
			normType = "gcrma";
		} else if (normalizedType == NORM_MAS5) {
			normType = "mas5";
		} else if (normalizedType == NORM_RMA) {
			normType = "rma";
		}
		return normType;
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
