package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonCluster;
import com.novelbio.base.dataStructure.ArrayOperate;

public class GffGeneCluster {
	private static Logger logger = Logger.getLogger(GffGeneCluster.class);
	/** 是否含有reference的GffDetailGene */
	boolean isContainsRef = true;
	Boolean sameExon = null;
	String chrID;
	int startLoc = 0;
	int endLoc = 0;
	/**
	 * 当ref和this的exon都在边界时，如果两个外显子的边界差距在指定bp以内(譬如10bp以内)，就修正为靠近内侧的
	 * 如下<br>
	 * //--------10-------20-------------30-----40----------------50---------60----------------<br>
		//-------10----(18)--------------30-----40-------------------(52)----60-------------<br>
		//-------10------(20)-------------30----40----------------(50)-------60-------------<br>
		 * 注意只修正一个端点，也就是靠近内侧的，如括弧所标注
		 *差距是小于但不等于 所以设定为10表示10以下的差距才会修正，但是不会修正10
	 */
	int boundMaxFalseGapBp = 9;
	/** 是否已经将refgff放置在isContainsRef中 
	 * 这里的算法是首先将refGffGene放置在lsGeneCluster的最前面。
	 * 然后如果存在Ref，则将第一位的lsGeneCluster放置在lsGenesRef
	 * 如果不存在Ref，则将最长的LsGene放置在LsGenesRef
	 * 那么这个标签就是来确定该步骤是否已经完成
	 * */
	boolean setRefLsGene = false;
	/** 最后比完的结果放在这里 */
	ArrayList<GffDetailGene> lsCombGenesResult;
	/**
	 * 挑选出需要进行修正的GffDetailGeneList，首先选择RefGffGene
	 * 设定的时候会将lsGeneCluster里面的对应项目删除
	 */
	ArrayList<GffDetailGene> lsGenesRef;
	/**
	 * list--所有不同来源的Gffhash<br>
	 * list--每个GffHash的GffDetail<br>
	 * <br>
	 * 如果本组中有RefGffGene，则放在第一个位置。那么就用第一个位置的GffDetailGene进行比较
	 * 如果本组没有RefGffGene，则选择本组中最长的一个GffDetailGene进行比较<br>
	 * <br>
	 * 如果存在reference<b>其中第一个一定是Reference的GffGene</b>
	 */
	ArrayList<ArrayList<GffDetailGene>> lsGeneCluster = new ArrayList<ArrayList<GffDetailGene>>();
	/**依次添加的GffHash的名字 */
	ArrayList<String> lsListGffName = new ArrayList<String>();
	
	double likelyhood = 0.7;
	
	ArrayList<ArrayList<ExonClusterBoundInfo>> lsIso2ExonBoundInfoStatistics = new ArrayList<ArrayList<ExonClusterBoundInfo>>();
	/**
	 * 是否含有refGffDetailGene，没有说明Ref跳过了这个loc
	 * @param isContainsRef
	 */
	public void setIsContainsRef(boolean isContainsRef) {
		this.isContainsRef = isContainsRef;
	}
	/**
	 * @param gffFileName 仅仅用来记录Gff文件名
	 * @param lsGffDetailGenes
	 */
	public void addLsGffDetailGene(String gffFileName, ArrayList<GffDetailGene> lsGffDetailGenes) {
		lsGeneCluster.add(lsGffDetailGenes);
		lsListGffName.add(gffFileName);
	}
	/** 当输入了Ref和This两个GffHash的时候才能用 */
	public ArrayList<GffDetailGene> getThisGffGene() {
		setRefGffGene();
		if (isContainsRef) {
			if (lsGeneCluster.size() == 0) {
				return new ArrayList<GffDetailGene>();
			} else {
				return lsGeneCluster.get(0);
			}
		} else {
			return lsGenesRef;
		}
	}
	public ArrayList<GffDetailGene> getRefGffGene() {
		setRefGffGene();
		if (isContainsRef) {
			return lsGenesRef;
		}
		else {
			return null;
		}
	}
	
