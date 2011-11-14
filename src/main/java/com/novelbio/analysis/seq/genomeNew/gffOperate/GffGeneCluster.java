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
 * 专门用于处理冯英的转录本重建，给定已经聚成cluster的信息，然后聚类
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
	 * cufflink的转录本
	 */
	GffHashGene gffHashThisCufflink;
	/**
	 * refGene的转录本
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
	 * 合并转录本，将一个cluster里面的转录本头尾连起来
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
	 * 指定转录本，在指定的GffDetailGene查找与之最接近的转录本
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
	 * 比较两个转录本之间的异同，然后合并到gffGeneIsoInfoIn，获得一个最可信的转录本
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
			///////////////////////////////////////////////////////获得后一个exon/////////////////////////////////////////////////////
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
			
			//看该exon是否在别的转录本内
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
			//TODO 是否要判断该lsexon都比已有的lsFinal要小？
			lsIsoFinal.addAll(lsexon);
		}
		gffGeneIsoInfoIn.setLsIsoform(lsIsoFinal);
		gffGeneIsoInfoIn.setIsoName(gffGeneIsoInfoCmp.getIsoName());
	}
	
	
	
	
	
	
	/**
	 * 给定一组exon的信息，返回具体的Iso信息
	 * @param compSubArrayCluster 输入一个exon组
	 * @param inAnotherIso 该exon是否在别的基因内
	 * @param highExp 该基因是否高表达，高表达的基因需要严格检查junction，低表达的不需要严格检查转录本
	 * @param UTR 是不是最头或者最尾部的
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
		//高表达
		if (highExp) {
			if (inAnotherIso) {
				if (lsExonThis.size() > 0) {
					for (CompSubArrayInfo compSubArrayInfo : lsExonThis) {
						//如果与下一个有junction
						if (inUpIso && getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())
						||
						!inUpIso && exonBefore != null && getJunctionSite(chrID,(int)compSubArrayInfo.getStart())
						) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//没有junction说明这个基因可能有问题，就删除
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
						//如果与上一个有junction
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
						//没有junction说明这个基因可能有问题，就删除
						else {
							logger.error("本exon与上面没有联系" + chrID+" "+(int)compSubArrayInfo.getStart());
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
						//如果与下一个有junction
						if (inUpIso && getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())
						||
						!inUpIso && getJunctionSite(chrID,(int)compSubArrayInfo.getStart())
						) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//没有junction说明这个基因可能有问题，就删除
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
						//本exon与lsResultExon里面的exon有交集的话就跳过
						if (lsResultExon.size() > 0 && 
								(gffDetailGene1.isCis5to3() && compSubArrayInfo.getStart() < exon[0]
							||
							!gffDetailGene1.isCis5to3() && compSubArrayInfo.getStart() > exon[0])
						    )
						{
							continue;
						}
						//如果有junction
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
						//没有junction说明这个基因可能有问题，就用comp来补充
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
	 * 给定一组exon的信息，返回具体的Iso信息
	 * @param compSubArrayCluster 输入一个exon组
	 * @param inAnotherIso 该exon是否在别的基因内
	 * @param highExp 该基因是否高表达，高表达的基因需要严格检查junction，低表达的不需要严格检查转录本
	 * @param UTR 是不是最头或者最尾部的
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
		//高表达
		if (highExp) {
			if (inAnotherIso) {
				if (lsExonThis.size() > 0) {
					for (CompSubArrayInfo compSubArrayInfo : lsExonThis) {
						//如果与下一个有junction
						if (inUpIso && getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())
						||
						!inUpIso && exonBefore != null && getJunctionSite(chrID,exonBefore[1], (int)compSubArrayInfo.getStart())
						) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//没有junction说明这个基因可能有问题，就删除
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
						//如果与上一个有junction
						if (
						exonBefore != null && getJunctionSite(chrID,(int)compSubArrayInfo.getStart())
						||
			    		//或者和后面一个exon有jun
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
						//没有junction说明这个基因可能有问题，就删除
						else {
							logger.error("本exon与上面没有联系" + chrID+" "+(int)compSubArrayInfo.getStart());
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
						//如果与下一个有junction
						if (inUpIso && getJunctionSite(chrID,(int)compSubArrayInfo.getEnd())
						||
						!inUpIso && exonBefore != null && getJunctionSite(chrID,exonBefore[1], (int)compSubArrayInfo.getStart())
						) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//没有junction说明这个基因可能有问题，就删除
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
						//本exon与lsResultExon里面的exon有交集的话就跳过
						if (lsResultExon.size() > 0 && 
								(gffDetailGene1.isCis5to3() && compSubArrayInfo.getStart() < exon[0]
							||
							!gffDetailGene1.isCis5to3() && compSubArrayInfo.getStart() > exon[0])
						    )
						{
							//理论上不会有这种情况出现
							logger.error("exon与lsResultExon里面的exon有交集："+ chrID + exon[0] + exon[1]);
							continue;
						}
						//如果有junction，和前面的exon有jun
						if (exonBefore != null && getJunctionSite(chrID,(int)compSubArrayInfo.getStart())
						||
						//或者和后面一个exon有jun
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
						//没有junction说明这个基因可能有问题，就用comp来补充
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
					//首先看上exon与下一个exon之间是否有junction
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
	 * 给定一组exon的信息，返回具体的Iso信息
	 * @param compSubArrayCluster 输入一个exon组
	 * @param inAnotherIso 该exon是否在别的基因内
	 * @param highExp 该基因是否高表达，高表达的基因需要严格检查junction，低表达的不需要严格检查转录本
	 * @param UTR 是不是最头或者最尾部的
	 * @return
	 */
	private ArrayList<int[]> getExonInfoNew(CompSubArrayCluster compSubArrayClusterThis, 
			CompSubArrayCluster compSubArrayClusterNext,int[] exonBefore ,boolean inAnotherIso,boolean highExp, boolean UTR)
	{
		ArrayList<CompSubArrayInfo> lsExonThis = compSubArrayClusterThis.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonComp = compSubArrayClusterThis.getLsCompSubArrayInfosComp();
		
		ArrayList<CompSubArrayInfo> lsExonNextThis = compSubArrayClusterNext.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonNextComp = compSubArrayClusterNext.getLsCompSubArrayInfosComp();
		//两组都只有一个exon
		if (lsExonThis.size() == 1 && lsExonComp.size() == 1) {
			double[] overlapInfo = ArrayOperate.cmpArray(lsExonThis.get(0).getCell(),lsExonComp.get(0).getCell());
			//如果两组exon一模一样
			if (overlapInfo[0] == 0) {
				int[] exon = new int[]{(int)lsExonThis.get(0).getStart(),(int)lsExonThis.get(0).getEnd()};
				ArrayList<int[]> lsResult = new ArrayList<int[]>();
				lsResult.add(exon);
				return lsResult;
			}
			else {
				int[] exon = new int[2];
				//首先考察cufflink的，也就是this的，如果this有剪接位点，就返回
				//考察起点
				if (getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonThis.get(0).getStart())) {
					exon[0] = (int)lsExonThis.get(0).getStart();
				}
				else if (getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonComp.get(0).getStart())) {
					exon[0] = (int)lsExonComp.get(0).getStart();
				}
				else {
					if (inAnotherIso) {
						logger.error("没找到合适的junction，而且在另一个转录本内：" + gffDetailGene1.getChrID() + " " + (int)lsExonThis.get(0).getStart());
						return null;
					}
					logger.error("没找到合适的junction，用ref的位置：" + gffDetailGene1.getChrID() + " " + (int)lsExonThis.get(0).getStart());
					exon[0] = (int)lsExonComp.get(0).getStart();
				}
				//考察终点
				if (getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonThis.get(0).getEnd())) {
					exon[1] = (int)lsExonThis.get(0).getEnd();
				}
				else if (getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonComp.get(0).getEnd())) {
					exon[1] = (int)lsExonComp.get(0).getEnd();
				}
				else {
					if (inAnotherIso) {
						logger.error("没找到合适的junction，而且在另一个转录本内：" + gffDetailGene1.getChrID() + " " + (int)lsExonThis.get(0).getEnd());
						return null;
					}
					logger.error("没找到合适的junction，用ref的位置：" + gffDetailGene1.getChrID() + " " + (int)lsExonThis.get(0).getEnd());
					exon[1] = (int)lsExonComp.get(0).getEnd();
				}
				ArrayList<int[]> lsResult = new ArrayList<int[]>();
				lsResult.add(exon);
				return lsResult;
			}
		}
		//第一组有一个exon，第二组有很多个
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
	 * 给定一组exon的信息，返回具体的Iso信息
	 * @param compSubArrayCluster 输入一个exon组
	 * @param inAnotherIso 该exon是否在别的基因内
	 * @param highExp 该基因是否高表达，高表达的基因需要严格检查junction，低表达的不需要严格检查转录本
	 * @param UTR 是不是最头或者最尾部的
	 * @return
	 */
	private ArrayList<int[]> getExonInfo2222(CompSubArrayCluster compSubArrayClusterThis, 
			CompSubArrayCluster compSubArrayClusterNext,int[] exonBefore ,boolean inAnotherIso,boolean highExp, boolean UTR)
	{
		ArrayList<CompSubArrayInfo> lsExonThis1 = compSubArrayClusterThis.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonThis2 = compSubArrayClusterThis.getLsCompSubArrayInfosComp();
		
		ArrayList<CompSubArrayInfo> lsExonNext1 = compSubArrayClusterNext.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonNext2 = compSubArrayClusterNext.getLsCompSubArrayInfosComp();
		//说明是第一个exon
		if (exonBefore == null) {
			//如果有一个没有对应的5UTR，并且还不是孤exon--该转录本不止一个exon
			if ((lsExonThis1.size() == 0 || lsExonThis2.size() == 0) && compSubArrayClusterNext != null) {
				if (gffDetailGene1.isCis5to3()) {
					
					//看该UTR所在的exon与下一个exon之间是不是直接相连，如果相连，就可以合并了
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
	//UTR区域的exon
	private ArrayList<int[]> getExonInfoUTRcis( CompSubArrayCluster compSubArrayClusterThis, CompSubArrayCluster compSubArrayClusterNext, boolean inAnotherIso) {
		
		
		
		
		
		
		
		ArrayList<CompSubArrayInfo> lsExonThis1 = compSubArrayClusterThis.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonThis2 = compSubArrayClusterThis.getLsCompSubArrayInfosComp();
		
		ArrayList<CompSubArrayInfo> lsExonNext1 = compSubArrayClusterNext.getLsCompSubArrayInfosThis();
		ArrayList<CompSubArrayInfo> lsExonNext2 = compSubArrayClusterNext.getLsCompSubArrayInfosComp();

		if ((lsExonThis1.size() == 0 || lsExonThis2.size() == 0) && compSubArrayClusterNext != null) {
			// 看该UTR所在的exon与下一个exon之间是不是直接相连，如果相连，就可以合并了
			ArrayList<int[]> lsInfo = mapReads.region0Info( gffDetailGene1.getChrID(), (int) compSubArrayClusterThis.getEndSite(), (int) compSubArrayClusterNext.getStartSite());
			if (lsInfo.size() == 0) {
				//查找junction位点
				ArrayList<Integer> lsJunct = getJunctionSite( gffDetailGene1.getChrID(), (int) compSubArrayClusterThis.getEndSite());
				//没有junction位点
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
	
	///////////////////// 读取 junction  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static HashMap<String, ArrayList<Integer>> hashJunction = new HashMap<String, ArrayList<Integer>>();
	static HashMap<String,Integer> hashJunctionBoth = new HashMap<String,Integer>();
	/**
	 * 读取junction文件
	 * @param junctionFile
	 */
	public static void geneInso(String junctionFile) {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(junctionFile, false);
		for (String string : txtReadandWrite.readfileLs()) {
			if (string.startsWith("track")) {
				continue;
			}
			String[] ss = string.split("\t");
			//junction位点都设定在exon上
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
	 * 给定坐标和位点，找出locsite
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
	 * 给定坐标和位点，找出locsite
	 * @param chrID
	 * @param locStartSite 无所谓前后，内部自动判断
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
