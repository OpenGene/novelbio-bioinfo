package com.novelbio.bioinfo.base.binarysearch;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.bioinfo.base.Align;

public class TestBinarySearch {
	
	@Test
	public void testBsearchSearchCis() {
		List<Align> lsAlign = new ArrayList<>();
		BinarySearch<Align> binarySearch = new BinarySearch<>(lsAlign, true);
		
		lsAlign.add(new Align("chr1:10-20"));
		lsAlign.add(new Align("chr1:40-60"));
		lsAlign.add(new Align("chr1:80-100"));
		lsAlign.add(new Align("chr1:120-140"));
		lsAlign.add(new Align("chr1:160-180"));
		lsAlign.add(new Align("chr1:200-220"));
		lsAlign.add(new Align("chr1:210-245"));
		lsAlign.add(new Align("chr1:240-260"));
		lsAlign.add(new Align("chr1:280-300"));
		
		BsearchSite<Align> bsearchSite = binarySearch.searchLocation(130);
		Assert.assertEquals(3, bsearchSite.getIndexAlignThis());
		Assert.assertEquals(new Align("chr1:80-100"), bsearchSite.getAlignUp());
		Assert.assertEquals(new Align("chr1:120-140"), bsearchSite.getAlignThis());
		Assert.assertEquals(new Align("chr1:160-180"), bsearchSite.getAlignDown());

		bsearchSite = binarySearch.searchLocation(10);
		Assert.assertEquals(0, bsearchSite.getIndexAlignThis());
		Assert.assertEquals(null, bsearchSite.getAlignUp());
		Assert.assertEquals(new Align("chr1:10-20"), bsearchSite.getAlignThis());
		Assert.assertEquals(new Align("chr1:40-60"), bsearchSite.getAlignDown());
		
		bsearchSite = binarySearch.searchLocation(9);
		Assert.assertEquals(null, bsearchSite.getAlignUp());
		Assert.assertEquals(null, bsearchSite.getAlignThis());
		Assert.assertEquals(new Align("chr1:10-20"), bsearchSite.getAlignDown());
		
		bsearchSite = binarySearch.searchLocation(244);
		Assert.assertEquals(new Align("chr1:210-245"), bsearchSite.getAlignUp());
		Assert.assertEquals(new Align("chr1:240-260"), bsearchSite.getAlignThis());
		Assert.assertEquals(new Align("chr1:280-300"), bsearchSite.getAlignDown());
		
		bsearchSite = binarySearch.searchLocation(215);
		Assert.assertEquals(new Align("chr1:200-220"), bsearchSite.getAlignUp());
		Assert.assertEquals(new Align("chr1:210-245"), bsearchSite.getAlignThis());
		Assert.assertEquals(new Align("chr1:240-260"), bsearchSite.getAlignDown());
	}
	
	@Test
	public void testBsearchSearchDuCis() {
		List<Align> lsAlign = new ArrayList<>();
		BinarySearch<Align> binarySearch = new BinarySearch<>(lsAlign, true);
		
		lsAlign.add(new Align("chr1:10-20"));
		lsAlign.add(new Align("chr1:40-60"));
		lsAlign.add(new Align("chr1:80-100"));
		lsAlign.add(new Align("chr1:120-140"));
		lsAlign.add(new Align("chr1:160-180"));
		lsAlign.add(new Align("chr1:200-220"));
		lsAlign.add(new Align("chr1:210-245"));
		lsAlign.add(new Align("chr1:240-260"));
		lsAlign.add(new Align("chr1:280-300"));
		
		BsearchSiteDu<Align> bsearchSiteDu = binarySearch.searchLocationDu(130, 220);
		BsearchSite<Align> bsearchSiteLeft = bsearchSiteDu.getSiteLeft();
		Assert.assertEquals(new Align("chr1:80-100"), bsearchSiteLeft.getAlignUp());
		Assert.assertEquals(new Align("chr1:120-140"), bsearchSiteLeft.getAlignThis());
		Assert.assertEquals(new Align("chr1:160-180"), bsearchSiteLeft.getAlignDown());

		BsearchSite<Align> bsearchSiteRight = bsearchSiteDu.getSiteRight();
		Assert.assertEquals(new Align("chr1:200-220"), bsearchSiteRight.getAlignUp());
		Assert.assertEquals(new Align("chr1:210-245"), bsearchSiteRight.getAlignThis());
		Assert.assertEquals(new Align("chr1:240-260"), bsearchSiteRight.getAlignDown());
		
		List<Align> lsAligns = bsearchSiteDu.getAllElement();
		Assert.assertEquals(4, lsAligns.size());
		Assert.assertEquals(new Align("chr1:120-140"), lsAligns.get(0));
		Assert.assertEquals(new Align("chr1:160-180"), lsAligns.get(1));
		Assert.assertEquals(new Align("chr1:200-220"), lsAligns.get(2));
		Assert.assertEquals(new Align("chr1:210-245"), lsAligns.get(3));

		lsAligns = bsearchSiteDu.getCoveredElement();
		Assert.assertEquals(2, lsAligns.size());
		Assert.assertEquals(new Align("chr1:160-180"), lsAligns.get(0));
		Assert.assertEquals(new Align("chr1:200-220"), lsAligns.get(1));
		
		bsearchSiteDu = binarySearch.searchLocationDu(280, 300);
		lsAligns = bsearchSiteDu.getCoveredElement();
		Assert.assertEquals(1, lsAligns.size());
		Assert.assertEquals(new Align("chr1:280-300"), lsAligns.get(0));
	}
	
