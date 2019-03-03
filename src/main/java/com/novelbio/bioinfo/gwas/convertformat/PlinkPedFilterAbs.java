package com.novelbio.bioinfo.gwas.convertformat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 把plinkped中的位点过滤掉
 * 设定杂合率为0.2，则杂合率小于0.2的，将杂合的样本剔除
 * 杂合率高于0.2的，将本位点剔除
 * 
 * @author novelbio
 *
 */
public abstract class PlinkPedFilterAbs {
	
	String ped;
	String mid;
	
	String pedNew;
	String midNew;
	
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
	
	public void dofilter() {
		readPed();
		fillFilterInfo();
		filter();
	}
	
	protected abstract void readPed();
	
	protected abstract void fillFilterInfo();
	
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
		
		if (FileOperate.isFileExistAndBigThan0(mid)) {
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
	
}
