package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.mapper.geneanno.MapSpeciesFile;
import com.novelbio.database.service.AbsGetSpring;

public class ServSpeciesFile extends AbsGetSpring implements MapSpeciesFile{
	private static Logger logger = Logger.getLogger(ServSpeciesFile.class);

	@Inject
	private MapSpeciesFile mapSpeciesFile;
	public ServSpeciesFile()  
	{
		mapSpeciesFile = (MapSpeciesFile) factory.getBean("mapSpeciesFile");
	}
	@Override
	public SpeciesFile querySpeciesFile(SpeciesFile speciesFile) {
		return mapSpeciesFile.querySpeciesFile(speciesFile);
	}
	@Override
	public ArrayList<SpeciesFile> queryLsSpeciesFile(SpeciesFile speciesFile) {
		return mapSpeciesFile.queryLsSpeciesFile(speciesFile);
	}

	@Override
	public void insertSpeciesFile(SpeciesFile speciesFile) {
		mapSpeciesFile.insertSpeciesFile(speciesFile);
	}

	@Override
	public void updateSpeciesFile(SpeciesFile speciesFile) {
		mapSpeciesFile.updateSpeciesFile(speciesFile);
	}
	/**
	 * @param taxID ����ѡ�û����Ͳ���ѡ��
	 * @param version ��ѡ����Ҫ��hg19�ȵ����ƣ������ҹ���Ҳ�ò���
	 * @return û�еĻ��򷵻�size==0��list
	 */
	public ArrayList<SpeciesFile> queryLsSpeciesFile(int taxID, String version) {
		if (taxID <= 0) {
			return new ArrayList<SpeciesFile>();
		}
		SpeciesFile speciesFile = new SpeciesFile();
		speciesFile.setTaxID(taxID);
		if (version != null && version.equals("")) {
			speciesFile.setVersion(version);
		}
		return mapSpeciesFile.queryLsSpeciesFile(speciesFile);
	}
	/**
	 * û�оͲ��룬�о�����
	 * @param taxInfo
	 */
	public void update(SpeciesFile speciesFile) {
		if (speciesFile.getTaxID() == 0) {
			return;
		}
		ArrayList<SpeciesFile> lsSpeciesFileQ = queryLsSpeciesFile(speciesFile);
		if (lsSpeciesFileQ == null || lsSpeciesFileQ.size() == 0) {
			insertSpeciesFile(speciesFile);
		}
		boolean updateFlag = true;
		for (SpeciesFile speciesFile2 : lsSpeciesFileQ) {
			if (speciesFile2.equalsDeep(speciesFile)) {
				updateFlag = false;
			}
		}
		if (updateFlag) {
			updateSpeciesFile(speciesFile);
		}
	}
}
