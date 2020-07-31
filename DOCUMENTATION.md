# UAM-MATSim Input Files
* UAM-MATSim Version: uam-1.0-SNAPSHOT

## 0. Preparation
A standard MATSim scenario is required, which is to be converted into a UAM-enabled MATSim scenario.

### Automatic flight routing
* Retrieve OpenStreetMap data for standard MATSim scenario's study area, e.g. via [geofabrik](https://www.geofabrik.de/data/download.html)
* Load the following shapefiles into QGIS: roads, rail, (if desired) waterways
* Merge all layers
* Use `split lines with lines` (splitting it along itself) OR use `v.clean` with option `break` (and if need be also `rmline` to remove 0-length links) on the merged layer 
* Add a new column to the merged and split layer with desired travel cost, e.g. with:
```
CASE
  WHEN "fclass_2" = 'rail' THEN 1
  WHEN  "fclass" = 'motorway' OR "fclass" = 'motorway_link' OR "fclass" = 'primary' OR "fclass" = 'primary_link' OR "fclass" = 'trunk' OR "fclass" = 'trunk_link' THEN 1
  WHEN  "fclass" = 'secondary' OR "fclass" = 'secondary_link' OR "fclass" = 'tertiary' OR "fclass" = 'tertiary_link' THEN 3
  ELSE 9
END
```

