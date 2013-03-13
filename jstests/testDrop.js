Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';");
jql.execute("use db;")

mq = "select customer, product, alsoOwns, payment as empty from xxx;" +
     "select customer, product, alsoOwns, payment from intermediate where paytype='visa' and payment>100;";

result1 = jql.execute(mq);
print("Result1 = " + result1);

jql.execute("drop table xxx, yyy;");

result1 = jql.execute(mq);