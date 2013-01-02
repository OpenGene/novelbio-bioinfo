package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs.MapReadsProcessInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListAbs;
import com.novelbio.database.model.species.Species;
/**
 * 输入的mapping结果已经排序好，并且染色体已经分开好。
 * 不考虑内存限制的编
 * 
 * @author zong0jie
 * 
 */
public class MapReads extends MapReadsAbs{
	private static Logger logger = Logger.getLogger(MapReads.class);

	 boolean uniqReads = false;
	 int startCod = -1;
	 boolean booUniqueMapping = true;
	 /** 仅选取某个方向的reads */
	 Boolean FilteredStrand = null;
	 Species species;
	 
	 long readsSize = 0;
	 
	 long allReadsNum = 0;
	 
	 /** 用这个类来依次添加reads */
	 MapReadsAddAlignRecord mapReadsAddAlignRecord;
	 
	 public void setSpecies(Species species) {
		mapChrID2Len = species.getMapChromInfo();
	}
	
	 /** 有时候需要用测序量最大的一个样本的reads数来做标准化
	  * <b>在读取结束后设定</b>
	  * @param allReadsNum
	  */
	 public void setAllReadsNum(long allReadsNum) {
		this.allReadsNum = allReadsNum;
	}
	 /** 总共有多少reads参与了mapping，这个从ReadMapFile才能得到。 */
	public long getAllReadsNum() {
		if (allReadsNum > 0) {
			return allReadsNum;
		}
		for (ChrMapReadsInfo chrMapReadsInfo : mapChrID2ReadsInfo.values()) {
			allReadsNum = allReadsNum + chrMapReadsInfo.getReadsChrNum();
		}
		return allReadsNum;
	}
	
	/**
	 * @param uniqReads 当reads mapping至同一个位置时，是否仅保留一个reads
	 * @param startCod 从起点开始读取该reads的几个bp，韩燕用到 小于0表示全部读取 大于reads长度的则忽略该参数
	 * @param colUnique  Unique的reads在哪一列 novelbio的标记在第七列，从1开始计算
	 * @param booUniqueMapping 重复的reads是否只选择一条
	 * @param FilteredStrand 是否仅选取某一方向的reads，null不考虑
	 */
	public void setFilter(boolean uniqReads, int startCod, boolean booUniqueMapping, Boolean FilteredStrand) {
		this.uniqReads = uniqReads;
		this.startCod = startCod;
		this.booUniqueMapping = booUniqueMapping;
		this.FilteredStrand = FilteredStrand;
	}
	/**
	 * 从这里得到的实际某条染色体所包含的reads书目
	 */
	public long getChrReadsNum(String chrID) {
		return mapChrID2ReadsInfo.get(chrID.toLowerCase()).getReadsChrNum();
	}
	/**
	 * 从这里得到的实际某条染色体的高度总和
	 */
	public long getChrReadsPipNum(String chrID) {
		return mapChrID2ReadsInfo.get(chrID.toLowerCase()).getReadsPipNum();
	}
	/**
	 * 从这里得到的实际某条染色体高度的平均值
	 */
	public double getChrReadsPipMean(String chrID) {
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		return chrMapReadsInfo.getReadsPipNum()/chrMapReadsInfo.chrLength;
	}
	
	protected boolean isUniqueMapping() {
		return booUniqueMapping;
	}
	 /**
	  * 设定标准化方法，可以随时设定，不一定要在读取文件前
	  * 默认是NORMALIZATION_ALL_READS
	  * @param normalType
	  */
	 public void setNormalType(int normalType) {
		NormalType = normalType;
	}
	 
