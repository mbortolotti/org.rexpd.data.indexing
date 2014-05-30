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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.rexpd.crystal.CrystalSystem;
import org.rexpd.structure.structure.Peak;
import org.rexpd.structure.structure.Radiation;
import org.rexpd.structure.structure.SpaceGroup;


public class Dicvol06 extends IndexingProgram {

	@Override
	public String toString() {
		return "DICVOL06";
	}

	@Override
	public List<IndexingSolution> runIndexing(List<? extends Peak> peaksList, Radiation radiation) throws IOException,
	InterruptedException {
			
	    /** Creates temp dir if it doesn't exist **/
		
	    File tempDir = new File("temp");
	    if (!tempDir.exists()) tempDir.mkdir();
	    
		/** delete previous run files, if necessary **/
		
		new File(tempDir, toString() + ".in").delete();
		new File(tempDir, toString() + ".out").delete();
		new File(tempDir, toString() + ".ord").delete();

		/** write the peak list input **/

		if (peaksList != null)
			writeProgramInput(peaksList, radiation);

		/** Run the command-line indexing program **/

		String command = getCommand();

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(true);

		Process process = builder.start();
		OutputStream os = process.getOutputStream();
		PrintWriter osWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
		InputStream is = process.getInputStream();
		BufferedReader isReader = new BufferedReader(new InputStreamReader(is));

		osWriter.println(toString() + ".in");
		osWriter.flush();

		osWriter.println(toString() + ".out");
		osWriter.flush();
		osWriter.close();

		while (isReader.readLine() != null)
			;
		isReader.close();
		
		process.waitFor();

		/** Parse the output **/

		IndexingSolution solution = null;
		List<IndexingSolution> solutions = new ArrayList<IndexingSolution>();
		
//		if (!(new File(toString() + ".ord").exists()))
//			return solutions;
//
//		Reader reader = new FileReader(toString() + ".ord");
//		BufferedReader br = new BufferedReader(reader);
//		
//		String outputLine;
//		StringBuilder stringBuilder = new StringBuilder();
//		String ls = System.getProperty("line.separator");
//		while ((outputLine = br.readLine()) != null) {
//			stringBuilder.append(outputLine);
//			stringBuilder.append(ls);
//		}
//		br.close();
//		String output = stringBuilder.toString();
		
		String output = readProgramOutput();
		if (output == null)
			return solutions;
		BufferedReader br = new BufferedReader(new StringReader(output));

		do {

			String line = br.readLine();
			if (line == null || line.indexOf("SOLUTION NUMBER") != -1) {
				if (solution != null && solution.getStructure() != null)
					solutions.add(solution);
				if (line == null)
					break;
				solution = new IndexingSolution();
			}

			String strippedLine = line.replaceAll("\\s", "").toUpperCase();
			
			if (strippedLine.indexOf("TRICLINICSYSTEM") != -1)
				solution.getStructure().setHallSymbol(SpaceGroup.fromCrystalSystem(CrystalSystem.TRICLINIC).hall());
			else if (strippedLine.indexOf("MONOCLINICSYSTEM") != -1)
				solution.getStructure().setHallSymbol(SpaceGroup.fromCrystalSystem(CrystalSystem.MONOCLINIC).hall());
			else if (strippedLine.indexOf("ORTHORHOMBICSYSTEM") != -1)
				solution.getStructure().setHallSymbol(SpaceGroup.fromCrystalSystem(CrystalSystem.ORTHOROMBIC).hall());
			else if (strippedLine.indexOf("TETRAGONALSYSTEM") != -1)
				solution.getStructure().setHallSymbol(SpaceGroup.fromCrystalSystem(CrystalSystem.TETRAGONAL).hall());
			else if (strippedLine.indexOf("TRIGONALSYSTEM") != -1)
				solution.getStructure().setHallSymbol(SpaceGroup.fromCrystalSystem(CrystalSystem.TRIGONAL).hall());
			else if (strippedLine.indexOf("HEXAGONALSYSTEM") != -1)
				solution.getStructure().setHallSymbol(SpaceGroup.fromCrystalSystem(CrystalSystem.HEXAGONAL).hall());
			else if (strippedLine.indexOf("CUBICSYSTEM") != -1)
				solution.getStructure().setHallSymbol(SpaceGroup.fromCrystalSystem(CrystalSystem.CUBIC).hall());

			/** parse direct (or reduced, if available) lattice onstants **/ 
			else if (strippedLine.indexOf("DIRECTPARAMETERS") != -1 || strippedLine.indexOf("REDUCEDCELL") != -1) {
				/** special case for triclinic solutions: cell parameters found in next output line **/
				if (strippedLine.indexOf("DIRECTPARAMETERS:A=") == -1 && strippedLine.indexOf("REDUCEDCELL:A=") == -1)
					line = br.readLine();
				
				System.out.println(line);
				int pos = -1;
				String parametersLine = line.replaceAll(" *\\= *", "\\=");
				pos = parametersLine.indexOf("A=");
				if (pos != -1)
					solution.getStructure().getCell().setA(Double.parseDouble(parametersLine.substring(pos + 2, parametersLine.indexOf(' ', pos + 2))));
				pos = parametersLine.indexOf("B=");
				if (pos != -1)
					solution.getStructure().getCell().setB(Double.parseDouble(parametersLine.substring(pos + 2, parametersLine.indexOf(' ', pos + 2))));
				pos = parametersLine.indexOf("C=");
				if (pos != -1)
					solution.getStructure().getCell().setC(Double.parseDouble(parametersLine.substring(pos + 2, parametersLine.indexOf(' ', pos + 2))));
				pos = parametersLine.indexOf("ALPHA=");
				if (pos != -1)
					solution.getStructure().getCell().setAlpha(Double.parseDouble(parametersLine.substring(pos + 6, parametersLine.indexOf(' ', pos + 6))));
				pos = parametersLine.indexOf("BETA=");
				if (pos != -1)
					solution.getStructure().getCell().setBeta(Double.parseDouble(parametersLine.substring(pos + 5, parametersLine.indexOf(' ', pos + 5))));
				pos = parametersLine.indexOf("GAMMA=");
				if (pos != -1)
					solution.getStructure().getCell().setGamma(Double.parseDouble(parametersLine.substring(pos + 6, parametersLine.indexOf(' ', pos + 6))));
				
				pos = parametersLine.indexOf("ALP=");
				if (pos != -1)
					solution.getStructure().getCell().setAlpha(Double.parseDouble(parametersLine.substring(pos + 4, parametersLine.indexOf(' ', pos + 4))));
				pos = parametersLine.indexOf("BET=");
				if (pos != -1)
					solution.getStructure().getCell().setBeta(Double.parseDouble(parametersLine.substring(pos + 4, parametersLine.indexOf(' ', pos + 4))));
				pos = parametersLine.indexOf("GAM=");
				if (pos != -1)
					solution.getStructure().getCell().setGamma(Double.parseDouble(parametersLine.substring(pos + 4, parametersLine.indexOf(' ', pos + 4))));
			}
			if (line.indexOf("1.- M(") != -1)
				solution.setFomM(parseFom(line));
			if (line.indexOf("2.- F(") != -1)
				solution.setFomF(parseFom(line));

		} while (true);
		
		br.close();
			
		/** move generated files in temp dir **/	   
	    
	    new File(toString() + ".in").renameTo(new File(tempDir, toString() + ".in"));
	    new File(toString() + ".out").renameTo(new File(tempDir, toString() + ".out"));
	    new File(toString() + ".ord").renameTo(new File(tempDir, toString() + ".ord"));

		return solutions;
	}
	
