package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetail;
import com.novelbio.analysis.seq.genomeNew.listOperate.ListAbs;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.CmpListCluster;
import com.novelbio.database.model.modcopeid.CopedID;


/**
 * 获得Gff的项目信息<br/>
 * 具体的GffHash需要实现ReadGffarray并通过该方法填满三个表
 * @Chrhash hash（ChrID）--ChrList--GeneInforList(GffDetail类)
 * @locHashtable hash（LOCID）--GeneInforlist
 * @LOCIDList 顺序存储每个基因号或条目号
 */
public abstract class GffHash <T extends GffDetailAbs, K extends GffCodAbs<T>, M extends GffCodAbsDu<T, K>> {
	/**
	 * 起点默认为开区间
	 */
	int startRegion = 1;
	/**
	 * 起点是否为闭区间，不是则为开区间，<br>
	 * False: 开区间的意思是，24表示从0开始计数的24位，也就是实际的25位<br>
	 * True: 闭区间的意思是，24就代表第24位<br>
	 * UCSC的默认文件的起点是开区间
	 */
	public void setStartRegion(boolean region) {
		if (region) 
			this.startRegion = 0;
		else 
			this.startRegion = 1;
	}
	/**
	 * 终点默认为闭区间
	 */
	int endRegion = 0;
	/**
	 * 起点是否为闭区间，不是则为开区间，<br>
	 * False: 开区间的意思是，24表示从0开始计数的24位，也就是实际的25位<br>
	 * True: 闭区间的意思是，24就代表第24位<br>
	 * UCSC的默认文件的终点是闭区间间
	 */
	public void setEndRegion(boolean region) {
		if (region) 
			this.endRegion = 0;
		else 
			this.endRegion = 1;
	}
	
	String gfffilename = "";
	
	public String getGffFilename() {
		return gfffilename;
	}
	Logger logger = Logger.getLogger(GffHash.class);
	/**
	 * 哈希表LOC--LOC细节<br>
	 * 用于快速将LOC编号对应到LOC的细节<br>
	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的条目编号 <br>
	  * 会有有多个LOCID共用一个区域的情况，所以有多个不同的LOCID指向同一个GffdetailUCSCgene<br>
	 */
	protected HashMap<String,T> locHashtable;
	/**
	 * 哈希表LOC--在arraylist上的Num<br>
	 * 用于快速将LOC编号对应到其对应的chr上的位置<br>
	 */
	protected HashMap<String,Integer> hashLoc2Num;
	/**
	 * 返回哈希表 LOC--LOC细节<br/>
	 * 用于快速将LOC编号对应到LOC的细节
	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的基因编号 <br/>
	 */
	public HashMap<String,Integer> getHashLocNum() {
		if (hashLoc2Num != null) {
			return hashLoc2Num;
		}
		hashLoc2Num = new LinkedHashMap<String, Integer>();
		for (ListAbs<T> listAbs : Chrhash.values()) {
			listAbs.getHashLocNum(hashLoc2Num);
		}
		return hashLoc2Num;
	}
	
