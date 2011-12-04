package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.SnpIndelRs;
import com.novelbio.database.mapper.geneanno.MapSnpIndelRs;
import com.novelbio.database.service.AbsGetSpring;

@Component
public class ServSnpIndelRs extends AbsGetSpring implements MapSnpIndelRs{
	public static void main(String[] args) {
		ServSnpIndelRs servSnpIndelRs = new ServSnpIndelRs();
    	SnpIndelRs re = new SnpIndelRs();
		re.setSnpRsID("aaa");
    	ArrayList<SnpIndelRs> lsncbiid = servSnpIndelRs.queryLsSnpIndelRs(re);
        System.out.println(lsncbiid.get(0).getSnpRsID());
//        assertEquals("aaa", lsncbiid.get(0).getSnpRsID());
	}
	@Autowired
	private MapSnpIndelRs mapSnpIndelRs;
	
	
	public ServSnpIndelRs() {
		mapSnpIndelRs = (MapSnpIndelRs) factory.getBean("mapSnpIndelRs");
	}
	
//	static MapSnpIndelRs mapSnpIndelRs;
//	static{
//		mapSnpIndelRs = (MapSnpIndelRs) factory.getBean("mapSnpIndelRs");
//	}
	private static ServNCBIID info; 
	
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
