package salience.dataset;

import java.io.Serializable;
import java.util.List;

public class Concept {

	String ne;
	List<String> kbEntries;

	public String getNe() {
		return ne;
	}

	public void setNe(String ne) {
		this.ne = ne;
	}

	public List<String> getKbEntries() {
		return kbEntries;
	}

	public void setKbEntries(List<String> kbEntries) {
		this.kbEntries = kbEntries;
	}

}