	@Test
	public void testBsearchSearchTrans() {
		List<Align> lsAlign = new ArrayList<>();
		BinarySearch<Align> binarySearch = new BinarySearch<>(lsAlign, false);
		lsAlign.add(new Align("chr1:300-280"));
		lsAlign.add(new Align("chr1:260-240"));
		lsAlign.add(new Align("chr1:245-210"));
		lsAlign.add(new Align("chr1:220-200"));
		lsAlign.add(new Align("chr1:180-160"));
		lsAlign.add(new Align("chr1:140-120"));
		lsAlign.add(new Align("chr1:100-80"));
		lsAlign.add(new Align("chr1:60-40"));
		lsAlign.add(new Align("chr1:20-10"));

		
		BsearchSite<Align> bsearchSite = binarySearch.searchLocation(130);
		Assert.assertEquals(new Align("chr1:180-160"), bsearchSite.getAlignUp());
		Assert.assertEquals(new Align("chr1:140-120"), bsearchSite.getAlignThis());
		Assert.assertEquals(new Align("chr1:100-80"), bsearchSite.getAlignDown());

		bsearchSite = binarySearch.searchLocation(10);
		Assert.assertEquals(new Align("chr1:60-40"), bsearchSite.getAlignUp());
		Assert.assertEquals(new Align("chr1:20-10"), bsearchSite.getAlignThis());
		Assert.assertEquals(null, bsearchSite.getAlignDown());

		bsearchSite = binarySearch.searchLocation(9);
		Assert.assertEquals(new Align("chr1:20-10"), bsearchSite.getAlignUp());
		Assert.assertEquals(null, bsearchSite.getAlignThis());
		Assert.assertEquals(null, bsearchSite.getAlignDown());
		
		bsearchSite = binarySearch.searchLocation(244);
		Assert.assertEquals(new Align("chr1:260-240"), bsearchSite.getAlignUp());
		Assert.assertEquals(new Align("chr1:245-210"), bsearchSite.getAlignThis());
		Assert.assertEquals(new Align("chr1:220-200"), bsearchSite.getAlignDown());

		bsearchSite = binarySearch.searchLocation(215);
		Assert.assertEquals(new Align("chr1:245-210"), bsearchSite.getAlignUp());
		Assert.assertEquals(new Align("chr1:220-200"), bsearchSite.getAlignThis());
		Assert.assertEquals(new Align("chr1:180-160"), bsearchSite.getAlignDown());
	}
	
	@Test
	public void testBsearchSearchDuTrans() {
		List<Align> lsAlign = new ArrayList<>();
		BinarySearch<Align> binarySearch = new BinarySearch<>(lsAlign, false);
		
		lsAlign.add(new Align("chr1:300-280"));
		lsAlign.add(new Align("chr1:260-240"));
		lsAlign.add(new Align("chr1:245-210"));
		lsAlign.add(new Align("chr1:220-200"));
		lsAlign.add(new Align("chr1:180-160"));
		lsAlign.add(new Align("chr1:140-120"));
		lsAlign.add(new Align("chr1:100-80"));
		lsAlign.add(new Align("chr1:60-40"));
		lsAlign.add(new Align("chr1:20-10"));
		
		BsearchSiteDu<Align> bsearchSiteDu = binarySearch.searchLocationDu(220, 130);
		
		BsearchSite<Align> bsearchSiteLeft = bsearchSiteDu.getSiteLeft();
		Assert.assertEquals(new Align("chr1:245-210"), bsearchSiteLeft.getAlignUp());
		Assert.assertEquals(new Align("chr1:220-200"), bsearchSiteLeft.getAlignThis());
		Assert.assertEquals(new Align("chr1:180-160"), bsearchSiteLeft.getAlignDown());
		
		BsearchSite<Align> bsearchSiteRight = bsearchSiteDu.getSiteRight();
		Assert.assertEquals(new Align("chr1:180-160"), bsearchSiteRight.getAlignUp());
		Assert.assertEquals(new Align("chr1:140-120"), bsearchSiteRight.getAlignThis());
		Assert.assertEquals(new Align("chr1:100-80"), bsearchSiteRight.getAlignDown());


		
		List<Align> lsAligns = bsearchSiteDu.getAllElement();
		Assert.assertEquals(4, lsAligns.size());
		Assert.assertEquals(new Align("chr1:245-210"), lsAligns.get(0));
		Assert.assertEquals(new Align("chr1:220-200"), lsAligns.get(1));
		Assert.assertEquals(new Align("chr1:180-160"), lsAligns.get(2));
		Assert.assertEquals(new Align("chr1:140-120"), lsAligns.get(3));

		lsAligns = bsearchSiteDu.getCoveredElement();
		Assert.assertEquals(2, lsAligns.size());
		Assert.assertEquals(new Align("chr1:220-200"), lsAligns.get(0));
		Assert.assertEquals(new Align("chr1:180-160"), lsAligns.get(1));
		
		bsearchSiteDu = binarySearch.searchLocationDu(280, 300);
		lsAligns = bsearchSiteDu.getCoveredElement();
		Assert.assertEquals(1, lsAligns.size());
		Assert.assertEquals(new Align("chr1:300-280"), lsAligns.get(0));
	}
}
