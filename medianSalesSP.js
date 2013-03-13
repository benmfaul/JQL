var table = functionArgs[1];
var column = functionArgs[2];

var array = [];
for (i=0;i<table.size();i++) {
	record = table.get(i);
	var val = parseFloat(record.get(column));
	array[i] = val;
}
return median(array);

function median(values) {
    values.sort( function(a,b) {return a - b;} );
    var half = Math.floor(values.length/2);
    if(values.length % 2)
        return values[half];
    else
        return (values[half-1] + values[half]) / 2.0;
}