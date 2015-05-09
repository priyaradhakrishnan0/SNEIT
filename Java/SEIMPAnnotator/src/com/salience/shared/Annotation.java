package com.salience.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Annotation implements Serializable {

	String comments, annotator;
	List<String> sneList;
	List<KBAnnotation> kbList;

	public List<KBAnnotation> getKbList() {
		return kbList;
	}

	public void setKbList(List<KBAnnotation> kbList) {
		this.kbList = kbList;
	}
	
	public void addKbEntry(KBAnnotation kbAnn) {
		kbList=new ArrayList<KBAnnotation>();
		kbList.add(kbAnn);
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getAnnotator() {
		return annotator;
	}

	public void setAnnotator(String annotator) {
		this.annotator = annotator;
	}

	public List<String> getSneList() {
		return sneList;
	}

	public void setSneList(List<String> sneList) {
		this.sneList = sneList;
	}

}
