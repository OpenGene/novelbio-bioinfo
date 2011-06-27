package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 获得Gff的项目信息<br/>
 * 具体的GffHash需要实现ReadGffarray并通过该方法填满三个表
 * @Chrhash hash（ChrID）--ChrList--GeneInforList(GffDetail类)
 * @locHashtable hash（LOCID）--GeneInforlist
 * @LOCIDList 顺序存储每个基因号或条目号
 */
public abstract class GffHash {

	
	private GffHash() {	}
	
	
	
	public GffHash(String gfffilename) throws Exception {
		ReadGffarray(gfffilename);
	}
	
	/**
	 * 哈希表LOC--LOC细节<br/>
	 * 用于快速将LOC编号对应到LOC的细节
	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的条目编号 <br/>
	 */
	protected Hashtable<String,GffDetailAbs> locHashtable;
	
	/**
	 * 返回哈希表 LOC--LOC细节<br/>
	 * 用于快速将LOC编号对应到LOC的细节
	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的基因编号 <br/>
	 */
	public Hashtable<String,GffDetailAbs> getLocHashtable() {
		return locHashtable;
	}
	
	/**
	 * 这个List顺序存储每个基因号或条目号，这个打算用于提取随机基因号，实际上是所有条目按顺序放入，但是不考虑转录本(UCSC)或是重复(Peak)
	 * 这个ID与locHash一一对应，但是不能用它来确定某条目的前一个或后一个条目
	 */
	protected ArrayList<String> LOCIDList;
	
	/**
	 * 返回List顺序存储每个基因号或条目号，这个打算用于提取随机基因号。
	 * 不能通过该方法获得某个LOC在基因上的定位
	 */
	public ArrayList<String> getLOCIDList() {
		return LOCIDList;
	}
	
	/**
	 * 顺序存储ChrHash中的ID，这个就是ChrHash中实际存储的ID，如果两个Item是重叠的，就用"/"隔开，
	 * 那么该list中的元素用split("/")分割后，上locHashtable就可提取相应的GffDetail，目前主要是Peak用到
	 * 顺序获得，可以获得某个LOC在基因上的定位。
	 * 其中TigrGene的ID每个就是一个LOCID，也就是说TIGR的ID不需要进行切割，当然切了也没关系
	 */
	protected ArrayList<String> LOCChrHashIDList;
	
	/**
	 * 顺序存储ChrHash中的ID，这个就是ChrHash中实际存储的ID，如果两个Item是重叠的，就用"/"隔开，
	 * 那么该list中的元素用split("/")分割后，上locHashtable就可提取相应的GffDetail，目前主要是Peak用到
	 * 顺序获得，可以获得某个LOC在基因上的定位。
	 * 其中TigrGene的ID每个就是一个LOCID，也就是说TIGR的ID不需要进行切割，当然切了也没关系
	 */
	public ArrayList<String> getLOCChrHashIDList() {
		return LOCChrHashIDList;
	}
	
	/**
	 * 这个是真正的查找用hash表<br>
	 * 这个哈希表来存储
	 * hash（ChrID）--ChrList--GeneInforList(GffDetail类)<br>
	 * 其中ChrID为小写，
	 * 代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID
	 * chr格式，全部小写 chr1,chr2,chr11<br>
	 */
	protected Hashtable<String,ArrayList<GffDetailAbs>> Chrhash;
	
	/**
	 * 返回真正的查找用hash表<br>
	 * 这个哈希表来存储
	 * hash（ChrID）--ChrList--GeneInforList(GffDetail类)<br>
	 * 其中ChrID为小写，
	 * 代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID
	 * chr格式，全部小写 chr1,chr2,chr11<br>
	 */
	protected Hashtable<String,ArrayList<GffDetailAbs>> getChrhash()
	{
		return Chrhash;
	}
	
	
	/**
	 * @本方法需要被覆盖
	 * 最底层读取gff的方法<br>
	 * 输入Gff文件，最后获得两个哈希表和一个list表,
	 * 结构如下：<br/>
	 * @1.Chrhash
	 * （ChrID）--ChrList--GeneInforList(GffDetail类)<br/>
	 *   其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID,
	 * chr格式，全部小写 chr1,chr2,chr11<br/>
	 * 
	 * @2.locHashtable
	 * （LOCID）--GeneInforlist，其中LOCID代表具体的条目编号,各个条目定义由相应的GffHash决定 <br/>
	 * 
	 * @3.LOCIDList
	 * （LOCID）--LOCIDList，按顺序保存LOCID,只能用于随机查找基因，不建议通过其获得某基因的序号<br/>
	 * @throws Exception 
	 */
	protected abstract void ReadGffarray(String gfffilename) throws Exception;

