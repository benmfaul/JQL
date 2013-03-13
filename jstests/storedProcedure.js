Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sales.json';use db;");

var roll = -1;

// Check the summary table, if it doesn't exist, make one.
if (!jql.tableExists("rollup")) {
	jql.addTable("rollup");
	jql.execute("insert into rollup (lastdate) values(0);");
	roll = -1;
}

// Get all the products
prods = jql.execute("select distinct product from sales;");

// Get all the dates
dates = jql.execute("select distinct orderdate from sales where orderdate > " + roll + ";");

print("\n");
print(jql.sformat("%1$-10s|%2$-10s|%3$-10s|%4$-10s",
			"Date","Product","Tot-Amt","Tot-Sales"));
for (i=0;i<dates.size();i++) {
	date = dates.get(i).get("orderdate");
	for (j=0;j<prods.size();j++) {
		prod = prods.get(j).get("product");
		stmt = "select orderdate, product, sum('count') as scount, sum(amount) as samount from sales where product = '" 
			+ prod + "' and orderdate = " + date + ";";
		//print("STMT = " + stmt);
		sumrow = jql.execute(stmt);
		if (sumrow.size()==0) {
			jql.addRecord(sumrow,"orderdate",date,"product",prod,"scount",0,"samount",0);
		}
		sumrow = sumrow.get(0);
		print(jql.sformat("%1$-10.0f|%2$-10s|%3$-10.0f|%4$-10.0f",
				sumrow.get("orderdate"),
				sumrow.get("product"),
				sumrow.get("scount"),
				sumrow.get("samount")));
	}
}

all = jql.execute("insert into xxx (id,data) values(666,999);");
jql.openStoredProcedures();

sales = jql.execute("select * from sales;");
fr = new java.io.FileReader("sumSalesSP.js");
jql.sp.store("SUM_SALES",fr);

nr = new java.io.FileReader("medianSales.js");

jql.sp.store("MEDIAN_SALES",nr);

rc = jql.sp.exec("SUM_SALES",sales,"amount");
print("Total sales = " + rc);

rc = jql.sp.exec("MEDIAN_SALES",sales,"amount");
print("Median sales = " + rc);
