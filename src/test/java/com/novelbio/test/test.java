package com.novelbio.test;

import java.io.BufferedReader;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class test {
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args)  {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter("/media/winE/Bioinformatics/Kegg/genes/organisms/ath/ath_cazy.list", false, true);
		BufferedReader aaString;
		try {
			String aa= txtReadandWrite.readFirstLine();
			System.out.println(aa);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
