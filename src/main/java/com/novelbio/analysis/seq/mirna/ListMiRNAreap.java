package com.novelbio.analysis.seq.mirna;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.MiRNAList;
import com.novelbio.analysis.seq.genome.gffOperate.MirMature;
import com.novelbio.analysis.seq.genome.gffOperate.MirPre;
import com.novelbio.base.dataOperate.TxtReadandWrite;
/**
 * 读取miRNA.dat的信息，构建listabs表，方便给定mirID和loc，从而查找到底是5p还是3p
 * @author zong0jie
 *
 */
public class ListMiRNAreap extends MiRNAList {
	private static final Logger logger = Logger.getLogger(ListMiRNAreap.class);

	/**
	 * 读取miRNA.data文件，同时读取和它一起的整理好的taxID2species文件
	 */
	protected void ReadGffarrayExcep(String rnadataFile) {
		ReadGffarrayExcepMirReap(rnadataFile);
	}
	
	protected void ReadGffarrayExcepMirReap(String rnadataFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(rnadataFile, false);
		MirPre mirPre = null; MirMature mirMature = null;
		int start = 0; int end = 0;
		boolean cis5to3 = true;
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String name = ss[8].split(";")[0].split("=")[1].toLowerCase();
			if (ss[2].startsWith("precursor") ) {
				mirPre = new MirPre();
				mirPre.setName(name);
				mirPre.setCis5to3(true);
				cis5to3 = ss[6].equals("+");
				if (cis5to3) {
					start = Integer.parseInt(ss[3]);
					end = Integer.parseInt(ss[4]);
				}
				else {
					start = Integer.parseInt(ss[4]);
					end = Integer.parseInt(ss[3]);
				}
				//装入chrHash
				getMapChrID2LsGff().put(mirPre.getName().toLowerCase(), mirPre);
			}
			if (ss[2].startsWith("mature")) {
				mirMature = new MirMature();
				mirMature.setCis5to3(true);
				//30..50
				mirMature.addItemName(name);
				if (cis5to3) {
					mirMature.setStartAbs(Integer.parseInt(ss[3]) - start);
					mirMature.setEndAbs(Integer.parseInt(ss[4]) - start);
				}
				else {
					mirMature.setStartAbs(start - Integer.parseInt(ss[4]));
					mirMature.setEndAbs(start - Integer.parseInt(ss[3]));
				}
				mirPre.add(mirMature);
			}
		}
		txtRead.close();
	}
	/**
	 * 如果没有找到，则返回null
	 * @param mirName mir的名字
	 * @param start 具体的
	 * @param end
	 * @return
	 */
	public String searchMirName(String mirName, int start, int end) {
		MirMature element = searchElement(mirName, start, end);
		if (element == null) {
			logger.error("出现未知miRNA前体名字，是否需要更新miRNA.dat文件：" + mirName);
			return null;
		}
		return element.getNameSingle();
	}
	
}

