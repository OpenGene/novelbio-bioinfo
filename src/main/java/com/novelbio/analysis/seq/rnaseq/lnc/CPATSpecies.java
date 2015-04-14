package com.novelbio.analysis.seq.rnaseq.lnc;

import java.util.LinkedHashMap;
import java.util.Map;

import com.novelbio.analysis.annotation.genAnno.AnnoAbs;

public class CPATSpecies {

	public static final int HUMAN = 9606;
	public static final int MOUSE = 10090;
	public static final int ZEBRAFISH = 7955;
	public static final int FRUITFLY = 7227;
	public static final int OTHER = 000000;
	
	/**
	 * key是mapping的内容<br>
	 * value是mapping的int代号
	 * @return
	 */
	public static Map<String, Integer> getCPATSpecies() {
		Map<String, Integer> mapSpecie2Specie = new LinkedHashMap<>();
		mapSpecie2Specie.put("Human", CPATSpecies.HUMAN);
		mapSpecie2Specie.put("Mouse", CPATSpecies.MOUSE);
		mapSpecie2Specie.put("ZebraFish", CPATSpecies.ZEBRAFISH);
		mapSpecie2Specie.put("FruitFly", CPATSpecies.FRUITFLY);
		mapSpecie2Specie.put("Other", CPATSpecies.OTHER);
		return mapSpecie2Specie;
	}
}
