Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';");
jql.execute("use db;");

order = jql.execute("update xxx set customer=1+1 where acctnum=483284;"); 
rets = jql.prettyPrint(order);
print("Asc: " + rets + "\n");