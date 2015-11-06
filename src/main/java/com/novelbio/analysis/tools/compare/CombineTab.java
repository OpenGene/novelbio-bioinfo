package com.novelbio.analysis.tools.compare;

import java.awt.image.BufferedImage;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.base.PathDetail;
import com.novelbio.base.SepSign;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.ImageUtils;
import com.novelbio.base.plot.VennImage;
import com.novelbio.database.service.SpringFactoryBioinfo;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 选定多个表，
 * 每个表中选定几列需要选择的列
 * 最后将这些表中的几列合并到一个table中，合并的ID为选中的ID列
 * @author zong0jie
 */
public class CombineTab {

	public static void main(String[] args) {
		String parentFile = "/home/novelbio/NBCsource/test/overlap/";
		String file1 = parentFile + "test1";
		String file2 = parentFile + "test2";
		String file3 = parentFile + "test3";
		CombineTab comb = new CombineTab();
		comb.setColExtractDetail(file1, "1");
		comb.setColExtractDetail(file2, "2");
		comb.setColExtractDetail(file3, "3");
		comb.setColCompareOverlapID(1,2);
		comb.exeToFile();
		for (String[] result : comb.getLsResultFromImage()) {
			for (int i = 0; i < result.length; i++) {
				System.out.print(result[i]+"   ");
			}
			System.out.println("");
		}
		VennImage vennImage = new VennImage("/home/novelbio/NBCsource/test/overlap/testR.tiff",3000,3000);
		vennImage.setMain("use VennDiagram to make a Venn plot");
		vennImage.setSub("by GaoZhu");
		comb.renderScriptAndDrawImage("/home/novelbio/NBCsource/test/overlap/", vennImage);
		//comb.deleteAllTempFile();
	}
	
	private static final Logger logger = Logger.getLogger(CombineTab.class);
	public static String tempFolder = PathDetail.getRworkspaceTmp();
	private List<String> tempFiles = new ArrayList<String>();
	
	LinkedHashMap<String, String> mapFileName2ConditionAbbr = new LinkedHashMap<>();
	/** ColCompareComb：将待查找的列合并起来，用"_"连接<br>
	 * ColCompareSep：分开的待查找的列
	 * */
	LinkedHashMap<String, List<String>> mapColCompareComb_To_ColCompareSep = new LinkedHashMap<>();
	Configuration freeMarkerConfiguration = (Configuration)SpringFactoryBioinfo.getFactory().getBean("freemarkNBC");

	HashMap<String, LinkedHashMap<String, String[]>> mapFileName_To_ColCompareComb2ExtractCol = new LinkedHashMap<>();
	/**
	 * 文件名---具体要包含哪几列，不含比较列
	 */
	LinkedHashMap<String, int[]> mapFileName2ExtractColNum = new LinkedHashMap<>();
	Map<String, Map<String,Boolean>> mapKey2mapShortName2Exist = new LinkedHashMap<>();
	Map<String, ArrayList<String>> mapShortName2lsGeneID = new LinkedHashMap<>();
	/** 存储 某个prefix独有的元素的数量，譬如 A，B，C三组，A独有30个，B独有27个，C独有23个这种 */
	Map<String, Integer> mapPrefix2NumOnly = new LinkedHashMap<>();
	
	/** 需要比较那几列 */
	int[] colCompareOverlapID;
	/** 并集里面的空格填充什么 */
	String strNull = null;
	
	ArrayList<String[]> lsResultUnion = new ArrayList<String[]>();
	ArrayList<String[]> lsResultIntersection = new ArrayList<String[]>();
	BufferedImage bufferedImage = null;
	ArrayList<String[]> lsResultFromImage = new ArrayList<String[]>();
	boolean runningFlag = true;
	/**
	 * 空格用什么字符串填充，默认为"";
	 * @param strNull
	 */
	public void setStrNull(String strNull) {
		this.strNull = strNull;
	}
	/**
	 * 待取交集的ID列
	 * @param colID
	 */
	public void setColCompareOverlapID(int... colID) {
		Set<Integer> setID = new LinkedHashSet<>();
		for (Integer integer : colID) {
			setID.add(integer-1);
		}
		this.colCompareOverlapID = new int[setID.size()];
		int i = 0;
		for (Integer integer : setID) {
			colCompareOverlapID[i++] = integer;
		}
		runningFlag = false;
	}
	
