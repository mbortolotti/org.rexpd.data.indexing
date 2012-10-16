package org.rexpd.diffraction.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.rexpd.structure.structure.PeaksListModelFreePeaks;
import org.rexpd.structure.structure.Radiation;

public class IndexingContext {

	private IndexingProgram indexingProgram = null;
	private PeaksListModelFreePeaks peaksListModel = null;
	private List<IndexingSolution> solutions = null;
	private Radiation radiation = null;
	
	public IndexingContext(IndexingProgram program, PeaksListModelFreePeaks peaksList, Radiation rad) {
		indexingProgram = program;
		peaksListModel = peaksList;
		radiation = rad;
		solutions = new ArrayList<IndexingSolution>();
	}
	
	public IndexingProgram getIndexingProgram() {
		return indexingProgram;
	}

	public void setIndexingProgram(IndexingProgram program) {
		indexingProgram = program;
	}

	public PeaksListModelFreePeaks getPeaksListModel() {
		return peaksListModel;
	}

	public void setPeaksListModel(PeaksListModelFreePeaks peaksList) {
		peaksListModel = peaksList;
	}
	
	public void runIndexing() throws IOException, InterruptedException {
		solutions = indexingProgram.runIndexing(peaksListModel.getPeaksList(), radiation);
	}
	
	public List<IndexingSolution> getSolutions() {
		return solutions;
	}

	
}
