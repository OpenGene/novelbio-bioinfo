package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;
import org.broadinstitute.sting.utils.exceptions.StingException;

import com.novelbio.analysis.seq.chipseq.repeatMask.repeatRun;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.rnaseq.TophatJunction;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.CompSubArrayCluster;
import com.novelbio.base.dataStructure.CompSubArrayInfo;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 专门用于处理冯英的转录本重建，给定已经聚成cluster的信息，然后聚类
 * @author zong0jie
 *
 */
public class GffGeneCluster {
	static int highExpReads = 90;
	/**
	 * 平均覆盖度到多少算高
	 * @param highExpReads
	 */
	public static void setHighExpReads(int highExpReads) {
		GffGeneCluster.highExpReads = highExpReads;
	}
	
	private static Logger logger = Logger.getLogger(GffGeneCluster.class);
	
	GffGeneCluster gffGeneClusterUp;
	GffGeneCluster gffGeneClusterDown;
	
	int start = 0;
	int end = 0;
	
	public static void setMapReads(String chrLenFile, String mapBed) {
		if (!FileOperate.isFileExist(mapBed)) {
			return;
		}
		mapReads = new MapReads(chrLenFile, 1, mapBed);
		mapReads.setSplit(11, 12);
		mapReads.setFilter(false, -1, 0, false, null);
		try {
			mapReads.ReadMapFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	GffDetailGene gffDetailGene1;
	GffDetailGene gffDetailGene2;
	ArrayList<GffDetailGene> lsGffGeneThis;
	ArrayList<GffDetailGene> lsGffGeneComp;
	static MapReads mapReads;
	
	static TophatJunction tophatJunction = new TophatJunction();
	public static void geneInso(String junctionFile)
	{
		tophatJunction.setJunFile(junctionFile);
	}
	
	
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
		if (gffGeneIsoInfo.getIsoName().contains("01852")) {
			System.out.println("stop");
		}
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
		if (gffGeneIsoInfoIn.isCis5to3() != gffGeneIsoInfoCmp.isCis5to3()) {
			return;
		}
		ArrayList<int[]> lsIsoFinal = new ArrayList<int[]>();
		
		ArrayList<CompSubArrayCluster> lsCmpArrayClusters = gffGeneIsoInfoIn.compIsoLs(gffGeneIsoInfoCmp);
		double meanregion = 0;
		if (mapReads == null) {
			meanregion = highExpReads;
		}
		else {
			try {
				meanregion = mapReads.regionMean(gffGeneIsoInfoIn.getChrID(), gffGeneIsoInfoIn.getIsoInfo());
			} catch (Exception e) {
				logger.error("mapReads 出错：" + gffGeneIsoInfoIn.getChrID() + " " + gffGeneIsoInfoIn.getIsoInfo());
			}
		}
//		double meanregion = mapReads.regionMean(gffGeneIsoInfoIn.getChrID(), gffGeneIsoInfoIn.getIsoInfo());
//		double meanregion = 40;// mapReads.regionMean(gffGeneIsoInfoIn.getChrID(), gffGeneIsoInfoIn.getIsoInfo());
		boolean highExp = true;
		if (meanregion < highExpReads) {
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
			GffCodGene gffCodGene  = gffHashRef.searchLocation(gffDetailGene1.getChrID(), (int)compSubArrayCluster.getStartSite());
			
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
	
	
	
	int inAnotherIsoSupportJunNumHigh = 8;
	int inAnotherIsoSupportJunNumLow = 4;
	int inThisIsoSupportJunNumHigh = 8;
	int inThisIsoSupportJunNumLow = 4;
	
	
	
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
						//如果与下一个有junction。并且支持的reads在4条以上
						if (inUpIso && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getEnd()) > inAnotherIsoSupportJunNumHigh
						||
						!inUpIso && exonBefore != null && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getStart()) > inAnotherIsoSupportJunNumHigh
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
						if (exonBefore != null && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getStart()) > inThisIsoSupportJunNumHigh) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						else if (exonBefore == null&& tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getEnd()) > inThisIsoSupportJunNumHigh) {
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
						if (inUpIso && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getEnd()) > inAnotherIsoSupportJunNumLow
						||
						!inUpIso && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getStart()) > inAnotherIsoSupportJunNumLow
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
						if (exonBefore != null && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getStart()) > inThisIsoSupportJunNumLow) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						else if (exonBefore == null&& tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getEnd()) > inThisIsoSupportJunNumLow) {
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
		int smallInt = 20;
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
		
		if (lsExonThis != null && lsExonThis.size() > 0 && lsExonComp != null && lsExonComp.size() > 0 ) {
			CompSubArrayInfo startThis = lsExonThis.get(0);
			CompSubArrayInfo startComp = lsExonComp.get(0);
			
			CompSubArrayInfo endThis = lsExonThis.get(lsExonThis.size()-1);
			CompSubArrayInfo endComp = lsExonComp.get(lsExonComp.size()-1);
			
			double cut = Math.abs(startThis.getStart() - startComp.getStart());
			if (cut > 0 && cut < smallInt) {
				int junThis = tophatJunction.getJunctionSite(chrID,(int)startThis.getStart());
				int junCmp = tophatJunction.getJunctionSite(chrID,(int)startComp.getStart());
				if (junThis < junCmp/7 || junThis < inThisIsoSupportJunNumHigh) {
					startThis.setStart((int)startComp.getStart());
				}
			}
			
			double cutend = Math.abs(endThis.getEnd() - endComp.getEnd());
			if (cutend > 0 && cutend < smallInt) {
				int junThis = tophatJunction.getJunctionSite(chrID,(int)endThis.getEnd());
				int junCmp = tophatJunction.getJunctionSite(chrID,(int)endComp.getEnd());
				if (junThis < junCmp/7 || junThis < inThisIsoSupportJunNumHigh) {
					endThis.setEnd((int)endComp.getEnd());
				}
			}
		}
	
	
		
		
		
		
		int[] exon = new int[2];
		//高表达
		if (highExp) {
			if (inAnotherIso) {
				if (lsExonThis.size() > 0) {
					for (CompSubArrayInfo compSubArrayInfo : lsExonThis) {
						//如果与下一个有junction
						if (inUpIso && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getEnd()) > inAnotherIsoSupportJunNumHigh
						||
						!inUpIso && exonBefore != null && tophatJunction.getJunctionSite(chrID,exonBefore[1], (int)compSubArrayInfo.getStart()) > inAnotherIsoSupportJunNumHigh
						) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//没有junction说明这个基因可能有问题，就删除
						else{
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
					for (int i = 0; i < lsExonThis.size(); i++) {
						CompSubArrayInfo compSubArrayInfo = lsExonThis.get(i);
						//如果与上一个有junction
						if (
						exonBefore != null && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getStart()) > inThisIsoSupportJunNumHigh
						||
			    		//或者和后面一个exon有jun
						exonAfter != null && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getEnd(), exonAfter[0]) > inThisIsoSupportJunNumHigh
						)
						 {
							exon = new int[2];
							exon[0] = (int)compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						else if (exonBefore == null&& tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getEnd()) > inThisIsoSupportJunNumHigh) {
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
						if (inUpIso && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getEnd()) > inAnotherIsoSupportJunNumLow
						||
						!inUpIso && exonBefore != null && tophatJunction.getJunctionSite(chrID,exonBefore[1], (int)compSubArrayInfo.getStart()) > inAnotherIsoSupportJunNumLow
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
						if (exonBefore != null && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getStart()) > inThisIsoSupportJunNumLow
						||
						//或者和后面一个exon有jun
						exonAfter != null && tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getEnd(), exonAfter[0]) > inThisIsoSupportJunNumLow
						) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						else if (exonBefore == null&& tophatJunction.getJunctionSite(chrID,(int)compSubArrayInfo.getEnd()) > inThisIsoSupportJunNumLow) {
							exon = new int[2];
							exon[0] = (int) compSubArrayInfo.getStart();
							exon[1] = (int) compSubArrayInfo.getEnd();
							lsResultExon.add(exon);
						}
						//没有junction说明这个基因可能有问题，就用comp来补充
						else {/** 不进行补全
							for (CompSubArrayInfo compSubArrayInfo2 : lsExonComp) {
								if (lsResultExon.size() > 0) {
									if ((gffDetailGene1.isCis5to3()
											&& compSubArrayInfo2.getStart() > exon[0] || !gffDetailGene1
											.isCis5to3()
											&& compSubArrayInfo2.getStart() < exon[0])) {
										exon = new int[2];
										exon[0] = (int) compSubArrayInfo2
												.getStart();
										exon[1] = (int) compSubArrayInfo2
												.getEnd();
										lsResultExon.add(exon);
									}
								} else {
									exon = new int[2];
									exon[0] = (int) compSubArrayInfo2
											.getStart();
									exon[1] = (int) compSubArrayInfo2.getEnd();
									lsResultExon.add(exon);
								}
							}
						*/}
					}
				}
				else {
					//首先看上exon与下一个exon之间是否有junction
					if (exonBefore!= null && exonAfter!= null && tophatJunction.getJunctionSite(chrID, exonBefore[1], exonAfter[0]) > inThisIsoSupportJunNumLow) {
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
		//TODO:看一下ENSGALT00000021374 这个为什么5UTR煤油延长
		//延长5UTR
		if (lsResultExon.size() > 0 && exonBefore == null && lsExonComp.size() > 0) {
			int[] tmpexon = lsResultExon.get(0);
			if (tmpexon[0] > tmpexon[1]) {
				tmpexon[0] = Math.max(tmpexon[0], (int)lsExonComp.get(0).getStart());
			}
			else {
				tmpexon[0] = Math.min(tmpexon[0], (int)lsExonComp.get(0).getStart());
			}
		}
		//延长3UTR
		if (lsResultExon.size() > 0 && exonAfter == null && lsExonComp.size() > 0) {
			int[] tmpexon = lsResultExon.get(lsResultExon.size() - 1);
			if (tmpexon[0] > tmpexon[1]) {
				tmpexon[1] = Math.min(tmpexon[1], (int)lsExonComp.get(lsExonComp.size() - 1).getEnd());
			}
			else {
				tmpexon[1] = Math.max(tmpexon[1], (int)lsExonComp.get(lsExonComp.size() - 1).getEnd());
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
				if (tophatJunction.getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonThis.get(0).getStart()) > 0) {
					exon[0] = (int)lsExonThis.get(0).getStart();
				}
				else if (tophatJunction.getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonComp.get(0).getStart()) > 0) {
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
				if (tophatJunction.getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonThis.get(0).getEnd()) > 0) {
					exon[1] = (int)lsExonThis.get(0).getEnd();
				}
				else if (tophatJunction.getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonComp.get(0).getEnd()) > 0) {
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
				if (tophatJunction.getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonThis.get(0).getStart()) > 0) {
					exon[0] = (int)lsExonThis.get(0).getStart();
				}
				else if (tophatJunction.getJunctionSite(gffDetailGene1.getChrID(),(int)lsExonComp.get(0).getStart()) > 0) {
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
						ArrayList<Integer> lsJunct = tophatJunction.getJunctionSite(gffDetailGene1.getChrID(), (int)compSubArrayClusterThis.getEndSite());
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
				ArrayList<Integer> lsJunct = getJunctionSite( gffDetailGene1.getChrID(), (int) compSubArrayClusterThis.getEndSite(), cond);
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
	

}
