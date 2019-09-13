// Common chart properties
var options = { 
  padding: { top: 0, right: 0, bottom: -10, left: 0 },
  axis: { x: { show: false } },
  legend: { show: false },
  tooltip: { show: false }
};

// Generate first chart
var first = c3.generate($.extend({
  bindto: '#first',
  data: { json: { data: [9, 10, 9, 11, 12] }, type: 'bar' },
  bar: { width: { ratio: 0.95 } },
  color: { pattern: ['rgba(177, 212, 231, 0.4)'] }
}, options));

// Generate second chart
var second = c3.generate($.extend({
	bindto: '#second',
	data: { json: { data: [9, 10, 9, 11, 12] }, type: 'area' },
  point: { show: false },
  color: { pattern: ['hsl(200, 40%, 83%)'] }
}, options));

// Generate third chart
var third = c3.generate($.extend({
	bindto: '#third',
		data: { json: {	data1: [25], data2: [20], data3: [75] }, type : 'pie' },
	pie: { label: { show: false }, expand: false },
  color: { pattern: ['rgba(177, 212, 231, 0.4)'] }
}, options));