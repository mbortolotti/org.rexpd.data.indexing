package org.rexpd.data.indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.rexpd.structure.structure.Peak;
import org.rexpd.structure.structure.Radiation;
import org.rexpd.structure.structure.SpaceGroup;
import org.rexpd.structure.structure.SpaceGroup.CRYSTAL_SYSTEM;


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
			savePeakList(peaksList, radiation);

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

		/* Parse the output */

		List<IndexingSolution> solutions = new ArrayList<IndexingSolution>();
		
		if (!(new File(toString() + ".out").exists()))
			return solutions;

		Reader reader = new FileReader(toString() + ".out");
		BufferedReader br = new BufferedReader(reader);

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
						CRYSTAL_SYSTEM symmetry = solutions.get(n_sol - 1).getStructure().getCell().guessCrystalSystem();
						solutions.get(n_sol - 1).getStructure().setHallSymbol(SpaceGroup.fromCrystalSystem(symmetry).hall());
					}
					line = br.readLine();
				}
			}
		}	
		
		br.close();
			
		/* move generated files in temp dir */	   
	    
	    new File(toString() + ".in").renameTo(new File(tempDir, toString() + ".in"));
	    new File(toString() + ".out").renameTo(new File(tempDir, toString() + ".out"));	

	    new File("ITODOC.LST").renameTo(new File(tempDir, "ITODOC.LST"));
	    new File("ITOUT.LST").renameTo(new File(tempDir, "ITOUT.LST"));	

		return solutions;
	}

	private void savePeakList(List<? extends Peak> peaksList, Radiation radiation) throws IOException {
		String filename = toString() + ".in";
		PrintWriter output = new PrintWriter(filename);
		double lambda = radiation.getDefaultComponent().getWaveLength();
		Locale locale = Locale.US;
		output.println("*** ITO peak list ***");
		output.print("0005");
		output.print(getIndexingOptions().searchOrthorombic ? " 1" : "-1");
		output.print(getIndexingOptions().searchMonoclinic ? " 1" : "-1");
		output.print(getIndexingOptions().searchTriclinic ? " 1" : "-1");
		output.print(String.format(locale, "%5.2f", getIndexingOptions().SearchTol2D));
		output.print(String.format(locale, "%5.2f", getIndexingOptions().SearchTol3D));
		output.print(String.format(locale, "%10.5f", lambda));
		for (int blank = 0; blank < 20; blank++)
			output.print(" ");
		output.print(String.format(locale, "%10.5f", getIndexingOptions().MW));
		output.print(String.format(locale, "%10.5f", getIndexingOptions().density));
		output.print(String.format(locale, "%8.4f", getIndexingOptions().peakPosError));
		output.println("  ");
		output.print(String.format(locale, "%10.4f", getIndexingOptions().zeroError));
		output.print(String.format(locale, "%10.1f", getIndexingOptions().minFOM));
		output.println(String.format(locale, "%10.0f", (double) getIndexingOptions().minIndexedLines));
		for (int np = 0; np < peaksList.size(); np++) {
			Peak peak = peaksList.get(np);
			output.println(String.format(locale, "%8.5f", peak.getDSpacing()));
		}
		output.println("0.");
		output.println("END");
		output.close();
	}

}
