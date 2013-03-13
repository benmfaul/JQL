var table = functionArgs[1];
var column = functionArgs[2];

var amt = 0;
for (i=0;i<table.size();i++) {
	record = table.get(i);
	var val = parseDouble(record.get(column));
	amt += val;
	print("VAL = " + amt);
}
return val;
