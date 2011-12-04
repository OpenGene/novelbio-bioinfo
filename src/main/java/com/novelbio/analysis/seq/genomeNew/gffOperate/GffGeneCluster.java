package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.chipseq.repeatMask.repeatRun;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.CompSubArrayCluster;
import com.novelbio.base.dataStructure.CompSubArrayInfo;

/**
 * ר�����ڴ�����Ӣ��ת¼���ؽ��������Ѿ��۳�cluster����Ϣ��Ȼ�����
 * @author zong0jie
 *
 */
public class GffGeneCluster {
	private static Logger logger = Logger.getLogger(GffGeneCluster.class);
	
	GffGeneCluster gffGeneClusterUp;
	GffGeneCluster gffGeneClusterDown;
	
	int start = 0;
	int end = 0;
	
	
	GffDetailGene gffDetailGene1;
	GffDetailGene gffDetailGene2;
	ArrayList<GffDetailGene> lsGffGeneThis;
	ArrayList<GffDetailGene> lsGffGeneComp;
	MapReads mapReads = new MapReads(1, "");
	/**
	 * cufflink��ת¼��
	 */
	GffHashGene gffHashThisCufflink;
	/**
	 * refGene��ת¼��
	 */
	GffHashGene gffHashRef;
	public GffGeneCluster(GffHashGene gffHashCufflink, GffHashGene gffHashRef, ArrayList<GffDetailGene> lsGffGeneThis, ArrayList<GffDetailGene> lsGffGeneComp)
	{
		this.gffHashThisCufflink = gffHashCufflink;
		this.gffHashRef = gffHashRef;
		this.lsGffGeneComp = lsGffGeneComp;
		this.lsGffGeneThis = lsGffGeneThis;
		if (lsGffGeneThis != null && lsGffGeneThis.size() > 0) {
			gffDetailGene1 = lsGffGeneThis.get(0);
		}
		if (lsGffGeneComp != null && lsGffGeneComp.size() > 0) {
			gffDetailGene2 = lsGffGeneComp.get(0);
		}
		if (gffDetailGene1 != null && gffDetailGene2 != null) {
			start = Math.min(gffDetailGene1.getNumberstart(),gffDetailGene2.getNumberstart());
		}
	}
	
	
	
