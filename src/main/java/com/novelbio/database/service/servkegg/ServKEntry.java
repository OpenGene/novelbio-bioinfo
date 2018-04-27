package com.novelbio.database.service.servkegg;

import java.util.List;

import com.novelbio.database.dao.kegg.RepoKEntry;
import com.novelbio.database.model.kegg.KGentry;
import com.novelbio.database.service.SpringFactoryBioinfo;
public class ServKEntry {
	RepoKEntry mapKEntry;
	
	public ServKEntry() {
		mapKEntry = (RepoKEntry)SpringFactoryBioinfo.getFactory().getBean("repoKEntry");
	}
	
	public List<KGentry> findByName(String name) {
		return mapKEntry.findByName(name);
	}
	
	public KGentry findByNamePathAndIdAndReaction(String name, String pathName, int id, String reaction) {
		return mapKEntry.findByNamePathAndIdAndReaction(name, pathName, id, reaction);
	}
	
	public List<KGentry> findByPathNameAndParentId(String pathName, int parentId) {
		return mapKEntry.findByPathNameAndParentId(pathName, parentId);
	}
	public List<KGentry> findByPathNameAndEntryId(String pathName, int entryID) {
		return mapKEntry.findByPathNameAndEntryId(pathName, entryID);
	}
	public List<KGentry> findByPathName(String pathName) {
		return mapKEntry.findByPathName(pathName);
	}
	public List<KGentry> findByNameAndTaxId(String name, int taxId) {
		return mapKEntry.findByNameAndTaxId(name, taxId);
	}
	
	public void save(KGentry kGentry) {
		mapKEntry.save(kGentry);
	}
	
	public void deleteAll() {
		mapKEntry.deleteAll();
	}
	
	static class ManageHolder {
		static ServKEntry instance = new ServKEntry();
	}
	
	public static ServKEntry getInstance() {
		return ManageHolder.instance;
	}




}
