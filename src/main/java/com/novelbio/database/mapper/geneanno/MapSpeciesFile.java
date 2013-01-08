package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.mapper.MapperSql;

public interface MapSpeciesFile extends MapperSql{
	
	public SpeciesFile querySpeciesFile(SpeciesFile speciesFile);

	public ArrayList<SpeciesFile> queryLsSpeciesFile(SpeciesFile speciesFile);
	
	public void insertSpeciesFile(SpeciesFile speciesFile);

	public void updateSpeciesFile(SpeciesFile speciesFile);
}