* Load vertiport locations into QGIS
* Use `v.net.allpairs` with connecting threshold of at least 1000 and the newly-created cost column
* Explode generated flight routes
* On exploded layer, create new field `ID1` as text (unlimited): `concat(xat(0),'-',yat(0))`
* On exploded layer, create new field `ID2` as text (unlimited): `concat(xat(-1),'-',yat(-1))`
* Export exploded layer as CSV (columns `ID1` and `ID2` are sufficient to export)
* Remove all duplicates from exported link-containing CSV file
* Combine all entries of `ID1` and `ID2`, remove duplicates, and split at `-` symbol to obtain node-containing CSV file
* Put both files into nodes and links format, respectively, for usage with [1. UAM-enabled MATSim Network](#1.-uam-enabled-matsim-network)

## 1. UAM-enabled MATSim Network
A UAM-enabled MATSim network is required to run a UAM-enabled MATSim simulation. In addition to all the elements of a typical MATSim network, a UAM-enabled network includes stations, flight paths (as links), and links to typical MATSim network elements (e.g. connection between a UAM station and a road). As can be done for typical MATSim network links, UAM network links can have their length, number of lanes, capacity and free-flow speed set.

Use [RunCreateUAMScenario](#runcreateuamscenario) to generate UAM-enabled MATSim network.

## 2. UAM Vehicles
A UAM vehicles file (or simply, uam file) is required to run a UAM-enabled MATSim simulation. As shown in the script excerpt below, a UAM vehicles file contains attribute values for stations, vehicle types, and vehicles.

Use [RunCreateUAMScenario](#runcreateuamscenario) to generate UAM vehicles file.
### Structure
```xml
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE uam SYSTEM "src/main/resources/dtd/uam.dtd">
<uam>
	<stations>
		<station name="station-name" id="station-id" landingcap="100" preflighttime="600" postflighttime="120" defaultwaittime="0" link="link-id" />
	</stations>
	<vehicleTypes>
		<vehicleType id="vehicle-type-id" capacity="2" cruisespeed="42" verticalspeed="10" boardingtime="30" deboardingtime="30" turnaroundtime="300" />
	</vehicleTypes>
	<vehicles>
		<vehicle id="vehicle-id" type="vehicle-type-id" starttime="00:00:00" endtime="24:00:00"  initialstation="station-id" />
	</vehicles>
</uam>
```
## 3. UAM-enabled Configuration File
A UAM-enabled configuration file is required to run a UAM-enabled MATSim simulation. In addition to all the elements of a typical MATSim configuration file, a UAM-enabled configuration file includes UAM-specific parameters required for handling UAM simulation. 

The following are two approaches to generating a UAM-enabled configuration file:
* Automated:
	* Use [ConfigAddUAMParameters](#configadduamparameters) to generate UAM-enabled configuration file.
* Manual:
	* Create or take existing MATSim configuration (.xml) file and add UAM-specific parameters
	* Required parameters for the `uam` configuration group:
		* `inputUAMFile`
		* `availableAccessModes`
		* `parallelRouters`
		* `searchRadius`
		* `walkDistance`
		* `routingStrategy`
			* Possible strategies: `MAXUTILITY`, `MAXACCESSUTILITY`, `MINTRAVELTIME`, `MINACCESSTRAVELTIME`, `MINDISTANCE`, `MINACCESSDISTANCE`, `PREDEFINED`
		* `ptSimulation`
	* Required parameters for the `planCalcScore` configuration group:
		* Under `activityParams` parameter set
			* `activityType`
			* `scoringThisActivityAtAll`
		* Under `modeParams` parameter set
			* `mode`
			* `constant`
			* `marginalUtilityOfDistance_util_m`
			* `marginalUtilityOfTraveling_util_hr`
			* `monetaryDistanceRate`


## Miscellaneous
### Running Scenarios without Simulating Public Transport
When Running scenarios with public transport, yet without simulating PT vehicles, the following setup on MATSim standard config file must be performed:

On uam module:
```xml
<param name="ptSimulation" value="false" /> 
```

Add the following parameterset on the planscalcroute module:
```xml
 	<parameterset type="teleportedModeParameters">
			<param name="mode" value="pt" />
			<param name="teleportedModeSpeed" value="10.0" />
			<param name="beelineDistanceFactor" value="1.0" />
	</parameterset> 
```

### Convert MITO's trips.csv to MATSim population
On the example of running the conversion on Linux:
```bash
java -Xmx\<RAM\>G -cp uam-1.0-SNAPSHOT.jar:libs/* net.bhl.matsim.uam.utils.RunTripCSVToMATSimPlan "\<PATH-TO-TRIP.CSV\>" "\<PATH-TO-NETWORK.XML\>"
```

# UAM-MATSim Runscripts Overview
* UAM-MATSim Version: uam-1.0-SNAPSHOT

For all runscript explanations and examples, optional arguments are enclosed by parentheses. When using the following runscripts, remove all parentheses before usage.

## 1. Simulation Scripts
### RunUAMScenario
Starts UAM-enabled MATSim simulation.
* Location: net.bhl.matsim.uam.run.RunUAMScenario
* Arguments: see [Simulation Script](#simulation-script)

## 2. Scenario Scripts
### RunCreateUAMScenario
Creates UAM-including MATSim network and corresponding uam-vehicles file, which are prerequisites for running a UAM-enabled MATSim simulation.
* Location: net.bhl.matsim.uam.scenario.RunCreateUAMRoutedScenario
* Arguments (optional): `PATH network.xml(.gz) uam-stations.csv (detour-factor) (uam-vehicles.csv) (flight-nodes.csv flight-links.csv)`
	* uam-stations.csv header [format]: station_id [`String`], station_name [`String`], station_uam_parking_capacity [`double`], station_x [`double`], station_y [`double`], station_z [`double` m], vtol_z [`double` m], road_access_capacity [`double`], road_access_freespeed [`double` m/s], station_capacity [`double`], station_freespeed [`double` m/s], flight_access_capacity [`double`], flight_access_freespeed [`double` m/s], station_preflighttime [`double` s], station_postflighttime [`double` s], station_defaultwaittime [`double` s]
	* (optional) detour-factor: type `double`, default: `1.0`
	* (optional) uam-vehicles.csv header [format]: vehicle_type_id [`String`], vehicle_per_station [`int`], start_time [`HH:MM:SS`], end_time [`HH:MM:SS`], passenger_capacity [`int`], cruise_speed [`double` m/s], vertical_speed [`double` m/s], boarding_time [`double` s], deboarding_time [`double` s], turnaround_time [`double` s]
	* (optional) flight-nodes.csv header [format]: node_id [`String`], node_x [`double`], node_y [`double`], node_z [`double` m]
	* (optional) flight-links.csv header [format]: node_from [`String`], node_to [`String`], link_capacity [`double`], link_freespeed [`double` m/s]

### RunAddModeToNetwork
Adds specified mode(s) to an existing network if other specified mode(s) is (are) present.
* Location: net.bhl.matsim.uam.scenario.utils.network.RunAddModeToNetwork
* Arguments: `PATH/network.xml(.gz)`

### RunAddPopulationAttributes
Adds socio-demographic attributes to each person object in an existing population (or plan) file.
* Location: net.bhl.matsim.uam.scenario.population.RunAddPopulationAttributes
* Arguments: `PATH/plans.xml(.gz)`

### RunCreateUAMPersonAttributes
Generates a xml subpopulation attributes file containing the id of potential uam users based on a provided config file containing a search radius. If there is at least one station within reach, the user is considered a potential uam user.
* Location: net.bhl.matsim.uam.scenario.population.RunCreateUAMPersonAttributes
* Arguments: `PATH/config.xml`

### RunNetworkToSHP
Generates a shape file of a given network in MATSim format.
* Location: net.bhl.matsim.uam.scenario.utils.RunNetworkToSHP
* Arguments (optional): `PATH/network.xml(.gz) EPSG:EPSG-code (allowed-modes)`
	* EPSG:EPSG-code example: `EPSG:31468`
	* (optional) allowed-modes example: `car,pt,uam`

### ConfigAddUAMParameters
Adds UAM parameters to an existing configuration file
* Location: net.bhl.matsim.uam.scenario.utils
* Arguments: `PATH/config.xml NAME-uam-file(.xml) allowed-modes threads search-radius walk-distance routing-strategy pt-simulation PATH/output-file.xml`
	* allowed-modes example: `car,pt,uam`
	* Possible routing strategies: `MAXUTILITY`, `MAXACCESSUTILITY`, `MINTRAVELTIME`, `MINACCESSTRAVELTIME`, `MINDISTANCE`, `MINACCESSDISTANCE`, `PREDEFINED`
	* pt-simulation: `true`, `false`

## 3. Analysis Scripts
### RunCalculatePTTravelTimes
Provides schedule-based public transport travel times for a list of origins and destinations.
* Location: net.bhl.matsim.uam.analysis.traveltimes.RunCalculatePTTravelTimes
* Arguments: `PATH/network.xml(.gz) PATH/transit-schedule.xml(.gz) PATH/transit-vehicles.xml(.gz) PATH/coordinate-pairs-file.csv PATH/output-file.csv`
	* coordinate-pairs-file.csv header [format]: origin_x [`double`], origin_y [`double`], destination_x [`double`], destination_y [`double`], trip_time [`HH:MM:SS`]

### RunCalculateCarTravelTimes
Provides travel times by car for a list of origins and destinations. Uses events file from a previous simulation to update free flow speed in the network. 
* Location: net.bhl.matsim.uam.analysis.traveltimes.RunCalculateCarTravelTimes
* Arguments: `PATH/network.xml(.gz) PATH/networkChangeEvents.xml(.gz) PATH/coordinate-pairs-file.csv PATH/output-file.csv`
	* coordinate-pairs-file.csv header [format]: origin_x [`double`], origin_y [`double`], destination_x [`double`], destination_y [`double`], trip_time [`HH:MM:SS`]

### RunCalculateUAMTravelTimes
Provides travel times for UAM trips for a list of origins and destinations and a selected strategy. 
* Location: net.bhl.matsim.uam.analysis.traveltimes.RunCalculateUAMTravelTimes
* Arguments: `PATH/network.xml(.gz) PATH/uam.xml(.gz) PATH/transit-schedule.xml(.gz) PATH/transit-vehicles.xml(.gz) PATH/coordinate-pairs-file.csv PATH/output-file.csv UAMStrategy PATH/output-networkEventsChange-file.xml(.gz)`
	* coordinate-pairs-file.csv header [format]: origin_x [`double`], origin_y [`double`], destination_x [`double`], destination_y [`double`], trip_time [`HH:MM:SS`]
	* allowed UAMStrategies: `minTravelTime, minAccessTravelTime, minDistance, minAccessDistance`

### RunGenerateNetworkChangeEventsFile
Generates a NetworkChangeEvents file containing changes in the network throughout the day. 
* Location: net.bhl.matsim.uam.analysis.traveltimes.RunGenerateNetworkChangeEventsFile
* Arguments: `PATH/network.xml(.gz) PATH/events.xml(.gz) PATH/output-networkEventsChange-file.xml(.gz)`

### ConvertDemographicsFromPopulation
Creates a demographics file by reading through and gathering socio-demographic attributes from each person object in an existing population (or plan) file.
* Location: net.bhl.matsim.uam.analysis.demographics.ConvertDemographicsFromPopulation
* Arguments: `PATH/plans.xml(.gz) PATH/households.xml(.gz) PATH/output-file.csv`

### ConvertLinkStatsFromEvents
Provides the average speed per link per hour of the input network from an output simulation events file.
* Location: net.bhl.matsim.uam.analysis.traffic.run.ConvertLinkStatsFromEvents
* Arguments: `PATH/network.xml(.gz) PATH/events.xml(.gz) PATH/output-file.csv`

### ConvertTransitTripsFromEvents
Provides a csv file containing information of all public transport trips performed from an events output file.
* Location: net.bhl.matsim.uam.analysis.transit.run.ConvertTransitTripsFromEvents
* Arguments: `PATH/network.xml(.gz) PATH/events.xml(.gz) PATH/output-file.csv`

### ConvertTripsFromEvents
Creates a trips file by reading through and gathering trip information from an existing events file.
* Location: net.bhl.matsim.uam.analysis.trips.run.ConvertTripsFromEvents
* Arguments: `PATH/network.xml(.gz) PATH/events.xml(.gz) PATH/output-file.csv`

### ConvertTripsFromPopulation
Creates a trips file by reading through and gathering trip information from an existing population (or plan) file.
* Location: net.bhl.matsim.uam.analysis.trips.run.ConvertTripsFromPopulation
* Arguments: `PATH/network.xml(.gz) PATH/plans.xml(.gz) PATH/output-file.csv`

### ConvertUAMDemandFromEvents
Generates a csv file containing information of each UAM trip performed from a previous simulation using the events file. 
* Location: net.bhl.matsim.uam.analysis.uamdemand.run.ConvertUAMDemandFromEvents
* Arguments: `PATH/network.xml(.gz) PATH/events.xml(.gz) PATH/uam-vehicles.xml(.gz) PATH/output-file.csv`

### ConvertUAMStationsFromUAMVehicles
Generates a csv file containing the following information about UAM Stations: name, id, landing capacity, pre-flight time, post-flight time, default wait time and link.
* Location: net.bhl.matsim.uam.analysis.uamstations.run.ConvertUAMStationsFromUAMVehicles
* Arguments: `PATH/network.xml(.gz) PATH/uam-vehicles.xml(.gz) PATH/output-file.csv`

### Analysis Batch Scripts
#### BatchConvertLinkStatsFromEvents
Executes [ConvertLinkStatsFromEvents](#convertlinkstatsfromevents) for all MATSim output folders within the provided base folder.
* Location: net.bhl.matsim.uam.analysis.traffic.run.BatchConvertLinkStatsFromEvents
* Arguments: `PATH` to base folder containing one or more MATSim output folders

#### BatchConvertTransitTripsFromEvents
Executes [ConvertTransitTripsFromEvents](#converttransittripsfromevents) for all MATSim output folders within the provided base folder.
* Location: net.bhl.matsim.uam.analysis.transit.run.BatchConvertTransitTripsFromEvents
* Arguments: `PATH` to base folder containing one or more MATSim output folders

#### BatchConvertTripsFromEvents
Executes [ConvertTripsFromEvents](#converttripsfromevents) for all MATSim output folders within the provided base folder.
* Location: net.bhl.matsim.uam.analysis.trips.run.BatchConvertTripsFromEvents
* Arguments: `PATH` to base folder containing one or more MATSim output folders

#### BatchConvertTripsFromPopulation
Executes [ConvertTripsFromPopulation](#converttripsfrompopulation) for all MATSim output folders within the provided base folder.
* Location: net.bhl.matsim.uam.analysis.trips.run.BatchConvertTripsFromPopulation
* Arguments: `PATH` to base folder containing one or more MATSim output folders

#### BatchConvertUAMDemandFromEvents
Executes [ConvertUAMDemandFromEvents](#convertuamdemandfromevents) for all MATSim output folders within the provided base folder.
* Location: net.bhl.matsim.uam.analysis.uamdemand.run.BatchConvertUAMDemandFromEvents
* Arguments: `PATH` to base folder containing one or more MATSim output folders

#### BatchConvertUAMStationsFromUAMVehicles
Executes [ConvertUAMStationsFromUAMVehicles](#convertuamstationsfromuamvehicles) for all MATSim output folders within the provided base folder.
* Location: net.bhl.matsim.uam.analysis.uamstations.run.BatchConvertUAMStationsFromUAMVehicles
* Arguments: `PATH` to base folder containing one or more MATSim output folders

#### RunBatchConversions
Executes [BatchConvertUAMStationsFromUAMVehicles](#batchconvertuamstationsfromuamvehicles), [BatchConvertUAMDemandFromEvents](#batchconvertuamdemandfromevents), [BatchConvertTripsFromEvents](#batchconverttripsfromevents), [BatchConvertTransitTripsFromEvents](#batchconverttransittripsfromevents), [BatchConvertTripsFromPopulation](#batchconverttripsfrompopulation), [BatchConvertLinkStatsFromEvents](#batchconvertlinkstatsfromevents) consecutively for all MATSim output folders within the provided base folder.
* Location: net.bhl.matsim.uam.analysis.RunBatchConversions
* Arguments: `PATH` to base folder containing one or more MATSim output folders

## 4. Other Utility Scripts
### RunConvertDeckGLBuildings
Convert QGIS geojson file from OSM buildings to deck.gl-readable buildings input file.
* Location: net.bhl.matsim.uam.analysis.trips.run.RunConvertDeckGLBuildings
* Arguments: GEOJSON-File

### ConvertDeckGLTripsFromEvents
Convert events file to deck.gl-readable trips input file.
* Location: net.bhl.matsim.uam.analysis.trips.run.ConvertDeckGLTripsFromEvents
* Arguments (optional): `PATH/network.xml(.gz) PATH/events.xml(.gz) (EPSG:EPSG-code)`
	* (optional) EPSG:EPSG-code example: `EPSG:2154`

