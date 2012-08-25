package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.domain.geneanno.SepSign;
/** ����pileup�ļ���snp calling */
public class SnpCalling extends RunProcess<SnpFilterDetailInfo>{
	private static Logger logger = Logger.getLogger(SNPGATKcope.class);

	/** ��Ҫдsnp�Ļ�����Ϣ */
	GffChrAbs gffChrAbs;
	
	/** ��pileup�ı��л�ȡsnp����Ϣ
	 * 0:SamleName
	 * 1:SampleFile
	 * 2:OutputFile
	 *  */
	ArrayList<String[]> lsSample2PileUpFiles = new ArrayList<String[]>();
	
	/** ���ڶ��������snpȥ����ģ�����key��ʾ��snp���ڵ������Ϣ��value���Ǹ�λ������snp��� */
	HashMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel;
	
	/** �������������� */
	SnpFilter snpFilter = new SnpFilter();
	SnpGroupFilterInfo snpGroupFilterInfo = new SnpGroupFilterInfo();
	
	long readLines;
	long readByte;
	int findSnp;
	
	TxtReadandWrite txtSnpOut;
	
	/** �ҵ���snp�����ֻ�װ��������� */
	public void setMapSiteInfo2MapInfoSnpIndel(HashMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel) {
		this.mapSiteInfo2MapInfoSnpIndel = mapSiteInfo2MapInfoSnpIndel;
	}

	/** �趨snpGroupInfoFilter, ������filter�����������Ϣ����Ϊ���ǵ��������Ĺ��˷��� */
	public void setSampleDetail(SnpGroupFilterInfo snpGroupInfoFilter) {
		this.snpGroupFilterInfo = snpGroupInfoFilter;
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
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**������KΪ��λ��
	 * @return
	 */
	public double getFileSizeK() {
		double allFileSize = 0;
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
			String pileUpFile = sample2PileupFile[1];
			allFileSize = FileOperate.getFileSize(pileUpFile);
		} 
		return allFileSize;
	}
	/**������KΪ��λ�Ĺ����ļ����ܺͣ�gz���ļ��ͻ�ӱ�����
	 * @return
	 */
	public double getFileSizeEvaluateK() {
		double allFileSize = 0;
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
			String pileUpFile = sample2PileupFile[1];
			double size = FileOperate.getFileSize(pileUpFile);
			//�����ѹ���ļ��ͼ���Դ�ļ�Ϊ6���� */
			if (FileOperate.getFileNameSep(pileUpFile)[1].toLowerCase().equals("gz")) {
				size = size * 6;
			}
			else {
				size = size * 1.2;
			}
			allFileSize = allFileSize + size;
		} 
		return allFileSize;
	}
	@Override
	protected void running() {
		filterSnp();
	}
	
	/** ���趨snp������£���pileup�ļ��л�ȡsnp��Ϣ */
	public void filterSnp() {
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
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
	private void addPileupToLsSnpIndel(String sampleName, String pileUpFile, String outPutFile) {
		txtSnpOut = new TxtReadandWrite(outPutFile, true);
		TxtReadandWrite txtReadPileUp = new TxtReadandWrite(pileUpFile, false);
		setFilter(sampleName);
		
		for (String pileupLines : txtReadPileUp.readlines()) {
			readLines ++;
			readByte += pileUpFile.length();
			////// �м������Ϣ /////////////////////
			suspendCheck();
			if (flagStop)
				break;
			if (readLines%10000 == 0 ) {
				SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
				snpFilterDetailInfo.allLines = readLines;
				snpFilterDetailInfo.allByte = readByte;
				setRunInfo(snpFilterDetailInfo);
			}
			////////////////////////////////////////////////
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, sampleName);
			mapInfoSnpIndel.setSamToolsPilup(pileupLines);
			
			ArrayList<SiteSnpIndelInfo> lsFilteredSnp = snpFilter.getFilterdSnp(mapInfoSnpIndel);
			if (lsFilteredSnp.size() > 0) {
				addSnp_2_mapSiteInfo2MapInfoSnpIndel(mapInfoSnpIndel);
				writeInFile(mapInfoSnpIndel, lsFilteredSnp);
			}
		}
		
		SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
		snpFilterDetailInfo.allLines = readLines;
		snpFilterDetailInfo.allByte = readByte;
		snpFilterDetailInfo.fileName = pileUpFile;
		setRunInfo(snpFilterDetailInfo);
		
		txtSnpOut.close();
	}
	/** �趨������ */
	private void setFilter(String sampleName) {
		snpGroupFilterInfo.clearSampleName();
		snpGroupFilterInfo.addSampleName(sampleName);
		snpFilter.clearSampleFilterInfo();
		snpFilter.addSampleFilterInfo(snpGroupFilterInfo);
	}
	/** �����װ���ϣ������ */
	private void addSnp_2_mapSiteInfo2MapInfoSnpIndel(MapInfoSnpIndel mapInfoSnpIndel) {
		if (mapSiteInfo2MapInfoSnpIndel == null)
			return;

		String key = mapInfoSnpIndel.getRefID() + SepSign.SEP_ID + mapInfoSnpIndel.getRefSnpIndelStart();
		if (mapSiteInfo2MapInfoSnpIndel.containsKey(key)) {
			MapInfoSnpIndel maInfoSnpIndelExist = mapSiteInfo2MapInfoSnpIndel.get(key);
			maInfoSnpIndelExist.addAllenInfo(mapInfoSnpIndel);
			return;
		}
		else {
			mapSiteInfo2MapInfoSnpIndel.put(key, mapInfoSnpIndel);
		}
	}
	
	private void writeInFile(MapInfoSnpIndel mapInfoSnpIndel, ArrayList<SiteSnpIndelInfo> lsFilteredSnp) {
		if (txtSnpOut == null) {
			return;
		}
		ArrayList<String[]> lsInfo = mapInfoSnpIndel.toStringLsSnp(lsFilteredSnp);
		for (String[] strings : lsInfo) {
			txtSnpOut.writefileln(strings);
		}
	}

}


