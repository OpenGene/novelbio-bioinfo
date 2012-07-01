package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.generalConf.NovelBioConst;
/**
 * 给定snp和indel等信息，获得改变的氨基酸等
 * @author zong0jie
 *
 */
public class GffChrSnpIndel {
	GffChrAbs gffChrAbs;
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 根据indel或者snp注释上该snp的信息
	 * @param mapInfo
	 * @return
	 */
	public GffCodGene getSnpIndel(MapInfoSnpIndel mapInfo) {
		if (mapInfo.getType().equals(MapInfoSnpIndel.TYPE_MISMATCH)) {
			return getSnp(mapInfo);
		}
		else {
			return getIndel(mapInfo);
		}
	}
	
	/**
	 * 待检验
	 * 给定mapInfo，其中mapInfo的flagLoc为snp坐标位点，chrID为染色体位置
	 * 获得该snp所在的三个碱基，以及所对应的氨基酸
	 * startLoc为起点碱基坐标，endLoc为终点碱基坐标
	 * 默认搜索最长转录本
	 * 将具体的序列信息填充mapInfo
	 * 返回该snp的定位信息
	 */
	private GffCodGene getSnp(MapInfoSnpIndel mapInfo) {
		GffCodGene gffCodeGene = gffChrAbs.getGffHashGene().searchLocation(mapInfo.getRefID(), mapInfo.getRefSnpIndelStart());
		GffGeneIsoInfo gffGeneIsoInfo = gffCodeGene.getCodInExonIso();
		if (gffGeneIsoInfo != null ) {
			mapInfo.setGffIso(gffGeneIsoInfo);
			mapInfo.setExon(true);
			//mRNA层面
			//就算在外显子中，但是如果是非编码rna，或者在UTR区域中，也返回
			if (!gffGeneIsoInfo.isCodInAAregion(gffCodeGene.getCoord())) {
				return gffCodeGene;
			}
			int LocStart = gffGeneIsoInfo.getLocAAbefore(mapInfo.getRefSnpIndelStart());//该位点所在AA的第一个loc
			int LocEnd = gffGeneIsoInfo.getLocAAend(mapInfo.getRefSnpIndelStart());
			if (LocEnd <0) {//如果不在转录本中
				if (gffGeneIsoInfo.isCis5to3()) {
					LocEnd = LocStart + 2;
				}
				else {
					LocEnd = LocStart - 2;
				}
			}
			SeqFasta NR = null;
			ArrayList<ExonInfo> lsTmp = gffGeneIsoInfo.getRangeIso(LocStart, LocEnd);
			if (lsTmp == null) {
				NR = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.isCis5to3(), mapInfo.getRefID(), LocStart, LocEnd);
			}
			else {
				NR = gffChrAbs.getSeqHash().getSeq(mapInfo.getRefID(), lsTmp, false);
			}
			mapInfo.setSeq(NR,false);
			mapInfo.setReplaceLoc(-gffGeneIsoInfo.getLocAAbeforeBias(mapInfo.getRefSnpIndelStart()) + 1);
		}
		else {
			if (gffCodeGene.isInsideLoc()) {
				GffGeneIsoInfo gffGeneIsoInfo2 = gffCodeGene.getGffDetailThis().getLongestSplit();
				mapInfo.setGffIso(gffGeneIsoInfo2);
			}
			mapInfo.setExon(false);
		}
		//基因层面
		
