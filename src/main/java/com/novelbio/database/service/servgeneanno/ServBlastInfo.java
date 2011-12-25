package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;
import com.novelbio.database.mapper.geneanno.MapGene2Go;
import com.novelbio.database.service.AbsGetSpring;
@Service
public class ServBlastInfo extends AbsGetSpring implements MapBlastInfo {
	@Inject
	MapBlastInfo mapBlastInfo;
	
	public ServBlastInfo()
	{
		mapBlastInfo = (MapBlastInfo)factory.getBean("mapBlastInfo");
	}
	
	@Override
	public BlastInfo queryBlastInfo(BlastInfo qBlastInfo) {
		// TODO Auto-generated method stub
		return mapBlastInfo.queryBlastInfo(qBlastInfo);
	}

	@Override
	public ArrayList<BlastInfo> queryLsBlastInfo(BlastInfo qBlastInfo) {
		// TODO Auto-generated method stub
		return mapBlastInfo.queryLsBlastInfo(qBlastInfo);
	}

	@Override
	public void insertBlastInfo(BlastInfo blastInfo) {
		// TODO Auto-generated method stub
		mapBlastInfo.insertBlastInfo(blastInfo);
	}

	@Override
	public void updateBlastInfo(BlastInfo blastInfo) {
		// TODO Auto-generated method stub
		mapBlastInfo.updateBlastInfo(blastInfo);
	}

}
