package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.analysis.seq.genome.gffOperate.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.dataStructure.ArrayOperate;

public class GffGeneCluster {
	/** �Ƿ���reference��GffDetailGene */
	boolean isContainsRef = true;
	Boolean sameExon = null;
	String chrID;
	int startLoc = 0;
	int endLoc = 0;
	/**
	 * ��ref��this��exon���ڱ߽�ʱ��������������ӵı߽�����ָ��bp����(Ʃ��10bp����)��������Ϊ�����ڲ��
	 * ����<br>
	 * //--------10-------20-------------30-----40----------------50---------60----------------<br>
		//-------10----(18)--------------30-----40-------------------(52)----60-------------<br>
		//-------10------(20)-------------30----40----------------(50)-------60-------------<br>
		 * ע��ֻ����һ���˵㣬Ҳ���ǿ����ڲ�ģ�����������ע
		 *�����С�ڵ������� �����趨Ϊ10��ʾ10���µĲ��Ż����������ǲ�������10
	 */
	int boundMaxFalseGapBp = 9;
	/** �Ƿ��Ѿ���refgff������isContainsRef�� 
	 * ������㷨�����Ƚ�refGffGene������lsGeneCluster����ǰ�档
	 * Ȼ���������Ref���򽫵�һλ��lsGeneCluster������lsGenesRef
	 * ���������Ref�������LsGene������LsGenesRef
	 * ��ô�����ǩ������ȷ���ò����Ƿ��Ѿ����
	 * */
	boolean setRefLsGene = false;
	/** ������Ľ���������� */
	ArrayList<GffDetailGene> lsCombGenesResult;
	/**
	 * ��ѡ����Ҫ����������GffDetailGeneList������ѡ��RefGffGene
	 * �趨��ʱ��ὫlsGeneCluster����Ķ�Ӧ��Ŀɾ��
	 */
	ArrayList<GffDetailGene> lsGenesRef;
	/**
	 * list--���в�ͬ��Դ��Gffhash<br>
	 * list--ÿ��GffHash��GffDetail<br>
	 * <br>
	 * �����������RefGffGene������ڵ�һ��λ�á���ô���õ�һ��λ�õ�GffDetailGene���бȽ�
	 * �������û��RefGffGene����ѡ���������һ��GffDetailGene���бȽ�<br>
	 * <br>
	 * �������reference<b>���е�һ��һ����Reference��GffGene</b>
	 */
	ArrayList<ArrayList<GffDetailGene>> lsGeneCluster = new ArrayList<ArrayList<GffDetailGene>>();
	/**������ӵ�GffHash������ */
	ArrayList<String> lsListGffName = new ArrayList<String>();
	
	double likelyhood = 0.7;
	
