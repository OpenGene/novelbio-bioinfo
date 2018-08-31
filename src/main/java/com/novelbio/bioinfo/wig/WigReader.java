package com.novelbio.bioinfo.wig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.RPChromosomeRegion;
import org.broad.igv.bbfile.WigItem;
import org.broad.igv.bbfile.ZoomDataRecord;
import org.broad.igv.bbfile.ZoomLevelIterator;

import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.bioinfo.mappedreads.MapReadsAbs;

public class WigReader extends MapReadsAbs {
	BBFileReader bbFileReader;
	int span = 0;
	Map<String, String> mapChrIdLowcase2ChrId = new HashMap<>();
	public static void main(String[] args) throws IOException {
		WigReader wigReader = new WigReader();
		wigReader.setReadsInfoFile("/media/winE/NBC/Project/Project_MaHong/huangqiyue/out3.bw");
//		wigReader.setBigWigFile("/media/hdfs/nbCloud/public/customerData/Projects/DN14001/bam/IonXpress_013.bam.bw");
	}
	
	public void setReadsInfoFile(String bigwigFile) {
		try {
			bbFileReader = new BBFileReader(bigwigFile);
			for (String chrId : bbFileReader.getChromosomeNames()) {
				mapChrIdLowcase2ChrId.put(chrId.toLowerCase(), chrId);
			}
			for (String chrId : mapChrIdLowcase2ChrId.values()) {
				int chrIdInt = bbFileReader.getChromosomeID(chrId);
				RPChromosomeRegion rpChromosomeRegion = bbFileReader.getChromosomeBounds(chrIdInt, chrIdInt);
				mapChrID2Len.put(chrId.toLowerCase(), (long) rpChromosomeRegion.getEndBase());
			}
			WigItem wi = bbFileReader.getBigWigIterator().next();
			span = wi.getEndBase() - wi.getStartBase();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void ReadMapFileExp() throws Exception {
	}

	//TODO 待测试
	@Override
	public double[] getRangeInfo(int thisInvNum, String chrID, int startNum, int endNum, int type) {
		if (thisInvNum <= 0) thisInvNum = span;
		chrID = mapChrIdLowcase2ChrId.get(chrID.toLowerCase());
		int[] startEndLoc = correctStartEnd(mapChrID2Len, chrID, startNum, endNum);
		startNum = startEndLoc[0]; endNum = startEndLoc[1];
		
		int binNum = (int) ((double)(startEndLoc[1] - startEndLoc[0] + 1) / thisInvNum);
		BigWigIterator bi = bbFileReader.getBigWigIterator(chrID, startNum, chrID, endNum, false);		
		if (!bi.hasNext()) {
			return new double[binNum];
		}
		
		double[] tmpResult = new double[endNum - startNum + 1]; 
		WigItem wi;
		while ((wi = bi.next()) != null) {
			for (int i = wi.getStartBase() + 1; i <= wi.getEndBase(); i++) {
				if (i < startNum - 1) {
					continue;
				} else if ( i > endNum) {
					break;
				}
				tmpResult[i - startNum] = wi.getWigValue();
			}
		}
		normDouble(NormalType, tmpResult, getAllReadsNum());
		double[] tmp = null;
		if (thisInvNum == 1) {
			tmp = tmpResult;
		} else {
			try {
				tmp = MathComput.mySpline(tmpResult, binNum, 0, 0, type);
			} catch (Exception e) {
				return null;
			}
		}
		
		try {
			tmp = equationsCorrect(tmp);
		} catch (Exception e) {
			return tmp;
		}
		
		return tmp;
	}

	@Override
	protected double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type) {		
		chrID = mapChrIdLowcase2ChrId.get(chrID.toLowerCase());
		int[] startEndLoc = correctStartEnd(mapChrID2Len, chrID, startNum, endNum);
		startNum = startEndLoc[0]; endNum = startEndLoc[1];
		
		BigWigIterator bi = bbFileReader.getBigWigIterator(chrID, startNum, chrID, endNum, false);		
		if (!bi.hasNext()) {
			return new double[binNum];
		}
		
		double[] tmpResult = new double[endNum - startNum + 1]; 
		WigItem wi;
		while ((wi = bi.next()) != null) {
			for (int i = wi.getStartBase() + 1; i <= wi.getEndBase(); i++) {
				if (i < startNum - 1) {
					continue;
				} else if ( i > endNum) {
					break;
				}
				tmpResult[i - startNum] = wi.getWigValue();
			}
		}
		normDouble(NormalType, tmpResult, getAllReadsNum());
		double[] tmp = null;
		if (tmpResult.length == binNum) {
			tmp = tmpResult;
		} else {
			try {
				tmp = MathComput.mySpline(tmpResult, binNum, 0, 0, type);
			} catch (Exception e) {
				return null;
			}
		}
		
		try {
			tmp = equationsCorrect(tmp);
		} catch (Exception e) {
			return tmp;
		}
		
		return tmp;
	}

	@Override
	protected long getAllReadsNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] getReadsDensity(String chrID, int startLoc, int endLoc, int binNum) {
		chrID = mapChrIdLowcase2ChrId.get(chrID.toLowerCase());

		
		try {
			BBFileReader bbFileReader = new BBFileReader("/media/hdfs/nbCloud/public/customerData/Projects/DN14001/bam/IonXpress_013.bam.bw");
			
			ZoomLevelIterator zi = bbFileReader.getZoomLevelIterator(10);
			ZoomDataRecord zz = null;
			int i = 0;
			while ((zz = zi.next()) != null) {
				if (i++ > 20) {
					break;
				}
				System.out.println(zz.getChromName() + "\t" + zz.getChromStart()  + "\t" + zz.getChromEnd() + "\t" + zz.getSumData());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

}