	public void setColCompareOverlapID(ArrayList<Integer> lsColID) {
		Set<Integer> setID = new LinkedHashSet<>();
		for (Integer integer : lsColID) {
			setID.add(integer-1);
		}
		this.colCompareOverlapID = new int[setID.size()];
		int i = 0;
		for (Integer integer : setID) {
			colCompareOverlapID[i++] = integer;
		}
		runningFlag = false;
	}

	/**
	 * 获得每个文件名, 对于每个文件，设定它的ID列
	 * @param condTxt 文本名
	 * @param codName 该文本的简称
	 * @param colDetail 该文本具体获取哪几列
	 */
	public void setColExtractDetail(String condTxt, String codName, int... colDetail) {
		Set<Integer> setID = new LinkedHashSet<>();
		for (Integer integer : colDetail) {
			setID.add(integer-1);
		}
		int[] colReal = new int[setID.size()];
		int i = 0;
		for (Integer integer : setID) {
			colReal[i++] = integer;
		}
		mapFileName2ExtractColNum.put(condTxt, colReal);
		mapFileName2ConditionAbbr.put(condTxt,codName);
		runningFlag = false;
	}
	
	/** 总共几个文件来取交集 */
	public int getAllFileNum() {
		return mapFileName2ConditionAbbr.size();
	}
	
	/**
	 * 
	 *  获得每个文件名, 对于每个文件，设定它的ID列
	 *  那么文件的简称由文本名生成
	 * @param condTxt 文本名
	 * @param colDetai 该文本具体获取哪几列
	 */
	@Deprecated
	public void setColDetai(String condTxt,int... colDetail) {
		Set<Integer> setID = new LinkedHashSet<>();
		for (Integer integer : colDetail) {
			setID.add(integer-1);
		}
		int[] colReal = new int[setID.size()];
		int i = 0;
		for (Integer integer : setID) {
			colReal[i++] = integer;
		}
		mapFileName2ExtractColNum.put(condTxt, colReal);
		mapFileName2ConditionAbbr.put(condTxt,FileOperate.getFileNameSep(condTxt)[0]);
		runningFlag = false;
	}
 
	/**
	 * 取并集
	 * @return
	 */
	private void exeToFile() {
		if (runningFlag && lsResultUnion.size() > 0) {
			return;
		}
		mapPrefix2NumOnly.clear();
		List<String> lsTitle = new ArrayList<>();
		for (Entry<String, String> entry : mapFileName2ConditionAbbr.entrySet()) {
			String filename = entry.getKey();
			String conditionAbbr = entry.getValue();
			ArrayList<String[]> lsInfoCodAllCols = getFileInfoAllCols(filename);
			
			//添加公共列的title，因为每个文档的公共列都一致，譬如第1，2，3列。所以只要添加一次即可
			if (lsTitle.size() == 0) {
				for (int i = 0; i < colCompareOverlapID.length; i++) {
					lsTitle.add(lsInfoCodAllCols.get(0)[i]);
				}
			}
			List<String> subTitle = new ArrayList<>();
			for (int i = 0; i < mapFileName2ExtractColNum.get(filename).length; i++) {
				subTitle.add(lsInfoCodAllCols.get(0)[i + colCompareOverlapID.length] + "_" + conditionAbbr);
			}
			lsTitle.addAll(subTitle);
			set_MapCompareComb_And_MapFileNamel(filename, lsInfoCodAllCols.subList(1, lsInfoCodAllCols.size()));
		}
		combInfo();
		lsResultIntersection.add(0, lsTitle.toArray(new String[0]));
		
		lsTitle.add(colCompareOverlapID.length, "OverlapInfo");
		lsResultUnion.add(0, lsTitle.toArray(new String[0]));

		runningFlag = true;
	}
	
