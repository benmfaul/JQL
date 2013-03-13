/**
  * Future, not supported yet
  */


Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';");
jql.execute("use db;")

embed = new java.util.Date();

types = Array();
types[0] = "Date";
types[1] = new java.util.Date();
types[2] = "Count";
types[3] = new java.lang.Integer(0);

jql.setDataTypes("table",types);
print(jql.toJson(embed));