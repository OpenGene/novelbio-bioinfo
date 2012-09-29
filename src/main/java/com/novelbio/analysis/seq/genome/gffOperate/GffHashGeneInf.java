package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.genome.GffChrStatistics;
import com.novelbio.database.model.modgeneid.GeneID;
/**
 * ��GffHash��GffHashGene�ķ���ȫ������������
 * @author zong0jie
 *
 */
public interface GffHashGeneInf  {
	/**
	 * ����Ƿ�Ϊ�����䣬������Ϊ�����䣬<br>
	 * False: ���������˼�ǣ�24��ʾ��0��ʼ������24λ��Ҳ����ʵ�ʵ�25λ<br>
	 * True: ���������˼�ǣ�24�ʹ�����24λ<br>
	 * UCSC��Ĭ���ļ����յ��Ǳ������
	 */
	public void setEndRegion(boolean region);
	/**
	 * ����List˳��洢ÿ������Ż���Ŀ�ţ��������������ȡ�������š�
	 * ����ͨ���÷������ĳ��LOC�ڻ����ϵĶ�λ
	 */
	public ArrayList<String> getLsNameNoRedundent();
	/**
	 * ���ض�ȡ��Gff�ļ���
	 * @return
	 */
	public String getGffFilename();
	
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
	/**
	 * ����CopedID�����ػ����������Ϣ��
	 * @param copedID 
	 * @return
	 * û�оͷ���null
	 */
	GffDetailGene searchLOC(GeneID copedID);
	/**
	 * ����LOCID����������Ӧ��ת¼��
	 * û�оͷ����ת¼��
	 * @param LOCID
	 * @return
	 */
	GffGeneIsoInfo searchISO(String LOCID);
	/**
	 * ��ø�ת¼���������ID
	 * @return
	 */
	int getTaxID();

	void writeToGTF(String GTFfile, String title);
	/**
	 * ��һ��Ⱦɫ���е� ���в�ֹһ��ת¼���� ������Ϣд���ı�������GTF��ʽ
	 * Ҳ����˵��������һ��ת¼���Ļ���Ͳ�д���ı���
	 * @param txtWrite
	 * @param lsGffDetailGenes
	 * @param title
	 */
	void writeToGFFIsoMoreThanOne(String GFFfile, String title);
	/**
	 * ��Ҫ����Rsem������һ��GeneID��ӦIsoID�ı�
	 * @param Gene2IsoFile
	 */
	void writeGene2Iso(String Gene2IsoFile);

}