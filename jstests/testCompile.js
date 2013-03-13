Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';");
jql.execute("use db;")

stmts = jql.compile("select * from xxx order by customer desc, amount desc;");

time1 = java.lang.System.currentTimeMillis();
for (i=0;i<10000;i++) {
	order = jql.execute("select * from xxx order by customer desc, amount desc;");
	//rets = jql.prettyPrint(order);
	//print("Asc: " + rets + "\n");
}
time2 = java.lang.System.currentTimeMillis();
print("Done, Non-Compiled: " + (time2-time1));


time1 = java.lang.System.currentTimeMillis();
for (i=0;i<10000;i++) {
	order = jql.execute(stmts);
	//rets = jql.prettyPrint(order);
	//print("Asc: " + rets + "\n");
}
time2 = java.lang.System.currentTimeMillis();
print("Done, Compiled: " + (time2-time1));