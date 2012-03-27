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
	 * ��ȡ�����ļ���Ҳ����������genome���еĶ�Ӧ�ļ�
	 */
	int taxID = 0;
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	static HashMap<Integer, String> hashBWA = new HashMap<Integer, String>();
	String outPathAndFileNamePrix = "";
	/**
	 * ����ļ�·�����ļ���
	 * @param outPathAndFileNamePrix
	 */
	public void setOutPathAndFileNamePrix(String outPathAndFileNamePrix) {
		this.outPathAndFileNamePrix = FileOperate.addSep(outPathAndFileNamePrix.trim());
	}
	/**
	 * �ļ�����0���ļ�����1���ļ���prix��2��group
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
	 * ����˫�˻��ߵ��˷��ر��
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
	 * ר�ű���PE�ļ����������SE��ֱ�ӱ�����arraylist��
	 */
	HashMap<String, ArrayList<String[]>> hashGroup2FileNamePrix = new HashMap<String, ArrayList<String[]>>();
	
	/**
	 * ���趨�õ��ļ�����group���з��飬������PE����
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
				logger.error("ͬһ������ֳ����������ļ�");
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
			//TODO:����������ת����ͬ��mapping���
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
