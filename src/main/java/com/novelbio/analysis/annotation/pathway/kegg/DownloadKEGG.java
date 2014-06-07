package com.novelbio.analysis.annotation.pathway.kegg;

import java.util.LinkedHashSet;
import java.util.Set;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

public class DownloadKEGG {
	Set<String> setSpecies = new LinkedHashSet<>();
	String savePath;
	
	public void setSavePath(String savePath) {
		this.savePath = FileOperate.addSep(savePath);
	}
	
	public void addSpecies(String speciesAbbr) {
		setSpecies.add(speciesAbbr);
	}
	/** 添加物种 */
	public void addSpecies(Species species) {
		setSpecies.add(species.getAbbrName());
	}
	
	public void download() {
		for (String speciesAbbr : setSpecies) {
			final DownloadSpeciesKGML downloadSpeciesKGML = new DownloadSpeciesKGML();
			downloadSpeciesKGML.setSavePath(savePath);
			downloadSpeciesKGML.setSpeciesKeggName(speciesAbbr);
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						downloadSpeciesKGML.download();
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			});
			thread.start();
			downloadSpeciesKGML.executDownLoad();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
