Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';");


sum = jql.execute("select sum(amount) from xxx;");
print("DataOnly: " + sum + "\n");