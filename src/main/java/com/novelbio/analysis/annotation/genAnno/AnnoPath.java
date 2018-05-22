package com.novelbio.analysis.annotation.genAnno;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.model.kegg.KGentry;
import com.novelbio.database.model.kegg.KGpathway;
import com.novelbio.generalconf.TitleFormatNBC;

public class AnnoPath extends AnnoAbs {

	@Override
	protected String[] getTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add("KeggID");
		lsTitle.add("KeggTerm");
		if (blast) {
			lsTitle.add(TitleFormatNBC.Evalue.toString());
			lsTitle.add("BlastAccID");
			lsTitle.add("BlastSymbol/AccID");
			lsTitle.add("BlastDescription");
		}
		return lsTitle.toArray(new String[0]);
	}

	@Override
	protected List<String[]> getInfo(int taxID, String accID) {
		return getGenPath(accID, taxID, false, 0, 0);
	}

	@Override
	protected List<String[]> getInfoBlast(int taxID, int subTaxID,
			double evalue, String accID) {
		// TODO Auto-generated method stub
		return getGenPath(accID, taxID, true, subTaxID, evalue);
	}


	private static ArrayList<String[]> getGenPath(String accID,int taxID,boolean blast,int subTaxID,double evalue) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		GeneID copedID = new GeneID(accID, taxID);
//		if (copedID.getIDtype() == GeneID.IDTYPE_ACCID) {
//			int resultNum = 2;
//			if (blast) {
//				resultNum = 4;
//			}
//			List<String> lsTmpResult = new ArrayList<>();
//			lsTmpResult.add(copedID.getSymbol());
//			for (int i = 1; i < resultNum; i++) {
//				lsTmpResult.add("");
//			}
//			lsResult.add(lsTmpResult.toArray(new String[0]));
//			return lsResult;
//		}
		
		copedID.setBlastInfo(evalue, subTaxID);
		// 本基因的Path信息
		ArrayList<KGpathway> lsKGentrythis = copedID.getKegPath(false);
		if (lsKGentrythis.size() == 0) {
			List<String> lsAccIDinfo = new ArrayList<>();
			lsAccIDinfo.add(copedID.getSymbol());
			lsAccIDinfo.add("");
			lsAccIDinfo.add("");
			lsResult.add(lsAccIDinfo.toArray(new String[0]));
		}
		HashSet<String> hashPathID = new HashSet<String>();

		for (KGpathway kGpathway : lsKGentrythis) {
			if (hashPathID.contains(kGpathway.getMapNum())) {
				continue;
			}
			List<String> lsAccIDinfo = new ArrayList<>();
			lsAccIDinfo.add(copedID.getSymbol());
			lsAccIDinfo.add( "PATH:" + kGpathway.getMapNum());
			lsAccIDinfo.add(kGpathway.getTitle());
			lsResult.add(lsAccIDinfo.toArray(new String[0]));
		}
		if (!blast) {
			if (lsResult.size() == 0) {
				return null;
			}
			return lsResult;
		}
		// blast基因的Path信息
		ArrayList<String[]> lsResultBlast = new ArrayList<String[]>();

		GeneID copedIDblast = copedID.getGeneIDBlast();
		if (copedIDblast == null) {
			for (String[] strings : lsResult) {
				String[] result = ArrayOperate.copyArray(strings, 8);
				for (int i = 0; i < result.length; i++) {
					if (result[i] == null) {
						result[i] = "";
					}
				}
				lsResultBlast.add(result);
			}
			return lsResultBlast;
		}
		HashSet<String> hashPathIDBlast = new HashSet<String>();
		List<KGentry> lsPathBlast = copedIDblast.getKegEntity(false);
		int k = 0;
		for (int i = 0; i < lsPathBlast.size(); i++) {
			if (hashPathIDBlast.contains(lsPathBlast.get(i).getPathName())) {
				continue;
			}
			hashPathIDBlast.add(lsPathBlast.get(i).getPathName());
			if (lsResult == null)
				lsResult = new ArrayList<String[]>();
			String[] tmpResultBlast = new String[7];
			// 初始化
			for (int j = 0; j < tmpResultBlast.length; j++) {
				tmpResultBlast[j] = "";
			}
			if (k < lsResult.size()) {
				for (int j = 0; j < lsResult.get(k).length; j++) {
					tmpResultBlast[j] = lsResult.get(k)[j];
				}
			} else {
				tmpResultBlast[0] = copedID.getSymbol();
			}
			tmpResultBlast[3] = copedID.getLsBlastInfos().get(0).getEvalue() + "";
			tmpResultBlast[4] = copedIDblast.getSymbol();
			tmpResultBlast[5] = lsPathBlast.get(i).getPathName();
			tmpResultBlast[6] = lsPathBlast.get(i).getPathTitle();
			lsResultBlast.add(tmpResultBlast);
			k ++;
		}

		return lsResultBlast;
	}
	
	
}
