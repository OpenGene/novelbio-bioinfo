package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.SnpIndelRs;
import com.novelbio.database.mapper.geneanno.MapSnpIndelRs;
import com.novelbio.database.service.AbsGetSpring;

@Service
public class ServSnpIndelRs extends AbsGetSpring implements MapSnpIndelRs{
	public static void main(String[] args) {
		ServSnpIndelRs servSnpIndelRs = new ServSnpIndelRs();
    	SnpIndelRs re = new SnpIndelRs();
		re.setSnpRsID("aaa");
    	ArrayList<SnpIndelRs> lsncbiid = servSnpIndelRs.queryLsSnpIndelRs(re);
        System.out.println(lsncbiid.get(0).getSnpRsID());
//        assertEquals("aaa", lsncbiid.get(0).getSnpRsID());
	}
	@Inject
	protected MapSnpIndelRs mapSnpIndelRs2;
	static MapSnpIndelRs mapSnpIndelRs;
	static{
		mapSnpIndelRs = (MapSnpIndelRs) factory.getBean("mapSnpIndelRs");
	}
	private static ServGeneAnno info; 
	
	@Override
	public SnpIndelRs querySnpIndelRs(SnpIndelRs snpIndelRs) {
		return mapSnpIndelRs.querySnpIndelRs(snpIndelRs);
	}

	@Override
	public ArrayList<SnpIndelRs> queryLsSnpIndelRs(SnpIndelRs snpIndelRs) {
		// TODO Auto-generated method stub
		return mapSnpIndelRs.queryLsSnpIndelRs(snpIndelRs);
	}

}