	/**
	 * 读取指定文本的信息
	 * 包含标题列
	 * @param cond
	 * @return
	 * 获得的结果已经按照输入的colID顺序经过排序了
	 */
	private ArrayList<String[]> getFileInfoAllCols(String readFile) {
		int[] colExtract = mapFileName2ExtractColNum.get(readFile);
		int[] colReadFromFile = new int[colCompareOverlapID.length + colExtract.length];
		//合并列
		for (int i = 0; i < colCompareOverlapID.length; i++) {
			colReadFromFile[i] = colCompareOverlapID[i] + 1;
		}
		for (int i = 0; i < colExtract.length; i++) {
			colReadFromFile[colCompareOverlapID.length+i] = colExtract[i] + 1;
		}
		
		ArrayList<String[]> lsTmpInfo = ExcelTxtRead.readLsExcelTxt(readFile, colReadFromFile, 1, -1, true);
		return lsTmpInfo;
	}
	
	/**
	 * 本方法会自动去冗余，保留第一次出现的ID
	 * 设定唯一列的信息，然后将具体的信息装入具体的hash表中
	 * @param fileName 输入文件名
	 * @param lsTmpInfo 待取交集的列
	 * 
	 */
	private void set_MapCompareComb_And_MapFileNamel(String fileName, List<String[]> lsTmpInfo) {
		//本表的colID2colDetail信息
		LinkedHashMap<String, String[]> mapColCompareID2ExtractInfo = new LinkedHashMap<String, String[]>();
		mapFileName_To_ColCompareComb2ExtractCol.put(fileName, mapColCompareID2ExtractInfo);
		if (colCompareOverlapID.length > lsTmpInfo.get(0).length) {
			logger.error("输入列名长度有问题");
		}
		for (String[] strings : lsTmpInfo) {
			String colIDcombineStr = ""; List<String> colIDarray = new ArrayList<>();
			//flag列的信息
			for (int i = 0; i < colCompareOverlapID.length; i++) {
				colIDarray.add(strings[i]);
				if (i == 0) {
					colIDcombineStr += strings[i];
				} else {
					colIDcombineStr += SepSign.SEP_ID + strings[i];
				}
			}
			//删除flag列的信息
			String[] tmpExtractColInfo = new String[strings.length - colCompareOverlapID.length];
			for (int i = colCompareOverlapID.length; i < strings.length; i++) {
				tmpExtractColInfo[i - colCompareOverlapID.length] = strings[i];
			}
			//已经有了就跳过
			if (mapColCompareID2ExtractInfo.containsKey(colIDcombineStr)) {
				continue;
			}
			mapColCompareID2ExtractInfo.put(colIDcombineStr, tmpExtractColInfo);
			//不重复的所有ID，为取并集做准备
			mapColCompareComb_To_ColCompareSep.put(colIDcombineStr, colIDarray);
		}
	}
	/**
	 * 获得取并集的结果
	 * @return
	 */
	private void combInfo() {
		lsResultUnion = new ArrayList<String[]>();
		lsResultIntersection = new ArrayList<String[]>();
		for (String colCompareComb : mapColCompareComb_To_ColCompareSep.keySet()) {
			List<String> colCompareSep = mapColCompareComb_To_ColCompareSep.get(colCompareComb);
			boolean flagInterSection = true;
			
			Map<String,Boolean> mapPrefix2isExist = new LinkedHashMap<String,Boolean>();
			mapKey2mapShortName2Exist.put(colCompareComb, mapPrefix2isExist);
			
			//每个ID在所有多个表中全部查找一遍
			for (String fileName : mapFileName2ConditionAbbr.keySet()) {
				LinkedHashMap<String, String[]> mapColCompareComb2ExtractCol = mapFileName_To_ColCompareComb2ExtractCol.get(fileName);
				String[] extractCol = mapColCompareComb2ExtractCol.get(colCompareComb);
				//没找到，就用空格替换
				if (extractCol == null) {
					flagInterSection = false;
					extractCol = new String[mapFileName2ExtractColNum.get(fileName).length];
					for (int i = 0; i < extractCol.length; i++) {
						extractCol[i] = strNull;
					}
					mapPrefix2isExist.put(mapFileName2ConditionAbbr.get(fileName),false);
				} else {
					mapPrefix2isExist.put(mapFileName2ConditionAbbr.get(fileName),true);
				}
				//合并列
				for (String string : extractCol) {
					colCompareSep.add(string);
				}
			}
			fillMapPrefix2NumOnly(mapPrefix2isExist);
			colCompareSep.add(colCompareOverlapID.length, getCommonShortName(mapPrefix2isExist));
			lsResultUnion.add(colCompareSep.toArray(new String[0]));
			if (flagInterSection) {
				lsResultIntersection.add(colCompareSep.toArray(new String[0]));
			}
		}
		writeResult();
	}
	
