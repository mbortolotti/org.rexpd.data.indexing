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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.rexpd.crystal.CrystalSystem;
import org.rexpd.structure.structure.Peak;
import org.rexpd.structure.structure.Radiation;
import org.rexpd.structure.structure.SpaceGroup;


public class NTREOR extends IndexingProgram {

	@Override
	public String toString() {
		return "NTREOR";
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
		new File(tempDir, toString() + ".con").delete();
		new File(tempDir, toString() + ".sho").delete();

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

		writer.println(toString() + ".con");
		writer.flush();

		writer.println(toString() + ".sho");
		writer.flush();

		writer.println(getIndexingOptions().zeroError);
		writer.flush();

		writer.println();
		writer.flush();

		writer.close();

		while (isReader.readLine() != null)
			;
		isReader.close();

		process.waitFor();

		/* Parse the output */

		List<IndexingSolution> solutions = new ArrayList<IndexingSolution>();
		IndexingSolution solution = null;
		

//		if (!(new File(toString() + ".sho").exists()))
//			return solutions;
//
//		String outputFileName = toString() + ".sho";
//		//Reader reader = new FileReader(outputFileName);
//		BufferedReader br = new BufferedReader(new FileReader(outputFileName));
		
		String output = readProgramOutput();
		if (output == null)
			return solutions;
		BufferedReader br = new BufferedReader(new StringReader(output));
		

		String line = null;

		/** check for the last trial block **/
		String lastTrialLine = "";
		while ((line = br.readLine()) != null) {
			if (line.indexOf("N-TREOR trial #") != -1)
				lastTrialLine = line;
		}
		br.close();

		/** reopen the reader **/
		br = new BufferedReader(new StringReader(output));


		while ((line = br.readLine()) != null) {

			/** Last trial block contains the final solution **/
			if (line.indexOf(lastTrialLine) != -1) {
				while ((line = br.readLine()) != null) {


					/** solution entry starts with Crystal system definition **/
					if (line.indexOf("**** Refined cell parameters ****") != -1) {
						solution = new IndexingSolution();
						//				String csString = line.substring(line.lastIndexOf(":") + 1, line.length()).trim();
						//				for (CrystalSystem crystalSystem : CrystalSystem.values())
						//					if (crystalSystem.toString().equalsIgnoreCase(csString))
						//						solution.getStructure().setHallSymbol(SpaceGroup.fromCrystalSystem(crystalSystem).hall());
					}
					/** still waiting to find a solution **/
					if (solution == null)
						continue; 

					String stripped = line.replaceAll(" *\\= *", "\\=").trim();
					if (stripped.indexOf("A=") != -1) {
						int pos_a = stripped.indexOf("A=");
						int pos_b = stripped.indexOf("B=");
						int pos_c = stripped.indexOf("C=");
						if (pos_a == -1 || pos_b == -1 || pos_c == -1) continue;
						solution.getStructure().getCell().setA(Double.parseDouble(stripped.substring(pos_a + 2, pos_b)));
						solution.getStructure().getCell().setB(Double.parseDouble(stripped.substring(pos_b + 2, pos_c)));
						solution.getStructure().getCell().setC(Double.parseDouble(stripped.substring(pos_c + 2, stripped.length())));
					}
					else if (stripped.indexOf("Alpha=") != -1) {
						int pos_a = stripped.indexOf("Alpha=");
						int pos_b = stripped.indexOf("Beta=");
						int pos_c = stripped.indexOf("Gamma=");
						if (pos_a == -1 || pos_b == -1 || pos_c == -1) continue;
						solution.getStructure().getCell().setAlpha(Double.parseDouble(stripped.substring(pos_a + 6, pos_b)));
						solution.getStructure().getCell().setBeta(Double.parseDouble(stripped.substring(pos_b + 5, pos_c)));
						solution.getStructure().getCell().setGamma(Double.parseDouble(stripped.substring(pos_c + 6, stripped.length())));
					}
					else if (stripped.indexOf("M(") != -1) {
						int pos_1 = stripped.indexOf(")=");
						int pos_2 = stripped.indexOf("M'(");
						if (pos_1 != -1 || pos_2 != -1)
							solution.setFomM(Double.parseDouble(stripped.substring(pos_1 + 2, pos_2)));

						/** guess crystal symmetry, add solution to list **/
						CrystalSystem symmetry = solution.getStructure().getCell().guessCrystalSystem();
						solution.getStructure().setHallSymbol(SpaceGroup.fromCrystalSystem(symmetry).hall());

						solutions.add(solution); 
						solution = null;
						//if (--n_sol == 0) break;
					}
				}
			}
		}

		br.close();

		/** move generated files in temp dir **/
		new File(toString() + ".in").renameTo(new File(tempDir, toString() + ".in"));
		new File(toString() + ".out").renameTo(new File(tempDir, toString() + ".out"));
		new File(toString() + ".con").renameTo(new File(tempDir, toString() + ".con"));
		new File(toString() + ".sho").renameTo(new File(tempDir, toString() + ".sho"));

		return solutions;
	}

	@Override
	public String generateInput(List<? extends Peak> peaksList, Radiation radiation) {
		double lambda = radiation.getDefaultComponent().getWaveLength();
		Locale locale = Locale.US;
		String input = ("*** NTREOR peak list ***");
		for (int np = 0; np < peaksList.size(); np++) {
			Peak peak = peaksList.get(np);
			input += (String.format(locale, "%8.5f\r\n", peak.getDSpacing()));
		}
		input += ("\r\n");
		input += ("CHOICE=4,\r\n"); /* d-values */
		input += ("WAVE=" + String.format(locale, "%8.5f", lambda) + ",\r\n");
		input += ("VOL=" + String.format(locale, "%8.0f", getIndexingOptions().vMax) + ",\r\n");
		input += ("CEM=" +  String.format(locale, "%5.0f", getIndexingOptions().cellEdgeMax) + ",\r\n");
		input += ("END*\r\n");
		return input;
	}

	@Override
	protected String getOutputFileName() {
		return toString() + ".sho";
	}

}
