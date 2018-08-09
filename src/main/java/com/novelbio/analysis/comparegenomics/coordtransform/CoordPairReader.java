package com.novelbio.analysis.comparegenomics.coordtransform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * 专门来读取mummer.coords文件
 * @author zong0jie
 * @data 2018年8月5日
 */
public class CoordPairReader {
	List<CoordPair> lsCoordPairs  = new ArrayList<>();
	/** 相似度的cutoff，如果是不同版本之间的染色体比较，那么cutoff应该会很高 */
	double identityCutoff = 0;
	
	TxtReadandWrite txtRead;
	Iterator<String> itMummer;
	
	CoordPair lastPair;
	
	
	public CoordPairReader(String mummerCoord) {
		txtRead = new TxtReadandWrite(mummerCoord);
		itMummer = txtRead.readlines(5).iterator();
		String content = itMummer.next();
		if (!content.startsWith("==============")) {
			throw new RuntimeException(mummerCoord + " may not the correct mummer coord file.\n"
					+ "coord file line 5 should be: ==============");
		}
	}
	
	/** 相似度的cutoff，如果是不同版本之间的染色体比较，那么cutoff应该会很高 */
	public void setIdentityCutoff(double identityCutoff) {
		if (identityCutoff < 1) {
			identityCutoff = identityCutoff*100;
		}
		this.identityCutoff = identityCutoff;
	}
	
	public List<CoordPair> readNext() {
		List<CoordPair> lsCoordPairs = new ArrayList<>();
		if (lastPair != null) {
			lsCoordPairs.add(lastPair);
		}
		while (itMummer.hasNext()) {
			String mummerLine = itMummer.next();
			CoordPair coordPair = new CoordPair(mummerLine);
			if (lastPair != null && !coordPair.getRefID().equals(lastPair.getRefID())) {
				lastPair = coordPair;
				break;
			}
			if (coordPair.getIdentity() >= identityCutoff) {
				lsCoordPairs.add(coordPair);
			}
			lastPair = coordPair;
		}
		return lsCoordPairs;
	}
	
	public boolean hasNext() {
		return itMummer.hasNext();
	}
	
	public void close() {
		txtRead.close();
	}
	
	
}
