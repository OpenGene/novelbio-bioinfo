package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;



/**
 * UCSC konwn gene�Ļ���������Ϣ
 * @author zong0jie
 *
 */
public class GffCodGene extends GffCodAbs<GffDetailGeneCod, GffDetailGene>
{
	protected GffCodGene(String chrID, int Coordinate) {
		super(chrID, Coordinate);
		// TODO Auto-generated constructor stub
	}
	
	
//	public void getCodInfo() {
//		
//		
//	}
//	
//	/**
//	 * ���ظ�gffDetailGene�ľ�����Ϣ
//	 * string[3]
//	 * @param gffDetailGene
//	 */
//	private static void copeGffDetailGene(GffDetailGeneCod gffDetailGene) {
//		if (gffDetailGene.isCodInGenExtend()) {
//			ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfo = gffDetailGene.getLsCodSplit();
//			for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfo) {
//				if (gffGeneIsoInfo.isCodInIsoExtend()) {
//					gffGeneIsoInfo.getCodLocUTR();
//				}
//			}
//		}
//	}
	
	
}
