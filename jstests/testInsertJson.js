Jql = Packages.org.faul.jql.core.Jql;
jql = new Jql();
jql.execute("load db 'sampleDb.json';");
jql.execute("use db;")

embed = new Object;
embed.x = 100;
embed.y = 200;
embed.z = 300;
embed.name = "Embedded Object";
embed.array = [100,200,300];

obj = new Object;
obj.x = 1;
obj.y = 2;
obj.name = "Test object";
obj.array = [1,2,3,embed];

str = Jql.toJson(obj);
print("String version = " + str);

newobj = Jql.fromJson(str);
print("Object.name = " + newobj.get("name"));

db = jql.execute("select * from xxx;"); 
db = jql.execute("commit;");

//db = jql.execute("select * from xxx; commit;");  // do commit; to make selection the table
/*jql.addRecord(db,newobj);
// jql.addTableMap("xxx",db);       // you can also commit this way
print("----------------");
print(jql.prettyPrint(db));

print("---------------- Test Query -------------");
rec = jql.execute("select * from xxx where name='Test object';");
print(jql.prettyPrint(rec));

rec = jql.execute("select * from xxx;");
print("Checking-----------------------");
print(jql.prettyPrint(rec));


jql.updateRecord("xxx where id=0","zulu",1000);
//db = jql.execute("select * from xxx;");
print("After update------------------");
print(jql.prettyPrint(jql.getDataBase("xxx")));


jql.addSymbol("symbol",obj);
jql.execute("update xxx set zulu=toJson(symbol) where id=0;");
print("Finally------------------");
print(jql.prettyPrint(jql.getDataBase("xxx")));*/






