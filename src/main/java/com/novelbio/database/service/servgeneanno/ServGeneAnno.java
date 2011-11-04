package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.service.AbsGetSpring;
@Service
public class ServGeneAnno extends AbsGetSpring implements MapNCBIID{
	@Inject
	protected MapNCBIID mapNCBIID2;
	static MapNCBIID mapNCBIID;
	static{
		mapNCBIID = (MapNCBIID) factory.getBean("mapNCBIID");
	}
	//TODO Õý¹æÐ´·¨
//	@Inject
//	protected MapNCBIID mapNCBIID;
//	private static ServGeneAnno info; 
//	@PostConstruct
//	public void init()
//	{
//	    info = this;
//	    info.mapNCBIID = this.mapNCBIID;
//	}
	@Override
	public NCBIID queryNCBIID(NCBIID QueryNCBIID) {
		return mapNCBIID.queryNCBIID(QueryNCBIID);
	}
	@Override
	public ArrayList<NCBIID> queryLsNCBIID(NCBIID QueryNCBIID) {
		return mapNCBIID.queryLsNCBIID(QueryNCBIID);
	}
	@Override
	public void insertNCBIID(NCBIID nCBIID) {
		mapNCBIID.insertNCBIID(nCBIID);
		
	}
	@Override
	public void upDateNCBIID(NCBIID nCBIID) {
		mapNCBIID.insertNCBIID(nCBIID);
		
	}
	
	
}