	/**
	 * 需要覆盖
	 * 查找某个特定LOC的信息
	 * {return locHashtable.get(LOCID);}
	 * @param LOCID 给定某LOC的名称，注意名称是一个短的名字，譬如在UCSC基因中，不是locstring那种好几个基因连在一起的名字，而是单个的短的名字
	 * @return 返回该LOCID的具体GffDetail信息，用相应的GffDetail类接收
	 */
	public abstract GffDetailAbs LOCsearch(String LOCID);
	
	/**
	 * 需要覆盖
	 * {return Chrhash.get(chrID).get(LOCNum);}
	 * 给定chrID和该染色体上的位置，返回GffDetail信息
	 * @param chrID 小写
	 * @param LOCNum 该染色体上待查寻LOC的int序号
	 * @return  返回该LOCID的具体GffDetail信息，用相应的GffDetail类接收
	 */
	public abstract GffDetailAbs LOCsearch(String chrID,int LOCNum);
	
	/**
	 * 给定某个LOCID，返回该LOC在某条染色体中的位置序号号，第几位<br>
	 * 也就是Chrhash中某个chr下该LOC的位置<br>
	 * 该位置必须大于等于0，否则就是出错<br>
	 * 该比较是首先用单个LOCID从locHashtable获得其GffDetail类，然后用ChrID在Chrhash中获得某条染色体的gffdetail的List，然后比较他们的locString以及基因的起点和终点
	 * 仅仅将GffDetail的equal方法重写。
	 * @param LOCID 输入某基因编号
	 * @return string[2]<br>
	 * 0: 染色体编号，chr1,chr2等，都为小写<br>
	 * 1:该染色体上该LOC的序号，如1467等
	 */
	public String[] getLOCNum(String LOCID) {	
		String[] LOCNumInfo=new String[2];
		GffDetailAbs gffLOCdetail=locHashtable.get(LOCID);
		LOCNumInfo[0]=gffLOCdetail.ChrID;
		ArrayList<GffDetailAbs> locArrayList=Chrhash.get(LOCNumInfo[0]);
		LOCNumInfo[1]=locArrayList.indexOf(gffLOCdetail)+"";
		return LOCNumInfo;
	}
	
	/**
	 * return searchLocation(chrID, Coordinate);
	 * 
	 * 单坐标查找 输入ChrID，单个坐标，以及GffHash类<br>
	 * ,chr采用正则表达式抓取，无所谓大小写，会自动转变为小写, chr1,chr2,chr11<br>
	 * @param chrID
	 * @param Coordinate
	 * @return
	 * 没找到就返回null
	 */
	public abstract GffCodAbs searchLoc(String chrID, int Coordinate);
	
	
	/**
	 * 单坐标查找 输入ChrID，单个坐标，以及GffHash类<br>
	 * ,chr采用正则表达式抓取，无所谓大小写，会自动转变为小写, chr1,chr2,chr11<br>
	 * @param chrID
	 * @param Coordinate
	 * @return
	 * 没找到就返回null
	 */
	protected GffCodAbs searchLocation(String chrID, int Coordinate) {
		String Chrpatten = "Chr\\w+";// Chr1， chr2，
										// chr11的形式,注意还有chrx之类的，chr里面可以带"_"，所以说不能用"_"分割chr与字符
		/**
		 * 判断Chr格式是否正确，是否是有效的染色体
		 */
		Pattern pattern = Pattern.compile(Chrpatten, Pattern.CASE_INSENSITIVE); 
		Matcher matcher;
		matcher = pattern.matcher(chrID);
		if (!matcher.find()) {
			return null;
		}
		chrID = matcher.group().toLowerCase();
		ArrayList<GffDetailAbs> Loclist =  getChrhash().get(chrID);// 某一条染色体的信息
		if (Loclist == null) {
			return null;
		}
		return searchLocation(Loclist, chrID,Coordinate);
	}