	/**
	 * 返回哈希表 LOC--LOC细节<br/>
	 * 用于快速将LOC编号对应到LOC的细节
	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的基因编号 <br/>
	 */
	public HashMap<String,T> getLocHashtable() {
		if (locHashtable != null) {
			return locHashtable;
		}
		locHashtable = new LinkedHashMap<String, T>();
		for (ListAbs<T> listAbs : Chrhash.values()) {
			listAbs.getLocHashtable(locHashtable);
		}
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
	 * 顺序存储ChrHash中的ID，这个就是ChrHash中实际存储的ID，如果两个Item是重叠的，就用ListAbs.SEP隔开，
	 * 那么该list中的元素用split("/")分割后，上locHashtable就可提取相应的GffDetail，目前主要是Peak用到
	 * 顺序获得，可以获得某个LOC在基因上的定位。
	 * 其中TigrGene的ID每个就是一个LOCID，也就是说TIGR的ID不需要进行切割，当然切了也没关系
	 */
	public ArrayList<String> getLOCChrHashIDList() {
		if (LOCChrHashIDList != null) {
			return LOCChrHashIDList;
		}
		LOCChrHashIDList = new ArrayList<String>();
		for (ListAbs<T> lsAbs : Chrhash.values()) {
			LOCChrHashIDList.addAll(lsAbs.getLOCIDList());
		}
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
	protected LinkedHashMap<String,ListAbs<T>> Chrhash;
	
	/**
	 * 返回真正的查找用hash表<br>
	 * 这个哈希表来存储
	 * hash（ChrID）--ChrList--GeneInforList(GffDetail类)<br>
	 * 其中ChrID为小写，
	 * 代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID
	 * chr格式，全部小写 chr1,chr2,chr11<br>
	 */
	protected HashMap<String,ListAbs<T>> getChrhash()
	{
		return Chrhash;
	}
	
	/**
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 采用clone的方法获得信息
	 * 没找到就返回null
	 */
	public K searchLocation(String chrID, int Coordinate) {
		chrID = chrID.toLowerCase();
		ListAbs<T> Loclist =  getChrhash().get(chrID);// 某一条染色体的信息
		if (Loclist == null) {
			return null;
		}
		int[] locInfo = Loclist.LocPosition(Coordinate);// 二分法查找peaknum的定位
		if (locInfo == null) {
			return null;
		}
		K gffCod = setGffCod(chrID, Coordinate);
		if (locInfo[0] == 1) // 定位在基因内
		{
			gffCod.gffDetailThis = (T) Loclist.get(locInfo[1]).clone(); 
			gffCod.gffDetailThis.setCoord(Coordinate);
			gffCod.booFindCod = true;
			gffCod.ChrHashListNumThis = locInfo[1];
			gffCod.insideLOC = true;
			if (locInfo[1] - 1 >= 0) {
				gffCod.gffDetailUp =  (T) Loclist.get(locInfo[1]-1).clone();
				gffCod.gffDetailUp.setCoord(Coordinate);
				gffCod.ChrHashListNumUp = locInfo[1]-1;
				
			}
			if (locInfo[2] != -1) {
				gffCod.gffDetailDown = (T) Loclist.get(locInfo[2]).clone();
				gffCod.gffDetailDown.setCoord(Coordinate);
				gffCod.ChrHashListNumDown = locInfo[2];
			}
		} else if (locInfo[0] == 2) {
			gffCod.insideLOC = false;
			if (locInfo[1] >= 0) {
				gffCod.gffDetailUp =  (T) Loclist.get(locInfo[1]).clone();
				gffCod.gffDetailUp.setCoord(Coordinate);
				gffCod.ChrHashListNumUp = locInfo[1];		
			}
			if (locInfo[2] != -1) {
				gffCod.gffDetailDown = (T) Loclist.get(locInfo[2]).clone();
				gffCod.gffDetailDown.setCoord(Coordinate);
				gffCod.ChrHashListNumDown = locInfo[2];
			}
		}
		return gffCod;
	}

	protected abstract K setGffCod(String chrID, int coordinate);
	/**
	 * 返回双坐标查询的结果，内部自动判断 cod1 和 cod2的大小
	 * 如果cod1 和cod2 有一个小于0，那么坐标不存在，则返回null
	 * @param chrID
	 * @param cod1 必须大于0
	 * @param cod2 必须大于0
	 * @return
	 */
	public M searchLocation(String chrID, int cod1, int cod2) {
		chrID = chrID.toLowerCase();
		if (cod1 < 0 && cod2 < 0) {
			return null;
		}
		ListAbs<T> Loclist =  getChrhash().get(chrID);// 某一条染色体的信息
		if (Loclist == null) {
			return null;
		}
		
		K gffCod1 = searchLocation(chrID, Math.min(cod1, cod2));
		K gffCod2 = searchLocation(chrID, Math.max(cod1, cod2));
		if (gffCod1 == null) {
			System.out.println("error");
		}
		M gffCodDu = setGffCodDu(new ArrayList<T>(),gffCod1, gffCod2 );
		
		if (gffCodDu.gffCod1.getItemNumDown() < 0) {
			gffCodDu.lsgffDetailsMid = null;
		}
		else {
			for (int i = gffCodDu.gffCod1.getItemNumDown(); i <= gffCodDu.gffCod2.getItemNumUp(); i++) {
				gffCodDu.lsgffDetailsMid.add((T)Loclist.get(i).clone());
			}
		}
		return gffCodDu;
	}
	/**
	 * new一个对应的GffCodDu即可
	 * @return
	 */
	protected abstract M setGffCodDu(ArrayList<T> lsgffDetail,
			K gffCod1, K gffCod2);
	
	
 
	/**
	 * 在读取文件后如果有什么需要设置的，可以写在setOther();方法里面
	 * @param gfffilename
	 */
	public void ReadGffarray(String gfffilename) {
		if (this.gfffilename.equals(gfffilename)) {
			return;
		}
		this.gfffilename = gfffilename;
		try {
			ReadGffarrayExcep(gfffilename);
			setItemDistance();
			setOther();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	protected abstract void ReadGffarrayExcep(String gfffilename) throws Exception;

 
	
	
	
	/**
	 * 需要覆盖
	 * 查找某个特定LOC的信息
	 * {return locHashtable.get(LOCID);}
	 * @param LOCID 给定某LOC的名称，注意名称是一个短的名字，譬如在UCSC基因中，不是locstring那种好几个基因连在一起的名字，而是单个的短的名字
	 * @return 返回该LOCID的具体GffDetail信息，用相应的GffDetail类接收
	 */
	public T searchLOC(String LOCID){
		return  locHashtable.get(LOCID.toLowerCase());
	}
	/**
	 * 需要覆盖
	 * {return Chrhash.get(chrID).get(LOCNum);}
	 * 给定chrID和该染色体上的位置，返回GffDetail信息
	 * @param chrID 小写
	 * @param LOCNum 该染色体上待查寻LOC的int序号
	 * @return  返回该LOCID的具体GffDetail信息，用相应的GffDetail类接收
	 */
	public T searchLOC(String chrID,int LOCNum)
	{
		chrID = chrID.toLowerCase();
		return Chrhash.get(chrID).get(LOCNum);
	}
	
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
		String[] result = new String[2];
		T ele = locHashtable.get(LOCID);
		result[0] = ele.getParentName();
		result[1] = getHashLocNum().get(LOCID) + "";
		return result;
	}
	/**
	 * 设定每个GffDetail的tss2UpGene和tes2DownGene
	 */
	private void setItemDistance() {
		for (ListAbs<T> lsGffDetail : Chrhash.values()) {
			for (int i = 0; i < lsGffDetail.size(); i++) {
				T gffDetail = lsGffDetail.get(i);
				T gffDetailUp = null;
				T gffDetailDown = null;
				if (i > 0) {
					gffDetailUp = lsGffDetail.get(i-1);
				}
				if (i < lsGffDetail.size() - 1) {
					gffDetailDown = lsGffDetail.get(i + 1);
				}
				if (gffDetail.isCis5to3()) {
					gffDetail.tss2UpGene = distance(gffDetail, gffDetailUp, true);
					gffDetail.tes2DownGene = distance(gffDetail, gffDetailDown, false);
				}
				else {
					gffDetail.tss2UpGene = distance(gffDetail, gffDetailDown, false);
					gffDetail.tes2DownGene = distance(gffDetail, gffDetailUp, true);
				}
			}
		}
	}
	
	private int distance(T gffDetail1, T gffDetail2, boolean Up) {
		if (gffDetail2 == null) {
			return 0;
		}
		else {
			if (Up) {
				return Math.abs(gffDetail1.getStartAbs() - gffDetail2.getEndAbs());
			}
			else {
				return Math.abs(gffDetail1.getEndAbs() - gffDetail2.getStartAbs());
			}
		}
	}


	/**
	 * 在读取文件后如果有什么需要设置的，可以写在setOther();方法里面，本方发为空，直接继承即可
	 */
	protected void setOther()
	{
		
	}



	
	
}
