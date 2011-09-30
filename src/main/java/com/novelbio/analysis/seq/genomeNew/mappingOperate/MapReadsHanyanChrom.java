package com.novelbio.analysis.seq.genomeNew.mappingOperate;


import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.ChrStringHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGeneAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;

/**
 * �������ڴ����Ƶı�
 * @author zong0jie
 *
 */
public class MapReadsHanyanChrom extends MapReads{
	private static Logger logger = Logger.getLogger(MapReadsHanyanChrom.class);
	GffHashGeneAbs gffHashGene = null;
	/**
	 * 
	 * @param invNum ÿ������λ����
	 * @param chrFilePath ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ��<b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 */
	public MapReadsHanyanChrom(int invNum, String chrFilePath, String mapFile,GffHashGeneAbs gffHashGene) 
	{
		super(invNum, chrFilePath, mapFile, "");
		this.gffHashGene = gffHashGene;
	}

	/**
	 * ����reads�Ƿ������ķ�����һ�¶����й��˹����������ר����Ժ������Ŀ���ķ���
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
	protected int[] addLoc(String[] tmp,boolean uniqReads,int[] tmpOld,int startCod, Boolean cis5to3, int[] chrBpReads, long[] readsNum) {
		
		//��Ҫ���ݷ�����ɸѡreads
		if (cis5to3 != null) {
			GffCodGene gffCodGene = (GffCodGene) gffHashGene.searchLocation(tmp[colChrID], Integer.parseInt(tmp[colStartNum]));
			//���λ��һ�ڻ����ڣ�����reads��������ڻ���ķ�����Ŀ����ͬ������мӺͷ���
			if (gffCodGene.isInsideLoc() && cis5to3 == (gffCodGene.getGffDetailThis().isCis5to3() == tmp[colCis5To3].equals("+")) ) {
				return super.addLoc(tmp, uniqReads, tmpOld, startCod, null, chrBpReads, readsNum);
			}
			GffCodGene gffCodGene2 = (GffCodGene) gffHashGene.searchLocation(tmp[colChrID], Integer.parseInt(tmp[colEndNum]));
			//���λ����ڻ����ڣ�����reads��������ڻ���ķ�����Ŀ����ͬ������мӺͷ���
			if (gffCodGene2.isInsideLoc() && cis5to3 == (gffCodGene2.getGffDetailThis().isCis5to3() == tmp[colCis5To3].equals("+")) ) {
				return super.addLoc(tmp, uniqReads, tmpOld, startCod, null, chrBpReads, readsNum);
			}
			if (!gffCodGene.isInsideLoc() && !gffCodGene2.isInsideLoc()) {
				return super.addLoc(tmp, uniqReads, tmpOld, startCod, null, chrBpReads, readsNum);
			}
			return tmpOld;
		}
		return super.addLoc(tmp, uniqReads, tmpOld, startCod, null, chrBpReads, readsNum);
	}
	
}
