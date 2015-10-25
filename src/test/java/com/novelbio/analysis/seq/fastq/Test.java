package com.novelbio.analysis.seq.fastq;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	
	public static void main(String[] args) {
		 String regEx="(N)\\]([a-zA-Z_0-9]*):([0-9]*)\\]\\s*";  
		 Pattern pattern = Pattern.compile(regEx);  
		 String altInfo ="N]chr1:9802]";
		 Matcher matcher = pattern.matcher(altInfo);  
			String chr =  matcher.group(2);
			Long start2 = Long.parseLong(matcher.group(3));
			System.out.println(chr + "\t" +start2);
	}
}
