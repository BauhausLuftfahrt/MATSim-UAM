
# UAM-Extension

This extension enhances MATSim (see the project's [Website](https://www.matsim.org) or [GitHub](https://github.com/matsim-org) pages) by allowing the definition and simulation of Urban Air Mobility infrastructure, vehicles, and operations. This extension is a collaborative development project between [Airbus Urban Mobility](https://www.airbus.com/innovation/urban-air-mobility.html), [Bauhaus Luftfahrt e.V.](https://www.bauhaus-luftfahrt.net), [ETH Zürich](https://www.ethz.ch), and [TU München](https://www.tse.bgu.tum.de) and authored by [Raoul Rothfeld](https://github.com/RRothfeld) and [Milos Balac](https://github.com/balacmi), with support from [Aitan Militão](https://github.com/Aitanm).

## Installation and Usage
See [DOCUMENTATION](https://github.com/BauhausLuftfahrt/MATSim-UAM/blob/master/DOCUMENTATION.md).

Add the following to your maven pom.xml under `repositories`:
```xml
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
```
And the following to you maven pom.xml under `dependencies` for the latest version under active development:
```xml
        <dependency>
            <groupId>com.github.BauhausLuftfahrt</groupId>
            <artifactId>MATSim-UAM</artifactId>
            <version>master-SNAPSHOT</version>
        </dependency>
```
Older versions can be used by replacing the version text with any of the listed tags on [GitHub MATSim-UAM tags](https://github.com/BauhausLuftfahrt/MATSim-UAM/tags).

## Features
List of all current features provided by MATSim-UAM with the version of feature introduction in parentheses.

### Infrastructure
- UAM Vehicle Types: The vehicle type contains the capacity, cruisespeed, vertical speed, (de)boarding and turn around times. (v1)
- UAM Vehicles: Contains information about the UAM Vehicle Type, initial UAM Station and start/end time of operation. (v1)
- UAM Station: UAM Stations contain a predefined pre/post flight time for UAM Vehicles, default waiting time and the station link location. (v1)
- UAM Flight Network: Similar to conventional MATSim links, UAM flight links allow for the definition of flight routes with different flight segments. (v1)

### Simulation
- UAM Vehicles follow a schedule that has the following sequence, starting and ending with StayTask: PickUpDriveTask, PickUpTask, DropOffDriveTask, DropOffTask, TurnAroundTask, StayTask. (v1)
- UAM Passengers have the following sequence of activities: Access trip, preFlightTime, PickUpTask, UAM leg (UAMVehicle DropOffDriveTask), DropOffTask, PostFlightTime, Egress Trip. (v1)
- Dispatching strategy: MATSim-UAM uses a pooled dispatcher that allows for shared rides if trip origin and destination are the same, and if vehicle capacity constraint is met. (v1)
- Introduction of UAM routing strategies that aim to, e.g., maximize overral UAM trip utility, or minimize access/egress distance; including predefined routing strategy where UAM trips (i.e access/egress modes and UAM stations) can be defined externally, e.g. by MITO. (v1)
- Integration of option to skip public transport simulation and teleport its agents instead. (v1)

### Miscellaneous:
- Conversion of MATSim events into CSV files will display distances in the scenario's CRS, instead of assuming a metre-based CRS and storing distances as KM. (v1)
- Output plans will show expected distance and travel time for UAM legs. (v1)
- Simulation-independent conversion from events to UAM demand (.CSV) file. (v1)
- Warning log message when no age is given for an agent, so default age is being assumed in mode choice for cycling. (v1)
- The input file for UAM Vehicles is written out into the output folder. (v1)
- Included interface to interact with [MITO](https://www.msm.bgu.tum.de/en/research/modeling/mito/) (Microscopic Transport Orchestrator). (v1)
- Included external public transport and car router for calculating travel times for a list of origins and destinations. (v1)
- Included external UAM station router for calculating travel times and distances between all UAM stations. (v1)
- Included documentation about analysis and utility scripts. See [DOCUMENTATION](https://github.com/BauhausLuftfahrt/MATSim-UAM/blob/master/DOCUMENTATION.md). (v1)  

## Versions and Change Log

### v2.0
Travel time scripts:
- Replaced provision of, e.g., network and transit schedule via programme arguments to retrieval from config file
- Added distance and links path list to CalculateCarTravelTimes
- Added distance and route information to CalculatePTTravelTimes

Updates the extension to MATSim version 11 and current DVRP version. Major changes includes:
- UAMQSimPlugin is substituted by UAMQSimModule
- A new fleet is provided every new iteration by using UAMFleetData

### v1.1
Input/output:
- UAM-enabled MATSim networks are required to provide an flight attribute indicating every flight links' flight segment (e.g. vertical or horizontal). **No backwards compatibility of input network files.**
- Included script for generating networkChangeEvents (files) from simulation results

Logging:
- Limited waiting time and failed UAM routing warnings to ten occurrences

### v1
- Publication of first open source MATSim-UAM version

## Publications
The following list provides a reverse-chronological overview of publications related to or based on or related to the UAM-Extension:
* M. Balac, R. Rothfeld, S. Hörl, "The Prospects of Urban Air Mobility in Zurich, Switzerland", IEEE-ITSC 2019, Auckland, New Zealand, 2019.
* M. Balac, A. R. Vetrella, R. Rothfeld and B. Schmid, "Demand Estimation for Aerial Vehicles in Urban Settings," in IEEE Intelligent Transportation Systems Magazine, vol. 11, no. 3, pp. 105-116, 2019.
* A. Straubinger, R. Rothfeld, M. Shamiyeh, K.-D. Buechter, J. Kaiser, K.O. Ploetner, "An Overview of Current Research and Developments in Urban Air Mobility - Setting the Scene for UAM Introduction", ATRS 2019, Amsterdam, NL, 2019.
* M. Balac, R. Rothfeld and S. Hörl, "Simulation of Urban Air Mobility", MATSim User Meeting 2019, Leuven, Belgium, 2019.
* Fu, M., Rothfeld, R., & Antoniou, C, "Exploring Preferences for TransportationModes in an Urban Air Mobility Environment: Munich Case Study", Transportation Research Record, Washington D.C., USA, 2019.
* M. Shamiyeh, R. Rothfeld, M. Hornung, "A Performance Benchmark of Recent Personal Air Vehicle Concepts for Urban Air Mobility", 31st Congress of the International Council of the Aeronautical Sciences, Belo Horizonte, Brasil, 2018.
* R. Rothfeld, M. Balac, C. Antoniou, "Modelling and evaluating urban air mobility – an early research approach", mobil.TUM 2018, München, 2018.
* R. Rothfeld, M. Balac, K. O. Ploetner, C. Antoniou, "Agent-based Simulation of Urban Air Mobility", MATSim User Meeting 2018, Atlanta, Georgia, US, 2018.
* Rothfeld, R., Balac, M., Ploetner, K. O., & Antoniou, C. (2018). Agent-based Simulation of Urban Air Mobility. In 2018 Modeling and Simulation Technologies Conference (p. 3891).
* Rothfeld, R., Balac, M., Ploetner, K. O., & Antoniou, C. (2018). Initial Analysis of Urban Air Mobility’s Transport Performance in Sioux Falls. In 2018 Aviation Technology, Integration, and Operations Conference (p. 2886).
* A. Straubinger, R. Rothfeld, "Identification of Relevant Aspects for Personal Air Transport System Integration in Urban Mobility Modelling", 7th Transport Research Arena (TRA), Vienna, Austria, 2018.
* M. Balac, A. Vetrella, & K.W. Axhausen (2018). "Towards the integration of aerial transportation in urban settings", 97th Transportation Research Board Conference, Washington D.C.,USA, 2018.
