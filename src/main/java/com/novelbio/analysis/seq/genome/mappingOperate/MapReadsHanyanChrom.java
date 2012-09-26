package com.novelbio.analysis.seq.genome.mappingOperate;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;

/**
 * ����reads�Ƿ������ķ�����һ�¶����й��˹����������ר����Ժ������Ŀ���ķ�����
 * �������ڴ����Ƶı�
 * @author zong0jie
 *
 */
public class MapReadsHanyanChrom extends MapReads{
	private static Logger logger = Logger.getLogger(MapReadsHanyanChrom.class);
	GffHashGeneAbs gffHashGene = null;
	/**
	 * ����reads�Ƿ������ķ�����һ�¶����й��˹����������ר����Ժ������Ŀ���ķ�����
	 * ���ڵ�reads mapping��genome��ʱ��������reads���������ͬ��reads
	 * ����һ����Ϣ�����������ݼӵ���Ӧ��������
	 * @param tmp ���зָ�����Ϣ
	 * @param uniqReads ͬһλ����Ӻ��Ƿ��ȡ
	 * @param tmpOld ��һ�������յ㣬�����ж��Ƿ�����ͬһλ�����
	 * @param startCod ֻ��ȡǰ��һ�εĳ���
	 * @param cis5to3 �Ƿ�ֻѡȡĳһ����������У�Ҳ����������������лᱻ���ˣ�ע��÷���Ϊ��gene�ķ����������refgenome�ķ���
	 * @param chrBpReads ������Ҫ���ӵ�Ⱦɫ����Ϣ
	 * @param readsNum ��¼�ܹ�mapping��reads������Ϊ���ܹ�������ȥ���������鷽ʽ
	 * @return
	 * ��λ�����Ϣ��������һ���ж��Ƿ���ͬһλ��
	 */
	protected int[] addLoc(AlignRecord alignRecord ,boolean uniqReads,int[] tmpOld,int startCod, Boolean cis5to3, int[] chrBpReads, ChrMapReadsInfo chrMapReadsInfo) {
		//��Ҫ���ݷ�����ɸѡreads
		if (cis5to3 != null) {
			GffCodGene gffCodGene = gffHashGene.searchLocation(alignRecord.getRefID(), alignRecord.getStartAbs());
			//���λ��һ�ڻ����ڣ�����reads��������ڻ���ķ�����Ŀ����ͬ������мӺͷ���
			if (gffCodGene.isInsideLoc() && cis5to3 == (gffCodGene.getGffDetailThis().isCis5to3() == alignRecord.isCis5to3() ) ) {
				return super.addLoc(alignRecord, uniqReads, tmpOld, startCod, null, chrBpReads, chrMapReadsInfo);
			}
			GffCodGene gffCodGene2 = gffHashGene.searchLocation(alignRecord.getRefID(), alignRecord.getEndAbs());
			//���λ����ڻ����ڣ�����reads��������ڻ���ķ�����Ŀ����ͬ������мӺͷ���
			if (gffCodGene2.isInsideLoc() && cis5to3 == (gffCodGene2.getGffDetailThis().isCis5to3() == alignRecord.isCis5to3() ) ) {
				return super.addLoc(alignRecord, uniqReads, tmpOld, startCod, null, chrBpReads, chrMapReadsInfo);
			}
			
			if (!gffCodGene.isInsideLoc() && !gffCodGene2.isInsideLoc()) {
				return super.addLoc(alignRecord, uniqReads, tmpOld, startCod, null, chrBpReads, chrMapReadsInfo);
			}
			return tmpOld;
		} else {
			return super.addLoc(alignRecord, uniqReads, tmpOld, startCod, null, chrBpReads, chrMapReadsInfo);
		}
	}
	
}