	/**
	 *  存储 某个prefix独有的元素的数量，譬如 A，B，C三组，A独有30个，B独有27个，C独有23个这种
	 * @param mapPrefix2NumOnly
	 * @param mapPrefix2isExist
	 */
	private void fillMapPrefix2NumOnly(Map<String,Boolean> mapPrefix2isExist) {
		String prefixTmp = null;
		int prefixNum = 0;
		for (String prefix : mapPrefix2isExist.keySet()) {
			boolean isExist = mapPrefix2isExist.get(prefix);
			if (isExist ) {
				prefixTmp = prefix;
				prefixNum++;
			}
		}
		if (prefixNum == 1 && prefixTmp != null) {
			if (mapPrefix2NumOnly.containsKey(prefixTmp)) {
				mapPrefix2NumOnly.put(prefixTmp, mapPrefix2NumOnly.get(prefixTmp) + 1);
			} else {
				mapPrefix2NumOnly.put(prefixTmp, 1);
			}
		}
	}
	
	/** 给定一个 mapKey2mapShortName2Exist，返回有几个shortName是存在的
	 * 
	 * @param mapShortName2isExist key 文件名缩写， value：是否存在
	 * @return
	 */
	private String getCommonShortName(Map<String,Boolean> mapShortName2isExist) {
		String newName = "";
		int i = 0;
		for(String shortName : mapShortName2isExist.keySet()){
			if(mapShortName2isExist.get(shortName)) {
				if(i == 0) {
					newName += shortName;
					i++;
				}else {
					newName += "_"+shortName;
				}
			}
		}
		return newName;
	}
	
	private boolean writeResult() {
		for (String geneId : mapKey2mapShortName2Exist.keySet()) {
			String newName = "";
			int i = 0;
			for(String shortName : mapKey2mapShortName2Exist.get(geneId).keySet()){
				if(mapKey2mapShortName2Exist.get(geneId).get(shortName)){
					if(i == 0){
						newName += shortName;
						i++;
					}else {
						newName += "_"+shortName;
					}
				}
				if(mapShortName2lsGeneID.get(shortName) == null){
					ArrayList<String> lsGeneIds = new ArrayList<String>();
					lsGeneIds.add(geneId);
					mapShortName2lsGeneID.put(shortName, lsGeneIds);
				}else {
					mapShortName2lsGeneID.get(shortName).add(geneId);
				}
			}
			
			lsResultFromImage.add(new String[]{geneId,newName});
		}
		return true;
	}
	
	/** 输入文件只能是jpg或png，最好png */
	public void renderScriptAndDrawImage(String savePath, String title, String subTitle) {
		String tmpFile = tempFolder + FileOperate.getFileNameSep(savePath)[0] + DateUtil.getDateAndRandom() + ".tiff";
		String scriptPath = FileOperate.getPathName(savePath);
		scriptPath = scriptPath + "script/";
		FileOperate.DeleteFileFolder(scriptPath);
		FileOperate.createFolders(scriptPath);
		
		VennImage vennImage = new VennImage(tmpFile,3000,3000);
		vennImage.setMain(title);
		vennImage.setSub(subTitle);
		renderScriptAndDrawImage(scriptPath, vennImage);
		BufferedImage bufferedImage = ImageUtils.read(tmpFile);
		ImageUtils.saveBufferedImage(bufferedImage, savePath);
	}
	
