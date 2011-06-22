package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;

/**
 * ׼������mapping��fastQ�ļ�
 * @author zong0jie
 *
 */
public abstract class Mapping extends FastQ{
	
	public Mapping(String seqFile1, int QUALITY) {
		super(seqFile1, QUALITY);
		// TODO Auto-generated constructor stub
	}
	public Mapping(String seqFile1,String seqFile2, int QUALITY) {
		super(seqFile1, seqFile2, QUALITY);
		// TODO Auto-generated constructor stub
	}
	public Mapping(String seqFile1,String seqFile2, int FastQFormateOffset,int QUALITY) {
		super(seqFile1, seqFile2, FastQFormateOffset, QUALITY);
		// TODO Auto-generated constructor stub
	}
	/**
	 * ����seqFile����mapping��������mapping֮ǰ��Ҫ���й��˴���
	 * @param fileName �����ļ���
	 * ʵ���� fileName+"_Treat_SoapMap";
	 * @return ����reads��������Ҳ���ǲ�������<b>˫�˵Ļ�������2</b>
	 */
	public abstract BedSeq mapReads();
	
}
