package com.novelbio.analysis.microarray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

public class LimmaAffy {
	public static final String NORM_RMA = "RMA";
	public static final String NORM_GCRMA = "GCRMA";
	/**
	 * 对数据进行log2变换
	 */
	public static final String DATA_CONVERT_LOG2 = "log2";
	/**
	 * 不对数据进行变换
	 */
	public static final String DATA_CONVERT_NONE = "none";
	/**
	 * 差异基因的信息，写入存档用的
	 */
	String infoDifGene = "";
	public void setInfoDifGene(String infoDifGene) {
		this.infoDifGene = infoDifGene;
	}
	
	private String txtTmpNormData = NovelBioConst.R_WORKSPACE_MICROARRAY_NORMDATA_TMP;
	HashMap<String,String> hashRawData = new LinkedHashMap<String, String>();
	ArrayList<String> lsRawData = new ArrayList<String>();
	/**
	 * 忽略大小写
	 * string[2];
	 * 0：第几列信息，必须是intege
	 * 1：该列属于编组的名称
	 */
	ArrayList<String[]> lsGroupInfo = new ArrayList<String[]>();
	/**
	 * 忽略大小写
	 * 内容是group1vsgroup2
	 * 0：group1
	 * 1：group2
	 * group必须和lsGroupInfo中的string一致
	 */
	ArrayList<String[]> lsCompInfo = new ArrayList<String[]>();
	/**
	 * 标准化数据的文件路径
	 */
	String normDataFileRaw = NovelBioConst.R_WORKSPACE_MICROARRAY_NORMDATA + "raw";
	/**
	 * 标准化数据的文件路径
	 */
	String normDataFile = NovelBioConst.R_WORKSPACE_MICROARRAY_NORMDATA;
	/**
	 * 标准化方法
	 */
	String NormType = LimmaAffy.NORM_RMA;
	/**
	 * 探针列
	 */
	int colAccID = 1;
	/**
	 * 探针列
	 */
	public void setColAccID(int colAccID) {
		this.colAccID = colAccID;
	}
	/**
	 * 最后生成的脚本，直接写入txt就好
	 */
	ArrayList<String> lsScript = null;
	/**
	 * 默认不进行变换
	 */
	String dataConvertType = DATA_CONVERT_NONE;
	/**
	 * 对标准化的数据进行何种变换
	 * 默认不进行变换
	 * 可以采用DATA_CONVERT_LOG2等方法
	 */
	public void setDataConvertType(String dataConvertType) {
		this.dataConvertType = dataConvertType;
	}
	public void addCelFile(String rawDataFile, String prix) {
		lsRawData.add(rawDataFile);
		hashRawData.put(FileOperate.getFileName(rawDataFile), prix);
	}
	public void cleanRawData() {
		lsRawData.clear();
		hashRawData.clear();
	}
	public void setNormDataFile(String normDataFile) {
		this.normDataFile = normDataFile;
	}
	/**
	 * 获得标准化好的文件名称
	 * @return
	 */
	private String getNormDataFile() {
		//TODO
		return normDataFile;
	}
	public static void main(String[] args) {
		LimmaAffy limmaAffy = new LimmaAffy();
//		limmaAffy.setNormDataFile("/media/winE/NBC/Project/Microarray_YL_111012/CEL/fasfees");
		limmaAffy.addCelFile("/media/winF/NBC/Project/Microarray_YL_111012/CEL/090918-HG-U133_Plus_2-09135B_C07201.CEL", "aa");
		limmaAffy.addCelFile("/media/winF/NBC/Project/Microarray_YL_111012/CEL/090918-HG-U133_Plus_2-09135B_C08022.CEL", "bb");
		limmaAffy.addCelFile("/media/winF/NBC/Project/Microarray_YL_111012/CEL/090918-HG-U133_Plus_2-09135B_C08083.CEL", "vv");
		limmaAffy.addCelFile("/media/winF/NBC/Project/Microarray_YL_111012/CEL/090918-HG-U133_Plus_2-09135E_C08195.CEL","sefe");
		ArrayList<String> ls = limmaAffy.generateScriptNorm();
		for (String string : ls) {
			System.out.println(string);
		}
		limmaAffy.normData();
		System.out.println("###################");
		ArrayList<String[]> lsGroupInfo = new ArrayList<String[]>();
		lsGroupInfo.add(new String[]{2+"","aa"});
		lsGroupInfo.add(new String[]{3+"","bb"});
		lsGroupInfo.add(new String[]{4+"","aa"});
		lsGroupInfo.add(new String[]{5+"","bb"});
		limmaAffy.setLsGroupInfo(lsGroupInfo);
		ArrayList<String[]> lsCompInfo = new ArrayList<String[]>();
		
		lsCompInfo.add(new String[]{"aa","bb"});
		limmaAffy.setLsCompInfo(lsCompInfo);
		limmaAffy.difGeneFinder("/media/winE/Bioinformatics/R/Protocol/Microarray");
	}
	/**
	 * 忽略大小写
	 * 内容是group1vsgroup2
	 * 0：group1
	 * 1：group2
	 * group必须和lsGroupInfo中的string一致
	 */
	public void setLsCompInfo(ArrayList<String[]> lsCompInfo) {
		this.lsCompInfo = lsCompInfo;
	}
	/**
	 * 忽略大小写
	 * string[2];
	 * 0：第几列信息，实际列。必须是intege
	 * 1：该列属于编组的名称
	 */
	public void addCompInfo(String group1, String group2) {
		lsCompInfo.add(new String[]{group1, group2});
	}
	/**
	 * 清空设计
	 * @param columnNum
	 * @param groupName
	 */
	public void clearCompInfo() {
		lsCompInfo.clear();
	}
	
	
	/**
	 * 产生标准化的数据，并写入文本，并读取内存
	 * @return
	 */
	public ArrayList<String[]> normData()
	{
		CmdOperate cmdOperate = new CmdOperate(generateScriptNorm());
		cmdOperate.doInBackgroundR(infoDifGene+"MicroarrayLimma");
		ArrayList<String[]> lsGeneInfo = ExcelTxtRead.readLsExcelTxt(normDataFileRaw, 1);
		String[] title = lsGeneInfo.get(0);
		title[0] = "ProbeID";
		for (int i = 1; i < title.length; i++) {
			title[i] = hashRawData.get(title[i]);
		}
		TxtReadandWrite txtWrite = new TxtReadandWrite(normDataFile, true);
		txtWrite.ExcelWrite(lsGeneInfo, "\t", 1, 1);
		return lsGeneInfo;
	}
	/**
	 * 产生标准化的数据，并写入文本，并读取内存
	 * @return
	 */
	public ArrayList<String[]> normData(ArrayList<String[]> lsNormData)
	{
		TxtReadandWrite txtWrite = new TxtReadandWrite(normDataFile, true);
		txtWrite.ExcelWrite(lsNormData, "\t", 1, 1);
		return lsNormData;
	}
	public void difGeneFinder(String outPath)
	{
		ArrayList<String> lsScript = new ArrayList<String>();
		lsScript.add(scriptDifGeneFind());
		lsScript.add(getCompScriptSimple(outPath));
		CmdOperate cmdOperate = new CmdOperate(lsScript);
		cmdOperate.doInBackgroundR("MicroarrayLimmaDifGene");
	}
	/**
	 * 忽略大小写
	 * string[2];
	 * 0：第几列信息，实际列。必须是intege
	 * 1：该列属于编组的名称
	 */
	public void setLsGroupInfo(ArrayList<String[]> lsGroupInfo) {
		this.lsGroupInfo = lsGroupInfo;
	}
	/**
	 * 忽略大小写
	 * string[2];
	 * 0：第几列信息，实际列。必须是intege
	 * 1：该列属于编组的名称
	 */
	public void addGroupInfo(int columnNum, String groupName) {
		lsGroupInfo.add(new String[]{columnNum + "", groupName});
	}
	/**
	 * 清空设计
	 * @param columnNum
	 * @param groupName
	 */
	public void clearGroupInfo() {
		lsGroupInfo.clear();
	}
	/**
	 * 是否需要标准化数据，注意只有原始文件存在才能标准化数据
	 */
	boolean booNormRawData = false;
	/**
	 * 选择标准化的类型，在NORM_RMA等中选择
	 * @param normType
	 */
	public void setNormType(String normType) {
		this.NormType = normType;
	}
	
