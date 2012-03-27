package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.FastQMapAbs;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;

public class CtrlFastQMapping {
	private static Logger logger = Logger.getLogger(CtrlFastQMapping.class);
	public static final int COPE_FILTERING = 2;
	public static final int COPE_MAPPING = 4;
	public static final int COPE_TOBED = 8;
	public static final int COPE_BEDEXTEND = 8;
	
	public static final int LIBRARY_SINGLE_END = 16;
	public static final int LIBRARY_PAIR_END = 32;
	public static final int LIBRARY_MATE_PAIR = 64;
	
	int libraryType = LIBRARY_SINGLE_END;
	
	int fastqQuality = FastQ.QUALITY_MIDIAN;
	public void setFastqQuality(int fastqQuality) {
		this.fastqQuality = fastqQuality;
	}
	boolean uniqMapping = true;
	public void setUniqMapping(boolean uniqMapping) {
		this.uniqMapping = uniqMapping;
	}
	/**
	 * 读取配置文件，也就是物种与genome序列的对应文件
	 */
	int taxID = 0;
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	static HashMap<Integer, String> hashBWA = new HashMap<Integer, String>();
	String outPathAndFileNamePrix = "";
	/**
	 * 输出文件路径和文件名
	 * @param outPathAndFileNamePrix
	 */
	public void setOutPathAndFileNamePrix(String outPathAndFileNamePrix) {
		this.outPathAndFileNamePrix = FileOperate.addSep(outPathAndFileNamePrix.trim());
	}
	/**
	 * 文件名，0：文件名，1：文件的prix，2：group
	 */
	ArrayList<String[]> lsFileName = new ArrayList<String[]>();
	TreeSet<Integer> treeCopeInfo = new TreeSet<Integer>();
	/**
	 * whether the sequencing data is single end or pair end
	 * @param singleEnd
	 */
	public void setLibraryType(int libraryType) {
		this.libraryType = libraryType;
	}
	
	/**
	 * 根据双端或者单端返回表格
	 * @return
	 */
	public ArrayList<String[]> getlsGetFileInfo()
	{
		GUIFileOpen guiFileOpen = new GUIFileOpen();
		ArrayList<String> lsFileName = guiFileOpen.openLsFileName("fastq file");
		ArrayList<String[]> lsFileResult = new ArrayList<String[]>();
		
		for (String string : lsFileName) {
			String[] tmpResult = null;
			tmpResult = new String[]{string, FileOperate.getFileNameSep(string)[0], "1"};
			lsFileResult.add(tmpResult);
		}
		return lsFileResult;
	}
	
	public void setFile(ArrayList<String[]> lsFile) {
		this.lsFileName = lsFileName;
	}
	/**
	 * add cope type
	 * @param copeInfo such as COPE_FILTERING
	 */
	public void setCopeInfo(int copeInfo) {
		treeCopeInfo.add(copeInfo);
	}
	/**
	 * 专门保存PE文件名，如果是SE，直接保存在arraylist中
	 */
	HashMap<String, ArrayList<String[]>> hashGroup2FileNamePrix = new HashMap<String, ArrayList<String[]>>();
	
	/**
	 * 将设定好的文件按照group进行分组，必须是PE建库
	 */
	private void setFileName() {
		for (String[] strings : lsFileName) {
			String key = strings[2] + "_" + strings[1];
			ArrayList<String[]> lsArrayList = null;
			if (hashGroup2FileNamePrix.containsKey(key)) {
				lsArrayList = hashGroup2FileNamePrix.get(key);
			}
			else {
				lsArrayList = new ArrayList<String[]>();
				hashGroup2FileNamePrix.put(key, lsArrayList);
			}
			lsArrayList.add(strings);
			if (lsArrayList.size() > 2) {
				logger.error("同一分组出现超过两个的文件");
			}
		}

	}
	
	public void running() {
		if (libraryType == LIBRARY_SINGLE_END) {
			for (String[] strings : lsFileName) {
				runSE(taxID, strings[0],  strings[1]);
			}
		}
	}
	
	private void runSE(int taxID, String filePathName, String prix) {
		if (!FileOperate.isFileExist(filePathName)) {
			return;
		}
		FastQ fastQ = null; 
		if (treeCopeInfo.contains(COPE_FILTERING)) {
			fastQ = new FastQ(filePathName, fastqQuality);
			String fileName = FileOperate.getFileNameSep(filePathName)[0];
			fastQ = fastQ.filterReads(outPathAndFileNamePrix + fileName + "_" + prix + ".fq" );
			filePathName = fastQ.getFileName();
		}
		if (treeCopeInfo.contains(COPE_MAPPING)) {
			//TODO:可以在这里转换不同的mapping软件
			fastQ = new FastQMapBwa(filePathName, fastqQuality, FileOperate.changeFileSuffix(filePathName, "_mapping", "sam"), uniqMapping);
			String chrFile = hashBWA.get(taxID).trim();
			((FastQMapAbs)fastQ).setFilePath("", chrFile);
			((FastQMapAbs)fastQ).mapReads();
			if (treeCopeInfo.contains(COPE_TOBED)) {
				String bedFileExtend = FileOperate.changeFileSuffix(fastQ.getFileName(), "Extend", "bed");
				String bedFileSE = FileOperate.changeFileSuffix(fastQ.getFileName(), "SE", "bed");
				((FastQMapAbs)fastQ).getBedFileSE(bedFileSE);
				if (treeCopeInfo.contains(COPE_BEDEXTEND)) {
					((FastQMapAbs)fastQ).getBedFile(bedFileExtend);
				}
			}
		}


	}
	
	
}
