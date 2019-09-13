// Set up crossfilter
var ndx = crossfilter([
	{key: 'C', value: '6'},	{key: 'B', value: '5'},
	{key: 'B', value: '0'},	{key: 'C', value: '3'},
	{key: 'A', value: '8'},	{key: 'B', value: '6'},
	{key: 'B', value: '9'},	{key: 'C', value: '6'},
	{key: 'B', value: '1'},	{key: 'B', value: '9'},
	{key: 'A', value: '8'},	{key: 'B', value: '2'},
	{key: 'A', value: '3'},	{key: 'B', value: '8'},
	{key: 'B', value: '0'},	{key: 'A', value: '5'},
	{key: 'A', value: '9'},	{key: 'C', value: '3'},
	{key: 'C', value: '1'},	{key: 'A', value: '5'},
	{key: 'B', value: '3'},	{key: 'A', value: '2'},
	{key: 'B', value: '3'},	{key: 'C', value: '2'},
	{key: 'B', value: '0'},	{key: 'C', value: '2'},
	{key: 'C', value: '6'},	{key: 'B', value: '7'},
	{key: 'B', value: '9'},	{key: 'C', value: '4'},
	{key: 'B', value: '5'},	{key: 'A', value: '6'},
	{key: 'A', value: '0'},	{key: 'A', value: '6'},
	{key: 'A', value: '9'},	{key: 'A', value: '7'},
	{key: 'C', value: '2'},	{key: 'B', value: '8'},
	{key: 'C', value: '4'},	{key: 'C', value: '3'},
	{key: 'B', value: '8'},	{key: 'B', value: '2'},
	{key: 'B', value: '6'},	{key: 'B', value: '1'},
	{key: 'A', value: '1'},	{key: 'A', value: '6'},
	{key: 'A', value: '6'},	{key: 'B', value: '4'},
	{key: 'B', value: '7'},	{key: 'C', value: '7'},
	{key: 'A', value: '7'},	{key: 'A', value: '7'}
]);

// Define dimensions
var key = ndx.dimension(function(d) { return d.key; }),
		value = ndx.dimension(function(d) { return d.value; });

// Define groups
var keyGroup = key.group(),
		valueGroup = value.group();

// Define charts properties
var keyChart = dc.rowChart('#row-chart'),
		valueChart = dc.barChart('#bar-chart');

// Define charts
keyChart
.dimension(key)
.group(keyGroup);

valueChart
.dimension(value)
.group(valueGroup)
.x(d3.scale.linear().domain([0, 10]));

// Optional design
valueChart.round(dc.round.floor)
var width = $('#width').width();
valueChart.width(width);
keyChart.width(width); // für CC nich nötig
keyChart.margins({top: 0, right: 35, bottom: 25, left: 5})
valueChart.margins({top: 5, right: 35, bottom: 25, left: 15})

// Update all charts
dc.renderAll();

// Setup crosscompare
crosscompare
.addLegend(keyChart)
.addLegend(valueChart)
.add(keyChart, { type: 'bar', order: 'desc' })
.add(valueChart);