	/**
	 * 当ref和this的exon都在边界时，如果两个外显子的边界差距在指定bp以内(譬如10bp以内)，就修正为靠近内侧的
	 * 如下<br>
	 * //--------10-------20-------------30-----40----------------50---------60----------------<br>
		//-------10----(18)--------------30-----40-------------------(52)----60-------------<br>
		//-------10------(20)-------------30----40----------------(50)-------60-------------<br>
		 * 注意只修正一个端点，也就是靠近内侧的，如括弧所标注
		 *差距是小于但不等于 所以设定为10表示10以下的差距才会修正，但是不会修正10
		 *设定的值自动取绝对值，不得大于20
	 */
	public void setBoundMaxFalseGapBp(int boundMaxFalseGapBp) {
		boundMaxFalseGapBp = Math.abs(boundMaxFalseGapBp);
		if (boundMaxFalseGapBp > 20) {
			boundMaxFalseGapBp = 20;
		}
		this.boundMaxFalseGapBp = boundMaxFalseGapBp;
	}
	/** 获得修饰好的GffDetailGene
	 * @return
	 */
	public ArrayList<GffDetailGene> getCombinedGffGene() {
		if (lsCombGenesResult == null) {
			setRefGffGene();
			lsCombGenesResult = compareAndModify_GffGeneNew();
		}
		return lsCombGenesResult;
	}
	/** 设定lsGenesRef，同时将lsGeneCluster中对应项目删除
	 * 为合并做好准备
	 */
	private void setRefGffGene() {
		if (setRefLsGene) {
			return;
		}
		setRefLsGene = true;
		if (isContainsRef) {
			lsGenesRef = lsGeneCluster.get(0);
			lsGeneCluster.remove(0);
		}
		else {
			int longestGffGene = getLongestGffGene_In_LsGeneCluster();
			lsGenesRef = lsGeneCluster.get(longestGffGene);
			lsGeneCluster.remove(longestGffGene);
		}
	}
	
	private int getLongestGffGene_In_LsGeneCluster() {
		int lengthGffGene = 0;
		int index = 0;
		if (lsGeneCluster.size() == 1) {
			return 0;
		}
		for (int i = 0; i < lsGeneCluster.size(); i++) {
			for (GffDetailGene gffDetailGene : lsGeneCluster.get(i)) {
				if (gffDetailGene.getLength() > lengthGffGene) {
					lengthGffGene = gffDetailGene.getLength();
					index = i;
				}
			}
		}
		return index;
	}

	private ArrayList<GffDetailGene> compareAndModify_GffGene() {
		ListGff lsGffDetailGenes = new ListGff();
		for (GffDetailGene gffDetailGeneRefRaw : lsGenesRef) {//遍历每个GffDetail
			GffDetailGene gffDetailGeneRef = gffDetailGeneRefRaw.clone();

			GffDetailGene gffDetailGeneResult = gffDetailGeneRefRaw.clone();
			gffDetailGeneResult.clearIso();
			
			HashSet<String> setGffIsoRefSelectName = new HashSet<String>();//所有选中的Iso的名字，也就是与cufflink预测的转录本相似的转录本
			//这里的lsGeneCluster已经去除了refGff
			for (ArrayList<GffDetailGene> lsgffArrayList : lsGeneCluster) {//获得GffCluster里面每个GffHash的list，这个一般只有一个GffHash
				for (GffDetailGene gffDetailGeneCalculate : lsgffArrayList) {//获得另一个GffHash里面的GffDetailGene
					for (GffGeneIsoInfo gffIsoThis : gffDetailGeneCalculate.getLsCodSplit()) {//遍历该GffDetailGene的转录本，并挑选出最接近的进行比较	
						GffGeneIsoInfo gffIsoRef = gffDetailGeneRef.getSimilarIso(gffIsoThis, likelyhood);
						
						if (gffIsoRef == null) {
							gffDetailGeneResult.addIso(gffIsoThis);
							continue;
						}
						
						setGffIsoRefSelectName.add(gffIsoRef.getName());
						GffGeneIsoInfo gffIsoTmpResult = compareIso(gffIsoRef, gffIsoThis);
						gffDetailGeneResult.addIso(gffIsoTmpResult);
					}
				}
			}
			
			for (String isoName : setGffIsoRefSelectName) {
				gffDetailGeneRef.removeIso(isoName);
			}
			gffDetailGeneResult.addIsoSimple(gffDetailGeneRef);
			lsGffDetailGenes.add(gffDetailGeneResult);
		}
		lsGffDetailGenes = lsGffDetailGenes.combineOverlapGene();
		return lsGffDetailGenes;
	}
	//TODO
	private ArrayList<GffDetailGene> compareAndModify_GffGeneNew() {
		ListGff lsGffDetailGenes = new ListGff();
		GffDetailGene gffDetailGeneRef = getGffDetailGene(lsGenesRef);
		GffDetailGene gffDetailGeneResult = gffDetailGeneRef.clone();
		gffDetailGeneResult.clearIso();
		HashSet<String> setGffIsoRefSelectName = new HashSet<String>();//所有选中的Iso的名字，也就是与cufflink预测的转录本相似的转录本
		//这里的lsGeneCluster已经去除了refGff
		for (ArrayList<GffDetailGene> lsgffArrayList : lsGeneCluster) {//获得GffCluster里面每个GffHash的list，这个一般只有一个GffHash
			GffDetailGene gffDetailGeneCalculate = getGffDetailGene(lsgffArrayList);				
			Set<String> setRefIsoNameTmp = modifyGffRef(gffDetailGeneResult, gffDetailGeneRef, gffDetailGeneCalculate);
			setGffIsoRefSelectName.addAll(setRefIsoNameTmp);
		}
		
		for (String isoName : setGffIsoRefSelectName) {
			gffDetailGeneRef.removeIso(isoName);
		}
		gffDetailGeneResult.addIsoSimple(gffDetailGeneRef);
		lsGffDetailGenes.add(gffDetailGeneResult);
		return lsGffDetailGenes;
	}
	
