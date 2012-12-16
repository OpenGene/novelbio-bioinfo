package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.domain.geneanno.SepSign;
/** ����pileup�ļ���snp calling */
public class SnpCalling extends RunProcess<SnpFilterDetailInfo>{
	private static Logger logger = Logger.getLogger(SnpCalling.class);

	/** ��Ҫдsnp�Ļ�����Ϣ */
	GffChrAbs gffChrAbs;
	
	/** ��pileup�ı��л�ȡsnp����Ϣ
	 * 0:SamleName
	 * 1:SampleFile
	 * 2:OutputFile
	 *  */
	ArrayList<String[]> lsSample2PileUpFiles = new ArrayList<String[]>();
	
	/** ���ڶ��������snpȥ����ģ�����key��ʾ��snp���ڵ������Ϣ��value���Ǹ�λ������snp��� */
	Map<String, RefSiteSnpIndel> mapSiteInfo2RefSiteSnpIndel = null;
	
	/** �������������� */
	SnpFilter snpFilter = new SnpFilter();
	
	long readLines, readByte;
	/** �ҵ���snp���� */
	int findSnp;
	
	int snpLevel = SnpGroupFilterInfo.Heto;
	
	TxtReadandWrite txtSnpOut;
	
	/** �ҵ���snp�����ֻ�װ��������� */
	public void setMapSiteInfo2RefSiteSnpIndel(Map<String, RefSiteSnpIndel> mapSiteInfo2RefSiteSnpIndel) {
		this.mapSiteInfo2RefSiteSnpIndel = mapSiteInfo2RefSiteSnpIndel;
	}
	
	/** snp���˵ȼ� */
	public void setSnpLevel(int snpLevel) {
		this.snpLevel = snpLevel;
	}
	
	public void setSnp_Hete_Contain_SnpProp_Min(double setSnp_Hete_Contain_SnpProp_Min) {
		snpFilter.setSnp_Hete_Contain_SnpProp_Min(setSnp_Hete_Contain_SnpProp_Min);
	}
	public void setSnp_HetoMore_Contain_SnpProp_Min(double snp_HetoMore_Contain_SnpProp_Min) {
		snpFilter.setSnp_HetoMore_Contain_SnpProp_Min(snp_HetoMore_Contain_SnpProp_Min);
	}
	/**
	 * ���Լ������ļ���ֻҪ��Щ�ļ�����ͬ�Ĺ��˹������
	 * @param sampleName
	 * @param pileUpfile
	 * @param outSnpFile ����ļ�����null��������������Ĭ��
	 */
	public void addSnpFromPileUpFile(String sampleName, String pileUpfile,String outSnpFile) {
		lsSample2PileUpFiles.add(new String[]{sampleName, pileUpfile, outSnpFile});
	}
	/** ��� */
	public void clearSnpFromPileUpFile() {
		lsSample2PileUpFiles.clear();
		readLines = 0;
		readByte = 0;
		findSnp = 0;
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**������KΪ��λ�Ĺ����ļ����ܺͣ�gz���ļ��ͻ�ӱ�����
	 * @return
	 */
	public double getFileSizeEvaluateK() {
		ArrayList<String> lsFileName = new ArrayList<String>();
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
			lsFileName.add( sample2PileupFile[1]);
		} 
		return FileOperate.getFileSizeEvaluateK(lsFileName);
	}
	@Override
	protected void running() {
		filterSnp();
	}
	