	private void setChrLenFromReadBed() {
		if (mapChrID2Len.size() > 0)
			return;
		
		String chrID = ""; AlignRecord lastAlignRecord = null;
		for (AlignRecord alignRecord : alignSeqReader.readLines()) {
			if (!alignRecord.getRefID().equals(chrID)) {
				if (lastAlignRecord != null) {
					mapChrID2Len.put(chrID.toLowerCase(), (long)lastAlignRecord.getEndAbs());
				}
				chrID = alignRecord.getRefID();
			}
			lastAlignRecord = alignRecord;
		}
		mapChrID2Len.put(lastAlignRecord.getRefID().toLowerCase(), (long)lastAlignRecord.getEndAbs());
	}
	/**
	 * 经过标准化，和equations修正
	 * 给定染色体，与起点和终点，返回该染色体上tag的密度分布，如果该染色体在mapping时候不存在，则返回null
	 * @param chrID 小写
	 * @param startLoc 起点坐标，为实际起点 如果startNum<=0 并且endNum<=0，则返回全长信息
	 * @param endLoc 
	 * @param binNum 待分割的块数
	 * @return
	 */
	public  double[] getReadsDensity(String chrID, int startLoc, int endLoc, int binNum ) {
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum = getRangeInfo(tagBinLength, chrID, startLoc, endLoc,1);
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	
	/**
	 * 给定需要计算的区域，装在ArrayList-ExonInfo里面，返回仅仅考虑这些区域的基因组分布密度图<br>
	 * 给马红那边的杨红星开发的。他提出想看全基因组上tss区域的甲基化分布情况，exon区域的甲基化分布情况。<br>
	 * 他的思路是用一定长度的slide window划过基因组然后看该位点内有甲基化的基因的表达情况。<br>
	 * 那么我的做法就是除了tss区域，其他区域的甲基化全部设定为0，也就是仅保留指定lsExonInfos内的甲基化，然后后面走常规步骤<br>
	 * @param lsExonInfos
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param binNum
	 * @return
	 */
	public  double[] getReadsDensity(ListAbs<ExonInfo> lsExonInfos, String chrID, int startLoc, int endLoc, int binNum ) {
		//首先将reads标准化为一个400-500bp宽的大块，每一块里面应该是该区域里面tags的总数，所以求该区域里面的最大值
		//然后再在大块上面统计，
		//大概估算了一下，基本上宽度在一个tag的1.5倍的时候计数会比较合理
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum = getRangeInfo(tagBinLength, chrID, startLoc, endLoc,1);
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	
	/**
	 * 当输入为macs的bed文件时，自动<b>跳过chrm项目</b><br>
	 * 所有chr项目都小写
	 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。注意，mapping文件中的chrID和chrLengthFile中的chrID要一致，否则会出错
	 * @throws Exception
	 */
	protected void ReadMapFileExp() throws Exception {
		allReadsNum = 0;
		setChrLenFromReadBed();
		AlignRecord alignRecordFirst = alignSeqReader.readFirstLine();
		if (!prepareAlignRecord(alignRecordFirst)) {
			return;
		}
		
		for (AlignRecord alignRecord : alignSeqReader.readLines()) {
			allReadsNum++;
			mapReadsAddAlignRecord.addAlignRecord(alignRecord);
			
			suspendCheck();
			if (flagStop) {
				break;
			}
			readsSize = readsSize + alignRecord.getRawStringInfo().getBytes().length;
			if (allReadsNum%1000 == 0) {
				MapReadsProcessInfo mapReadsProcessInfo = new MapReadsProcessInfo(readsSize);
				setRunInfo(mapReadsProcessInfo);
			}
		}
		mapReadsAddAlignRecord.summary();
	}
	
	/**
	 * 准备添加reads信息。主要是初始化mapReadsAddAlignRecord
	 * 此外就是判定一下startCod是否能用
	 * @param alignRecordFirst
	 * @return 如果设setFilter中定了 startCod > 0 并且reads没有方向
	 * 则返回false
	 */
	public boolean prepareAlignRecord(AlignRecord alignRecordFirst) {
		mapReadsAddAlignRecord = new MapReadsAddAlignRecord(this);
		if (startCod > 0 && alignRecordFirst.isCis5to3() == null) {
			logger.error("不能设定startCod，因为没有设定方向列");
			return false;
		}
		return true;
	}
	
	/**添加samBam的文件用来获得信息
	 * 注意在添加之前要先执行{@link #prepareAlignRecord(AlignRecord)}
	 */
	public void addAlignRecord(AlignRecord alignRecord) {
		mapReadsAddAlignRecord.addAlignRecord(alignRecord);
	}
}

/**
 * 本类的作用就是将reads的信息装入MapReads的mapChrID2ReadsInfo中去。
 * 把这个模块独立出来可以方便一次读取bam文件，然后做好多事情
 * @author zong0jie
 *
 */
class MapReadsAddAlignRecord {
	private static final Logger logger = Logger.getLogger(MapReadsAddAlignRecord.class);
	MapReads mapReads;
	int[] chrBpReads = null;//保存每个bp的reads累计数
	String lastChr="";
	boolean flag = true;// 当没有该染色体时标记为false并且跳过所有该染色体上的坐标
	ChrMapReadsInfo chrMapReadsInfo = null;
	int[] tmpOld = new int[2];//更新 tmpOld
	
	public MapReadsAddAlignRecord(MapReads mapReads) {
		this.mapReads = mapReads;
	}
	
	public void addAlignRecord(AlignRecord alignRecord) {
		String tmpChrID = alignRecord.getRefID().toLowerCase();
		if (!tmpChrID.equals(lastChr)) {
			tmpOld = new int[2];//更新 tmpOld
			summary();
			lastChr = tmpChrID;// 实际这是新出现的ChrID
			logger.error(lastChr);
			
			Long chrLength = mapReads.mapChrID2Len.get(lastChr.toLowerCase());
			flag = true;
			if (chrLength == null) {
				logger.error("出现未知chrID "+lastChr);
				flag = false; return;
			}

			chrBpReads = new int[(int) (chrLength + 1)];// 同样为方便，0位记录总长度。这样实际bp就是实际长度
			chrBpReads[0] = chrLength.intValue();
			chrMapReadsInfo = new ChrMapReadsInfo(lastChr, mapReads);
			mapReads.mapChrID2ReadsInfo.put(lastChr, chrMapReadsInfo);
		}
		//没有该染色体则跳过
		if (flag == false) return;
		tmpOld = addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
		chrMapReadsInfo.addReadsAllNum(1);
	}
	
	public void summary() {
		if (!lastChr.equals("") && flag) {
			chrMapReadsInfo.sumChrBp(chrBpReads);
			chrBpReads = null;
		}
	}
	/**
	 * 具体加和的处理方法
	 * 给定一行信息，将具体内容加到对应的坐标上
	 * @param alignRecord reads信息
	 * @param tmpOld 上一组的起点终点，用于判断是否是在同一位点叠加
	 * @param chrBpReads 具体需要叠加的染色体信息
	 * @param chrMapReadsInfo 记录总共mapping的reads数量，为了能够传递下去，采用数组方式
	 * @return
	 * 本位点的信息，用于下一次判断是否是同一位点
	 */
	protected int[] addLoc(AlignRecord alignRecord, int[] tmpOld, int[] chrBpReads, ChrMapReadsInfo chrMapReadsInfo) {
		boolean cis5to3This = alignRecord.isCis5to3();
		if ((mapReads.FilteredStrand != null && alignRecord.isCis5to3() != mapReads.FilteredStrand)
				|| (mapReads.isUniqueMapping() && alignRecord.getMappingNum() > 1)
				) {
			return tmpOld;
		}
		
		int[] tmpStartEnd = new int[2];
		tmpStartEnd[0] = alignRecord.getStartAbs();
		tmpStartEnd[1] = alignRecord.getEndAbs();

		//如果本reads和上一个reads相同，则认为是线性扩增，跳过
		if (mapReads.uniqReads && tmpStartEnd[0] == tmpOld[0] && tmpStartEnd[1] == tmpOld[1] ) {
			return tmpOld;
		}

		ArrayList<? extends Alignment> lsadd = null;
		//如果没有可变剪接
		lsadd = alignRecord.getAlignmentBlocks();
		lsadd = setStartCod(lsadd, mapReads.startCod, cis5to3This);

		addChrLoc(chrBpReads, lsadd);
		chrMapReadsInfo.readsAllNum = chrMapReadsInfo.readsAllNum + 1;
		return tmpStartEnd;
	}
	/**
	 * 根据正反向截取相应的区域，最后返回需要累加的ArrayList<int[]>
	 * @param lsStartEnd
	 * @param cis5to3
	 * @return 如果cis5to3 = True，那么正着截取startCod长度的序列
	 * 如果cis5to3 = False，那么反着截取startCod长度的序列
	 */
	private ArrayList<? extends Alignment> setStartCod(ArrayList<? extends Alignment> lsStartEnd, int StartCodLen, boolean cis5to3) {
		if (StartCodLen <= 0) {
			return lsStartEnd;
		}
		ArrayList<Align> lsResult = new ArrayList<Align>();
		
		if (cis5to3) {
			for (int i = 0; i < lsStartEnd.size(); i++) {
				Alignment alignment = lsStartEnd.get(i);
				if (StartCodLen - lsStartEnd.get(i).Length() > 0) {
					Align align = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					lsResult.add(align);
					StartCodLen = StartCodLen - alignment.Length();
				}
				else {
					Align lastAlign = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getStartAbs() + StartCodLen - 1);
					lsResult.add(lastAlign);
					break;
				}
			}
		}
		else {
			for (int i = lsStartEnd.size() - 1; i >= 0; i--) {
				Alignment alignment = lsStartEnd.get(i);
				if (StartCodLen - alignment.Length() > 0) {
					Align align = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					
					lsResult.add(0,align);
					StartCodLen = StartCodLen - alignment.Length();
				}
				else {
					Align align = new Align(alignment.getRefID(), alignment.getEndAbs() - StartCodLen + 1, alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					lsResult.add(0,align);
				}
			}
		}
		return lsResult;
	}
	/**
	 * 给定一条序列的坐标信息，以及本次需要累加的坐标区域
	 * 将该区域的坐标累加到目的坐标上去
	 * @param chrLoc 坐标位点，0为坐标长度，1开始为具体坐标，所以chrLoc[123] 就是实际123位的坐标
	 * @param lsAddLoc 间断的坐标区域，为int[2] 的list，譬如 100-250，280-300这样子，注意提供的坐标都是闭区间，所以首位两端都要加上
	 */
	private void addChrLoc(int[] chrLoc, ArrayList<? extends Alignment> lsAddLoc) {
		for (Alignment is : lsAddLoc) {
			for (int i = is.getStartAbs(); i <=is.getEndAbs(); i++) {
				if (i >= chrLoc.length) {
					logger.info("超出范围："+ i);
					break;
				}
				if (i < 0) {
					logger.info("超出范围："+ i);
					continue;
				}
				chrLoc[i]++;
			}
		}
	}
}