	/**
	 * 渲染R脚本并画图
	 * @param savePath 只能是tiff格式的
	 * @return
	 */
	private void renderScriptAndDrawImage(String scriptPath, VennImage vennImage) {
		tempFiles.add(vennImage.getSavePath());
		//提供给freemarker的渲染数据集 
		Map<String,Object> mapData = new HashMap<String, Object>();
		Map<String,String> mapShortName2PathName = new HashMap<String, String>();
		TxtReadandWrite txtReadandWrite = null;
		String fileName = null;
		for (String key : mapFileName2ConditionAbbr.keySet()) {
			if (!FileOperate.isFileExistAndBigThanSize(key, 0))
				return;
			fileName = tempFolder + mapFileName2ConditionAbbr.get(key) + DateUtil.getDateAndRandom() + ".txt";
			txtReadandWrite = new TxtReadandWrite(fileName,true);
			for (String[] content : ExcelTxtRead.readLsExcelTxt(key, 2)) {
				String combineContent = content[colCompareOverlapID[0]];
				for (int i = 1; i < colCompareOverlapID.length; i++) {
					combineContent = combineContent + SepSign.SEP_ID + content[colCompareOverlapID[i]];
				}
				txtReadandWrite.writefileln(combineContent);
			}
			fileName = fileName.replace("\\", "/");
			mapShortName2PathName.put(mapFileName2ConditionAbbr.get(key), fileName);
			txtReadandWrite.flush();
			tempFiles.add(fileName);
		}
		vennImage.setDataSize(mapShortName2PathName.size());
		mapData.put("data", mapShortName2PathName);
		mapData.put("vennImage", vennImage);
//		// 加载模板
//		Configuration cf = new Configuration();
//		// 模板存放路径
//		try {
//			cf.setDirectoryForTemplateLoading(new File(rootTemp));
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		cf.setClassicCompatible(true);
//		cf.setEncoding(Locale.getDefault(), "UTF-8");
		String scriptName = null;
		try {
			Template template = freeMarkerConfiguration.getTemplate("/R/Venn.ftl");
			StringWriter sw = new StringWriter();
			// 处理并把结果输出到字符串中
			template.process(mapData, sw);
			scriptName = tempFolder + "script" + DateUtil.getDateAndRandom()+".txt";
			txtReadandWrite = new TxtReadandWrite(scriptName,true);
			txtReadandWrite.writefile(sw.toString());
			tempFiles.add(scriptName);
		} catch (Exception e) {
			logger.error("render error! ", e);
			deleteAllTempFile();
			throw new RuntimeException("render error! ", e);
		} finally {
			txtReadandWrite.close();
		}
		//TODO		String cmd = PathNBCDetail.getRscript() + scriptName.replace("\\", "/");
		
		FileOperate.copyFile(scriptName, scriptPath + FileOperate.getFileName(scriptName), true);
		
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(PathDetail.getRscript());
		lsCmd.add(scriptName.replace("\\", "/"));
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.runWithExp();
	}
	
	/**
	 * 删除除所有的临时文件
	 */
	public void  deleteAllTempFile() {
		for (String fileName : tempFiles) {
			if (!FileOperate.isFileExist(fileName))
				continue;
			FileOperate.delFile(fileName);
		}
	}
	
	public ArrayList<String[]> getResultLsIntersection() {
		exeToFile();
		return lsResultIntersection;
	}
	public ArrayList<String[]> getResultLsUnion() {
		exeToFile();
		return lsResultUnion;
	}
	
	/**
	 * 得到不同GeneID的分布<br>
	 * 如　Gbp1   A_B_AB（表示在A B AB中都出现过）   Pkdcc   A_AB（表示在A AB中出现的）  
	 * @return
	 */
	public ArrayList<String[]> getLsResultFromImage() {
		return lsResultFromImage;
	}
	
	/** 获取样本名，及其对应的基因数 */
	public Map<String, Integer> getMapSample2GeneNum() {
		return mapPrefix2NumOnly;
	}
}
