package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;
import com.novelbio.analysis.seq.resequencing.SnpSampleFilter.SnpIndelHomoHetoType;
/** ĳһ�������Ĺ�����Ϣ
 * �������ٶ����Ӻ�snp���������Ӻ�snp�ȵ�
 *  */
public class SampleDetail {
	ArrayList<String> lsSampleName = new ArrayList<String>();
	int SnpIndelHomoNumMin = -1;
	int SnpIndelHomoNumMax = -1;
	
	int SnpIndelHetoNumMin = -1;
	int SnpIndelHetoNumMax = -1;
	
	int RefHomoMin = -1;
	int RefHomoMax = -1;
	
	int SnpIndelAllMin = -1;
	int snpIndelAllMax = -1;
	/** δ֪�����͵��������ó����ñ��� */
	double unKnownProp = 0.3;
	
	int ThisSnpIndelHomoNum = 0;
	int ThisSnpIndelHetoNum = 0;
	int ThisRefHomo = 0;
	int ThisSnpIndelAll = 0;
	double ThisUnKnownSite = 0;
	/** �趨�������� */
	public void addSampleName(String sampleName) {
		this.lsSampleName.add(sampleName);
	}
	/** λ��Ϊ����λ������������������� */
	public void setSampleRefHomoNum(int refHomoMin, int refHomoMax) {
		this.RefHomoMin = Math.min(refHomoMin, refHomoMax);
		this.RefHomoMax = Math.max(refHomoMin, refHomoMax);
	}
	/** λ��Ϊ�Ӻ�snpIndel������������������ */
	public void setSampleSnpIndelHetoNum(int snpIndelHetoNumMin, int snpIndelHetoNumMax) {
		this.SnpIndelHetoNumMin = Math.min(snpIndelHetoNumMin, snpIndelHetoNumMax);
		this.SnpIndelHetoNumMax = Math.max(snpIndelHetoNumMin, snpIndelHetoNumMax);
	}
	public void setSampleSnpIndelHomoNum(int snpIndelHomoNumMin, int snpIndelHomoNumMax) {
		this.SnpIndelHomoNumMin = Math.min(snpIndelHomoNumMin, snpIndelHomoNumMax);
		this.SnpIndelHomoNumMax = Math.max(snpIndelHomoNumMin, snpIndelHomoNumMax);
	}
	/** ���ܴ��ϻ����Ӻϣ��ۼ�λ������� */
	public void setSampleSnpIndelNum(int SnpIndelAllMin, int snpIndelAllMax) {
		this.SnpIndelAllMin = Math.min(SnpIndelAllMin, snpIndelAllMax);
		this.snpIndelAllMax = Math.max(SnpIndelAllMin, snpIndelAllMax);
	}
	/** δ֪�����͵��������ó����ñ�����Ĭ��0.3 */
	public void setSampleUnKnownProp(double unKnownProp) {
		this.unKnownProp = unKnownProp;
	}
	/** ��¶�����������ԣ���ֵ���㣬����������Ҳ�ᱻ��� */
	public void clearAll() {
		lsSampleName.clear();
		ThisSnpIndelHomoNum = 0;
		ThisSnpIndelHetoNum = 0;
		ThisRefHomo = 0;
		ThisUnKnownSite = 0;
		ThisSnpIndelAll = 0;
	}
	/** ��¶�����������ԣ���ֵ���㣬����������� */
	public void clearData() {
		ThisSnpIndelHomoNum = 0;
		ThisSnpIndelHetoNum = 0;
		ThisRefHomo = 0;
		ThisUnKnownSite = 0;
		ThisSnpIndelAll = 0;
	}
	/** ��¶�����������ԣ��ۼƲ����� */
	public void addSnpIndelHomoHetoType(SnpIndelHomoHetoType snpIndelHomoHetoType) {
		if (snpIndelHomoHetoType == SnpIndelHomoHetoType.IndelHeto || snpIndelHomoHetoType == SnpIndelHomoHetoType.SnpHeto) {
			ThisSnpIndelHetoNum++; ThisSnpIndelAll++;
		}
		else if (snpIndelHomoHetoType == SnpIndelHomoHetoType.SnpHomo || snpIndelHomoHetoType == SnpIndelHomoHetoType.IndelHomo) {
			ThisSnpIndelHomoNum++; ThisSnpIndelAll++;
		}
		else if (snpIndelHomoHetoType == SnpIndelHomoHetoType.RefHomo) {
			ThisRefHomo++;
		}
		else if (snpIndelHomoHetoType == SnpIndelHomoHetoType.SnpUnKonwn || snpIndelHomoHetoType == SnpIndelHomoHetoType.IndelUnKnown) {
			ThisSnpIndelAll++;
		}
		else if (snpIndelHomoHetoType == SnpIndelHomoHetoType.UnKnown) {
			ThisUnKnownSite++;
		}
	}
	/** ��¶�����������ԣ��Ƿ�ϸ� */
	public boolean isQualified() {
		if (ThisUnKnownSite/lsSampleName.size() >= unKnownProp) {
			return false;
		}
		
		if (compare(4, ThisRefHomo, RefHomoMin) && compare(2, ThisRefHomo, RefHomoMax)
				&&compare(4, ThisSnpIndelHetoNum, SnpIndelHetoNumMin) && compare(2, ThisSnpIndelHetoNum, SnpIndelHetoNumMax)
				&&compare(4, ThisSnpIndelHomoNum, SnpIndelHomoNumMin) && compare(2, ThisSnpIndelHomoNum, SnpIndelHomoNumMax)
				&&compare(4, ThisSnpIndelAll, SnpIndelAllMin) && compare(2, ThisSnpIndelAll, snpIndelAllMax)
				) {
			return true;
		}
		return false;
	}
	/** 
	 * @param compareType<br>
	 *  1��С�� <br>
	 *  2 С�ڵ��� <br>
	 *  3������ <br>
	 *  4 ���ڵ��� <br>
	 *  5������
	 * @param thisNum ����
	 * @param compareNum ��һ��
	 * @return
	 */
	private boolean compare(int compareType, int thisNum, int compareNum) {
		if (compareNum < 0) {
			return true;
		}
		if (compareType == 1) {
			return thisNum < compareNum;
		}
		else if (compareType == 2) {
			return thisNum <= compareNum;
		}
		else if (compareType == 3) {
			return thisNum == compareNum;
		}
		else if (compareType == 4) {
			return thisNum >= compareNum;
		}
		else if ( compareType == 5) {
			return thisNum > compareNum;
		}
		return false;
	}
}
