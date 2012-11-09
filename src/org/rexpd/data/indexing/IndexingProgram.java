package org.rexpd.data.indexing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.rexpd.core.utils.Config;
import org.rexpd.structure.structure.Peak;
import org.rexpd.structure.structure.Radiation;

public abstract class IndexingProgram {

	static final String PLUGIN_ID = "org.rexpd.data.indexing";
	static final String BIN_PATH = "data/bin/";
	
	private IndexingOptions indexingOptions = new IndexingOptions();

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

}
