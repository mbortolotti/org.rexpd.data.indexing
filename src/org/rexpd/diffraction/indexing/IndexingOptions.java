package org.rexpd.diffraction.indexing;

public class IndexingOptions {
	
	public boolean searchCubic = true;
	public boolean searchTetragonal = true;
	public boolean searchHexagonal = true;
	public boolean searchOrthorombic = true;
	public boolean searchMonoclinic = false;
	public boolean searchTriclinic = false;
	public double aMax = 25.0;
	public double bMax = 25.0;
	public double cMax = 25.0;
	public double betaMin = 90.0;
	public double betaMax = 125.0;
	public double vMin = 0.0;
	public double vMax = 2500.0;
	public double MW = 0.0;
	public double density = 0.0;
	public double densityError = 0.0;
	public double peakPosError = 0.0;
	public double minFOM = 0.0;
	public int minLines = 5;
	public int minIndexedLines = 0;
	public int nLines = 0;
	public int nSpurious = 0;
	public double zeroError = 0.0;
	public boolean refineZero = false;
	public int maxZeroShifts = 10;
	public boolean extendedSearch = false;
	public double SearchTol2D = 3.0;
	public double SearchTol3D = 4.5;

}
