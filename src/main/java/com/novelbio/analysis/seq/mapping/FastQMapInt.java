package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQOld;

public interface FastQMapInt {
	/**
	 * �趨����Ƭ�γ��ȣ�Ĭ����solexa�ĳ��ȣ�150-500
	 */
	public void setInsertSize(int minInsertLen, int maxInsertLen);
	/**
	 * @param exeFile �����ļ�����·��
	 * @param chrFile �����ļ�����·��
	 */
	public void setFilePath(String exeFile, String chrFile);
	/**
	 * ����seqFile����mapping��������mapping֮ǰ��Ҫ���й��˴���
	 * @param fileName �����ļ���
	 * ʵ���� fileName+"_Treat_SoapMap";
	 * @return ����reads��������Ҳ���ǲ�������<b>˫�˵Ļ�������2</b>
	 */
	public SamFile mapReads();
	/**
	 * bwa���õ�
	 * �趨mapping����������bwa�����ã�Ĭ��Ϊ20
	 * һ��30���£�Ҳ�������õ�15����12
	 * @param mapQ
	 */
	public abstract void setMapQ(int mapQ);
}