	/**
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 没找到就返回null
	 */
	private GffCodAbs searchLocation(ArrayList<GffDetailAbs> Loclist, String chrID, int Coordinate) {
		
		String[] locationString = new String[5];
		locationString[0] = "GffCodInfo_searchLocation error";
		locationString[1] = "GffCodInfo_searchLocation error";
		int[] locInfo = LocPosition(Loclist, Coordinate);// 二分法查找peaknum的定位
		if (locInfo[0] == 1) // 定位在基因内
		{
			GffCodAbs gffCodAbs = SearchLOCinside(Loclist, locInfo[1], locInfo[2],chrID,Coordinate);// 查找具体哪个内含子或者外显子
			gffCodAbs.geneChrHashListNum[0] = locInfo[1];

			if (locInfo[1] == -1)
				gffCodAbs.geneDetail[0] = null;
			else
				gffCodAbs.geneDetail[0] = Loclist.get(locInfo[1]);

			if (locInfo[2] == -1)
				gffCodAbs.geneDetail[1] = null;
			else
				gffCodAbs.geneDetail[1] = Loclist.get(locInfo[2]);

			gffCodAbs.geneChrHashListNum[1] = locInfo[2];
			return gffCodAbs;
		} else if (locInfo[0] == 2) {
			GffCodAbs gffCodAbs = SearchLOCoutside(Loclist, locInfo[1], locInfo[2],chrID,Coordinate);// 查找基因外部的peak的定位情况
			if (locInfo[1] == -1)
				gffCodAbs.geneDetail[0] = null;
			else
				gffCodAbs.geneDetail[0] = Loclist.get(locInfo[1]);

			if (locInfo[2] == -1)
				gffCodAbs.geneDetail[1] = null;
			else
				gffCodAbs.geneDetail[1] = Loclist.get(locInfo[2]);

			gffCodAbs.geneChrHashListNum[0] = locInfo[1];
			gffCodAbs.geneChrHashListNum[1] = locInfo[2];
			return gffCodAbs;
		}
		return null;
	}

	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 */
	private int[] LocPosition(ArrayList<GffDetailAbs> Loclist, int Coordinate) {
		int[] LocInfo = new int[3];
		int endnum = 0;
		endnum = Loclist.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate < Loclist.get(beginnum).numberstart) {
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			return LocInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate > Loclist.get(endnum).numberstart) {
			LocInfo[1] = endnum;
			LocInfo[2] = -1;
			if (Coordinate < Loclist.get(endnum).numberend) {
				LocInfo[0] = 1;
				return LocInfo;
			} else {
				LocInfo[0] = 2;
				return LocInfo;
			}
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == Loclist.get(number).numberstart) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < Loclist.get(number).numberstart
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		LocInfo[1] = beginnum;
		LocInfo[2] = endnum;
		if (Coordinate <= Loclist.get(beginnum).numberend)// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			LocInfo[0] = 1;
			return LocInfo;
		}
		// location在基因外部
		LocInfo[0] = 2;
		return LocInfo;
	}

	/**
	 * 必须被覆盖，填充 result insideLOC LOCID begincis5to3 distancetoLOCStart
	 * distancetoLOCEnd endcis5to3 等几乎所有信息
	 * 
	 * @param coordinate
	 * @param loclist
	 * @param beginnum
	 *            本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因
	 * @param endnum
	 *            下个基因的序号 -1表示后面没有基因
	 * @return
	 */
	protected abstract GffCodAbs SearchLOCinside(ArrayList<GffDetailAbs> loclist,
			int beginnum, int endnum, String chrID, int Coordinate);

	/**
	 * 必须被覆盖，填充 result insideLOC LOCID begincis5to3 distancetoLOCStart
	 * distancetoLOCEnd endcis5to3
	 * 
	 * @param coordinate
	 * @param loclist
	 * @param beginnum
	 *            本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因
	 * @param endnum
	 *            下个基因的序号 -1表示后面没有基因
	 * @return
	 */
	protected abstract GffCodAbs SearchLOCoutside(ArrayList<GffDetailAbs> loclist,
			int beginnum, int endnum, String chrID, int Coordinate);

}
