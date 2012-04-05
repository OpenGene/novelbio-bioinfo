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
	 * �����ݽ���log2�任
	 */
	public static final String DATA_CONVERT_LOG2 = "log2";
	/**
	 * �������ݽ��б任
	 */
	public static final String DATA_CONVERT_NONE = "none";
	/**
	 * ����������Ϣ��д��浵�õ�
	 */
	String infoDifGene = "";
	public void setInfoDifGene(String infoDifGene) {
		this.infoDifGene = infoDifGene;
	}
	
	private String txtTmpNormData = NovelBioConst.R_WORKSPACE_MICROARRAY_NORMDATA_TMP;
	HashMap<String,String> hashRawData = new LinkedHashMap<String, String>();
	ArrayList<String> lsRawData = new ArrayList<String>();
	/**
	 * ���Դ�Сд
	 * string[2];
	 * 0���ڼ�����Ϣ��������intege
	 * 1���������ڱ��������
	 */
	ArrayList<String[]> lsGroupInfo = new ArrayList<String[]>();
	/**
	 * ���Դ�Сд
	 * ������group1vsgroup2
	 * 0��group1
	 * 1��group2
	 * group�����lsGroupInfo�е�stringһ��
	 */
	ArrayList<String[]> lsCompInfo = new ArrayList<String[]>();
	/**
	 * ��׼�����ݵ��ļ�·��
	 */
	String normDataFileRaw = NovelBioConst.R_WORKSPACE_MICROARRAY_NORMDATA + "raw";
	/**
	 * ��׼�����ݵ��ļ�·��
	 */
	String normDataFile = NovelBioConst.R_WORKSPACE_MICROARRAY_NORMDATA;
	/**
	 * ��׼������
	 */
	String NormType = LimmaAffy.NORM_RMA;
	/**
	 * ̽����
	 */
	int colAccID = 1;
	/**
	 * ̽����
	 */
	public void setColAccID(int colAccID) {
		this.colAccID = colAccID;
	}
	/**
	 * ������ɵĽű���ֱ��д��txt�ͺ�
	 */
	ArrayList<String> lsScript = null;
	/**
	 * Ĭ�ϲ����б任
	 */
	String dataConvertType = DATA_CONVERT_NONE;
	/**
	 * �Ա�׼�������ݽ��к��ֱ任
	 * Ĭ�ϲ����б任
	 * ���Բ���DATA_CONVERT_LOG2�ȷ���
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
	 * ��ñ�׼���õ��ļ�����
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
	 * ���Դ�Сд
	 * ������group1vsgroup2
	 * 0��group1
	 * 1��group2
	 * group�����lsGroupInfo�е�stringһ��
	 */
	public void setLsCompInfo(ArrayList<String[]> lsCompInfo) {
		this.lsCompInfo = lsCompInfo;
	}
	/**
	 * ���Դ�Сд
	 * string[2];
	 * 0���ڼ�����Ϣ��ʵ���С�������intege
	 * 1���������ڱ��������
	 */
	public void addCompInfo(String group1, String group2) {
		lsCompInfo.add(new String[]{group1, group2});
	}
	/**
	 * ������
	 * @param columnNum
	 * @param groupName
	 */
	public void clearCompInfo() {
		lsCompInfo.clear();
	}
	
	
	/**
	 * ������׼�������ݣ���д���ı�������ȡ�ڴ�
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
	 * ������׼�������ݣ���д���ı�������ȡ�ڴ�
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
	 * ���Դ�Сд
	 * string[2];
	 * 0���ڼ�����Ϣ��ʵ���С�������intege
	 * 1���������ڱ��������
	 */
	public void setLsGroupInfo(ArrayList<String[]> lsGroupInfo) {
		this.lsGroupInfo = lsGroupInfo;
	}
	/**
	 * ���Դ�Сд
	 * string[2];
	 * 0���ڼ�����Ϣ��ʵ���С�������intege
	 * 1���������ڱ��������
	 */
	public void addGroupInfo(int columnNum, String groupName) {
		lsGroupInfo.add(new String[]{columnNum + "", groupName});
	}
	/**
	 * ������
	 * @param columnNum
	 * @param groupName
	 */
	public void clearGroupInfo() {
		lsGroupInfo.clear();
	}
	/**
	 * �Ƿ���Ҫ��׼�����ݣ�ע��ֻ��ԭʼ�ļ����ڲ��ܱ�׼������
	 */
	boolean booNormRawData = false;
	/**
	 * ѡ���׼�������ͣ���NORM_RMA����ѡ��
	 * @param normType
	 */
	public void setNormType(String normType) {
		this.NormType = normType;
	}
	
	/**
	 * ������׼����R�ű�
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
	 * ���ɶ�ȡcel�ļ���script<br>
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
	 * ������ѡ��������script <br>
	 * eset=read.table(file=\""+txtTmpNormData+"\",he=T,sep=\"\\t\",row.names=1)<br>
	 * eset = log2(eset)<br>
	#��ͨt����<br>
	#design = model.matrix(~ -1+factor (c(1,1,2,2,3,3)))  #-1����ƾ�����ȥ���ؾ࣬ factor�������е����ӣ�Ҳ���ǱȶԵ�оƬ��ͬ�������ִ����ظ�<br>
	design = model.matrix(~ -1+factor (c(rep(1,15),rep(2,41))))<br>
	colnames(design) = c("H","S") #����оƬ��,оƬ�����������֣�����Ϊa9522<br>

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
	 * �����ȽϷ����Ľű�
	contrast.matrix = makeContrasts( HvsS = H - S,levels=design) <br>
	#�Ƚ��뵼��<br>
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
	 * write.table(topTable(fit2.eBayes, coef="HvsS", adjust="fdr", sort.by="B", number=50000),  file="HvsS.xls", row.names=F, sep="\t")  #���AF7��9522�Ľ��,���Ƚϵ�����ֻ��һ��ʱ������дcoef
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
	 * �����ƱȽϾ��󣬲�������д����ʱNormlization�ļ��У��Դ����з�
	 * 0: design = model.matrix(~ -1+factor (1,1,1,2,2,2))
	 * 1: colnames(design) = c("H","S")
	 * @return
	 */
	private String[] getDesign()
	{
		String scriptDesign = "design = model.matrix(~ -1+factor(c(";
		String scriptColName = "colnames(design) = c(";
		//�����н�������
		Collections.sort(lsGroupInfo, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer m1 = Integer.parseInt(o1[0]);
				Integer m2 = Integer.parseInt(o2[0]);
				return m1.compareTo(m2);
			}
		});
		//������Ҫ�Ƚϵ���д����ʱ�ļ���
		setTmpNormDataTxtFile(colAccID, txtTmpNormData);
		//ȷ����ʵ�����--Ʃ��WT��������groupID֮��Ĺ�ϵ
		//������ʱ�ļ������з���
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
			//������������Ӧ��ID
			groupID = hashName2GroupID.get(strings[1]);
			scriptDesign = scriptDesign +","+ groupID;
		}
		scriptDesign = scriptDesign + ")))"+TxtReadandWrite.huiche;
		//���λ��ÿ�����������
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
	 * �������ȶ�lsGroupInfo��������
	 * ��ָ������������������к󣬽���Щ����д�������ļ���д��txt�ı�
	 * @return
	 */
	private void setTmpNormDataTxtFile(int colNameID, String txtFileName)
	{
		int[] columnID = new int[lsGroupInfo.size()+1];
		columnID[0] = colNameID;
		//�����Ҫ���������
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
	 * ����׼������д���ı���script
	 * write.exprs(esetOld, file = "adfs")
	 */
	private String scriptWriteNormData()
	{
		String script = "write.exprs(esetOld, file=\"" + normDataFileRaw + "\")";
		return script;
	}
	/**
	 * ��ȡ��׼�����ݵĽű�
	 * eset=read.table(file="txtTmpNormData",he=T,sep="\t",row.names=1)";
	 * @return
	 */
	private String scriptReadNormTmpData() {
		String script = "library(limma)" + TxtReadandWrite.huiche;
		script = script +  "eset=read.table(file=\""+txtTmpNormData+"\",he=T,sep=\"\\t\",row.names=1)";
		return script;
	}

}
