package com.novelbio.analysis.seq.genome.gffoperate.trfrna;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.listoperate.ListCodAbs;
import com.novelbio.listoperate.ListCodAbsDu;
import com.novelbio.listoperate.ListHashSearch;

/** 存储miRNA的list */
public class TrfRNAList extends ListHashSearch<TrfMature, ListCodAbs<TrfMature>, ListCodAbsDu<TrfMature,ListCodAbs<TrfMature>>, TrfPre> {
	private static final Logger logger = LoggerFactory.getLogger(TrfRNAList.class);
	
	private String speciesName;
	
	/** 设定trfRNA的物种，如果没有物种就读取全部tfrna */
	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}
	/**
	 * 拿起点和终点overlap查找
	 * 如果没有找到，则返回null
	 * @param mirName mir的名字
	 * @param start 具体的
	 * @param end
	 * @return
	 */
	public String searchMirName(String mirName, int start, int end) {
		TrfMature element = searchElement(mirName, start, end);
		if (element == null) {
			logger.debug("cannot find miRNA on: " + mirName + " " + start + " " + end);

			return null;
		}
		return element.getNameSingle();
	}
	/**
	 * 拿重心位点查找
	 * 如果没有找到，则返回null
	 * @param mirName mir的名字
	 * @param start 具体的
	 * @param end
	 * @return
	 */
	public String searchMirNameMid(String mirName, int start, int end) {
		TrfMature element = searchElement(mirName, (start+end)/2);
		if (element == null) {
			logger.debug("cannot find miRNA on: " + mirName + " " + start + " " + end);

			return null;
		}
		return element.getNameSingle();
	}
	
	@Override
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		this.gfffilename = gfffilename;
		TxtReadandWrite txtRead = new TxtReadandWrite(gfffilename);
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) continue;
			
			String[] ss = content.split(", ");
			//如果物种不一致，则跳过
			if (!StringOperate.isRealNull(speciesName) && !ss[1].equals(speciesName)) {
				continue;
			}
			
			TrfPre trfPre = new TrfPre();
			trfPre.setCis5to3(true);
			trfPre.setName(ss[4]);
			trfPre.setDescription(ss[5]);
			String seq = ss[6].replace("5'", "").replace("3'", "").trim();
			trfPre.setTrfPreSeq(seq);
			
			TrfMature trfMature = new TrfMature(ss[4], ss[4]);
			trfMature.setCis5to3(true);
			trfMature.setTrfLoc(ss[3]);
			String[] loc = ss[7].split("-");
			int start = Integer.parseInt(loc[0].toLowerCase().replace("start:", "").trim());
			int end = Integer.parseInt(loc[1].toLowerCase().replace("end:", "").trim());

			trfMature.setStartAbs(start);
			trfMature.setEndAbs(end);
			trfPre.add(trfMature);
  
			//装入chrHash
			getMapChrID2LsGff().put(trfPre.getName().toLowerCase(), trfPre);
		}
		
		txtRead.close();
	}
	
	public void addMirMature(TrfRNAList trfRNAList) {
		for (TrfPre trfPre : trfRNAList.getMapChrID2LsGff().values()) {
			getMapChrID2LsGff().put(trfPre.getName().toLowerCase(), trfPre);
			if (lsNameAll != null) {
				lsNameAll.addAll(trfPre.getLsNameAll());
			}
			if (lsNameNoRedundent != null) {
				for (TrfMature gff : trfPre) {
					lsNameNoRedundent.add(gff.getNameSingle().toLowerCase());
				}
			}
			if (mapName2DetailNum != null) {
				mapName2DetailNum.putAll(trfPre.getMapName2DetailAbsNum());
			}
		}
	}


}
