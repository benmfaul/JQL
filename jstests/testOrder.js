Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';");
jql.execute("use db;")

order = jql.execute("select * from xxx order by customer desc, amount desc;");
rets = jql.prettyPrint(order);
print("Asc: " + rets + "\n");
