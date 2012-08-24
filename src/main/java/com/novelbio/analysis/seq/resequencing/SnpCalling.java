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
	
	public static final int Homo = 5;
	public static final int HetoLess = 10;
	public static final int Heto = 20;
	public static final int HetoMore = 30;

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
	SnpFilter sampleFilter = new SnpFilter();
	SnpGroupInfoFilter SnpGroupInfoFilter = new SnpGroupInfoFilter();
	
	long readLines;
	long readByte;
	int findSnp;
	
	TxtReadandWrite txtSnpOut;
	
	/** �ҵ���snp�����ֻ�װ��������� */
	public void setMapSiteInfo2MapInfoSnpIndel(HashMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel) {
		this.mapSiteInfo2MapInfoSnpIndel = mapSiteInfo2MapInfoSnpIndel;
	}
	/**
	 * @param snpLevel Homo��HetoLess��
	 */
	public void setSnpLevel(int snpLevel) {
		if (snpLevel == Homo) {
			SnpGroupInfoFilter.setSampleRefHomoNum(1, 1);
		}
		else if (snpLevel == HetoLess) {
			SnpGroupInfoFilter.setSampleSnpIndelNum(1, 1);
		}
		else if (snpLevel == Heto) {
			SnpGroupInfoFilter.setSampleSnpIndelNum(1, 1);
			SnpGroupInfoFilter.setSampleSnpIndelHetoLessNum(0, 0);
		}
		else if (snpLevel == HetoMore) {
			SnpGroupInfoFilter.setSampleSnpIndelNum(1, 1);
			SnpGroupInfoFilter.setSampleSnpIndelHetoLessNum(0, 0);
			SnpGroupInfoFilter.setSampleSnpIndelHetoNum(0, 0);
		}
	}
	/** Ҳ����ֱ���趨SampleDetail */
	public void setSampleDetail(SnpGroupInfoFilter sampleDetail) {
		this.SnpGroupInfoFilter = sampleDetail;
	}
	/**
	 * ���Լ������ļ���ֻҪ��Щ�ļ�����ͬ�Ĺ��˹������
	 * @param sampleName
	 * @param pileUpfile
	 * @param outSnpFile ����ļ�����null��������������Ĭ��
	 */
	public void setSnpFromPileUpFile(String sampleName, String pileUpfile,String outSnpFile) {
		lsSample2PileUpFiles.add(new String[]{sampleName, pileUpfile, outSnpFile});
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
	 * @param SnpGroupInfoFilter ���������趨���˵�״̬�����������е�������Ϣû�����壬�ᱻ���
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
			
			if (sampleFilter.isFilterdSnp(mapInfoSnpIndel)) {
				addSnp_2_mapSiteInfo2MapInfoSnpIndel(mapInfoSnpIndel);
				writeInFile(mapInfoSnpIndel);
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
		SnpGroupInfoFilter.clearSampleName();
		SnpGroupInfoFilter.addSampleName(sampleName);
		sampleFilter.clearSampleFilterInfo();
		sampleFilter.addSampleFilterInfo(SnpGroupInfoFilter);
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
	
	private void writeInFile(MapInfoSnpIndel mapInfoSnpIndel) {
		if (txtSnpOut == null) {
			return;
		}
		ArrayList<String[]> lsInfo = mapInfoSnpIndel.toStringLsSnp();
		for (String[] strings : lsInfo) {
			txtSnpOut.writefileln(strings);
		}
	}

}

class SnpFilterDetailInfo {
	/** ���ж�ȡ���ֽ� */
	long allByte;
	long allLines;
	int findSnp;
	/** ����Ϊnullʱ��ʾ����˸�������snp���� */
	String fileName;
}
