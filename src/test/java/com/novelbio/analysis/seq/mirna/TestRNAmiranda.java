package com.novelbio.analysis.seq.mirna;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;

import com.novelbio.analysis.seq.mirna.RNAmiranda.MirandaPair;

public class TestRNAmiranda {
	public static void main(String[] args) throws IOException {
		ClassPathResource resource = new ClassPathResource("TestRNAmiranda_mirandaResult", TestRNAmiranda.class);
		String mirandaResultFile = resource.getFile().getAbsolutePath();
		
		RNAmiranda rnAmiranda = new RNAmiranda();
		for (MirandaPair mirandaPair : rnAmiranda.readPerlines(mirandaResultFile)) {
			System.out.println(mirandaPair.getMirandaUnitMaxEnergy().getAlign());
		}
	}
}
