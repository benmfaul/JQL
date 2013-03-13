Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sales.json';");
jql.execute("use db;");

data = jql.execute("select * from sales order by orderdate;");
print(jql.toCSV(data));

jql.addTable("csv");
data = "N::Date,JSON::Apples,N::Oranges,N::Tomatoes\n" +
	"0,10,20,30\n" +
	"1,100,200,300\n" +
	"2,1000,2000,3000\n" +
	"3,\"{\"red\":10, \"green\":20}\", 20, 30";
	
jql.fromCSV("csv",data);
csv = jql.getTable("csv");

print("\nFrom CSV:\n");
print(jql.format(csv));

data = jql.toCSV(csv);
print("\nTo CSV:");
print(data);
		