	private double parseFom(String line) {
		int pos_e = line.indexOf("=");
		if (pos_e != -1) {
			String stripped = line.substring(pos_e + 1);
			int pos_p = stripped.indexOf("(");
			if (pos_p != -1)
				return Double.parseDouble(stripped.substring(0, pos_p));
			else
				return Double.parseDouble(stripped);
		}
		return 0.0;
	}

//	private void savePeakList(List<? extends Peak> peaksList, Radiation radiation) throws IOException {
//		String filename = toString() + ".in";
//		PrintWriter output = new PrintWriter(filename);
//		
//		output.print(generateInput(peaksList, radiation));
//		
////		output.println("*** DICVOL06 peak list ***");
////		DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
////		int npeaks = peaksList.size();
////		int nlines = getIndexingOptions().nLines > 0 ? getIndexingOptions().nLines : npeaks;
////		output.print(nlines + " 3 ");
////		output.print(getIndexingOptions().searchCubic ? "1 " : "0 ");
////		output.print(getIndexingOptions().searchTetragonal ? "1 " : "0 ");
////		output.print(getIndexingOptions().searchHexagonal ? "1 " : "0 ");
////		output.print(getIndexingOptions().searchOrthorombic ? "1 " : "0 ");
////		output.print(getIndexingOptions().searchMonoclinic ? "1 " : "0 ");
////		output.print(getIndexingOptions().searchTriclinic ? "1" : "0");
////		output.println();
////		output.print(df.format(getIndexingOptions().aMax) + " ");
////		output.print(df.format(getIndexingOptions().bMax) + " ");
////		output.print(df.format(getIndexingOptions().cMax) + " ");
////		output.print(df.format(getIndexingOptions().vMin) + " ");
////		output.print(df.format(getIndexingOptions().vMax) + " ");
////		output.print(df.format(getIndexingOptions().betaMin) + " ");
////		output.print(df.format(getIndexingOptions().betaMax));
////		output.println();
////		double lambda = radiation.getDefaultComponent().getWaveLength();
////		output.print(df.format(lambda) + " ");
////		output.print(df.format(getIndexingOptions().MW) + " ");
////		output.print(df.format(getIndexingOptions().density) + " ");
////		output.print(df.format(getIndexingOptions().densityError));
////		output.println();
////		output.print(df.format(getIndexingOptions().peakPosError) + " ");
////		output.print(df.format(getIndexingOptions().minFOM) + " ");
////		output.print(getIndexingOptions().nSpurious + " ");
////		output.print(df.format(getIndexingOptions().zeroError) + " ");
////		output.print(getIndexingOptions().refineZero ? "1 " : "0 ");
////		output.print(getIndexingOptions().extendedSearch ? "1 " : "0 ");
////		output.println();
////		df.applyPattern("#.#####");
////		for (int np = 0; np < npeaks; np++) {
////			Peak peak = peaksList.get(np);
////			output.println(df.format(peak.getDSpacing()));
////		}
//		
//		output.close();
//	}

