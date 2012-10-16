package org.rexpd.diffraction.indexing;


import org.rexpd.structure.structure.Structure;

public class IndexingSolution {
	
	private Structure structure = null;
	boolean selected = false;
	private double fomM = 0;
	private double fomF = 0;
	
	public IndexingSolution() {
		structure = new Structure(null);
	}
	
	public Structure getStructure() {
		return structure;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean sel) {
		selected = sel;
	}

	public double getFomM() {
		return fomM;
	}
	
	public void setFomM(double fom) {
		fomM = fom;
	}
	
	public double getFomF() {
		return fomF;
	}
	
	public void setFomF(double fom) {
		fomF = fom;
	}

}
