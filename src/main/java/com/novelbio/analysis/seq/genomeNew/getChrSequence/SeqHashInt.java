package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public interface SeqHashInt {
	/**
	 * �趨������Ϣ
	 * @param CaseChange �Ƿ�������ת��ΪСд
	 * @param regx ��������������ʽ���ڶ�ȡChromFa�ļ���ʱʹ�ã�����ץȡ�ļ����е����������ļ���null���趨
	 * ��ȡChr�ļ��е�ʱ��Ĭ���趨�� "\\bchr\\w*"
	 * @param append ��ȡChrID��ʱ��û��
	 * @param chrPattern ����������chr1:1123-4567����ʱ��chr1��ȡ������������ʽ
	 */
	public void setInfo(boolean CaseChange, String regx,boolean append, String chrPattern) ;
	
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
	 * �����ȡ�ļ�
	 */
	public void setFile();
	
	/**
	 * ���趨Chr�ļ��󣬿��Խ����г���������ļ� ����ļ�Ϊ chrID(Сд)+��\t��+chrLength+���� ����˳�����
	 * 
	 * @param outFile
	 *            ��������ļ���������ȫ��·��
	 * @throws IOException
	 */
	public void saveChrLengthToFile(String outFile) ;
	public String getSeq(String chrID, long startlocation, long endlocation) throws IOException ;
	
	
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
	public String getSeq(boolean cisseq, String chrID, long startlocation, long endlocation);

	/**
	 * ����Ⱦɫ����λ�úͷ��򷵻�����<br>
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param chrlocationȾɫ���ŷ�����
	 *            ��Chr:1000-2000,�Զ���chrIDСд,chrID����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд
	 * @param cisseq����
	 *            ��true:���� false:���򻥲�
	 */
	public String getSeq(String chrlocation, boolean cisseq);

	/**
	 * ����peakλ�㣬����ָ����Χ��sequence,chr����������ʽץȡ������ν��Сд�����Զ�ת��ΪСд
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

	public String getSeq(String chr, int peaklocation, int region,
			boolean cisseq);

	/**
	 * �������У��������ձ� ��÷��򻥲�����
	 */
	public String resCompSeq(String sequence,
			HashMap<Character, Character> complementmap);
	/**
	 * <br>
	 * ��ȡ����Ϊ�����䣬�������ȡ30-40bp��ôʵ����ȡ���Ǵ�30��ʼ��40������11�����
	 * @param cisseq ������
	 * @param lsInfo ArrayList-int[] ������ת¼����ÿһ����һ��������
	 * @param getIntron �Ƿ���ȡ�ں�������True���ں���Сд�������Ӵ�д��False��ֻ��ȡ������
	 */
	public String getSeq(boolean cisseq, String chrID,ArrayList<int[]> lsInfo, boolean getIntron);
	

}
