package net.bhl.matsim.uam.utils;

import java.io.*;

/**
 * This script converts QGIS geojson file from OSM buildings to deck.gl-readable
 * buildings input file.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class RunConvertDeckGLBuildings {
	static public void main(String[] args) throws IOException {
		// PROVIDE: GEOJSON-File
		extract(args[0]);
		System.out.println("done.");
	}

	static public void extract(String geojson) throws IOException {
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			InputStream fis = new FileInputStream(geojson);
			br = new BufferedReader(new InputStreamReader(fis));

			File file = new File(geojson + ".json");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			bw.write("[" + System.lineSeparator());

			boolean first = true;
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (!line.contains("coordinates"))
					continue;
				else {
					if (!first)
						bw.write("," + System.lineSeparator());

					int index = line.indexOf("coordinates\": ") + "coordinates\": ".length();
					String linePart = line.substring(index);
					linePart = linePart.substring(0, linePart.length() - 4);
					linePart = linePart.replace("[ [ [ [ ", "[ [ [ ");
					linePart = linePart.replace(" ] ] ] ]", "] ] ] ");

					bw.write("{\"height\": 100, ");
					bw.write("\"polygon\": ");
					bw.write(linePart);
					bw.write("}");
					first = false;
				}
			}

			bw.write(System.lineSeparator() + "]");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (br != null)
					br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}