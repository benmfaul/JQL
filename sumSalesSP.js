table = functionArgs[1];
x = functionArgs[2];

print("table = " + table + '\n');
print("x = " + x + '\n');
amt = 0;
for (i=0;i<table.size();i++) {
	record = table.get(i);
	amount = record.get("amount");
	amt += parseFloat(amount);
}
return amt;