	@Override
	protected String getOutputFileName() {
		return toString() + ".ord";
	}

	@Override
	public String generateInput(List<? extends Peak> peaksList, Radiation radiation) {
		String input = ("*** DICVOL06 peak list ***\r\n");
		DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
		int npeaks = peaksList.size();
		int nlines = getIndexingOptions().nLines > 0 ? getIndexingOptions().nLines : npeaks;
		input += (nlines + " 3 ");
		input += (getIndexingOptions().searchCubic ? "1 " : "0 ");
		input += (getIndexingOptions().searchTetragonal ? "1 " : "0 ");
		input += (getIndexingOptions().searchHexagonal ? "1 " : "0 ");
		input += (getIndexingOptions().searchOrthorombic ? "1 " : "0 ");
		input += (getIndexingOptions().searchMonoclinic ? "1 " : "0 ");
		input += (getIndexingOptions().searchTriclinic ? "1" : "0");
		input += ("\r\n");
		input += (df.format(getIndexingOptions().aMax) + " ");
		input += (df.format(getIndexingOptions().bMax) + " ");
		input += (df.format(getIndexingOptions().cMax) + " ");
		input += (df.format(getIndexingOptions().vMin) + " ");
		input += (df.format(getIndexingOptions().vMax) + " ");
		input += (df.format(getIndexingOptions().betaMin) + " ");
		input += (df.format(getIndexingOptions().betaMax));
		input += ("\r\n");
		double lambda = radiation.getDefaultComponent().getWaveLength();
		input += (df.format(lambda) + " ");
		input += (df.format(getIndexingOptions().MW) + " ");
		input += (df.format(getIndexingOptions().density) + " ");
		input += (df.format(getIndexingOptions().densityError));
		input += ("\r\n");
		input += (df.format(getIndexingOptions().peakPosError) + " ");
		input += (df.format(getIndexingOptions().minFOM) + " ");
		input += (getIndexingOptions().nSpurious + " ");
		input += (df.format(getIndexingOptions().zeroError) + " ");
		input += (getIndexingOptions().refineZero ? "1 " : "0 ");
		input += (getIndexingOptions().extendedSearch ? "1 " : "0 ");
		input += ("\r\n");
		df.applyPattern("#.#####");
		for (int np = 0; np < npeaks; np++) {
			Peak peak = peaksList.get(np);
			input += (df.format(peak.getDSpacing()));
			input += ("\r\n");
		}
		return input;
	}

}
