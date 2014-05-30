package org.rexpd.data.indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.rexpd.crystal.CrystalSystem;
import org.rexpd.structure.structure.Peak;
import org.rexpd.structure.structure.Radiation;
import org.rexpd.structure.structure.SpaceGroup;


public class ITO extends IndexingProgram {

	@Override
	public String toString() {
		return "ITO";
	}

	@Override
	public List<IndexingSolution> runIndexing(List<? extends Peak> peaksList, Radiation radiation)
	throws IOException, InterruptedException {	 
		
		/* Creates temp dir if it doesn't exist */
		
	    File tempDir = new File("temp");
	    if (!tempDir.exists()) tempDir.mkdir();
		
		/* delete previous run files */
		
		new File(tempDir, toString() + ".in").delete();
		new File(tempDir, toString() + ".out").delete();

		/* write the peak list input */

		if (peaksList != null)
			writeProgramInput(peaksList, radiation);

		/* Run the command-line indexing program */

		String command = getCommand();

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(true);

		Process process = builder.start();
		OutputStream os = process.getOutputStream();
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
		InputStream is = process.getInputStream();
		BufferedReader isReader = new BufferedReader(new InputStreamReader(is));

		writer.println();
		writer.flush();

		writer.println(toString() + ".in");
		writer.flush();

		writer.println(toString() + ".out");
		writer.flush();
		writer.close();
		
		while (isReader.readLine() != null)
			;
		isReader.close();
		
		process.waitFor();

		/** Parse the output **/

		List<IndexingSolution> solutions = new ArrayList<IndexingSolution>();
		
		
//		if (!(new File(toString() + ".out").exists()))
//			return solutions;
//
//		Reader reader = new FileReader(toString() + ".out");
//		BufferedReader br = new BufferedReader(reader);

		String output = readProgramOutput();
		if (output == null)
			return solutions;
		BufferedReader br = new BufferedReader(new StringReader(output));
		String line = null;

		while ((line = br.readLine()) != null) {
			
			/** Enter solution parsing loop - repeat if a better solution set is found **/
			if (line.indexOf("MOST PROBABLE SOLUTIONS") != -1) {
				
				int n_sol = 0;
				solutions.clear();

				while (line.indexOf("Q(A)") == -1)
					line = br.readLine();
				line = br.readLine();

				while (line.indexOf("THE DIRECT CONSTANTS OF THESE LATTICES") == -1) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					if (tokenizer.countTokens() == 9) {
						int token = 0;
						while (token++ < 7) tokenizer.nextToken();
						double fom = Double.parseDouble(tokenizer.nextToken());
						IndexingSolution solution = new IndexingSolution();
						solution.setFomM(fom);
						solutions.add(solution);
					}
					line = br.readLine();
				}
				line = br.readLine();
				while (line.indexOf("A") == -1)
					line = br.readLine();
				line = br.readLine();
				
				while (n_sol++ < solutions.size() && line.trim().length() != 0 && line.indexOf("*") == -1) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					if (tokenizer.countTokens() == 7) {
						solutions.get(n_sol - 1).getStructure().getCell().setA(Double.parseDouble(tokenizer.nextToken()));
						solutions.get(n_sol - 1).getStructure().getCell().setB(Double.parseDouble(tokenizer.nextToken()));
						solutions.get(n_sol - 1).getStructure().getCell().setC(Double.parseDouble(tokenizer.nextToken()));
						solutions.get(n_sol - 1).getStructure().getCell().setAlpha(Double.parseDouble(tokenizer.nextToken()));
						solutions.get(n_sol - 1).getStructure().getCell().setBeta(Double.parseDouble(tokenizer.nextToken()));
						solutions.get(n_sol - 1).getStructure().getCell().setGamma(Double.parseDouble(tokenizer.nextToken()));
						CrystalSystem symmetry = solutions.get(n_sol - 1).getStructure().getCell().guessCrystalSystem();
						solutions.get(n_sol - 1).getStructure().setHallSymbol(SpaceGroup.fromCrystalSystem(symmetry).hall());
					}
					line = br.readLine();
				}
			}
		}	
		
		br.close();
			
		/** move generated files in temp dir **/	   
	    
	    new File(toString() + ".in").renameTo(new File(tempDir, toString() + ".in"));
	    new File(toString() + ".out").renameTo(new File(tempDir, toString() + ".out"));	

	    new File("ITODOC.LST").renameTo(new File(tempDir, "ITODOC.LST"));
	    new File("ITOUT.LST").renameTo(new File(tempDir, "ITOUT.LST"));	

		return solutions;
	}

	@Override
	public String generateInput(List<? extends Peak> peaksList, Radiation radiation) {
		double lambda = radiation.getDefaultComponent().getWaveLength();
		Locale locale = Locale.US;
		String inputs = ("*** ITO peak list ***\r\n");
		inputs += ("0005");
		inputs += (getIndexingOptions().searchOrthorombic ? " 1" : "-1");
		inputs += (getIndexingOptions().searchMonoclinic ? " 1" : "-1");
		inputs += (getIndexingOptions().searchTriclinic ? " 1" : "-1");
		inputs += (String.format(locale, "%5.2f", getIndexingOptions().SearchTol2D));
		inputs += (String.format(locale, "%5.2f", getIndexingOptions().SearchTol3D));
		inputs += (String.format(locale, "%10.5f", lambda));
		for (int blank = 0; blank < 20; blank++)
			inputs += (" ");
		inputs += (String.format(locale, "%10.5f", getIndexingOptions().MW));
		inputs += (String.format(locale, "%10.5f", getIndexingOptions().density));
		inputs += (String.format(locale, "%8.4f", getIndexingOptions().peakPosError));
		inputs += ("  \r\n");
		inputs += (String.format(locale, "%10.4f", getIndexingOptions().zeroError));
		inputs += (String.format(locale, "%10.1f", getIndexingOptions().minFOM));
		inputs += (String.format(locale, "%10.0f", (double) getIndexingOptions().minIndexedLines));
		inputs += ("\r\n");
		for (int np = 0; np < peaksList.size(); np++) {
			Peak peak = peaksList.get(np);
			inputs += (String.format(locale, "%8.5f\r\n", peak.getDSpacing()));
		}
		inputs += ("0.\r\n");
		inputs += ("END\r\n");
		return inputs;
	}

	@Override
	protected String getOutputFileName() {
		return toString() + ".out";
	}

}
