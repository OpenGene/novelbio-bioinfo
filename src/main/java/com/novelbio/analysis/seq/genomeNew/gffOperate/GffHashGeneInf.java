package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * ��GffHash��GffHashGene�ķ���ȫ������������
 * @author zong0jie
 *
 */
public interface GffHashGeneInf  {
	
	/**
	 * ���������/geneID�����ػ����������Ϣ��
	 * @param accID
	 * @return
	 */
	public GffDetailGene getGeneDetail(String accID);
	/**
	 * 	�����������ܳ��ȣ��ں����ܳ��ȵ���Ϣ��ֻͳ���ת¼������Ϣ
	 * ������
	 * Ϊһ��ArrayList-Integer
	 * 0: all5UTRLength <br>
	 * 1: all3UTRLength <br>
	 * 2: allExonLength ������5UTR��3UTR�ĳ��� <br> 
	 * 3: allIntronLength <br>
	 * 4: allup2kLength <br>
	 * 5: allGeneLength <br>
	 * @return 
	 */
	public ArrayList<Long> getGeneStructureLength(int upBp);

	/**
	 * ����Ƿ�Ϊ�����䣬������Ϊ�����䣬<br>
	 * False: ���������˼�ǣ�24��ʾ��0��ʼ������24λ��Ҳ����ʵ�ʵ�25λ<br>
	 * True: ���������˼�ǣ�24�ʹ����24λ<br>
	 * UCSC��Ĭ���ļ����յ��Ǳ������
	 */
	public void setEndRegion(boolean region);
	
	
	/**
	 * ����List˳��洢ÿ������Ż���Ŀ�ţ��������������ȡ�������š�
	 * ����ͨ���÷������ĳ��LOC�ڻ����ϵĶ�λ
	 */
	public ArrayList<String> getLOCIDList();
	
	
	/**
	 * ˳��洢ChrHash�е�ID���������ChrHash��ʵ�ʴ洢��ID���������Item���ص��ģ�����"/"������
	 * ��ô��list�е�Ԫ����split("/")�ָ����locHashtable�Ϳ���ȡ��Ӧ��GffDetail��Ŀǰ��Ҫ��Peak�õ�
	 * ˳���ã����Ի��ĳ��LOC�ڻ����ϵĶ�λ��
	 * ����TigrGene��IDÿ������һ��LOCID��Ҳ����˵TIGR��ID����Ҫ�����и��Ȼ����Ҳû��ϵ
	 */
	public ArrayList<String> getLOCChrHashIDList();
	
	/**
	 * ����ĳ��LOCID�����ظ�LOC��ĳ��Ⱦɫ���е�λ����źţ��ڼ�λ<br>
	 * Ҳ����Chrhash��ĳ��chr�¸�LOC��λ��<br>
	 * ��λ�ñ�����ڵ���0��������ǳ���<br>
	 * �ñȽ��������õ���LOCID��locHashtable�����GffDetail�࣬Ȼ����ChrID��Chrhash�л��ĳ��Ⱦɫ���gffdetail��List��Ȼ��Ƚ����ǵ�locString�Լ�����������յ�
	 * ������GffDetail��equal������д��
	 * @param LOCID ����ĳ������
	 * @return string[2]<br>
	 * 0: Ⱦɫ���ţ�chr1,chr2�ȣ���ΪСд<br>
	 * 1:��Ⱦɫ���ϸ�LOC����ţ���1467��
	 */
	public String[] getLOCNum(String LOCID);

}
