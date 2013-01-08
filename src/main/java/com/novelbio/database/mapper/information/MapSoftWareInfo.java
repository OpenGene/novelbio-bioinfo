package com.novelbio.database.mapper.information;

import java.util.ArrayList;

import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.mapper.MapperSql;

public interface MapSoftWareInfo extends MapperSql{
	
	public SoftWareInfo querySoftWareInfo(SoftWareInfo softWareInfo);

	public ArrayList<SoftWareInfo> queryLsSoftWareInfo(SoftWareInfo softWareInfo);
	
	public void insertSoftWareInfo(SoftWareInfo softWareInfo);

	public void updateSoftWareInfo(SoftWareInfo softWareInfo);
}
