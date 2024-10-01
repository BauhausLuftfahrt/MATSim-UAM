package net.bhl.matsim.uam.infrastructure.readers;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.matsim.core.utils.io.IOUtils;

import java.io.IOException;
import java.util.List;

public class CSVReaders {
	public static List<String[]> readTSV(String file) {
		return readFile(file, '\t');
	}

	public static List<String[]> readCSV(String file) {
		return readFile(file, CSVParser.DEFAULT_SEPARATOR);
	}

	public static List<String[]> readSemicolonSV(String file) {
		return readFile(file, ';');
	}

	public static List<String[]> readFile(String file, char separator) {
		try (CSVReader reader = new CSVReaderBuilder(IOUtils.getBufferedReader(file))
				.withCSVParser(new CSVParserBuilder().withSeparator(separator).build()).build()) {
			return reader.readAll();
		} catch (IOException | CsvException e) {
			throw new IllegalArgumentException(e);
		}
	}
}