	/** ���趨snp������£���pileup�ļ��л�ȡsnp��Ϣ */
	public void filterSnp() {
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
			 notifyGUI(sample2PileupFile[1]);
			
			suspendCheck();
			if (flagStop) {
				break;
			}
			
			String outPutFile = sample2PileupFile[2];
			if (outPutFile != null && outPutFile.trim().equals("")) {
				outPutFile = FileOperate.changeFileSuffix(sample2PileupFile[1], "_SnpInfo", "txt");
			}
			
			addPileupToLsSnpIndel(sample2PileupFile[0], sample2PileupFile[1], outPutFile);
		}
	}
	private void notifyGUI(String pileupFileName) {
		SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
		snpFilterDetailInfo.allLines = readLines;
		snpFilterDetailInfo.allByte = readByte;
		snpFilterDetailInfo.showMessage = "reading file " + FileOperate.getFileName(pileupFileName);
		setRunInfo(snpFilterDetailInfo);
	}
	/** 
	 * ����vcf�����Ǵ�pileUp�л�ȡsnp�ķ���
	 * ��pileUp��snp��Ϣ����mapSiteInfo2RefSiteSnpIndel��
	 * ͬʱ����һ��snp����Ϣ��
	 * @param sampleName
	 * @param SnpGroupFilterInfo ���������趨���˵�״̬�����������е�������Ϣû�����壬�ᱻ���
	 * @param pileUpFile
	 */
	private void addPileupToLsSnpIndel(String sampleName, String pileupFile, String outPutFile) {
		if (FileOperate.isFileDirectory(FileOperate.getParentPathName(outPutFile))) {
			txtSnpOut = new TxtReadandWrite(outPutFile, true);
		}
		TxtReadandWrite txtReadPileUp = new TxtReadandWrite(pileupFile, false);
		snpFilter.setSampleFilterInfoSingle(snpLevel);
		for (String pileupLines : txtReadPileUp.readlines()) {
			readLines ++;
			readByte += pileupFile.length();
			////// �м������Ϣ /////////////////////
			suspendCheck();
			if (flagStop) break;
			if (readLines%10000 == 0 ) {
				notifyGUI(readLines, readByte);
			}
			////////////////////////////////////////////////
			RefSiteSnpIndel refSiteSnpIndel = new RefSiteSnpIndel(gffChrAbs, sampleName);
			refSiteSnpIndel.setSamToolsPilup(pileupLines);
			
			ArrayList<SiteSnpIndelInfo> lsFilteredSnp = snpFilter.getFilterdSnp(refSiteSnpIndel);
			if (lsFilteredSnp.size() > 0) {
				if(!writeInFile(refSiteSnpIndel, lsFilteredSnp)) {
					logger.error("���ִ���");
				}
				
				if (mapSiteInfo2RefSiteSnpIndel != null) {
					addSnp_2_mapSiteInfo2RefSiteSnpIndel(refSiteSnpIndel);
				} else {
					refSiteSnpIndel.clear();
				}
			} else {
				refSiteSnpIndel.clear();
			}
			refSiteSnpIndel = null;
		}
		
		if (txtSnpOut != null) {
			txtSnpOut.close();
		}
	}
	
	private void notifyGUI(long readLines, long readByte) {
		SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
		snpFilterDetailInfo.allLines = readLines;
		snpFilterDetailInfo.allByte = readByte;
		setRunInfo(snpFilterDetailInfo);
		logger.info("readLines:" + readLines);
	}
	
	/** �����װ���ϣ������ */
	private void addSnp_2_mapSiteInfo2RefSiteSnpIndel(RefSiteSnpIndel refSiteSnpIndel) {
		String key = refSiteSnpIndel.getRefID() + SepSign.SEP_ID + refSiteSnpIndel.getRefSnpIndelStart();
		if (mapSiteInfo2RefSiteSnpIndel.containsKey(key)) {
			RefSiteSnpIndel maInfoSnpIndelExist = mapSiteInfo2RefSiteSnpIndel.get(key);
			maInfoSnpIndelExist.addAllenInfo(refSiteSnpIndel);
		}
		else {
			mapSiteInfo2RefSiteSnpIndel.put(key, refSiteSnpIndel);
			
			int snpNum = mapSiteInfo2RefSiteSnpIndel.size();
			if (snpNum % 10000 == 0) {
				logger.info("tree map size: "+ snpNum);
			}
		}
	}
	
	private boolean writeInFile(RefSiteSnpIndel refSiteSnpIndel, ArrayList<SiteSnpIndelInfo> lsFilteredSnp) {
		if (txtSnpOut == null) {
			return true;
		}
		ArrayList<String[]> lsInfo = refSiteSnpIndel.toStringLsSnp(lsFilteredSnp);
		if (lsInfo.size() == 0) {
			logger.error("error");
		}
		for (String[] strings : lsInfo) {
			txtSnpOut.writefileln(strings);
			return true;
		}
		
		return false;
	}

}