	/** 把若干gffDetailGene合并为1个,克隆的方法获得 */
	private GffDetailGene getGffDetailGene(List<GffDetailGene> lsGffDetailGenes) {
		GffDetailGene gffDetailGene = lsGffDetailGenes.get(0).clone();
		for (int i = 1; i < lsGffDetailGenes.size(); i++) {
			GffDetailGene gffDetailGene2 = lsGffDetailGenes.get(i);
			gffDetailGene.addIsoSimple(gffDetailGene2);
		}
		return gffDetailGene;
	}
	/**
	 * 用gffDetailGeneCalculate去修正gffDetailGeneRef，结果放入gffDetailGeneResult中
	 * @param gffDetailGeneResult 
	 * @param gffDetailGeneRef
	 * @param gffDetailGeneCalculate 含有全部iso的gffDetailGene
	 * @return 返回被修正的gffRefIso的名字，以后就不会添加这些iso进入结果了
	 */
	private Set<String> modifyGffRef(GffDetailGene gffDetailGeneResult, GffDetailGene gffDetailGeneRef, GffDetailGene gffDetailGeneCalculate) {
		Set<String> setGffIsoRefSelectName = new HashSet<>();
		for (GffGeneIsoInfo gffIsoThis : gffDetailGeneCalculate.getLsCodSplit()) {//遍历该GffDetailGene的转录本，并挑选出最接近的进行比较	
			//选择了仅仅是起点和终点不同的exon，方便修正
			GffGeneIsoInfo gffIsoRef = gffDetailGeneRef.getAlmostSameIso(gffIsoThis);
			//TODO 开始做修正工作
			if (gffIsoRef == null) {
				GffGeneIsoInfo gffIsoSimilar = gffDetailGeneRef.getSimilarIso(gffIsoThis, 0.5);
				if (gffIsoSimilar != null) {
					gffIsoThis.setParentGeneName(gffIsoSimilar.getParentGeneName());
					if (gffIsoSimilar.ismRNA() && !gffIsoThis.ismRNA()) {
						gffIsoThis.setATGUAGauto(gffIsoSimilar.getATGsite(), gffIsoSimilar.getUAGsite());
					}
				}
				gffDetailGeneResult.addIso(gffIsoThis);
				continue;
			}
			
			GffGeneIsoInfo gffIsoTmpResult = compareIso(gffIsoRef, gffIsoThis);
			if (gffIsoTmpResult == null) {
				if (gffIsoRef.ismRNA() && !gffIsoRef.ismRNA()) {
					gffIsoThis.setATGUAGauto(gffIsoRef.getATGsite(), gffIsoRef.getUAGsite());
				}
				gffDetailGeneResult.addIso(gffIsoThis);
			} else {
				setGffIsoRefSelectName.add(gffIsoRef.getName());
				//添加ATG UAG信息
				if (!gffIsoTmpResult.ismRNA()) {
					if (gffIsoRef.ismRNA()) {
						gffIsoTmpResult.setATGUAGauto(gffIsoRef.getATGsite(), gffIsoRef.getUAGsite());
					} else if (gffIsoThis.ismRNA()) {
						gffIsoTmpResult.setATGUAGauto(gffIsoThis.getATGsite(), gffIsoThis.getUAGsite());
					}
				}
				gffDetailGeneResult.addIso(gffIsoTmpResult);
			}
		}
		return setGffIsoRefSelectName;
	}
	
