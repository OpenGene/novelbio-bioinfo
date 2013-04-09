package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.SnpIndelRs;
import com.novelbio.database.mapper.geneanno.MapSnpIndelRs;
import com.novelbio.database.service.SpringFactory;

public class ServSnpIndelRs implements MapSnpIndelRs{
	public static void main(String[] args) {
		ServSnpIndelRs servSnpIndelRs = new ServSnpIndelRs();
    	SnpIndelRs re = new SnpIndelRs();
		re.setSnpRsID("aaa");
    	ArrayList<SnpIndelRs> lsncbiid = servSnpIndelRs.queryLsSnpIndelRs(re);
        System.out.println(lsncbiid.get(0).getSnpRsID());
//        assertEquals("aaa", lsncbiid.get(0).getSnpRsID());
	}
	@Inject
	private MapSnpIndelRs mapSnpIndelRs;
	public ServSnpIndelRs() {
		mapSnpIndelRs = (MapSnpIndelRs)SpringFactory.getFactory().getBean("mapSnpIndelRs");
	}	
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