	/**
	 * 产生标准化的R脚本
	 * library(affy) <br>
	 * library(gcrma)<br>
	 * Data = ReadAffy("fswefse","fse3r")<br>
	 * esetOld =gcrma(Data)<br>
	 * write.exprs(esetOld, file = "adfs")<br>
	 * @return
	 */
	private ArrayList<String> generateScriptNorm() {
		lsScript = new ArrayList<String>();
		lsScript.add("library(affy)");
		lsScript.add("library(gcrma)");
		lsScript.add(scriptReadRawDataCel());
		return lsScript;
	}
	/**
	 * 生成读取cel文件的script<br>
	 * Data = ReadAffy("fswefse","fse3r")  <br>
	esetOld =gcrma(Data)<br>
	write.exprs(esetOld, file = "adfs")
	 * @return
	 */
	private String scriptReadRawDataCel()
	{
		String script = "data = ReadAffy(";
		for (String string : lsRawData) {
			script = script + "\"" + string + "\", ";
		}
		script = script.substring(0, script.length() - 2);
		script = script + ")" + TxtReadandWrite.huiche;
		if (NormType.equals(LimmaAffy.NORM_RMA)) {
			script = script + "esetOld = rma(data)" +TxtReadandWrite.huiche;
		}
		else if (NormType.equals(LimmaAffy.NORM_GCRMA)) {
			script = script + "esetOld = gcrma(data)"+TxtReadandWrite.huiche;
		}
		script = script + scriptWriteNormData();
		return script;
	}
	/**
	 * 生成挑选差异基因的script <br>
	 * eset=read.table(file=\""+txtTmpNormData+"\",he=T,sep=\"\\t\",row.names=1)<br>
	 * eset = log2(eset)<br>
	#普通t检验<br>
	#design = model.matrix(~ -1+factor (c(1,1,2,2,3,3)))  #-1：设计矩阵中去掉截距， factor：所含有的因子，也就是比对的芯片，同样的数字代表重复<br>
	design = model.matrix(~ -1+factor (c(rep(1,15),rep(2,41))))<br>
	colnames(design) = c("H","S") #加上芯片名,芯片名不能是数字，所以为a9522<br>

	 * @return
	 */
	private String scriptDifGeneFind()
	{
		String[] scriptDesignName = getDesign();
		String script = scriptReadNormTmpData();
		if (dataConvertType.equals(DATA_CONVERT_LOG2)) {
			script = "eset = log2(eset)"+TxtReadandWrite.huiche;
		}
		script = script +TxtReadandWrite.huiche + scriptDesignName[0] + scriptDesignName[1];
		
		return script;
	}
	/**
	 * 构建比较方法的脚本
	contrast.matrix = makeContrasts( HvsS = H - S,levels=design) <br>
	#比较与导出<br>
	fit = lmFit(eset, design) <br>
	fit2 = contrasts.fit(fit, contrast.matrix) <br>
	fit2.eBayes = eBayes(fit2) <br>
	write.table(topTable(fit2.eBayes, coef="HvsS", adjust="fdr", sort.by="B", number=50000),  file="HvsS.xls", row.names=F, sep="\t") 

	 * @return
	 */
	private String getCompScriptSimple(String outPath)
	{
		String script = "contrast.matrix = makeContrasts(";
		for (String[] strings : lsCompInfo) {
			script = script + strings[0] + "vs" + strings[1] + "=" + strings[0] + " - " + strings[1] + ",";
		}
		script = script + "levels=design)"+TxtReadandWrite.huiche;
		script = script + "fit = lmFit(eset, design)"+TxtReadandWrite.huiche+ "fit2 = contrasts.fit(fit, contrast.matrix) "+TxtReadandWrite.huiche+"fit2.eBayes = eBayes(fit2)"+TxtReadandWrite.huiche;
		for (String[] strings : lsCompInfo) {
			script = script + getWriteInfo(outPath, strings[0] + "vs" + strings[1]);
		}
		return script;
	}
	/**
	 * write.table(topTable(fit2.eBayes, coef="HvsS", adjust="fdr", sort.by="B", number=50000),  file="HvsS.xls", row.names=F, sep="\t")  #获得AF7对9522的结果,当比较的数据只有一对时，不用写coef
	 * @param compInfo
	 * @return
	 */
	private String getWriteInfo(String outpath, String compInfo)
	{
//		outpath = FileOperate.addSep(outpath);
		outpath = FileOperate.getParentPathName(outpath) + FileOperate.getFileNameSep(outpath)[0];
		String script = "write.table(topTable(fit2.eBayes, coef=\"" + compInfo + "\", adjust=\"fdr\", sort.by=\"B\", number=50000),  file=\""+outpath + compInfo+".xls\", row.names=F, sep=\"\\t\")"+TxtReadandWrite.huiche;
		return script;
	}
	
