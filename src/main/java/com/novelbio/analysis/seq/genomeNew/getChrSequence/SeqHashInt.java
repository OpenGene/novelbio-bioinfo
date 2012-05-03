package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;

public interface SeqHashInt {
	/**
	 * ����chrID��chrLength�Ķ�Ӧ��ϵ
	 * chrIDͨͨСд
	 * @return
	 */
	public HashMap<String, Long> getHashChrLength();
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻������chr�ĳ�����Ϣ
	 * 
	 * @param chrID
	 * @return ArrayList<String[]> 0: chrID 1: chr���� ���Ұ���chr���ȴ�С��������
	 */
	public ArrayList<String[]> getChrLengthInfo();
	
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
	 * @param chrID �ڲ��Զ�ת��ΪСд
	 * @return
	 */
	public long getChrLength(String chrID) ;
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
	 * @param chrID �ڲ��Զ�ת��ΪСд
	 * @return
	 */
	public long getChrLenMin() ;
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
	 * @param chrID �ڲ��Զ�ת��ΪСд
	 * @return
	 */
	public long getChrLenMax() ;
	/**
	 * ָ���Ⱦɫ���ֵ�����ذ�����ÿ��Ⱦɫ����Ӧֵ��Ⱦɫ�����������,resolution��int[resolution]�������ڻ�ͼ
	 * ��ôresolution���Ƿ��ص�int[]�ĳ���
	 * 
	 * @param chrID
	 * @param maxresolution
	 */
	public int[] getChrRes(String chrID, int maxresolution) throws Exception ;
	
	/**
	 * ���趨Chr�ļ��󣬿��Խ����г���������ļ� ����ļ�Ϊ chrID(Сд)+��\t��+chrLength+���� ����˳�����
	 * 
	 * @param outFile
	 *            ��������ļ���������ȫ��·��
	 * @throws IOException
	 */
	public void saveChrLengthToFile(String outFile) ;
	public SeqFasta getSeq(String chrID, long startlocation, long endlocation);
	
	
	/**
	 * * ����Ⱦɫ��list��Ϣ �������������Լ��Ƿ�Ϊ���򻥲�,����ChrIDΪ chr1��chr2��chr10���� ��������
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param cisseq
	 *            ������
	 * @param chrID
	 *            Ŀ��Ⱦɫ�����ƣ������ڹ�ϣ���в��Ҿ���ĳ��Ⱦɫ��
	 * @param startlocation
	 *            �������
	 * @param endlocation
	 *            �����յ�
	 * @return
	 */
	public SeqFasta getSeq(boolean cisseq, String chrID, long startlocation, long endlocation);
	
	/**
	 * ����peakλ�㣬����ָ����Χ��sequence������CaseChange�ı��Сд
	 * <br>
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param chr
	 *            ,
	 * @param peaklocation
	 *            peak summit������
	 * @param region
	 *            peak���ҵķ�Χ
	 * @param cisseq
	 *            true:������ false�����򻥲���
	 */
	public SeqFasta getSeq(String chr, int peaklocation, int region,
			boolean cisseq);

	/**
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * ����ת¼���ķ������Ǵӻ������5����3����ȡ��
	 * ������Ҫ�˹��趨cisseq
	 * @param cisseq �������Ƿ���Ҫ���򻥲���
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 */
	public SeqFasta getSeq(boolean cisseq, String chrID,List<ExonInfo> lsInfo, boolean getIntron);
	
	/**
	 * ����������е�����
	 * @return
	 */
	public ArrayList<String> getLsSeqName();
	/**
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����<br>
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 * û��Ⱦɫ������г�����Χ�򷵻�null
	 */
	public SeqFasta getSeq(String chrID, List<ExonInfo> lsInfo, boolean getIntron);
	
	/**
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����<br>
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param cisseq �����������������ת¼���Ļ����ϣ��Ƿ���Ҫ���򻥲���
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 * @param chrID
	 * @param cisseq ������
	 * @param start ʵ�ʵڼ���exon
	 * @param end ʵ�ʵڼ���exon
	 * @param lsInfo
	 * @param getIntron �Ƿ��ȡ�ں��ӣ��ں����Զ�Сд
	 * @return
	 */
	SeqFasta getSeq(String chrID, boolean cisseq, int start, int end,
			List<ExonInfo> lsInfo, boolean getIntron);
	/**
	 * ������֮����ʲô�ָ�
	 * @param sep
	 */
	void setSep(String sep);
	/**
	 * �Ƿ�Ҫ�趨ΪDNA��Ҳ���ǽ������е�Uȫ��ת��ΪT
	 */
	public void setDNAseq(boolean isDNAseq);
	
}