		return gffCodeGene;
	}
	
	/**
	 * 待检验
	 * 给定mapInfo，其中mapInfo的flagLoc为snp坐标位点，chrID为染色体位置
	 * 获得该snp所在的三个碱基，以及所对应的氨基酸
	 * startLoc为起点碱基坐标，endLoc为终点碱基坐标
	 * 默认搜索最长转录本
	 * 将具体的序列信息填充mapInfo
	 * 返回该snp的定位信息
	 */
	private GffCodGene getIndel(MapInfoSnpIndel mapInfo) {
		GffCodGene gffCodeGeneStart = gffChrAbs.getGffHashGene().searchLocation(mapInfo.getRefID(), mapInfo.getRefSnpIndelStart());
		GffCodGene gffCodeGeneEnd = gffChrAbs.getGffHashGene().searchLocation(mapInfo.getRefID(), mapInfo.getRefSnpIndelEnd());
		GffGeneIsoInfo gffGeneIsoInfoStart = gffCodeGeneStart.getCodInExonIso();
		GffGeneIsoInfo gffGeneIsoInfoEnd = gffCodeGeneEnd.getCodInExonIso();
		//起点和终点在同一个外显子中
		if (gffGeneIsoInfoStart != null && gffGeneIsoInfoEnd != null && 
				gffGeneIsoInfoStart.equals(gffGeneIsoInfoEnd) &&
				gffGeneIsoInfoStart.getLocInEleNum(mapInfo.getRefSnpIndelStart()) == gffGeneIsoInfoEnd.getLocInEleNum(mapInfo.getRefSnpIndelEnd()) 
				&& gffGeneIsoInfoStart.isCodInAAregion(mapInfo.getRefSnpIndelStart())) {
			mapInfo.setExon(true);
			mapInfo.setGffIso(gffGeneIsoInfoStart);
			int LocStart = gffGeneIsoInfoStart.getLocAAbefore(mapInfo.getRefSnpIndelStartCis());
			int LocEnd = gffGeneIsoInfoStart.getLocAAend(mapInfo.getRefSnpIndelEndCis());
			mapInfo.setStartEndLoc(LocStart, LocEnd);
			ArrayList<ExonInfo> lsTmp = gffGeneIsoInfoStart.getRangeIso(LocStart, LocEnd);
			//TODO 这里有一些问题
			if (lsTmp == null) {
				return gffCodeGeneStart;
			}
			SeqFasta NR = gffChrAbs.getSeqHash().getSeq(mapInfo.getRefID(), lsTmp, false);
			mapInfo.setSeq(NR,false);//因为上面已经反向过了
			mapInfo.setReplaceLoc(-gffGeneIsoInfoStart.getLocAAbeforeBias(mapInfo.getRefSnpIndelStartCis()) + 1);
		}
		else {
			if (gffGeneIsoInfoStart != null) {
				//基因层面
				mapInfo.setGffIso(gffGeneIsoInfoStart);
				mapInfo.setExon(true);
			}
			else if (gffGeneIsoInfoEnd != null) {
				mapInfo.setGffIso(gffGeneIsoInfoEnd);
				mapInfo.setExon(true);
			}
			else {
				if (gffCodeGeneStart.isInsideLoc()) {
					GffGeneIsoInfo gffGeneIsoInfo2 = gffCodeGeneStart.getGffDetailThis().getLongestSplit();
					mapInfo.setGffIso(gffGeneIsoInfo2);
				}
				mapInfo.setExon(false);
			}
		}
		return gffCodeGeneStart;
	}
	
	
	public void readSnpNum(String excelfile, int colChrID, int colSummit, String bedFile, String outFile ) {
		ArrayList<String[]> lsTmp = ExcelTxtRead.readLsExcelTxt(excelfile, 1, 0, 1, 0);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		gffChrAbs.setMapReads(bedFile, 1);
		gffChrAbs.loadMapReads();
		for (String[] strings : lsTmp) {
			MapInfo mapInfo = new MapInfo(strings[colChrID], Integer.parseInt(strings[colSummit]), Integer.parseInt(strings[colSummit]) );
			int Num = getSnpReadsNum(mapInfo);
			String[] tmpOut =  ArrayOperate.copyArray(strings, strings.length + 1);
			tmpOut[tmpOut.length - 1] = Num+"";
			lsResult.add(tmpOut);
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.writefile(lsResult);
		txtOut.close();
	}
	/**
	 * 位点在mapInfo的flagsite
	 * @param mapInfo
	 */
	public int getSnpReadsNum(MapInfo mapInfo) {
		double[] readsNum = gffChrAbs.getMapReads().getRengeInfo(1, mapInfo.getRefID(), mapInfo.getFlagSite(), mapInfo.getFlagSite(), 0);
		int Num = (int)MathComput.mean(readsNum);
		return Num;
	}
}