	/**
	 * public出来仅仅是提供给Junit测试使用
	 * 比较两个方向相同，有交集的GffGeneIso的信息，修正gffGeneIsoInfoRef
	 * @param gffGeneIsoInfoRef
	 * @param gffGeneIsoInfoThis
	 * @return 返回全新的GffGeneIsoInfo
	 */
	public GffGeneIsoInfo compareIso(GffGeneIsoInfo gffGeneIsoInfoRef, GffGeneIsoInfo gffGeneIsoInfoThis) {		
		GffGeneIsoInfo gffGeneIsoInfoResult = gffGeneIsoInfoRef.clone();
		if (gffGeneIsoInfoThis == null || gffGeneIsoInfoThis.size() == 0 || gffGeneIsoInfoRef.equalsIso(gffGeneIsoInfoThis)) {
			return gffGeneIsoInfoResult;
		}
		gffGeneIsoInfoResult.clear();
		
		ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
		lsGffGeneIsoInfos.add(gffGeneIsoInfoRef); lsGffGeneIsoInfos.add(gffGeneIsoInfoThis);
		
		ArrayList<ExonCluster> lsExonClusters = GffGeneIsoInfo.getExonCluster(gffGeneIsoInfoRef.isCis5to3(), lsGffGeneIsoInfos);

		int[] tailBoundInfo = getBothStartNumEndNum(lsExonClusters);
		ArrayList<ExonClusterBoundInfo> lsExonBoundInfoStatistics = new ArrayList<ExonClusterBoundInfo>();//用于统计exon修正数量的
		
		/** 上一个位点 */
		ExonClusterBoundInfo lastExonClusterBoundInfo = new ExonClusterBoundInfo(gffGeneIsoInfoRef, gffGeneIsoInfoThis, boundMaxFalseGapBp);
		lastExonClusterBoundInfo.booStartUnify = true;
		lastExonClusterBoundInfo.booEndUnify = true;

		for (int exonClusterNum = 0; exonClusterNum < lsExonClusters.size(); exonClusterNum++) {
			ExonClusterBoundInfo exonClusterBoundInfo = new ExonClusterBoundInfo(gffGeneIsoInfoRef, gffGeneIsoInfoThis, boundMaxFalseGapBp);
			exonClusterBoundInfo.setLastExonClusterBoundInfo(lastExonClusterBoundInfo);
			exonClusterBoundInfo.setLsExonClustersAndNum(lsExonClusters, exonClusterNum);
			exonClusterBoundInfo.setTailBoundInfo(tailBoundInfo);
			
			try {
				List<ExonInfo> lsExonInfos = exonClusterBoundInfo.calculate();
				gffGeneIsoInfoResult.addAll(lsExonInfos);
			} catch (Exception e) {
				List<ExonInfo> lsExonInfos = exonClusterBoundInfo.calculate();
				gffGeneIsoInfoResult.addAll(lsExonInfos);
			}

			
			lsExonBoundInfoStatistics.add(exonClusterBoundInfo);
			lastExonClusterBoundInfo = exonClusterBoundInfo;
		}
		//TODO
		lsIso2ExonBoundInfoStatistics.add(lsExonBoundInfoStatistics);
		return gffGeneIsoInfoResult;
	}
	/**
	 * lsExonClusters是两个转录本比较的结果，如下图<br>
	 * 0----1-----2-----3-----4<br>
	 * A----B-----C-----D----E 第一个转录本<br>
	 * *----*-----a-----b 第二个转录本<br>
	 * 返回其中较晚出现的起点和较早出现的终点坐标，也就是a和b的坐标，从0开始
	 * 那么就是返回2和3
	 * @param lsExonClusters
	 * @return
	 */
	private int[] getBothStartNumEndNum(ArrayList<ExonCluster> lsExonClusters) {
		int start = -1, end = -1;
		for (int i = 0; i < lsExonClusters.size(); i++) {
			ExonCluster exonCluster = lsExonClusters.get(i);
			if (start < 0 && exonCluster.getMapIso2LsExon().size() > 1) {
				start = i;
				end = i;
			}
			if (start > 0 && exonCluster.getMapIso2LsExon().size() > 1 ) {
				end = i;
			}
		}
		return new int[]{start, end};
	}
}
/**
 * 存储某个exoncluster中ref和this的exon的起点和终点是否一致
 * @author zong0jie
 */
class ExonClusterBoundInfo {
	GffGeneIsoInfo gffGeneIsoInfoRef;
	GffGeneIsoInfo gffGeneIsoInfoThis;
	/** 上一个位点的状态信息 */
	ExonClusterBoundInfo lastExonClusterBoundInfo;
	/** start位点是一致的 */
	boolean booStartUnify = true;
	/** end位点是一致的 */
	boolean booEndUnify = true;
	/** 起点选择Ref */
	boolean selectRefStart = true;
	/** 终点选择Ref */
	boolean selectRefEnd = true;
	
	/** 本次所比较的ExonCluster信息 */
	ArrayList<ExonCluster> lsExonClusters;
	/** 本次exoncluster的位置，从0开始计算 */
	int thisExonClusterNum;
	
	/**
	 * lsExonClusters是两个转录本比较的结果，如下图<br>
	 * 0----1-----2-----3-----4<br>
	 * A----B-----C-----D----E 第一个转录本<br>
	 * *----*-----a-----b 第二个转录本<br>
	 * 返回其中较晚出现的起点和较早出现的终点坐标，也就是a和b的坐标，从0开始
	 * 那么就是返回2和3
	 */
	int[] tailBoundInfo;
	
	/**
	 * 当ref和this的exon都在边界时，如果两个外显子的边界差距在指定bp以内(譬如10bp以内)，就修正为靠近内侧的
	 * 如下<br>
	 * //--------10-------20-------------30-----40----------------50---------60----------------<br>
		//-------10----(18)--------------30-----40-------------------(52)----60-------------<br>
		//-------10------(20)-------------30----40----------------(50)-------60-------------<br>
		 * 注意只修正一个端点，也就是靠近内侧的，如括弧所标注
		 *差距是小于但不等于 所以设定为10表示10以下的差距才会修正，但是不会修正10
	 */
	int boundMaxFalseGapBp = 10;
	
