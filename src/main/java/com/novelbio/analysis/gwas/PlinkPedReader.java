package com.novelbio.analysis.gwas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.RandomFileInt;
import com.novelbio.base.fileOperate.RandomFileInt.RandomFileFactory;

public class PlinkPedReader {

	/**
	 * 一列一个样本，<br>
	 * IRIS_313-15910 IRIS_313-15910 0 0 0 -9 G G T T A A T T T T A A C C G G T T G G<br>
	 * 其中第1列样本名，第2列家系名<br>
	 * 第3,4,5,6基本都是 0 0 0 -9，暂时不用管<br>
	 * 后面每两位是一个locus，每两个snp和{@link #plinkMap} 中的坐标对应
	 * 譬如:
	 * G G <--> 1 1579
	 * T T <--> 1 3044
	 * 以此类推
	 * <br><br>
	 * #==============================================#<br>
	 * 考虑建立索引然后随机读取进行优化<br>
	 */
	String plinkPed;
	RandomFileInt randomFile;
	public void setPlinkPed(String plinkPed) {
		
		randomFile = RandomFileFactory.createInstance(plinkPed);
	}
	/**
	 * 
	 * @param sampleName
	 * @param locStart 从第几位开始读取，包含该位置。
	 * @return
	 */
	public List<Allele> getItSnpsFromSample(String sampleName, int siteStart, int siteEnd) {
		byte[] readInfo = new byte[(int) (endReal - startReal)];
		randomChrFileInt.seek(startReal);
		randomChrFileInt.read(readInfo);		return null;
	}
	
	public static void createPlinkPedIndex(String plinkPed, String plinkPedIndex) throws IOException {
		List<String[]> lsIndexes = createPlinkPedIndex(plinkPed);
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkPedIndex, true);
		for (String[] index : lsIndexes) {
			txtWrite.writefileln(index);
		}
		txtWrite.close();
	}
	
	/**
	 * 建索引，本方法有错误
	 * index 文件格式如下
	 * chrID chrLength start   rowLength rowLenWithEnter
	 * @param plinkPed 输入plinkPed文件
	 * @param plinkIndex 输出的index文件
	 * @throws IOException
	 */
	@VisibleForTesting
	protected static List<String[]> createPlinkPedIndex(String plinkPed) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(FileOperate.getInputStream(plinkPed)));
		
		//String[] 其中0: SampleName 1:SampleStart 2:SampleLocStart
		List<String[]> lsIndexes = new ArrayList<>();
		String[] sampleIndex =null;
		
		//用来获取 1:SampleStart 2:SampleLocStart 的flag
		boolean isSampleStart = true;
		
		int result = 0;
		long site = 0;
		//当出现6个空格的时候，意味着开始读取snp了
		int spaceNum = 0;
		StringBuilder sBuilder = null;
		while ((result = bufferedReader.read()) > 0) {
			char charInfo = (char)result;
			//TODO 待测试
			if (isSampleStart && charInfo != '\r' && charInfo != '\n') {
				sampleIndex = new String[3];
				lsIndexes.add(sampleIndex);
				sampleIndex[1] = site+"";
				sBuilder = new StringBuilder();
				isSampleStart = false;
				spaceNum = 0;
			}
			if (spaceNum <= 6) {
				sBuilder.append((char)result);
				if (charInfo == ' ') {
					spaceNum++;
					if (spaceNum == 1) {
						//注意这里样本名不能含有空格
						sampleIndex[0] = sBuilder.toString().trim();
					}
				}
			}

			if (spaceNum == 6) {
				sampleIndex[2] = site +"";
			}
			
			//不支持苹果的换行符 "\r"
			if (charInfo == '\r' || charInfo == '\n') {
				isSampleStart = true;
			}
			site++;
		}
		return lsIndexes;
	}
	
	
}