	ArrayList<ArrayList<ExonClusterBoundInfo>> lsIso2ExonBoundInfoStatistics = new ArrayList<ArrayList<ExonClusterBoundInfo>>();
	/**
	 * �Ƿ���refGffDetailGene��û��˵��Ref���������loc
	 * @param isContainsRef
	 */
	public void setIsContainsRef(boolean isContainsRef) {
		this.isContainsRef = isContainsRef;
	}
	public void addLsGffDetailGene(String isoName, ArrayList<GffDetailGene> lsGffDetailGenes) {
		lsGeneCluster.add(lsGffDetailGenes);
		lsListGffName.add(isoName);
	}
	/** ��������Ref��This����GffHash��ʱ������� */
	public ArrayList<GffDetailGene> getThisGffGene() {
		setRefGffGene();
		if (isContainsRef) {
			if (lsGeneCluster.size() == 0) {
				return new ArrayList<GffDetailGene>();
			}
			else {
				return lsGeneCluster.get(0);
			}
		}
		else {
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
	
	/** ������κõ�GffDetailGene
	 * @return
	 */
	public ArrayList<GffDetailGene> getCombinedGffGene() {
		if (lsCombGenesResult == null) {
			setRefGffGene();
			lsCombGenesResult = compareAndModify_GffGene();
		}
		return lsCombGenesResult;
	}
	/**
	 * ��ref��this��exon���ڱ߽�ʱ��������������ӵı߽�����ָ��bp����(Ʃ��10bp����)��������Ϊ�����ڲ��
	 * ����<br>
	 * //--------10-------20-------------30-----40----------------50---------60----------------<br>
		//-------10----(18)--------------30-----40-------------------(52)----60-------------<br>
		//-------10------(20)-------------30----40----------------(50)-------60-------------<br>
		 * ע��ֻ����һ���˵㣬Ҳ���ǿ����ڲ�ģ�����������ע
		 *�����С�ڵ������� �����趨Ϊ10��ʾ10���µĲ��Ż����������ǲ�������10
		 *�趨��ֵ�Զ�ȡ����ֵ�����ô���20
	 */
	public void setBoundMaxFalseGapBp(int boundMaxFalseGapBp) {
		boundMaxFalseGapBp = Math.abs(boundMaxFalseGapBp);
		if (boundMaxFalseGapBp > 20) {
			boundMaxFalseGapBp = 20;
		}
		this.boundMaxFalseGapBp = boundMaxFalseGapBp;
	}
	
	/** �趨lsGenesRef��ͬʱ��lsGeneCluster�ж�Ӧ��Ŀɾ��
	 * Ϊ�ϲ�����׼��
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
				if (gffDetailGene.Length() > lengthGffGene) {
					lengthGffGene = gffDetailGene.Length();
					index = i;
				}
			}
		}
		return index;
	}

	private ArrayList<GffDetailGene> compareAndModify_GffGene() {
		ArrayList<GffDetailGene> lsGffDetailGenes = new ArrayList<GffDetailGene>();
		for (GffDetailGene gffDetailGeneRefRaw : lsGenesRef) {//����ÿ��GffDetail
			GffDetailGene gffDetailGeneRef = gffDetailGeneRefRaw.clone();
			GffDetailGene gffDetailGeneResult = gffDetailGeneRefRaw.clone();
			gffDetailGeneResult.clearIso();
			
			HashSet<String> setGffIsoRefSelectName = new HashSet<String>();//����ѡ�е�Iso�����֣�Ҳ������cufflinkԤ���ת¼�����Ƶ�ת¼��

			for (ArrayList<GffDetailGene> lsgffArrayList : lsGeneCluster) {//���GffCluster����ÿ��GffHash��list�����һ��ֻ��һ��GffHash
				for (GffDetailGene gffDetailGeneCalculate : lsgffArrayList) {//�����һ��GffHash�����GffDetailGene
					for (GffGeneIsoInfo gffIsoThis : gffDetailGeneCalculate.getLsCodSplit()) {//������GffDetailGene��ת¼��������ѡ����ӽ��Ľ��бȽ�	
						GffGeneIsoInfo gffIsoRef = gffDetailGeneRef.getSimilarIso(gffIsoThis, likelyhood);
						
						if (gffIsoRef == null) 
							continue;
						
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
		return lsGffDetailGenes;
	}
	/**
	 * public�����������ṩ��Junit����ʹ��
	 * �Ƚ�����������ͬ���н�����GffGeneIso����Ϣ������gffGeneIsoInfoRef
	 * @param gffGeneIsoInfoRef
	 * @param gffGeneIsoInfoThis
	 * @return ����ȫ�µ�GffGeneIsoInfo
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
		ArrayList<ExonClusterBoundInfo> lsExonBoundInfoStatistics = new ArrayList<ExonClusterBoundInfo>();//����ͳ��exon����������
		
		ExonClusterBoundInfo lastExonClusterBoundInfo = new ExonClusterBoundInfo(gffGeneIsoInfoRef, gffGeneIsoInfoThis, boundMaxFalseGapBp);
		lastExonClusterBoundInfo.booStartUnify = true;
		lastExonClusterBoundInfo.booEndUnify = true;

		for (int exonClusterNum = 0; exonClusterNum < lsExonClusters.size(); exonClusterNum++) {
			ExonClusterBoundInfo exonClusterBoundInfo = new ExonClusterBoundInfo(gffGeneIsoInfoRef, gffGeneIsoInfoThis, boundMaxFalseGapBp);
			exonClusterBoundInfo.setLasteExonClusterBoundInfo(lastExonClusterBoundInfo);
			exonClusterBoundInfo.setLsExonClustersAndNum(lsExonClusters, exonClusterNum);
			exonClusterBoundInfo.setTailBoundInfo(tailBoundInfo);
			
			ArrayList<ExonInfo> lsExonInfos = exonClusterBoundInfo.calculate();
			gffGeneIsoInfoResult.addAll(lsExonInfos);
			
			lsExonBoundInfoStatistics.add(exonClusterBoundInfo);
			lastExonClusterBoundInfo = exonClusterBoundInfo;
		}
		//TODO
		lsIso2ExonBoundInfoStatistics.add(lsExonBoundInfoStatistics);
		return gffGeneIsoInfoResult;
	}
	/**
	 * lsExonClusters������ת¼���ȽϵĽ��������ͼ<br>
	 * 0----1-----2-----3-----4<br>
	 * A----B-----C-----D----E ��һ��ת¼��<br>
	 * *----*-----a-----b �ڶ���ת¼��<br>
	 * �������н�����ֵ����ͽ�����ֵ��յ����꣬Ҳ����a��b�����꣬��0��ʼ
	 * ��ô���Ƿ���2��3
	 * @param lsExonClusters
	 * @return
	 */
	private int[] getBothStartNumEndNum(ArrayList<ExonCluster> lsExonClusters) {
		int start = -1, end = -1;
		boolean start1 = false; boolean start2 = false;
		boolean end1 = false; boolean end2 = false;
		for (int i = 0; i < lsExonClusters.size(); i++) {
			ExonCluster exonCluster = lsExonClusters.get(i);
			ArrayList<ArrayList<ExonInfo>> lsLsExonInfo = ArrayOperate.getArrayListValue(exonCluster.getMapIso2LsExon());
			ArrayList<ExonInfo> lsExonInfo1 = lsLsExonInfo.get(0);
			ArrayList<ExonInfo> lsExonInfo2 = lsLsExonInfo.get(1);
			if (lsExonInfo1.size() > 0) {
				start1 = true;
			}
			if (lsExonInfo2.size() > 0) {
				start2 = true;
			}
			if (start1 && start2) {
				start = i;
				break;
			}
		}
		for (int i = lsExonClusters.size() - 1; i >= 0; i--) {
			ExonCluster exonCluster = lsExonClusters.get(i);
			ArrayList<ArrayList<ExonInfo>> lsLsExonInfo = ArrayOperate.getArrayListValue(exonCluster.getMapIso2LsExon());
			ArrayList<ExonInfo> lsExonInfo1 = lsLsExonInfo.get(0);
			ArrayList<ExonInfo> lsExonInfo2 = lsLsExonInfo.get(1);
			if (lsExonInfo1.size() > 0) {
				end1 = true;
			}
			if (lsExonInfo2.size() > 0) {
				end2 = true;
			}
			if (end1 && end2) {
				end = i;
				break;
			}
		}
		return new int[]{start, end};
	}
}
/**
 * �洢ĳ��exoncluster��ref��this��exon�������յ��Ƿ�һ��
 * @author zong0jie
 */
class ExonClusterBoundInfo {
	GffGeneIsoInfo gffGeneIsoInfoRef;
	GffGeneIsoInfo gffGeneIsoInfoThis;
	/** ��һ��λ���״̬��Ϣ */
	ExonClusterBoundInfo lastExonClusterBoundInfo;
	/** startλ����һ�µ� */
	boolean booStartUnify = true;
	/** endλ����һ�µ� */
	boolean booEndUnify = true;
	/** ���ѡ��Ref */
	boolean selectRefStart = true;
	/** �յ�ѡ��Ref */
	boolean selectRefEnd = true;
	
	/** �������Ƚϵ�ExonCluster��Ϣ */
	ArrayList<ExonCluster> lsExonClusters;
	/** ����exoncluster��λ�ã���0��ʼ���� */
	int thisExonClusterNum;
	
	/**
	 * lsExonClusters������ת¼���ȽϵĽ��������ͼ<br>
	 * 0----1-----2-----3-----4<br>
	 * A----B-----C-----D----E ��һ��ת¼��<br>
	 * *----*-----a-----b �ڶ���ת¼��<br>
	 * �������н�����ֵ����ͽ�����ֵ��յ����꣬Ҳ����a��b�����꣬��0��ʼ
	 * ��ô���Ƿ���2��3
	 */
	int[] tailBoundInfo;
	
	/**
	 * ��ref��this��exon���ڱ߽�ʱ��������������ӵı߽�����ָ��bp����(Ʃ��10bp����)��������Ϊ�����ڲ��
	 * ����<br>
	 * //--------10-------20-------------30-----40----------------50---------60----------------<br>
		//-------10----(18)--------------30-----40-------------------(52)----60-------------<br>
		//-------10------(20)-------------30----40----------------(50)-------60-------------<br>
		 * ע��ֻ����һ���˵㣬Ҳ���ǿ����ڲ�ģ�����������ע
		 *�����С�ڵ������� �����趨Ϊ10��ʾ10���µĲ��Ż����������ǲ�������10
	 */
	int boundMaxFalseGapBp = 1;
	
	/**
	 * ��ref��this��exon��������ʱ��������������ӵı߽�����ָ��bp����(Ʃ��50bp����)���Ͳ�����
	 * ����<br>
	 * //--------(10)-----20-------------30-----40----------------50---------(60)----------------<br>
		//-----5-----------20--------------30-----40---------------50-------------70-------------<br>
		//-------(10)-----20--------------30----40----------------50----------(60)-------------<br>
		 * ע��ֻ����һ���˵㣬Ҳ���ǿ����ڲ�ģ�����������ע
		 *�����С�ڵ������� �����趨Ϊ10��ʾ10���µĲ��Ż����������ǲ�������10
	 */
	int boundMaxFalseGapBpTail = 150;
	
	public ExonClusterBoundInfo(GffGeneIsoInfo gffGeneIsoInfoRef, GffGeneIsoInfo gffGeneIsoInfoThis, int boundMaxFalseGapBp) {
		this.gffGeneIsoInfoRef = gffGeneIsoInfoRef;
		this.gffGeneIsoInfoThis = gffGeneIsoInfoThis;
		this.boundMaxFalseGapBp = boundMaxFalseGapBp;
	}
	public void setTailBoundInfo(int[] tailBoundInfo) {
		this.tailBoundInfo = tailBoundInfo;
	}
	public void setLasteExonClusterBoundInfo(ExonClusterBoundInfo lastExonClusterBoundInfo) {
		this.lastExonClusterBoundInfo = lastExonClusterBoundInfo;
	}
	/**
	 * @param lsExonClusters
	 * @param exonClusterNum ���μ���ڼ���exoncluster����0��ʼ����
	 * @return
	 */
	public void setLsExonClustersAndNum(ArrayList<ExonCluster> lsExonClusters, int thisExonClusterNum) {
		this.lsExonClusters = lsExonClusters;
		this.thisExonClusterNum = thisExonClusterNum;
	}
	
	/** startλ���Ƿ�һ�� */
	public boolean isStartUnify() {
		return booStartUnify;
	}
	/** endλ���Ƿ�һ�� */
	public boolean isEndUnify() {
		return booEndUnify;
	}
	/** ����Ƿ�ѡ��Ref */
	public boolean isSelectRefStart() {
		return selectRefStart;
	}
	/** �յ��Ƿ�ѡ��Ref */
	public boolean isSelectRefEnd() {
		return selectRefEnd;
	}
	/** ������ѡ����exon */
	public ArrayList<ExonInfo> calculate() {
		ExonCluster exonCluster = lsExonClusters.get(thisExonClusterNum);
		ArrayList<ExonInfo> lsExonInfosRef = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoRef);
		ArrayList<ExonInfo> lsExonInfosThis = exonCluster.getMapIso2LsExon().get(gffGeneIsoInfoThis);
		
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
	/** ��������ѡ��ı߽� */
	private void calSelectBounds(ExonCluster exonCluster, ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {
		if (tailBoundInfo[0] > thisExonClusterNum || tailBoundInfo[1] < thisExonClusterNum) {
			if (lsExonInfosRef.size() == 1) {
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
	 * �������������бȽ�ʱ��ֱ�ӷ���lsExonInfosThis
	 * Ҳ�������µ�ת¼������ɵ�
	 * �������lsExonInfosThisΪ�գ��Ǿͷ���lsExonInfosRef
	 * @param lastBound
	 * @param lastSelect
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return int[2]
	 * 0��0 ref���߽� 1 this���߽�
	 * 1��0 ref�յ�߽� 1 this�յ�߽�
	 * @return
	 */
	private void compExonMidSelect(ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {
		selectRefStart = false; selectRefEnd = false;
		//*��ʾ�ϴ�ѡ��������ʾ����ѡ��
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
	 * �������
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef ���벻Ϊ0
	 * @param gffGeneIsoInfoThis
	 * @param lsExonInfosThis ���벻Ϊ0
	 * @return 
	 * 0��0 ref���߽� 1 this���߽�
	 * 1��0 ref�յ�߽� 1 this�յ�߽�
	 */
	private void compareExonStart(ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {
		selectRefStart = false; selectRefEnd = false;
		if (lsExonInfosRef.size() == 0 || lsExonInfosThis.size() == 0) {
			return;
		}
		//������������exon
		if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(0)) == 0 && gffGeneIsoInfoThis.indexOf(lsExonInfosThis.get(0)) == 0) {
			setMarginBoundOutSide(gffGeneIsoInfoRef.isCis5to3(), true, lsExonInfosRef, lsExonInfosThis);
			setMarginBoundInSide(gffGeneIsoInfoRef.isCis5to3(), true, lsExonInfosRef, lsExonInfosThis);
		}
		else {
			 //--------10-----20-------------(30)-----40-------------------------
			//------------------------------25*---35*--40----------------------- *��ʾ�����������߽�
			//----------------------------------(30)----40------------------------
			if (gffGeneIsoInfoRef.indexOf(lsExonInfosRef.get(0)) != 0) {
				selectRefStart = true;
			}
			//------------------------------25*---35*--40----------------------- *��ʾ�����������߽�
			 //--------10-----20-------------(30)-----40-------------------------
			//----------------------------------(30)----40------------------------
			else {
				selectRefStart = false;
			}
		}
	}
	/**
	 * ����ĩ��
	 * @param lastBound
	 * @param lastSelectInfo
	 * @param gffGeneIsoInfoRef
	 * @param lsExonInfosRef ���벻Ϊ0
	 * @param gffGeneIsoInfoThis ���벻Ϊ0
	 * @param lsExonInfosThis
	 * @return
	 * 0��0 ref���߽� 1 this���߽�<br>
	 * 1��0 ref�յ�߽� 1 this�յ�߽�<br>
	 * @return
	 */
	private void compareExonEnd(ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {
		selectRefStart = false; selectRefEnd = false;
		//*��ʾ�ϴ�ѡ��������ʾ����ѡ��
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
		
		//��������յ�
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
	 * ��ref��this��exon���ڱ߽�ʱ��ѡ��exonβ�����ı߽�
	 * ����<br>
	 * //--------10-----20-------------30-----40----------------50-----60----------------<br>
		//-----(5)---------20-------------30----40----------------50--------(70)-------------<br>
		//-----(5)---------20-------------30----40----------------50--------(70)-------------<br>
		 * ע��ֻ����һ���˵㣬Ҳ���ǿ������ģ�����������ע
	 * @param selectInfo ����ѡ���λ�㣬��������
	 * 0��0 ref���߽� 1 this���߽�<br>
	 * 1��0 ref�յ�߽� 1 this�յ�߽�<br>
	 * @param cis
	 * @param start
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return 	
	 */
	private void setMarginBoundOutSide(boolean cis, boolean start, ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {		
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
	
	//TODO ���£������Ƿ����ѡ��ref�����ڲ��
	//���ڵĴ���ѡ��ref��this�������ڲ��
	//�������junctionɸѡ
	/**
	 * ��ref��this��exon���ڱ߽�ʱ��������������ӵı߽�����ָ��bp����(Ʃ��10bp����)��������Ϊ�����ڲ��
	 * ����<br>
	 * //--------10-------20-------------30-----40----------------50---------60----------------<br>
		//-------10----(18)--------------30-----40-------------------(52)----60-------------<br>
		//-------10------(20)-------------30----40----------------(50)-------60-------------<br>
		 * ע��ֻ����һ���˵㣬Ҳ���ǿ����ڲ�ģ�����������ע
	 * @param selectInfo ����ѡ���λ�㣬��������
	 * 0��0 ref���߽� 1 this���߽�<br>
	 * 1��0 ref�յ�߽� 1 this�յ�߽�<br>
	 * @param cis �����Ƿ���
	 * @param start ѡ����Ƿ�Ϊ��ǰ��exon
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return 	
	 */
	private void setMarginBoundInSide(boolean cis, boolean start, ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {		
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
	 * @param lsExonInfosRef size�������0
	 * @param lsExonInfosThis size�������0
	 * @param start �Ƿ�ѡ��������λ��
	 * @return
	 * 0��refBound
	 * 1��thisBound
	 */
	private int[] getExonBound(boolean start, ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {
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
	 * �Ƚϱ���exon�ı߽�
	 * @return int[2] 0һ�� 1��һ��<br>
	 * 0: 0���һ�� 1��˲�һ��<br>
	 * 1: 0�Ҷ�һ�� 1�Ҷ˲�һ��<br>
	 */
	private void setExonInfoBound() {
		booStartUnify = false; booEndUnify = false;
		
		ExonCluster exonCluster = lsExonClusters.get(thisExonClusterNum);
		ArrayList<ArrayList<ExonInfo>> lsLsExonInfo = ArrayOperate.getArrayListValue(exonCluster.getMapIso2LsExon());
		ArrayList<ExonInfo> lsExonInfos0 = lsLsExonInfo.get(0);
		ArrayList<ExonInfo> lsExonInfos1 = lsLsExonInfo.get(1);
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
	 * ����select����Ϣ���������
	 * @param lsExonInfosRef
	 * @param lsExonInfosThis
	 * @return
	 */
	private ArrayList<ExonInfo> getLsExonInfo(ArrayList<ExonInfo> lsExonInfosRef, ArrayList<ExonInfo> lsExonInfosThis) {
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
				lsResult.add(exonInfo);
			}
		}
		return lsResult;
	}
}