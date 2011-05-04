package com.novelBio.tools.compare;

import java.io.File;
import java.net.URL;

public class RunCompareList {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		CompareList aaaCompareList=new CompareList();
		
			try {
				aaaCompareList.getFileToList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
	}

}
