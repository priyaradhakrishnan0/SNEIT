package com.salience.shared;

import java.io.Serializable;

public class KBAnnotation implements Serializable {

	String ne;
	String kbEntry;

	public String getNe() {
		return ne;
	}

	public void setNe(String ne) {
		this.ne = ne;
	}

	public String getKbEntry() {
		return kbEntry;
	}

	public void setKbEntry(String kbEntry) {
		this.kbEntry = kbEntry;
	}

}
