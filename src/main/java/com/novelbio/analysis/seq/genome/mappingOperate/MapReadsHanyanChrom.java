package com.novelbio.analysis.seq.genome.mappingOperate;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;

/**
 * 根据reads是否与基因的方向相一致而进行过滤工作，这个是专门针对韩燕的项目做的分析，
 * 不考虑内存限制的编
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
	 * 准备添加reads信息。主要是初始化mapReadsAddAlignRecord
	 * 此外就是判定一下startCod是否能用
	 * @param alignRecordFirst
	 * @return 如果设setFilter中定了 startCod > 0 并且reads没有方向
	 * 则返回false
	 */
	public boolean prepareAlignRecord(AlignRecord alignRecordFirst) {
		mapReadsAddAlignRecord = new MapReadsAddAlignRecordHanyan(this, gffHashGene);
		if (startCod > 0 && alignRecordFirst.isCis5to3() == null) {
			logger.error("不能设定startCod，因为没有设定方向列");
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
	 * 根据reads是否与基因的方向相一致而进行过滤工作，这个是专门针对韩燕的项目做的分析，
	 * 用于当reads mapping至genome上时，仅保留reads与基因方向相同的reads
	 * 给定一行信息，将具体内容加到对应的坐标上
	 * @param tmp 本行分割后的信息
	 * @param uniqReads 同一位点叠加后是否读取
	 * @param tmpOld 上一组的起点终点，用于判断是否是在同一位点叠加
	 * @param startCod 只截取前面一段的长度
	 * @param cis5to3 是否只选取某一个方向的序列，也就是其他方向的序列会被过滤，注意该方向为与gene的方向而不是与refgenome的方向
	 * @param chrBpReads 具体需要叠加的染色体信息
	 * @param readsNum 记录总共mapping的reads数量，为了能够传递下去，采用数组方式
	 * @return
	 * 本位点的信息，用于下一次判断是否是同一位点
	 */
	protected int[] addLoc(AlignRecord alignRecord ,int[] tmpOld, int[] chrBpReads, ChrMapReadsInfo chrMapReadsInfo) {
		//TODO
//		//需要根据方向来筛选reads
//		if (mapReads.FilteredStrand != null) {
//			GffCodGene gffCodGene = gffHashGene.searchLocation(alignRecord.getRefID(), alignRecord.getStartAbs());
//			//如果位点一在基因内，并且reads方向相对于基因的方向与目的相同，则进行加和分析
//			if (gffCodGene.isInsideLoc() 
//					&& mapReads.FilteredStrand == (gffCodGene.getGffDetailThis().isCis5to3() == alignRecord.isCis5to3() ) ) {
//				return super.addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
//			}
//			GffCodGene gffCodGene2 = gffHashGene.searchLocation(alignRecord.getRefID(), alignRecord.getEndAbs());
//			//如果位点二在基因内，并且reads方向相对于基因的方向与目的相同，则进行加和分析
//			if (gffCodGene2.isInsideLoc() 
//					&& mapReads.FilteredStrand == (gffCodGene2.getGffDetailThis().isCis5to3() == alignRecord.isCis5to3() ) ) {
//				return super.addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
//			}
//			
//			if (!gffCodGene.isInsideLoc() && !gffCodGene2.isInsideLoc()) {
//				return super.addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
//			}
//			return tmpOld;
//		} else {
//			return super.addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
//		}
		
		return null;
	}
}
