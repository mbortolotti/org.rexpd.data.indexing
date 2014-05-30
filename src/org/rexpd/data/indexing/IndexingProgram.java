package org.rexpd.data.indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.rexpd.core.utils.Config;
import org.rexpd.structure.structure.Peak;
import org.rexpd.structure.structure.Radiation;

public abstract class IndexingProgram {

	static final String PLUGIN_ID = "org.rexpd.data.indexing";
	static final String BIN_PATH = "data/bin/";

	private IndexingOptions indexingOptions = new IndexingOptions();
	private String programInput = null;
	private String programOutput = null;

	public IndexingOptions getIndexingOptions() {
		return indexingOptions;
	}

	public String getCommand() throws IOException, InterruptedException {
		String OS = Platform.OS_WIN32; /* default */
		String suffix = "";
		if (Platform.isRunning())
			OS = Platform.getOS();
		if (OS.equals(Platform.OS_WIN32))
			suffix = "exe";
		else if (OS.equals(Platform.OS_MACOSX))
			suffix = "osx";
		else if (OS.equals(Platform.OS_LINUX))
			suffix = "linux";
		String command = BIN_PATH + toString().toLowerCase() + "." + suffix;
		command = Config.getURL(PLUGIN_ID, command).getPath();
		new File(command).setExecutable(true);
		return command;
	}

	public abstract List<IndexingSolution> runIndexing(List<? extends Peak> peaksList, Radiation radiation) 
			throws IOException, InterruptedException;

	public abstract String generateInput(List<? extends Peak> peaksList, Radiation radiation);

	protected void writeProgramInput(List<? extends Peak> peaksList, Radiation radiation) throws IOException {
		String filename = toString() + ".in";
		PrintWriter output = new PrintWriter(filename);
		output.print(getProgramInput(peaksList, radiation));
		//setProgramInput(null);
		output.close();
	}

	public String getProgramInput(List<? extends Peak> peaksList, Radiation radiation) {
		if (programInput == null)
			programInput = generateInput(peaksList, radiation);
		return programInput;
	}

	public void setProgramInput(String input) {
		programInput = input;
	}

	public String getProgramOutput() {
		return programOutput;
	}

	protected abstract String getOutputFileName();
	
	protected String readProgramOutput() {
		programOutput = null;
		StringBuilder stringBuilder = new StringBuilder();
		String outputLine;
		String ls = System.getProperty("line.separator");
		try {
			if (!(new File(getOutputFileName()).exists()))
				return null;
			Reader reader = new FileReader(getOutputFileName());
			BufferedReader br = new BufferedReader(reader);
			while ((outputLine = br.readLine()) != null) {
				stringBuilder.append(outputLine);
				stringBuilder.append(ls);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		programOutput = stringBuilder.toString();
		return programOutput;
	}

}
