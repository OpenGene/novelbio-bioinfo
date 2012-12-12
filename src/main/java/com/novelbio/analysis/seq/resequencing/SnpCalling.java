package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
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
	TreeMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel = null;
	
	/** �������������� */
	SnpFilter snpFilter = new SnpFilter();
	
	long readLines, readByte;
	/** �ҵ���snp���� */
	int findSnp;
	
	int snpLevel = SnpGroupFilterInfo.Heto;
	
	TxtReadandWrite txtSnpOut;
	
	/** �ҵ���snp�����ֻ�װ��������� */
	public void setMapSiteInfo2MapInfoSnpIndel(TreeMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel) {
		this.mapSiteInfo2MapInfoSnpIndel = mapSiteInfo2MapInfoSnpIndel;
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
			SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
			snpFilterDetailInfo.allLines = readLines;
			snpFilterDetailInfo.allByte = readByte;
			snpFilterDetailInfo.showMessage = "reading file " + FileOperate.getFileName(sample2PileupFile[1]);
			setRunInfo(snpFilterDetailInfo);
			
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
	/** 
	 * ����vcf�����Ǵ�pileUp�л�ȡsnp�ķ���
	 * ��pileUp��snp��Ϣ����mapSiteInfo2MapInfoSnpIndel��
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
		snpFilter.setSampleFilterInfoSingle(sampleName, snpLevel);
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
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, sampleName);
			mapInfoSnpIndel.setSamToolsPilup(pileupLines);
			
			ArrayList<SiteSnpIndelInfo> lsFilteredSnp = snpFilter.getFilterdSnp(mapInfoSnpIndel);
			if (lsFilteredSnp.size() > 0) {
				if(!writeInFile(mapInfoSnpIndel, lsFilteredSnp)) {
					logger.error("���ִ���");
				}
				
				if (mapSiteInfo2MapInfoSnpIndel != null) {
					addSnp_2_mapSiteInfo2MapInfoSnpIndel(mapInfoSnpIndel);
				} else {
					mapInfoSnpIndel.clear();
				}
			} else {
				mapInfoSnpIndel.clear();
			}
			mapInfoSnpIndel = null;
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
	private void addSnp_2_mapSiteInfo2MapInfoSnpIndel(MapInfoSnpIndel mapInfoSnpIndel) {
		String key = mapInfoSnpIndel.getRefID() + SepSign.SEP_ID + mapInfoSnpIndel.getRefSnpIndelStart();
		if (mapSiteInfo2MapInfoSnpIndel.containsKey(key)) {
			MapInfoSnpIndel maInfoSnpIndelExist = mapSiteInfo2MapInfoSnpIndel.get(key);
			maInfoSnpIndelExist.addAllenInfo(mapInfoSnpIndel);
			return;
		}
		else {
			mapSiteInfo2MapInfoSnpIndel.put(key, mapInfoSnpIndel);
			
			int snpNum = mapSiteInfo2MapInfoSnpIndel.size();
			if (snpNum % 10000 == 0) {
				logger.info("tree map size: "+ snpNum);
			}
		}
	}
	
	private boolean writeInFile(MapInfoSnpIndel mapInfoSnpIndel, ArrayList<SiteSnpIndelInfo> lsFilteredSnp) {
		if (txtSnpOut == null) {
			return true;
		}
		ArrayList<String[]> lsInfo = mapInfoSnpIndel.toStringLsSnp(lsFilteredSnp);
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


