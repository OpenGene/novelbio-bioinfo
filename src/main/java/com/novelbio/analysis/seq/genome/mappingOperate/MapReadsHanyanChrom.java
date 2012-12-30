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
	GffHashGeneAbs gffHashGene;
	public void setGffHashGene(GffHashGeneAbs gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	
	/**
	 * ׼�����reads��Ϣ����Ҫ�ǳ�ʼ��mapReadsAddAlignRecord
	 * ��������ж�һ��startCod�Ƿ�����
	 * @param alignRecordFirst
	 * @return �����setFilter�ж��� startCod > 0 ����readsû�з���
	 * �򷵻�false
	 */
	public boolean prepareAlignRecord(AlignRecord alignRecordFirst) {
		mapReadsAddAlignRecord = new MapReadsAddAlignRecordHanyan(this, gffHashGene);
		if (startCod > 0 && alignRecordFirst.isCis5to3() == null) {
			logger.error("�����趨startCod����Ϊû���趨������");
			return false;
		}
		return true;
	}
	

	
}

class MapReadsAddAlignRecordHanyan extends MapReadsAddAlignRecord {
	GffHashGeneAbs gffHashGene = null;
	
	public MapReadsAddAlignRecordHanyan(MapReads mapReads, GffHashGeneAbs gffHashGene) {
		super(mapReads);
		this.gffHashGene = gffHashGene;
	}
	
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
	protected int[] addLoc(AlignRecord alignRecord ,int[] tmpOld, int[] chrBpReads, ChrMapReadsInfo chrMapReadsInfo) {
		//��Ҫ���ݷ�����ɸѡreads
		if (mapReads.FilteredStrand != null) {
			GffCodGene gffCodGene = gffHashGene.searchLocation(alignRecord.getRefID(), alignRecord.getStartAbs());
			//���λ��һ�ڻ����ڣ�����reads��������ڻ���ķ�����Ŀ����ͬ������мӺͷ���
			if (gffCodGene.isInsideLoc() 
					&& mapReads.FilteredStrand == (gffCodGene.getGffDetailThis().isCis5to3() == alignRecord.isCis5to3() ) ) {
				return super.addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
			}
			GffCodGene gffCodGene2 = gffHashGene.searchLocation(alignRecord.getRefID(), alignRecord.getEndAbs());
			//���λ����ڻ����ڣ�����reads��������ڻ���ķ�����Ŀ����ͬ������мӺͷ���
			if (gffCodGene2.isInsideLoc() 
					&& mapReads.FilteredStrand == (gffCodGene2.getGffDetailThis().isCis5to3() == alignRecord.isCis5to3() ) ) {
				return super.addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
			}
			
			if (!gffCodGene.isInsideLoc() && !gffCodGene2.isInsideLoc()) {
				return super.addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
			}
			return tmpOld;
		} else {
			return super.addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
		}
	}
}
