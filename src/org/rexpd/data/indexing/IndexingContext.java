package org.rexpd.data.indexing;

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
	private boolean clearPreviousSolutions = true;
	
	public IndexingContext(PeaksListModelFreePeaks peaksList, Radiation rad, IndexingProgram program) {
		indexingProgram = program;
		peaksListModel = peaksList;
		radiation = rad;
		solutions = new ArrayList<IndexingSolution>();
	}
	
	public IndexingContext(PeaksListModelFreePeaks peaksList, Radiation rad) {
		this(peaksList, rad, null);
	}

	public void runIndexing() throws IOException, InterruptedException {
		/** perform indexing **/
		List<IndexingSolution> newSolutions = indexingProgram.runIndexing(peaksListModel.getPeaksList(), radiation);
		for (int n_sol = 0; n_sol < newSolutions.size(); n_sol++)
			newSolutions.get(n_sol).getStructure().setLabel(getIndexingProgram().toString() + " - " + Integer.toString(n_sol + 1));
		/** add new found solutions to solution list **/
		if (solutions == null)
			solutions = new ArrayList<IndexingSolution>();
		if (clearPreviousSolutions)
			solutions.clear();
		solutions.addAll(newSolutions);
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
	
	public Radiation getRadiation() {
		return radiation;
	}

	public void clearPreviousSolutions(boolean clear) {
		clearPreviousSolutions = clear;
	}
	
	public List<IndexingSolution> getSolutions() {
		return solutions;
	}

	
}
