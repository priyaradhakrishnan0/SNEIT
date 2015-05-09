package salience.kb;

import java.util.HashMap;
import java.util.List;

public class KBRetrieval {
	String ne;
	HashMap<String, Double> retrievals;
	
	public void setRetrievals(HashMap<String, Double>  results){
		this.retrievals = results;
	}
	
	public HashMap<String, Double>  getRetrievals(){
		return retrievals;
	}
	
	public void setNE(String ne){
		this.ne = ne;
	}
	
	public String getNE(){
		return ne;
	}
}