	/**
	 * 当ref和this的exon都在两端时，如果两个外显子的边界差距在指定bp以内(譬如50bp以内)，就不修正
	 * 如下<br>
	 * //--------(10)-----20-------------30-----40----------------50---------(60)----------------<br>
		//-----5-----------20--------------30-----40---------------50-------------70-------------<br>
		//-------(10)-----20--------------30----40----------------50----------(60)-------------<br>
		 * 注意只修正一个端点，也就是靠近内侧的，如括弧所标注
		 *差距是小于但不等于 所以设定为10表示10以下的差距才会修正，但是不会修正10
	 */
	int boundMaxFalseGapBpTail = 10;
	
	public ExonClusterBoundInfo(GffGeneIsoInfo gffGeneIsoInfoRef, GffGeneIsoInfo gffGeneIsoInfoThis, int boundMaxFalseGapBp) {
		this.gffGeneIsoInfoRef = gffGeneIsoInfoRef;
		this.gffGeneIsoInfoThis = gffGeneIsoInfoThis;
		this.boundMaxFalseGapBp = boundMaxFalseGapBp;
	}
	public void setTailBoundInfo(int[] tailBoundInfo) {
		this.tailBoundInfo = tailBoundInfo;
	}
	public void setLastExonClusterBoundInfo(ExonClusterBoundInfo lastExonClusterBoundInfo) {
		this.lastExonClusterBoundInfo = lastExonClusterBoundInfo;
	}
	/**
	 * @param lsExonClusters
	 * @param exonClusterNum 本次计算第几个exoncluster，从0开始计算
	 * @return
	 */
	public void setLsExonClustersAndNum(ArrayList<ExonCluster> lsExonClusters, int thisExonClusterNum) {
		this.lsExonClusters = lsExonClusters;
		this.thisExonClusterNum = thisExonClusterNum;
	}
	
