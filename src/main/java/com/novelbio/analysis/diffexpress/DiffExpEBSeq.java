package com.novelbio.analysis.diffexpress;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.generalConf.TitleFormatNBC;

import freemarker.template.Template;
@Component
@Scope("prototype")
public class DiffExpEBSeq extends DiffExpAbs{
	private static final Logger logger = Logger.getLogger(DiffExpEBSeq.class);

	@Override
	protected void setOutScriptPath() {
		outScript = workSpace + "EBSeq_" + DateUtil.getDateAndRandom() + ".R";
	}

	@Override
	protected void setFileNameRawdata() {
		fileNameRawdata = workSpace + "EBSeqGeneInfo_" + DateUtil.getDateAndRandom() + ".txt";
	}
	@Override
	protected String generateScript() {
		Map<String,Object> mapData = new HashMap<String, Object>();
		mapData.put("workspace", getWorkSpace());
		mapData.put("filename", getFileName());
		mapData.put("mapOut2Compare_vector", getMapOut2Compare_vector());
		mapData.put("mapOut2sample", getMapOutFile2Compare());
		mapData.put("isSensitive", isSensitive);
		String scriptContent = null;
		try {
			Template template = freeMarkerConfiguration.getTemplate("/R/diffgene/EBSeq.ftl");
			StringWriter sw = new StringWriter();
			TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outScript, true);
			// 处理并把结果输出到字符串中
			template.process(mapData, sw);
			scriptContent = sw.toString();
			txtReadandWrite.writefile(scriptContent);
			txtReadandWrite.close();
		} catch (Exception e) {
			logger.error("渲染出错啦! " + e.getMessage());
		}
		return scriptContent;
	}
	
	/**
	 * key是OutFile<br>
	 * value：0：1，2，3，4 等，本组样本信息
	 * 1："A","A","B","B" 等，样本具体情况
	 * @return
	 */
	private Map<String, String[]> getMapOut2Compare_vector() {		
		ArrayListMultimap<String, Integer> mapSample2LsColNum = ArrayListMultimap.create();
		Map<Integer, String> mapColNum2Sample = new HashMap<Integer, String>();
		for (int i = 0; i < lsSampleColumn2GroupName.size(); i++) {
			String sampleName = lsSampleColumn2GroupName.get(i)[1];
			mapSample2LsColNum.put(sampleName, i+1);
			mapColNum2Sample.put(i+1, sampleName);
		}		
		
		Map<String, String[]> mapOutFile2Compare_Vector = new LinkedHashMap<String, String[]>();
		for (String outFileName : mapOutFileName2Compare.keySet()) {
			String[] treat2Col = mapOutFileName2Compare.get(outFileName);
			List<Integer> lsTreatNum = mapSample2LsColNum.get(treat2Col[0]);
			List<Integer> lsColNum = mapSample2LsColNum.get(treat2Col[1]);
			List<Integer> lsThisPaire = new ArrayList<Integer>(lsTreatNum);
			lsThisPaire.addAll(lsColNum);
			Collections.sort(lsThisPaire);
			String[] tmpResult = new String[]{lsThisPaire.get(0) + "", CmdOperate.addQuot(mapColNum2Sample.get(lsThisPaire.get(0)))};
			for (int i = 1; i < lsThisPaire.size(); i++) {
				int thisColNum = lsThisPaire.get(i);
				tmpResult[0] = tmpResult[0] + ", " + thisColNum;
				tmpResult[1] = tmpResult[1] + ", " + CmdOperate.addQuot(mapColNum2Sample.get(thisColNum));
			}
			mapOutFile2Compare_Vector.put(FileHadoop.convertToLocalPath(outFileName), tmpResult);
		}
		return mapOutFile2Compare_Vector;
	}
	
	private Map<String, String[]> getMapOutFile2Compare() {
		Map<String, String[]> mapOut2CompareFinal = new HashMap<>();
		for (String fileName : mapOutFileName2Compare.keySet()) {
			mapOut2CompareFinal.put(FileHadoop.convertToLocalPath(fileName), mapOutFileName2Compare.get(fileName));
		}
		return mapOut2CompareFinal;
	}
	
	@Override
	protected void run() {
		Rrunning("EBSeq");
	}

	@Override
	protected List<String[]> modifySingleResultFile(String outFileName, String treatName, String controlName) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String[]> lsDifGene = ExcelTxtRead.readLsExcelTxt(outFileName, 1);
		String[] title = new String[]{TitleFormatNBC.AccID.toString(), treatName , controlName, TitleFormatNBC.Log2FC.toString(), TitleFormatNBC.FDR.toString()};
		lsResult.add(title);

		for (int i = 1; i < lsDifGene.size(); i++) {
			String[] tmpResult = lsDifGene.get(i);
			for (int j = 0; j < tmpResult.length; j++) {
				tmpResult[j] = tmpResult[j].replace("\"", "");
			}
//			String tmp = tmpResult[1];
//			tmpResult[1] = tmpResult[2];
//			tmpResult[2] = tmp;
			
			if ((tmpResult[1].equals("0") || tmpResult[1].equalsIgnoreCase("NA") || tmpResult[1].equalsIgnoreCase("none") ) 
					&& (tmpResult[2].equals("0") || tmpResult[2].equalsIgnoreCase("NA") || tmpResult[2].equalsIgnoreCase("none") )) {
				tmpResult[3] = "0";
			} else if (tmpResult[1].equals("0")) {
				tmpResult[3] = "-20";
			} else if (tmpResult[2].equals("0")) {
				tmpResult[3] = "20";
			}
			lsResult.add(tmpResult);
		}
		return lsResult;
	}

}
