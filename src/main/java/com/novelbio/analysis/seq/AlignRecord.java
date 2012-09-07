package com.novelbio.analysis.seq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.mappingOperate.Alignment;
import com.novelbio.analysis.seq.mapping.Align;

public interface AlignRecord extends Alignment{
	/** �Ƿ�Ϊunique mapping�����ǵĻ�mapping���˼�����ͬ��λ����ȥ */
	public Integer getMappingNum();
	public Integer getMapQuality();
	/** ��bed�ļ��Ƿ񱻸����һ��һ�ε� */
	public boolean isJunctionCovered();
	/** �����mapping��junction��ȥ��һ��bed�ļ���¼�ᱻ�гɱ��гɵļ�������ӱ��������
	 * Ҳ����һ��һ�ε�bed����ô����ÿһ�ε���Ϣ��
	 * ���Ǿ������꣬��1��ʼ
	 * @return
	 */
	public ArrayList<Align> getAlignmentBlocks();
	
	public String getRawStringInfo();
}