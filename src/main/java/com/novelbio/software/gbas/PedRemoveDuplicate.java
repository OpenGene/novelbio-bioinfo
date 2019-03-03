package com.novelbio.software.gbas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/** 将ped中重复坐标的位点删除
 * 
 * 在将老的基因组版本ped转成新版本ped时，由于基因组比对的问题，可能会出现老版本两个位点对应到新版本一个位点的情况
 * 这时候就需要把这种位点删除
 * @author zongjie
 *
 */
public class PedRemoveDuplicate {
	
	/** 没有排序的原始mid文件内容 */
	List<String[]> lsMidRaw;
	/** 排序并去重复后的mid内容 */
	List<String[]> lsPosFinal;
	
	public static void main(String[] args) {
		String outPath = "";
		List<String> lsFiles = FileOperate.getLsFoldFileName("", "bim");
		
		outPath = FileOperate.addSep(outPath);
		for (String mid : lsFiles) {
			String ped = mid.replace(".bim", ".ped");
			String name = FileOperate.getFileNameSep(ped)[0];
			String midOut = outPath + name + ".mid";
			String pedOut = outPath + name + ".ped";
			
			PedRemoveDuplicate pedRemoveDuplicate = new PedRemoveDuplicate();
			pedRemoveDuplicate.readMidFile(mid, midOut);
			pedRemoveDuplicate.handlePed(ped, pedOut);
		}
	}
	
	public void readMidFile(String mid, String midOut) {
		lsMidRaw = new ArrayList<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(mid);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			lsMidRaw.add(ss);
		}
		txtRead.close();
		
		List<String[]> lsInfoSort = new ArrayList<>(lsMidRaw);
		Collections.sort(lsInfoSort, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				String chrId1 = o1[0];
				String chrId2 = o2[0];
				
				Integer pos1 = Integer.parseInt(o1[1]);
				Integer pos2 = Integer.parseInt(o2[1]);
				int result = chrId1.compareTo(chrId2);
				if (result == 0) {
					result = pos1.compareTo(pos2);
				}
				return result;
			}
		});
		
		Set<String> setDuplicate = new HashSet<>();
		lsPosFinal = new ArrayList<>();
		for (String[] chrId2Pos2Ref2Alt : lsInfoSort) {
			String key = getChrId2Pos(chrId2Pos2Ref2Alt);
			if (setDuplicate.contains(key)) {
				continue;
			}
			setDuplicate.add(key);
			lsPosFinal.add(chrId2Pos2Ref2Alt);
		}
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(midOut, true);
		for (String[] ss : lsPosFinal) {
			txtWrite.writefileln(ArrayOperate.cmbString(ss, "\t"));
		}
		txtWrite.close();
	}
	
	public void handlePed(String pedIn, String pedOut) {
		TxtReadandWrite txtRead = new TxtReadandWrite(pedIn);
		TxtReadandWrite txtWrite = new TxtReadandWrite(pedOut, true);
		Map<String, String[]> mapPos2Allele = new HashMap<>();
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			int counter = 0;
			mapPos2Allele.clear();
			for (int i = 6; i < ss.length; i=i+2) {
				String[] allele = new String[]{ss[i], ss[i+1]};
				String[] chr2pos2ref2alt = lsMidRaw.get(counter);
				mapPos2Allele.put(getChrId2Pos(chr2pos2ref2alt), allele);
				counter++;
			}
			List<String> lsResult = new ArrayList<>();
			for (int i = 0; i < 6; i++) {
				lsResult.add(ss[i]);
			}
			for (String[] chr2pos2ref2alt : lsPosFinal) {
				String key = getChrId2Pos(chr2pos2ref2alt);
				String[] allele = mapPos2Allele.get(key);
				assertAllele(chr2pos2ref2alt, allele);
				lsResult.add(allele[0]);
				lsResult.add(allele[1]);
			}
			txtWrite.writefileln(lsResult.toArray(new String[0]));
		}
		txtRead.close();
		txtWrite.close();
	}
	
	private String getChrId2Pos(String[] chrId2Pos2Ref2Alt) {
		return chrId2Pos2Ref2Alt[0]+"@"+chrId2Pos2Ref2Alt[1];
	}
	
	private void assertAllele(String[] chr2pos2ref2alt, String[] allele) {
		Set<String> setAllele = new HashSet<>();
		setAllele.add("0");
		setAllele.add(chr2pos2ref2alt[2]);
		setAllele.add(chr2pos2ref2alt[3]);
		if (!setAllele.contains(allele[0]) || !setAllele.contains(allele[1])) {
			throw new RuntimeException(ArrayOperate.cmbString(chr2pos2ref2alt, "\t") + " but is " + ArrayOperate.cmbString(allele, " "));
		}
	}
}
