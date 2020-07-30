package net.bhl.matsim.uam.analysis.demographics;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.households.Household;
import org.matsim.households.HouseholdsReaderV10;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * This script Creates a demographics file by reading through and gathering
 * socio-demographic attributes from each person object in an existing
 * population (or plan) file. Necessary inputs are in the following order:
 * -plans file; -households file; -output file
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class ConvertDemographicsFromPopulation {

	public static void main(String[] args) throws IOException {
		// cmd-line input: input-population input-households outfile
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

		int i = 0;
		new PopulationReader(scenario).readFile(args[i++]);
		new HouseholdsReaderV10(scenario.getHouseholds()).readFile(args[i++]);
		String outfile = args[i++];

		Map<Id<Person>, Id<Household>> householdMap = new HashMap<>();
		for (Household household : scenario.getHouseholds().getHouseholds().values()) {
			household.getMemberIds().forEach(id -> householdMap.put(id, household.getId()));
		}

		Set<String> columns = new HashSet<>();
		Set<Map<String, String>> population = new HashSet<>();
		for (Person person : scenario.getPopulation().getPersons().values()) {
			Map<String, String> personAttrMap = new HashMap<>();

			personAttrMap.put("ID", person.getId().toString());
			columns.add("ID");

			String[] attributePairs = person.getAttributes().toString().split("[{}]+");
			for (String attributePair : attributePairs) {
				if (attributePair.isEmpty())
					continue;

				String[] extracts = attributePair.split("[;=]+");
				// e.g. key age object 39
				columns.add(extracts[1].trim());
				personAttrMap.put(extracts[1].trim(), extracts[3].trim());
			}

			Household household = scenario.getHouseholds().getHouseholds().get(householdMap.get(person.getId()));

			if (household != null) {
				personAttrMap.put("household_ID", household.getId().toString());
				columns.add("household_ID");

				personAttrMap.put("household_income", "" + household.getIncome().getIncome());
				columns.add("household_income");

				personAttrMap.put("household_currency", household.getIncome().getCurrency());
				columns.add("household_currency");

				personAttrMap.put("household_incomePeriod", household.getIncome().getIncomePeriod().toString());
				columns.add("household_incomePeriod");

				String[] hhAttributePairs = household.getAttributes().toString().split("[{}]+");
				for (String hhAttributePair : hhAttributePairs) {
					if (hhAttributePair.isEmpty())
						continue;

					String[] extracts = hhAttributePair.split("[;=]+");
					// e.g. key age object 39
					columns.add("household_" + extracts[1].trim());
					personAttrMap.put("household_" + extracts[1].trim(), extracts[3].trim());
				}
			}

			population.add(personAttrMap);
		}

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile)));

		// Make columns into ordered list, write header
		List<String> columnList = new ArrayList<>();
		columnList.addAll(columns);
		Collections.sort(columnList);
		System.out.println(columnList);

		boolean first = true;
		for (Map<String, String> personAttrMap : population) {
			if (first) {
				first = false;

				for (Iterator<String> it = columnList.iterator(); it.hasNext(); ) {
					writer.write(it.next());

					if (it.hasNext())
						writer.write(",");
					else
						writer.write(System.lineSeparator());
				}
			}

			for (Iterator<String> it = columnList.iterator(); it.hasNext(); ) {
				String key = it.next();
				if (personAttrMap.containsKey(key))
					writer.write(personAttrMap.get(key));

				if (it.hasNext())
					writer.write(",");
				else
					writer.write(System.lineSeparator());
			}
		}

		writer.close();

		System.out.println("done.");
	}
}
