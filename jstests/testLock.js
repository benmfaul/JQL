Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';");
jql.execute("use db;")

jql.execute("lock table xxx in exclusive mode;");

jql.execute("unlock table xxx;");
