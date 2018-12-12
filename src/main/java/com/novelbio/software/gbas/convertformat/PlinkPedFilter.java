package com.novelbio.software.gbas.convertformat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.fasta.SeqFasta;

/**
 * 把plinkped中的位点过滤掉
 * 设定杂合率为0.2，则杂合率小于0.2的，将杂合的样本剔除
 * 杂合率高于0.2的，将本位点剔除
 * 
 * @author novelbio
 *
 */
public class PlinkPedFilter {
	
	String ped;
	String mid;
	
	String pedNew;
	String midNew;
	
	/**
	 * 杂合率的指标，低于这个，则把杂合的样本删除
	 * 高于这个，则把相应的位点删除
	 */
	double heteProp = 0.2;
	
	/**
	 * key：strain name
	 * value：每个位点是否杂合，纯合0,杂合1,N为2
	 * 不过应该不含有N，因为之前一步需要做imputation
	 */
	Map<String, short[]> mapSeq2Site = new HashMap<>();
	/** 需要删除的品种 */
	Set<String> setStrainNeedDelete = new HashSet<>();
	/** 需要删除的位点，从0开始计算 */
	Set<Integer> setSiteNeedDelete = new HashSet<>();
	
	public void setPedMidRead(String ped, String mid) {
		this.ped = ped;
		this.mid = mid;
	}
	
	public void setPedMidWrite(String ped, String mid) {
		this.pedNew = ped;
		this.midNew = mid;
	}
	
	/**
	 * 杂合率的指标，<= 这个，则把杂合的样本删除
	 * 高于这个，则把相应的位点删除
	 */
	public void setHeteProp(double heteProp) {
		this.heteProp = heteProp;
	}
	/**
	 * 把突变信息读取到内存中
	 * @param plinkPed
	 * @param plinkMid
	 */
	protected void readPed() {
		TxtReadandWrite txtReadPed = new TxtReadandWrite(ped);
		for (String conent : txtReadPed.readlines()) {
			String[] ss = conent.split("\t");
			String strain = ss[0];
			short[] site = new short[ss.length-6];
			for (int i = 6; i < ss.length; i++) {
				String[] bases = ss[i].split(" ");
				site[i-6] = getBaseType(bases);
			}
			mapSeq2Site.put(strain, site);
		}
		txtReadPed.close();
	}
	
	/** 每个位点是否杂合，纯合0,杂合1,N为2 */
	private static short getBaseType(String[] bases) {
		if (bases[0].equalsIgnoreCase("N")) {
			return 2;
		}
		if (bases[0].equals(bases[1])) {
			return 0;
		}
		return 1;
	}
	
	/** 开始获得过滤信息，填充{@link #setStrainNeedDelete} 和 {@link #lsSiteNeedDelete}*/
	protected void fillFilterInfo() {
		int numAll = mapSeq2Site.size();
		int siteNum = mapSeq2Site.values().iterator().next().length;
		for (int i = 0; i < siteNum; i++) {
			int numHete = 0;
			List<String> lsStrainName = new ArrayList<>();
			for (String strain : mapSeq2Site.keySet()) {
				short[] site = mapSeq2Site.get(strain);
				short num = site[i];
				if (num != 0) {
					numHete++;
					lsStrainName.add(strain);
				}
			}
			if ((double)numHete/numAll > heteProp) {
				setSiteNeedDelete.add(i);
			} else {
				setStrainNeedDelete.addAll(lsStrainName);
			}
		}
	}
	
	protected void filter() {
		TxtReadandWrite txtReadPed = new TxtReadandWrite(ped);
		TxtReadandWrite txtWritePedNew = new TxtReadandWrite(pedNew, true);
		for (String content : txtReadPed.readlines()) {
			String[] ss = content.split("\t");
			String strain = ss[0];
			if (setStrainNeedDelete.contains(strain)) {
				continue;
			}
			List<String> lsResult = new ArrayList<>();
			for (int i = 0; i < 6; i++) {
				lsResult.add(ss[i]);
			}
			for (int i = 6; i < ss.length; i++) {
				if (setSiteNeedDelete.contains(i-6)) {
					continue;
				}
				lsResult.add(ss[i]);
			}
			txtWritePedNew.writefileln(lsResult);
		}
		txtReadPed.close();
		txtWritePedNew.close();
		
		TxtReadandWrite txtReadMid = new TxtReadandWrite(mid);
		TxtReadandWrite txtWriteMidNew = new TxtReadandWrite(midNew, true);
		int i = -1;
		for (String content : txtReadMid.readlines()) {
			if (content.startsWith("#")) {
				txtWriteMidNew.writefileln(content);
			}
			i++;
			if (setSiteNeedDelete.contains(i)) {
				continue;
			}
			txtWriteMidNew.writefileln(content);
		}
		txtReadMid.close();
		txtWriteMidNew.close();
	}
	
}
