package com.novelbio.analysis.comparegenomics.coordtransform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 专门来读取mummer.delta文件
 * delta文件中记录了ref-vs-alt的indel信息
 * 把这些信息读取进去
 * @author zong0jie
 * @data 2018年8月5日
 */
public class CoordPairIndelReader {
	
	Map<String, List<CoordPair>> mapChrId2CoordPair;
	Map<String, CoordPair> mapKey2CoordPair;
	
	public void setMapChrId2CoordPair(Map<String, List<CoordPair>> mapChrId2CoordPair) {
		this.mapChrId2CoordPair = mapChrId2CoordPair;
	}
	
	public void generateMapLoc2Pair() {
		mapKey2CoordPair = new HashMap<>();
		for (List<CoordPair> lsCoordPairs : mapChrId2CoordPair.values()) {
			for (CoordPair coordPair : lsCoordPairs) {
				mapKey2CoordPair.put(coordPair.getKey(), coordPair);
			}
		}
	}
	
	public void readDelta(String mummerDelta) {
		TxtReadandWrite txtRead = new TxtReadandWrite(mummerDelta);
		String[] keyCoordPair = new String[6];
		CoordPair coordPair = null;
		for (String content : txtRead.readlines(3)) {
			if (content.startsWith(">")) {
				String[] ss = content.substring(1).split(" ");
				keyCoordPair[0] = ss[0];
				keyCoordPair[1] = ss[1];
				continue;
			}
			String[] ss = content.split(" ");
			if (ss.length == 7) {
				keyCoordPair[2] = ss[0]; keyCoordPair[3] = ss[1];
				keyCoordPair[4] = ss[2];  keyCoordPair[5] = ss[3];
				coordPair = mapKey2CoordPair.get(ArrayOperate.cmbString(keyCoordPair, " "));
				continue;
			}
			int value = Integer.parseInt(content);
			coordPair.addIndelMummer(value);
		}
		txtRead.close();
	}
	
}
