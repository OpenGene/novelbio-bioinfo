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
 * 不考虑内存限制的编
 * @author zong0jie
 *
 */
public class MapReadsHanyanChrom extends MapReads{
	private static Logger logger = Logger.getLogger(MapReadsHanyanChrom.class);
	GffHashGeneAbs gffHashGene = null;
	/**
	 * 
	 * @param invNum 每隔多少位计数
	 * @param chrFilePath 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，<b>文件夹最后无所谓加不加"/"或"\\"</b>
	 * @param mapFile mapping的结果文件，一般为bed格式
	 */
	public MapReadsHanyanChrom(int invNum, String chrFilePath, String mapFile,GffHashGeneAbs gffHashGene) 
	{
		super(invNum, chrFilePath, mapFile, "");
		this.gffHashGene = gffHashGene;
	}

	/**
	 * 根据reads是否与基因的方向相一致而进行过滤工作，这个是专门针对韩燕的项目做的分析
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
	protected int[] addLoc(String[] tmp,boolean uniqReads,int[] tmpOld,int startCod, Boolean cis5to3, int[] chrBpReads, long[] readsNum) {
		
		//需要根据方向来筛选reads
		if (cis5to3 != null) {
			GffCodGene gffCodGene = (GffCodGene) gffHashGene.searchLocation(tmp[colChrID], Integer.parseInt(tmp[colStartNum]));
			//如果位点一在基因内，并且reads方向相对于基因的方向与目的相同，则进行加和分析
			if (gffCodGene.isInsideLoc() && cis5to3 == (gffCodGene.getGffDetailThis().isCis5to3() == tmp[colCis5To3].equals("+")) ) {
				return super.addLoc(tmp, uniqReads, tmpOld, startCod, null, chrBpReads, readsNum);
			}
			GffCodGene gffCodGene2 = (GffCodGene) gffHashGene.searchLocation(tmp[colChrID], Integer.parseInt(tmp[colEndNum]));
			//如果位点二在基因内，并且reads方向相对于基因的方向与目的相同，则进行加和分析
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
