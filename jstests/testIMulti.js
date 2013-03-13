Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql;
jql.execute("load db 'sampleDb.json';");
jql.execute("use db;")
mq = "select customer, product, alsoOwns, payment as empty from xxx;" +
     "select customer, product, alsoOwns, payment from intermediate where paytype='visa' and payment>100;";

result1 = jql.execute(mq);
print("Result1 = " + result1);

mq = "select customer, alsoOwns from xxx  where alsoOwns_0_ = 'm60';";
result2 = jql.execute(mq);
print("Result2 = " + result2); 

mq = "select customer, id, alsoOwns from xxx  where  alsoOwns CONTAINS ('m60','drill');";
result3 = jql.execute(mq);
print("String contains = " + jql.prettyPrint(result3));

mq = "select customer, id, alsoOwns from xxx  where  listOfNumbers CONTAINS (3);";
result3 = jql.execute(mq);
print("Number contains = " + jql.prettyPrint(result3));


mq = "select customer, id, alsoOwns from xxx  where  listOfNumbers CONTAINS ('string');";
result3 = jql.execute(mq);
print("Mixed array contains = " + jql.prettyPrint(result3));

