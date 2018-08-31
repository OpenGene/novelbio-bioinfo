package com.novelbio.bioinfo.phylotree;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.bioinfo.phylotree.AlignmentMuscle;

public class TestAlignmentMuscle {
	@Test
	public void testParam() {
		String input1 = "/home/novelbio/test1";
		String outPut = "/home/novelbio/test3.fa";
		
		AlignmentMuscle alignmentMuscle = new AlignmentMuscle();
		alignmentMuscle.setInputFasta(input1);
		alignmentMuscle.setOutputFasta(outPut);
		Assert.assertEquals("muscle -in /home/novelbio/test1 -out /home/novelbio/test3_tmp.fa", alignmentMuscle.getCmdExeStr().get(0));
	}
}