	/**
	 * 获得设计比较矩阵，并将数据写入临时Normlization文件中，自带换行符
	 * 0: design = model.matrix(~ -1+factor (1,1,1,2,2,2))
	 * 1: colnames(design) = c("H","S")
	 * @return
	 */
	private String[] getDesign()
	{
		String scriptDesign = "design = model.matrix(~ -1+factor(c(";
		String scriptColName = "colnames(design) = c(";
		//按照列进行排序
		Collections.sort(lsGroupInfo, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer m1 = Integer.parseInt(o1[0]);
				Integer m2 = Integer.parseInt(o2[0]);
				return m1.compareTo(m2);
			}
		});
		//仅将需要比较的列写入临时文件中
		setTmpNormDataTxtFile(colAccID, txtTmpNormData);
		//确定的实验分组--譬如WT，与具体的groupID之间的关系
		//根据临时文件来进行分析
		HashMap<String, Integer> hashName2GroupID = new LinkedHashMap<String, Integer>();
		int tmpGroupID = 1;
		for (int i = 0; i < lsGroupInfo.size(); i++) {
			String[] strings = lsGroupInfo.get(i);
			if (hashName2GroupID.containsKey(strings[1].toLowerCase())) {
				continue;
			}
			hashName2GroupID.put(strings[1].toLowerCase(), tmpGroupID);
			tmpGroupID ++;
		}
		int groupID = hashName2GroupID.get(lsGroupInfo.get(0)[1]);
		scriptDesign = scriptDesign + groupID;
		for (int i = 1; i < lsGroupInfo.size(); i++) {
			String[] strings = lsGroupInfo.get(i);
			//分组名称所对应的ID
			groupID = hashName2GroupID.get(strings[1]);
			scriptDesign = scriptDesign +","+ groupID;
		}
		scriptDesign = scriptDesign + ")))"+TxtReadandWrite.huiche;
		//依次获得每个分组的名称
		for (String string : hashName2GroupID.keySet()) {
			scriptColName = scriptColName + "\""+string + "\",";
		}
		scriptColName = scriptColName.substring(0, scriptColName.length()-1);
		scriptColName = scriptColName + ")"+TxtReadandWrite.huiche;
		String[] design = new String[2];
		design[0] = scriptDesign; design[1] = scriptColName;
		return design;
	}
	/**
	 * 必须首先对lsGroupInfo进行排序
	 * 当指定试验的列数、基因列后，将这些内容写入具体的文件，写入txt文本
	 * @return
	 */
	private void setTmpNormDataTxtFile(int colNameID, String txtFileName)
	{
		int[] columnID = new int[lsGroupInfo.size()+1];
		columnID[0] = colNameID;
		//获得需要计算的列数
		for (int i = 0; i < lsGroupInfo.size(); i++)
		{
			String[] strings = lsGroupInfo.get(i);
			columnID[i+1] = Integer.parseInt(strings[0]);
		}
		ArrayList<String[]> lsTmpNormData = ExcelTxtRead.readLsExcelTxt(getNormDataFile(), columnID, 1, -1);
		TxtReadandWrite txtWrite = new TxtReadandWrite(txtFileName, true);
		txtWrite.ExcelWrite(lsTmpNormData, "\t", 1, 1);
	}
	/**
	 * 将标准化数据写入文本的script
	 * write.exprs(esetOld, file = "adfs")
	 */
	private String scriptWriteNormData()
	{
		String script = "write.exprs(esetOld, file=\"" + normDataFileRaw + "\")";
		return script;
	}
	/**
	 * 读取标准化数据的脚本
	 * eset=read.table(file="txtTmpNormData",he=T,sep="\t",row.names=1)";
	 * @return
	 */
	private String scriptReadNormTmpData() {
		String script = "library(limma)" + TxtReadandWrite.huiche;
		script = script +  "eset=read.table(file=\""+txtTmpNormData+"\",he=T,sep=\"\\t\",row.names=1)";
		return script;
	}

}
