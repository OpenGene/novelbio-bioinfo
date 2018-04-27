package com.novelbio.test.database.domain.geneanno;

import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.seq.mirna.ListMiRNAdat;
import com.novelbio.analysis.seq.mirna.MiRNACount;
import com.novelbio.database.domain.species.Species;
import com.novelbio.database.model.geneanno.Go2Term;
import com.novelbio.database.model.geneanno.GOtype.GORelation;

import junit.framework.TestCase;

public class TestGo2Term extends TestCase {

	Go2Term go2Term = new Go2Term();
	@Before
	public void setUp() {
		go2Term.addChild("GO:001", GORelation.IS);
		go2Term.addChild("GO:002", GORelation.IS);
		go2Term.addChild("GO:003", GORelation.IS);
		
		go2Term.addParent("GO:001", GORelation.IS);
		go2Term.addParent("GO:002", GORelation.IS);
		go2Term.addParent("GO:003", GORelation.IS);
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testAddParent() {
		go2Term.addChild("GO:001", GORelation.PART_OF);
		//TODO 该方法过时
//		assertEquals("part@@GO:001@//@is@@GO:002@//@is@@GO:003", go2Term.getChild());
	}

}