	public GffDetailGene getCombGffDetail() {
		if (gffDetailGene1 == null) {
			return gffDetailGene2;
		}
		if (gffDetailGene2 == null) {
			return gffDetailGene1;
		}
		
		GffDetailGene gffDetailGeneNew = new GffDetailGene(gffDetailGene2.getChrID(), gffDetailGene2.getLocString(), gffDetailGene1.isCis5to3());
		if ((lsGffGeneThis == null || lsGffGeneThis.size() == 0)&& lsGffGeneComp != null) {
			return lsGffGeneComp.get(0);
		}
		else if (lsGffGeneThis != null && (lsGffGeneComp == null || lsGffGeneComp.size() == 0)) {
			return lsGffGeneThis.get(0);
		}
		combinIso();
		ArrayList<GffGeneIsoInfo> lsGffIso = gffDetailGene1.getLsCodSplit();
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffIso) {
			GffGeneIsoInfo gffSameIso = findSameIso(gffGeneIsoInfo, gffDetailGene2);
			 compIso(gffGeneIsoInfo, gffSameIso);
			 if (gffGeneIsoInfo.getIsoInfo().size() == 0) {
				continue;
			}
			gffDetailGeneNew.addIso(gffGeneIsoInfo);
		}
		if (gffDetailGeneNew.getLsCodSplit().size() == 0) {
			return null;
		}
		return gffDetailGeneNew;
	}
	
	
	
	/**
	 * �ϲ�ת¼������һ��cluster�����ת¼��ͷβ������
	 */
	private void combinIso()
	{
		for (int i = 1; i < lsGffGeneThis.size(); i++) {
			GffDetailGene gffDetailGene = lsGffGeneThis.get(i);
			gffDetailGene1.addIso(gffDetailGene);
		}
		for (int i = 1; i < lsGffGeneComp.size(); i++) {
			GffDetailGene gffDetailGene = lsGffGeneComp.get(i);
			gffDetailGene2.addIso(gffDetailGene);
		}
	}
	
	
	/**
	 * ָ��ת¼������ָ����GffDetailGene������֮��ӽ���ת¼��
	 * @param gffGeneIsoInfo
	 * @return
	 */
	private GffGeneIsoInfo findSameIso(GffGeneIsoInfo gffGeneIsoInfo, GffDetailGene gffDetailGene)
	{
		TreeMap<Double, GffGeneIsoInfo> mapGffIso = new TreeMap<Double, GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfoSub : gffDetailGene.getLsCodSplit()) {
			mapGffIso.put(gffGeneIsoInfo.compIso(gffGeneIsoInfoSub),gffGeneIsoInfoSub);
		}
		return mapGffIso.firstEntry().getValue();
	}
	
	/**
	 * �Ƚ�����ת¼��֮�����ͬ��Ȼ��ϲ���gffGeneIsoInfoIn�����һ������ŵ�ת¼��
	 */
	private void compIso(GffGeneIsoInfo gffGeneIsoInfoIn, GffGeneIsoInfo gffGeneIsoInfoCmp)
	{
		if (gffGeneIsoInfoIn == null) {
			gffGeneIsoInfoIn = gffGeneIsoInfoCmp;
			return;
		}
		if (gffGeneIsoInfoCmp == null) {
			return;
		}
		if (gffGeneIsoInfoCmp.getIsoName().contains("ENSGALT00000021833")) {
			System.out.println(gffGeneIsoInfoCmp.getIsoName());
		}
		
		ArrayList<int[]> lsIsoFinal = new ArrayList<int[]>();
		
		ArrayList<CompSubArrayCluster> lsCmpArrayClusters = gffGeneIsoInfoIn.compIsoLs(gffGeneIsoInfoCmp);
		double meanregion = 40; //mapReads.regionMean(gffGeneIsoInfoIn.getChrID(), gffGeneIsoInfoIn.getIsoInfo());
		boolean highExp = true;
		if (meanregion < 30) {
			highExp = false;
		}
		for (int i = 0; i < lsCmpArrayClusters.size(); i++) {
			CompSubArrayCluster compSubArrayCluster = lsCmpArrayClusters.get(i);
			int[] exonAfter = null;
			///////////////////////////////////////////////////////��ú�һ��exon/////////////////////////////////////////////////////
			for (int j = i+1; j < lsCmpArrayClusters.size(); j++) {
				CompSubArrayCluster compSubArrayClusterNext = lsCmpArrayClusters.get(j);
				if (compSubArrayClusterNext.getLsCompSubArrayInfosThis() == null || compSubArrayClusterNext.getLsCompSubArrayInfosThis().size() == 0 ) {
					continue;
				}
				else {
					exonAfter = new int[2];
					exonAfter[0] = (int) compSubArrayClusterNext.getLsCompSubArrayInfosThis().get(0).getStart();
					exonAfter[1] = (int) compSubArrayClusterNext.getLsCompSubArrayInfosThis().get(0).getEnd();
					break;
				}
			}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			int[] exonBefore = null;
			if (lsIsoFinal.size()>0) {
				exonBefore = lsIsoFinal.get(lsIsoFinal.size() -1);
			}
			
			boolean inAnotherIso = false; boolean inUpIso = false;
			
			//����exon�Ƿ��ڱ��ת¼����
			GffCodGene gffCodGene = gffHashRef.searchLocation(gffDetailGene1.getChrID(), (int)compSubArrayCluster.getStartSite());
			if (gffDetailGene1.isCis5to3() && gffCodGene.isInsideUp()
			||
			!gffDetailGene1.isCis5to3() && gffCodGene.isInsideDown()
			) {
				inAnotherIso = true; inUpIso = true;
			}
			if (gffDetailGene1.isCis5to3() && gffCodGene.isInsideDown()
					||		
					!gffDetailGene1.isCis5to3() && gffCodGene.isInsideUp()
			) {
				inAnotherIso = true; inUpIso = false;
			}
			ArrayList<int[]> lsexon = getExonInfoAll(compSubArrayCluster, exonBefore,exonAfter, inAnotherIso, inUpIso, highExp);
			//TODO �Ƿ�Ҫ�жϸ�lsexon�������е�lsFinalҪС��
			lsIsoFinal.addAll(lsexon);
		}
		gffGeneIsoInfoIn.setLsIsoform(lsIsoFinal);
		gffGeneIsoInfoIn.setIsoName(gffGeneIsoInfoCmp.getIsoName());
	}
	
	
	
	
	
	
	/**
	 * ����һ��exon����Ϣ�����ؾ����Iso��Ϣ
	 * @param compSubArrayCluster ����һ��exon��
	 * @param inAnotherIso ��exon�Ƿ��ڱ�Ļ�����
	 * @param highExp �û����Ƿ�߱���߱���Ļ�����Ҫ�ϸ���junction���ͱ���Ĳ���Ҫ�ϸ���ת¼��
	 * @param UTR �ǲ�����ͷ������β����
	 * @return
	 */
	private ArrayList<int[]> getExonInfo(CompSubArrayCluster compSubArrayClusterThis
			,int[] exonBefore ,boolean inAnotherIso,boolean inUpIso,boolean highExp)
	{
		String chrID = "";
		if (gffDetailGene1 != null) {
			chrID = gffDetailGene1.getChrID();
		}
		else {
			chrID = gffDetailGene2.getChrID();
		}
		ArrayList<int[]> lsResultExon = new ArrayList<int[]>();
		
		ArrayList<CompSubArrayInfo> lsExonThis = compSubArrayClusterThis.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonComp = compSubArrayClusterThis.getLsCompSubArrayInfosComp();
 
		int[] exon = new int[2];
		//�߱���
		if (highExp) {
			if (inAnotherIso) {
				if (lsExonThis.size() > 0) {
					for (CompSubArrayInfo compSubArrayInfo : lsExonThis) {
						//�������һ����junction
						if (inUpIso && getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())
						||
						!inUpIso && exonBefore != null && getJunctionSite(chrID,(int)compSubArrayInfo.getStart())
						) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//û��junction˵�����������������⣬��ɾ��
						else {
							lsResultExon.clear();
						}
					}
				}
				else
				{
//					return lsResultExon;
				}
			}
			else {
				if (lsExonThis.size() > 0) {
					for (CompSubArrayInfo compSubArrayInfo : lsExonThis) {
						//�������һ����junction
						if (exonBefore != null && getJunctionSite(chrID,(int)compSubArrayInfo.getStart())) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						else if (exonBefore == null&& getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//û��junction˵�����������������⣬��ɾ��
						else {
							logger.error("��exon������û����ϵ" + chrID+" "+(int)compSubArrayInfo.getStart());
							lsResultExon.clear();
						}
					}
				}
				else {
//					return lsResultExon;
				}
			}
		}
		else{
			if (inAnotherIso) {
				if (lsExonThis.size() > 0) {
					for (CompSubArrayInfo compSubArrayInfo : lsExonThis) {
						//�������һ����junction
						if (inUpIso && getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())
						||
						!inUpIso && getJunctionSite(chrID,(int)compSubArrayInfo.getStart())
						) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//û��junction˵�����������������⣬��ɾ��
						else {
							lsResultExon.clear();
						}
					}
				}
				else {
				}
			}
			else {
				if (lsExonThis.size() > 0) {
					for (CompSubArrayInfo compSubArrayInfo : lsExonThis) {
						//��exon��lsResultExon�����exon�н����Ļ�������
						if (lsResultExon.size() > 0 && 
								(gffDetailGene1.isCis5to3() && compSubArrayInfo.getStart() < exon[0]
							||
							!gffDetailGene1.isCis5to3() && compSubArrayInfo.getStart() > exon[0])
						    )
						{
							continue;
						}
						//�����junction
						if (exonBefore != null && getJunctionSite(chrID,(int)compSubArrayInfo.getStart())) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						else if (exonBefore == null&& getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//û��junction˵�����������������⣬����comp������
						else {
							for (CompSubArrayInfo compSubArrayInfo2 : lsExonComp) {
								if (lsResultExon.size() > 0 && 
								(gffDetailGene1.isCis5to3() && compSubArrayInfo2.getStart() > exon[0]
								||
								!gffDetailGene1.isCis5to3() && compSubArrayInfo2.getStart() < exon[0])
								) {
									exon = new int[2];
									exon[0] = (int) compSubArrayInfo2.getStart();
									exon[1] = (int) compSubArrayInfo2.getEnd();
									lsResultExon.add(exon);
								}
							}
						}
					}
				}
				else {
					for (CompSubArrayInfo compSubArrayInfo : lsExonComp) {
						exon[0] = (int) compSubArrayInfo.getStart();
						exon[1] = (int) compSubArrayInfo.getEnd();
						lsResultExon.add(exon);
					}
				}
			}
		}
		return lsResultExon;
	}
	
	
	/**
	 * ����һ��exon����Ϣ�����ؾ����Iso��Ϣ
	 * @param compSubArrayCluster ����һ��exon��
	 * @param inAnotherIso ��exon�Ƿ��ڱ�Ļ�����
	 * @param highExp �û����Ƿ�߱���߱���Ļ�����Ҫ�ϸ���junction���ͱ���Ĳ���Ҫ�ϸ���ת¼��
	 * @param UTR �ǲ�����ͷ������β����
	 * @return
	 */
	private ArrayList<int[]> getExonInfoAll(CompSubArrayCluster compSubArrayClusterThis
			,int[] exonBefore ,int exonAfter[], boolean inAnotherIso,boolean inUpIso,boolean highExp)
	{
		String chrID = "";
		if (gffDetailGene1 != null) {
			chrID = gffDetailGene1.getChrID();
		}
		else {
			chrID = gffDetailGene2.getChrID();
		}
		ArrayList<int[]> lsResultExon = new ArrayList<int[]>();
		
		ArrayList<CompSubArrayInfo> lsExonThis = compSubArrayClusterThis.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonComp = compSubArrayClusterThis.getLsCompSubArrayInfosComp();
		
		int[] exon = new int[2];
		//�߱���
		if (highExp) {
			if (inAnotherIso) {
				if (lsExonThis.size() > 0) {
					for (CompSubArrayInfo compSubArrayInfo : lsExonThis) {
						//�������һ����junction
						if (inUpIso && getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())
						||
						!inUpIso && exonBefore != null && getJunctionSite(chrID,exonBefore[1], (int)compSubArrayInfo.getStart())
						) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//û��junction˵�����������������⣬��ɾ��
						else {
							lsResultExon.clear();
						}
					}
				}
				else
				{
//					return lsResultExon;
				}
			}
			else {
				if (lsExonThis.size() > 0) {
					for (CompSubArrayInfo compSubArrayInfo : lsExonThis) {
						//�������һ����junction
						if (
						exonBefore != null && getJunctionSite(chrID,(int)compSubArrayInfo.getStart())
						||
			    		//���ߺͺ���һ��exon��jun
						exonAfter != null && getJunctionSite(chrID,(int)compSubArrayInfo.getStart(), exonAfter[0])
						) 
						 {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						else if (exonBefore == null&& getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//û��junction˵�����������������⣬��ɾ��
						else {
							logger.error("��exon������û����ϵ" + chrID+" "+(int)compSubArrayInfo.getStart());
							lsResultExon.clear();
						}
					}
				}
				else {
//					return lsResultExon;
				}
			}
		}
		else{
			if (inAnotherIso) {
				if (lsExonThis.size() > 0) {
					for (CompSubArrayInfo compSubArrayInfo : lsExonThis) {
						//�������һ����junction
						if (inUpIso && getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())
						||
						!inUpIso && exonBefore != null && getJunctionSite(chrID,exonBefore[1], (int)compSubArrayInfo.getStart())
						) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//û��junction˵�����������������⣬��ɾ��
						else {
							lsResultExon.clear();
						}
					}
				}
				else {
				}
			}
			else {
				if (lsExonThis.size() > 0) {
					for (CompSubArrayInfo compSubArrayInfo : lsExonThis) {
						//��exon��lsResultExon�����exon�н����Ļ�������
						if (lsResultExon.size() > 0 && 
								(gffDetailGene1.isCis5to3() && compSubArrayInfo.getStart() < exon[0]
							||
							!gffDetailGene1.isCis5to3() && compSubArrayInfo.getStart() > exon[0])
						    )
						{
							//�����ϲ����������������
							logger.error("exon��lsResultExon�����exon�н�����"+ chrID + exon[0] + exon[1]);
							continue;
						}
						//�����junction����ǰ���exon��jun
						if (exonBefore != null && getJunctionSite(chrID,(int)compSubArrayInfo.getStart())
						||
						//���ߺͺ���һ��exon��jun
						exonAfter != null && getJunctionSite(chrID,(int)compSubArrayInfo.getStart(), exonAfter[0])
						) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						else if (exonBefore == null&& getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//û��junction˵�����������������⣬����comp������
						else {
							for (CompSubArrayInfo compSubArrayInfo2 : lsExonComp) {
								if (lsResultExon.size() > 0) {
									if ((gffDetailGene1.isCis5to3() && compSubArrayInfo2.getStart() > exon[0] || !gffDetailGene1 .isCis5to3() && compSubArrayInfo2.getStart() < exon[0])) {
										exon = new int[2];
										exon[0] = (int) compSubArrayInfo2 .getStart();
										exon[1] = (int) compSubArrayInfo2 .getEnd();
										lsResultExon.add(exon);
									}
								}
								else {
									exon = new int[2];
									exon[0] = (int) compSubArrayInfo2.getStart();
									exon[1] = (int) compSubArrayInfo2.getEnd();
									lsResultExon.add(exon);
								}
							}
						}
					}
				}
				else {
					//���ȿ���exon����һ��exon֮���Ƿ���junction
					if (exonBefore!= null && exonAfter!= null && getJunctionSite(chrID, exonBefore[1], exonAfter[0])) {
						lsResultExon.clear();
					}
					else {
						for (CompSubArrayInfo compSubArrayInfo : lsExonComp) {
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
					}
				}
			}
		}
		return lsResultExon;
	}
	
	
	/**
	 * 
	 * ����һ��exon����Ϣ�����ؾ����Iso��Ϣ
	 * @param compSubArrayCluster ����һ��exon��
	 * @param inAnotherIso ��exon�Ƿ��ڱ�Ļ�����
	 * @param highExp �û����Ƿ�߱���߱���Ļ�����Ҫ�ϸ���junction���ͱ���Ĳ���Ҫ�ϸ���ת¼��
	 * @param UTR �ǲ�����ͷ������β����
	 * @return
	 */
	private ArrayList<int[]> getExonInfoNew(CompSubArrayCluster compSubArrayClusterThis, 
			CompSubArrayCluster compSubArrayClusterNext,int[] exonBefore ,boolean inAnotherIso,boolean highExp, boolean UTR)
	{
		ArrayList<CompSubArrayInfo> lsExonThis = compSubArrayClusterThis.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonComp = compSubArrayClusterThis.getLsCompSubArrayInfosComp();
		
		ArrayList<CompSubArrayInfo> lsExonNextThis = compSubArrayClusterNext.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonNextComp = compSubArrayClusterNext.getLsCompSubArrayInfosComp();
		//���鶼ֻ��һ��exon
		if (lsExonThis.size() == 1 && lsExonComp.size() == 1) {
			double[] overlapInfo = ArrayOperate.cmpArray(lsExonThis.get(0).getCell(),lsExonComp.get(0).getCell());
			//�������exonһģһ��
			if (overlapInfo[0] == 0) {
				int[] exon = new int[]{(int)lsExonThis.get(0).getStart(),(int)lsExonThis.get(0).getEnd()};
				ArrayList<int[]> lsResult = new ArrayList<int[]>();
				lsResult.add(exon);
				return lsResult;
			}
			else {
				int[] exon = new int[2];
				//���ȿ���cufflink�ģ�Ҳ����this�ģ����this�м���λ�㣬�ͷ���
				//�������
				if (getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonThis.get(0).getStart())) {
					exon[0] = (int)lsExonThis.get(0).getStart();
				}
				else if (getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonComp.get(0).getStart())) {
					exon[0] = (int)lsExonComp.get(0).getStart();
				}
				else {
					if (inAnotherIso) {
						logger.error("û�ҵ����ʵ�junction����������һ��ת¼���ڣ�" + gffDetailGene1.getChrID() + " " + (int)lsExonThis.get(0).getStart());
						return null;
					}
					logger.error("û�ҵ����ʵ�junction����ref��λ�ã�" + gffDetailGene1.getChrID() + " " + (int)lsExonThis.get(0).getStart());
					exon[0] = (int)lsExonComp.get(0).getStart();
				}
				//�����յ�
				if (getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonThis.get(0).getEnd())) {
					exon[1] = (int)lsExonThis.get(0).getEnd();
				}
				else if (getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonComp.get(0).getEnd())) {
					exon[1] = (int)lsExonComp.get(0).getEnd();
				}
				else {
					if (inAnotherIso) {
						logger.error("û�ҵ����ʵ�junction����������һ��ת¼���ڣ�" + gffDetailGene1.getChrID() + " " + (int)lsExonThis.get(0).getEnd());
						return null;
					}
					logger.error("û�ҵ����ʵ�junction����ref��λ�ã�" + gffDetailGene1.getChrID() + " " + (int)lsExonThis.get(0).getEnd());
					exon[1] = (int)lsExonComp.get(0).getEnd();
				}
				ArrayList<int[]> lsResult = new ArrayList<int[]>();
				lsResult.add(exon);
				return lsResult;
			}
		}
		//��һ����һ��exon���ڶ����кܶ��
		if (lsExonThis.size() == 1 && lsExonComp.size() != 1) {
			int[] exon = new int[2];
			ArrayList<int[]> ls0List = mapReads.region0Info(gffDetailGene1.getChrID(), (int)lsExonThis.get(0).getStart(), (int)lsExonThis.get(0).getEnd());
			if (ls0List.size() <= 0) {
				if (getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonThis.get(0).getStart())) {
					exon[0] = (int)lsExonThis.get(0).getStart();
				}
				else if (getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonComp.get(0).getStart())) {
					exon[0] = (int)lsExonComp.get(0).getStart();
				}
			}
		}
		
		 
		
		
		
		
		
		
		
		
		
		return null;
	}

	/**
	 * 
	 * ����һ��exon����Ϣ�����ؾ����Iso��Ϣ
	 * @param compSubArrayCluster ����һ��exon��
	 * @param inAnotherIso ��exon�Ƿ��ڱ�Ļ�����
	 * @param highExp �û����Ƿ�߱���߱���Ļ�����Ҫ�ϸ���junction���ͱ���Ĳ���Ҫ�ϸ���ת¼��
	 * @param UTR �ǲ�����ͷ������β����
	 * @return
	 */
	private ArrayList<int[]> getExonInfo2222(CompSubArrayCluster compSubArrayClusterThis, 
			CompSubArrayCluster compSubArrayClusterNext,int[] exonBefore ,boolean inAnotherIso,boolean highExp, boolean UTR)
	{
		ArrayList<CompSubArrayInfo> lsExonThis1 = compSubArrayClusterThis.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonThis2 = compSubArrayClusterThis.getLsCompSubArrayInfosComp();
		
		ArrayList<CompSubArrayInfo> lsExonNext1 = compSubArrayClusterNext.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonNext2 = compSubArrayClusterNext.getLsCompSubArrayInfosComp();
		//˵���ǵ�һ��exon
		if (exonBefore == null) {
			//�����һ��û�ж�Ӧ��5UTR�����һ����ǹ�exon--��ת¼����ֹһ��exon
			if ((lsExonThis1.size() == 0 || lsExonThis2.size() == 0) && compSubArrayClusterNext != null) {
				if (gffDetailGene1.isCis5to3()) {
					
					//����UTR���ڵ�exon����һ��exon֮���ǲ���ֱ������������������Ϳ��Ժϲ���
					ArrayList<int[]> lsInfo = mapReads.region0Info(gffDetailGene1.getChrID(), (int)compSubArrayClusterThis.getEndSite(), (int)compSubArrayClusterNext.getStartSite());
					if (lsInfo.size() == 0) {
						ArrayList<Integer> lsJunct = getJunctionSite(gffDetailGene1.getChrID(), (int)compSubArrayClusterThis.getEndSite());
						if (lsJunct.size() == 0) {
							int[] exon = new int[2];
							exon[0] = (int)compSubArrayClusterThis.getStartSite();
							exon[1] = (int)compSubArrayClusterNext.getStartSite();
							lsInfo.add(exon);
							return lsInfo;
						}
						else {
							
							
						}
						
					}
					
				}
				
				
				
				
			}
			
			
		}
		return null;
	}
	//UTR�����exon
	private ArrayList<int[]> getExonInfoUTRcis( CompSubArrayCluster compSubArrayClusterThis, CompSubArrayCluster compSubArrayClusterNext, boolean inAnotherIso) {
		
		
		
		
		
		
		
		ArrayList<CompSubArrayInfo> lsExonThis1 = compSubArrayClusterThis.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonThis2 = compSubArrayClusterThis.getLsCompSubArrayInfosComp();
		
		ArrayList<CompSubArrayInfo> lsExonNext1 = compSubArrayClusterNext.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonNext2 = compSubArrayClusterNext.getLsCompSubArrayInfosComp();

		if ((lsExonThis1.size() == 0 || lsExonThis2.size() == 0) && compSubArrayClusterNext != null) {
			// ����UTR���ڵ�exon����һ��exon֮���ǲ���ֱ������������������Ϳ��Ժϲ���
			ArrayList<int[]> lsInfo = mapReads.region0Info( gffDetailGene1.getChrID(), (int) compSubArrayClusterThis.getEndSite(), (int) compSubArrayClusterNext.getStartSite());
			if (lsInfo.size() == 0) {
				//����junctionλ��
				ArrayList<Integer> lsJunct = getJunctionSite( gffDetailGene1.getChrID(), (int) compSubArrayClusterThis.getEndSite());
				//û��junctionλ��
				if (lsJunct.size() == 0) {
					int[] exon = new int[2];
					exon[0] = (int) compSubArrayClusterThis.getStartSite();
					exon[1] = (int) compSubArrayClusterNext.getStartSite();
					lsInfo.add(exon);
					return lsInfo;
				} else {

				}
			}
		}
		return null;
	}
	
	///////////////////// ��ȡ junction  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static HashMap<String, ArrayList<Integer>> hashJunction = new HashMap<String, ArrayList<Integer>>();
	static HashMap<String,Integer> hashJunctionBoth = new HashMap<String,Integer>();
	/**
	 * ��ȡjunction�ļ�
	 * @param junctionFile
	 */
	public static void geneInso(String junctionFile) {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(junctionFile, false);
		for (String string : txtReadandWrite.readfileLs()) {
			if (string.startsWith("track")) {
				continue;
			}
			String[] ss = string.split("\t");
			//junctionλ�㶼�趨��exon��
			int junct1 = Integer.parseInt(ss[1]) + Integer.parseInt(ss[10].split(",")[0]);
			int junct2 = Integer.parseInt(ss[2]) - Integer.parseInt(ss[10].split(",")[1]) + 1;
			String strjunct1 = ss[0].toLowerCase() +"//"+junct1;
			String strjunct2 = ss[0].toLowerCase() +"//"+ junct2;
			String strJunBoth = strjunct1 + "///" + strjunct2;
			hashJunctionBoth.put(strJunBoth, Integer.parseInt(ss[4]));
			if (hashJunction.containsKey(strjunct1)) {
				ArrayList<Integer> lsJun2 = hashJunction.get(strjunct1);
				lsJun2.add(junct2);
			}
			else {
				ArrayList<Integer> lsJun2 = new ArrayList<Integer>();
				lsJun2.add(junct2);
				hashJunction.put(strjunct1, lsJun2);
			}
			if (hashJunction.containsKey(strjunct2)) {
				ArrayList<Integer> lsJun2 = hashJunction.get(strjunct2);
				lsJun2.add(junct1);
			}
			else {
				ArrayList<Integer> lsJun2 = new ArrayList<Integer>();
				lsJun2.add(junct1);
				hashJunction.put(strjunct2, lsJun2);
			}
		}
	}
	
	
	/**
	 * ���������λ�㣬�ҳ�locsite
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	private static boolean getJunctionSite(String chrID, int locSite)
	{
		if (hashJunction.containsKey(chrID.toLowerCase()+"//"+locSite) )
		{
			return true;
		}
		else {
			return false;
		}
	}
	/**
	 * ���������λ�㣬�ҳ�locsite
	 * @param chrID
	 * @param locStartSite ����νǰ���ڲ��Զ��ж�
	 * @param locEndSite
	 * @return
	 */
	private static boolean getJunctionSite(String chrID, int locStartSite, int locEndSite)
	{
		int locS = Math.min(locStartSite, locEndSite);
		int locE = Math.max(locStartSite, locEndSite);
		String key = chrID.toLowerCase() + "//" + locS +"///"+chrID.toLowerCase() + "//" + locE;
		if (hashJunctionBoth.containsKey(key) )
		{
			return true;
		}
		else {
			return false;
		}
	}
}