	/** start位点是否一致 */
	public boolean isStartUnify() {
		return booStartUnify;
	}
	/** end位点是否一致 */
	public boolean isEndUnify() {
		return booEndUnify;
	}
	/** 起点是否选择Ref */
	public boolean isSelectRefStart() {
		return selectRefStart;
	}
	/** 终点是否选择Ref */
	public boolean isSelectRefEnd() {
		return selectRefEnd;
	}
	/** 获得最后选定的exon */
	public List<ExonInfo> calculate() {
		ExonCluster exonCluster = lsExonClusters.get(thisExonClusterNum);
		List<ExonInfo> lsExonInfosRef = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoRef);
		List<ExonInfo> lsExonInfosThis = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoThis);
		if (lsExonInfosRef == null) {
			return lsExonInfosThis;
		} else if (lsExonInfosThis == null) {
			return lsExonInfosRef;
		}
		if (exonCluster.isSameExon()) {
			booStartUnify = true;
			booEndUnify = true;
			selectRefStart = true;
			selectRefEnd = true;
			return lsExonInfosRef;
		}
		
		calSelectBounds(exonCluster, lsExonInfosRef, lsExonInfosThis);
		setExonInfoBound();
		return getLsExonInfo(lsExonInfosRef, lsExonInfosThis);
	}
	/** 计算所需选择的边界 */
	private void calSelectBounds(ExonCluster exonCluster, List<ExonInfo> lsExonInfosRef, List<ExonInfo> lsExonInfosThis) {
		if (tailBoundInfo[0] > thisExonClusterNum || tailBoundInfo[1] < thisExonClusterNum) {
			if (lsExonInfosRef != null && lsExonInfosRef.size() == 1) {
				selectRefStart = true; selectRefEnd = true;
			}
			else {
				selectRefStart = false; selectRefEnd = false;
			}
		}
		else if (tailBoundInfo[0] == thisExonClusterNum) {
			compareExonStart(lsExonInfosRef, lsExonInfosThis);
		}
		else if (tailBoundInfo[1] >= thisExonClusterNum) {
			if (tailBoundInfo[1] == thisExonClusterNum)
				compareExonEnd(lsExonInfosRef, lsExonInfosThis);
			else
				compExonMidSelect(lsExonInfosRef, lsExonInfosThis);
		}
	}
	/**
	 * 当两个样本进行比较时，直接返回lsExonInfosThis
	 * 也就是用新的转录本替代旧的
	 * 但是如果lsExonInfosThis为空，那就返回lsExonInfosRef
	 * @param lastBound
	 * @param lastSelect
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return int[2]
	 * 0：0 ref起点边界 1 this起点边界
	 * 1：0 ref终点边界 1 this终点边界
	 * @return
	 */
	private void compExonMidSelect(List<ExonInfo> lsExonInfosRef, List<ExonInfo> lsExonInfosThis) {
		selectRefStart = false; selectRefEnd = false;
		//*表示上次选择，括弧表示本次选择
		//--------10-----20*-------------(30)-----40
		//--------10--------25-------------35----40
		//--------10-----20*-------------(30)----40
		if (!lastExonClusterBoundInfo.isEndUnify() && lastExonClusterBoundInfo.isSelectRefEnd()) {
			selectRefStart = true;
			//--------10-----20*-------------------------------(45)-----50------------
			//--------10--------25-------------35----40--------------------------------
			//--------10-----20*-------------------------------(45)----50-------------
			if (lsExonInfosRef.size() == 0 || lsExonInfosThis.size() == 0) {
				selectRefEnd = true;
			}
			else {
				int[] tmpEnd = getExonBound(false, lsExonInfosRef, lsExonInfosThis);
				if (Math.abs(tmpEnd[0] - tmpEnd[1]) <= boundMaxFalseGapBp) {
					selectRefEnd = true;
				}
			}
		}
		else if (lastExonClusterBoundInfo.isEndUnify()) {
			if (lsExonInfosRef.size() == 0 || lsExonInfosThis.size() == 0) {
				selectRefStart = false; selectRefEnd = false;
				return;
			}
			
			int[] tmpStart = getExonBound(true, lsExonInfosRef, lsExonInfosThis);
			if (Math.abs(tmpStart[0] - tmpStart[1]) <= boundMaxFalseGapBp) {
				selectRefStart = true;
			}
			int[] tmpEnd = getExonBound(false, lsExonInfosRef, lsExonInfosThis);
			if (Math.abs(tmpEnd[0] - tmpEnd[1]) <= boundMaxFalseGapBp) {
				selectRefEnd = true;
			}
		}
	}
	/**
	 * 返回左端
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef 必须不为0
	 * @param gffGeneIsoInfoThis
	 * @param lsExonInfosThis 必须不为0
	 * @return 
	 * 0：0 ref起点边界 1 this起点边界
	 * 1：0 ref终点边界 1 this终点边界
	 */
	private void compareExonStart(List<ExonInfo> lsExonInfosRef, List<ExonInfo> lsExonInfosThis) {
		selectRefStart = false; selectRefEnd = false;
		if (lsExonInfosRef.size() == 0 || lsExonInfosThis.size() == 0) {
			return;
		}
		//如果都含有起点exon
		if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(0)) == 0 && gffGeneIsoInfoThis.indexOf(lsExonInfosThis.get(0)) == 0) {
			setMarginBoundOutSide(gffGeneIsoInfoRef.isCis5to3(), true, lsExonInfosRef, lsExonInfosThis);
			setMarginBoundInSide(gffGeneIsoInfoRef.isCis5to3(), true, lsExonInfosRef, lsExonInfosThis);
		}
		else {
			 //--------10-----20-------------(30)-----40-------------------------
			//------------------------------25*---35*--40----------------------- *表示任意这两个边界
			//----------------------------------(30)----40------------------------
			if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(0)) != 0) {
				selectRefStart = true;
			}
			//------------------------------25*---35*--40----------------------- *表示任意这两个边界
			 //--------10-----20-------------(30)-----40-------------------------
			//----------------------------------(30)----40------------------------
			else {
				selectRefStart = false;
			}
		}
	}
	/**
	 * 返回末端
	 * @param lastBound
	 * @param lastSelectInfo
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef 必须不为0
	 * @param gffGeneIsoInfoThis 必须不为0
	 * @param lsExonInfosThis
	 * @return
	 * 0：0 ref起点边界 1 this起点边界<br>
	 * 1：0 ref终点边界 1 this终点边界<br>
	 * @return
	 */
	private void compareExonEnd(List<ExonInfo> lsExonInfosRef, List<ExonInfo> lsExonInfosThis) {
		selectRefStart = false; selectRefEnd = false;
		//*表示上次选择，括弧表示本次选择
		//--------10-----20*-------------(30)-----40
		//--------10--------25-------------35----40
		//--------10-----20*-------------(30)----40
		if (!lastExonClusterBoundInfo.isEndUnify()&& lastExonClusterBoundInfo.isSelectRefEnd()) {
			selectRefStart = true;
		}
		//--------10-----20-------------------------------------50---60
		//--------10-----20-------------35----40
		//--------10-----20-------------35----40-------------50--60
		if (lsExonInfosRef.size() == 0) {
			selectRefEnd = false;
			return;
		}
		else if (lsExonInfosThis.size() == 0) {
			//--------10-----20*-------------35*----40
			//--------10--------25-------------------------------------50---60
			//--------10-----20*-------------35*----40------------------------50--60
			if (selectRefStart == true) {
				selectRefEnd = true;
				return; 
			}
			//--------10-----20-------------35*----40
			//--------10--------25*------------------------------------50*---60
			//--------10--------25*------------------------------------50----60
			selectRefStart = false; selectRefEnd = false;
			return;
		}
		
		if (lastExonClusterBoundInfo.isEndUnify()) {
			int[] tmp = getExonBound(true, lsExonInfosRef, lsExonInfosThis);
			if (Math.abs(tmp[0] - tmp[1]) <= boundMaxFalseGapBp) {
				selectRefStart = true;
			}
			else
				selectRefStart = false;
		}
		
		//如果都是终点
		if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(lsExonInfosRef.size() - 1)) == gffGeneIsoInfoRef.size() - 1 
				&& gffGeneIsoInfoThis.indexOf(lsExonInfosThis.get(lsExonInfosThis.size() - 1)) == gffGeneIsoInfoThis.size() - 1 ) {
			setMarginBoundOutSide(gffGeneIsoInfoRef.isCis5to3(), false, lsExonInfosRef, lsExonInfosThis);
		}
		else {
			//--------10-----20--------------30---------40*----------------------------50*---60
			//--------10--------25--------------35--36
			//--------10--------25--------------35-----40*----------------------------50----60
			if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(lsExonInfosRef.size() - 1)) != gffGeneIsoInfoRef.size() - 1) {
				selectRefEnd = true;
			}
			//--------10-----20-------------35---37----------------------------50----60
			//--------10-----20*--------------35------40
			//--------10-----20-------------35--37-----------------------------50----60
			
			//--------10-----20-------------35---37
			//--------10-----20*------------35------40----------------------50----60
			//--------10-----20-------------35------40----------------------50----60
			else {
				selectRefEnd = false;
			}
		}
	}
	/**
	 * 当ref和this的exon都在边界时，选择exon尾部长的边界
	 * 如下<br>
	 * //--------10-----20-------------30-----40----------------50-----60----------------<br>
		//-----(5)---------20-------------30----40----------------50--------(70)-------------<br>
		//-----(5)---------20-------------30----40----------------50--------(70)-------------<br>
		 * 注意只修正一个端点，也就是靠近外侧的，如括弧所标注
	 * @param selectInfo 输入选择的位点，进行修正
	 * 0：0 ref起点边界 1 this起点边界<br>
	 * 1：0 ref终点边界 1 this终点边界<br>
	 * @param cis
	 * @param start
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return 	
	 */
	private void setMarginBoundOutSide(boolean cis, boolean start, List<ExonInfo> lsExonInfosRef, List<ExonInfo> lsExonInfosThis) {		
		int refBound = 0, thisBound = 0;
		int tmp[] = getExonBound(start, lsExonInfosRef, lsExonInfosThis);
		refBound = tmp[0]; thisBound = tmp[1];
		
		if (cis && start)
			selectRefStart = (refBound <= thisBound + boundMaxFalseGapBpTail ? true:false);
		else if (cis && !start)
			selectRefEnd = (refBound >= thisBound - boundMaxFalseGapBpTail ? true:false);
		else if (!cis && start)
			selectRefStart = (refBound >= thisBound - boundMaxFalseGapBpTail ? true:false);
		else if (!cis && !start)
			selectRefEnd = (refBound <= thisBound + boundMaxFalseGapBpTail ? true:false);
	}
	
	//TODO 以下，考虑是否仅仅选择ref靠近内侧的
	//现在的代码选择ref和this更靠近内侧的
	//考虑添加junction筛选
	/**
	 * 当ref和this的exon都在边界时，如果两个外显子的边界差距在指定bp以内(譬如10bp以内)，就修正为靠近内侧的
	 * 如下<br>
	 * //--------10-------20-------------30-----40----------------50---------60----------------<br>
		//-------10----(18)--------------30-----40-------------------(52)----60-------------<br>
		//-------10------(20)-------------30----40----------------(50)-------60-------------<br>
		 * 注意只修正一个端点，也就是靠近内侧的，如括弧所标注
	 * @param selectInfo 输入选择的位点，进行修正
	 * 0：0 ref起点边界 1 this起点边界<br>
	 * 1：0 ref终点边界 1 this终点边界<br>
	 * @param cis 正向还是反向
	 * @param start 选择的是否为靠前的exon
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return 	
	 */
	private void setMarginBoundInSide(boolean cis, boolean start, List<ExonInfo> lsExonInfosRef, List<ExonInfo> lsExonInfosThis) {		
		int refBound = 0, thisBound = 0;
		int tmp[] = getExonBound(!start, lsExonInfosRef, lsExonInfosThis);
		refBound = tmp[0]; thisBound = tmp[1];

		if (cis && start)
			selectRefEnd = (refBound >= thisBound - boundMaxFalseGapBp ? true:false);
		else if (cis && !start)
			selectRefStart = (refBound <= thisBound + boundMaxFalseGapBp ? true:false);
		else if (!cis && start)
			selectRefEnd = (refBound <= thisBound + boundMaxFalseGapBp ? true:false);
		else if (!cis && !start)
			selectRefStart = (refBound >= thisBound - boundMaxFalseGapBp ? true:false);
	}
	/**
	 * @param start 是否选择的是起点位点
	 * @param lsExonInfosRef size必须大于0
	 * @param lsExonInfosThis size必须大于0
	 * @return
	 * 0：refBound
	 * 1：thisBound
	 */
	private int[] getExonBound(boolean start, List<ExonInfo> lsExonInfosRef, List<ExonInfo> lsExonInfosThis) {
		boolean cis = lsExonInfosRef.get(0).isCis5to3();
		int[] result = new int[2];
		if (start) {
			if (cis) {
				result[0] = lsExonInfosRef.get(0).getStartCis();
				result[1] = lsExonInfosThis.get(0).getStartCis();
			}
			else {
				result[0] = lsExonInfosRef.get(lsExonInfosRef.size() - 1).getStartCis();
				result[1] = lsExonInfosThis.get(lsExonInfosThis.size() - 1).getStartCis();
			}
		}
		else {
			if (cis) {
				result[0] = lsExonInfosRef.get(lsExonInfosRef.size() - 1).getEndCis();
				result[1] = lsExonInfosThis.get(lsExonInfosThis.size() - 1).getEndCis();
			}
			else {
				result[0] = lsExonInfosRef.get(0).getEndCis();
				result[1] = lsExonInfosThis.get(0).getEndCis();
			}
		}
		return result;
	}
	/**
	 * 比较本组exon的边界
	 * @return int[2] 0一致 1不一致<br>
	 * 0: 0左端一致 1左端不一致<br>
	 * 1: 0右端一致 1右端不一致<br>
	 */
	private void setExonInfoBound() {
		booStartUnify = false; booEndUnify = false;
		
		ExonCluster exonCluster = lsExonClusters.get(thisExonClusterNum);
		if (exonCluster.getMapIso2LsExon().size() <= 1) {
			return;
		}
		
		List<List<ExonInfo>> lsLsExonInfo = ArrayOperate.getArrayListValue(exonCluster.getMapIso2LsExon());
		List<ExonInfo> lsExonInfos0 = lsLsExonInfo.get(0);
		List<ExonInfo> lsExonInfos1 = lsLsExonInfo.get(1);
		if (lsExonInfos0.size() == 0 || lsExonInfos1.size() == 0) {
			return;
		}
		if (lsExonInfos0.get(0).getStartCis() == lsExonInfos1.get(0).getStartCis()
			|| Math.abs(lsExonInfos0.get(0).getStartCis() - lsExonInfos1.get(0).getStartCis()) <= boundMaxFalseGapBp	
		) {
			booStartUnify = true;
		}
		if (lsExonInfos0.get(lsExonInfos0.size() - 1).getEndCis() == lsExonInfos1.get(lsExonInfos1.size() - 1).getEndCis()
			|| Math.abs(lsExonInfos0.get(lsExonInfos0.size() - 1).getEndCis() - lsExonInfos1.get(lsExonInfos1.size() - 1).getEndCis()) <= boundMaxFalseGapBp	
		)  {
			booEndUnify = true;
		}
	}
	
	/**
	 * 根据select的信息添加外显子
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return
	 */
	private List<ExonInfo> getLsExonInfo(List<ExonInfo> lsExonInfosRef, List<ExonInfo> lsExonInfosThis) {
		ArrayList<ExonInfo> lsResult = new ArrayList<ExonInfo>();
		if (selectRefStart == true) {
			if (selectRefEnd == true) {
				return lsExonInfosRef;
			}
			
			if (lsExonInfosRef.size() == 0) {
				selectRefEnd = true;
				return new ArrayList<ExonInfo>();
			}
			else {
				if (lsExonInfosThis.size() == 0) {
					selectRefEnd = true;
					return lsExonInfosRef;
				}
				else if (lsExonInfosThis.size() >= 1) {
					ExonInfo exonInfo = new ExonInfo();
					exonInfo.setCis5to3(lsExonInfosThis.get(0).isCis5to3());
					exonInfo.setStartCis(lsExonInfosRef.get(0).getStartCis());
					exonInfo.setEndCis(lsExonInfosThis.get(0).getEndCis());
					exonInfo.setParentListAbs(gffGeneIsoInfoRef);
					lsResult.add(exonInfo);
					for (int i = 1; i < lsExonInfosThis.size(); i++) {
						lsResult.add(lsExonInfosThis.get(i));
					}
				}
			}
		}
		else {
			if (selectRefEnd == false) {
				return lsExonInfosThis;
			}
			else {
				if (lsExonInfosThis.size() == 0 || lsExonInfosRef.size() == 0) {
					selectRefEnd = false;
					return lsExonInfosThis;
				}
				for (int i = 0; i < lsExonInfosThis.size() - 1; i++) {
					lsResult.add(lsExonInfosThis.get(i));
				}
				ExonInfo exonInfo = new ExonInfo();
				exonInfo.setCis5to3(lsExonInfosThis.get(0).isCis5to3());
				exonInfo.setStartCis(lsExonInfosThis.get(lsExonInfosThis.size() - 1).getStartCis());
				exonInfo.setEndCis(lsExonInfosRef.get(lsExonInfosRef.size() - 1).getEndCis());
				exonInfo.setParentListAbs(gffGeneIsoInfoRef);
				lsResult.add(exonInfo);
			}
		}
		return lsResult;
	}
}
