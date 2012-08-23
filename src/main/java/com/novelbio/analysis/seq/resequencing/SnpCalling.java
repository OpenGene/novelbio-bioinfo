package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
/** ����pileup�ļ���snp calling */
public class SnpCalling {
	public final int Homo = 5;
	public final int HetoLess = 10;
	public final int Heto = 20;
	public final int HetoMore = 30;
	
	Logger logger = Logger.getLogger(SNPGATKcope.class);
	GffChrAbs gffChrAbs;
	
	/** ��pileup�ı��л�ȡsnp����Ϣ */
	ArrayList<String[]> lsSample2PileUpFiles = new ArrayList<String[]>();
	HashMap<String, SampleDetail> setSample2SampleDetail = new HashMap<String, SampleDetail>();
	
	/** ���ڶ��������snpȥ����ģ�����key��ʾ��snp���ڵ������Ϣ��value���Ǹ�λ������snp��� */
	HashMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel = new HashMap<String, MapInfoSnpIndel>();
	
	
	
	/** �������������� */
	SnpSampleFilter sampleFilter = new SnpSampleFilter();
	SampleDetail sampleDetail = new SampleDetail();
	/**
	 * @param snpLevel Homo��HetoLess��
	 */
	public void setSnpLevel(int snpLevel) {
		if (snpLevel == Homo) {
			sampleDetail.setSampleRefHomoNum(1, 1);
		}
		else if (snpLevel == HetoLess) {
			sampleDetail.setSampleSnpIndelNum(1, 1);
		}
		else if (snpLevel == Heto) {
			sampleDetail.setSampleSnpIndelNum(1, 1);
			sampleDetail.setSampleSnpIndelHetoLessNum(0, 0);
		}
		else if (snpLevel == HetoMore) {
			sampleDetail.setSampleSnpIndelNum(1, 1);
			sampleDetail.setSampleSnpIndelHetoLessNum(0, 0);
			sampleDetail.setSampleSnpIndelHetoNum(0, 0);
		}
	}
	/** Ҳ����ֱ���趨SampleDetail */
	public void setSampleDetail(SampleDetail sampleDetail) {
		this.sampleDetail = sampleDetail;
	}
	
	public void setSnpFromPileUpFile(String sampleName, SampleDetail sampleDetail, String pileUpfile) {
		setSample2SampleDetail.put(sampleName, sampleDetail);
		lsSample2PileUpFiles.add(new String[]{sampleName, pileUpfile});
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/** 
	 * ����vcf�����Ǵ�pileUp�л�ȡsnp�ķ���
	 * ��pileUp��snp��Ϣ����mapSiteInfo2MapInfoSnpIndel��
	 * ͬʱ����һ��snp����Ϣ��
	 * @param sampleName
	 * @param sampleDetail ���������趨���˵�״̬
	 * @param pileUpFile
	 */
	private void addPileupToLsSnpIndel(String sampleName, SampleDetail sampleDetail, String pileUpFile) {
		String outPutFile = FileOperate.changeFileSuffix(pileUpFile, "_SnpInfo", "txt");
		TxtReadandWrite txtOut = new TxtReadandWrite(outPutFile, true);
		
		TxtReadandWrite txtReadPileUp = new TxtReadandWrite(pileUpFile, false);
		sampleDetail.clearSampleName();
		sampleDetail.addSampleName(sampleName);
		sampleFilter.clearSampleFilterInfo();
		sampleFilter.addSampleFilterInfo(sampleDetail);
		int snpNum = 0;
		int allNum = 0;
		for (String pileupLines : txtReadPileUp.readlines()) {
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, sampleName);
			mapInfoSnpIndel.setSamToolsPilup(pileupLines);

			if (sampleFilter.isFilterdSnp(mapInfoSnpIndel)) {
				addSnp_2_mapSiteInfo2MapInfoSnpIndel(mapInfoSnpIndel);
				
				ArrayList<String[]> lsInfo = mapInfoSnpIndel.toStringLsSnp();
				for (String[] strings : lsInfo) {
					txtOut.writefileln(strings);
				}
				
				snpNum++;
				if (snpNum %100 == 0) {
					logger.info("�ҵ�" + snpNum + "��snp");
				}
			}
			allNum++;
			if (allNum %100000 == 0) {
				logger.info("ɨ���" + allNum + "��snp");
			}
		}
		txtOut.close();
	}
	
	private void addSnp_2_mapSiteInfo2MapInfoSnpIndel(MapInfoSnpIndel mapInfoSnpIndel) {
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
	
	/** ���趨snp������£���pileup�ļ��л�ȡsnp��Ϣ */
	public void readSnpDetailFromPileUp() {
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
			SampleDetail sampleDetail = setSample2SampleDetail.get(sample2PileupFile[0]);
			addPileupToLsSnpIndel(sample2PileupFile[0], sampleDetail, sample2PileupFile[1]);
		}
	}

}
