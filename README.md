JQL - JSON Query Language in JAVA
===

NoSQL databases are quite the rage these days (e.g. MongoDB) employing JSON based data. JSON lends itself well 
to document based databases, and the JAVA List and Map constructs can easily be used with JSON to describe 
almost any type of data your application uses. In those cases requiring large scale databases 
and your data is object oriented (or document based), the new NoSQL databases will work nicely for you. 

However, if all you need is to keep your database mostly in memory, there really isn't a way to easily query 
your data without writing a lot of custom code. This is where JSON Query Language can work for you. 
By storing your objects in a JQL database, you can use familiar SQL commands to query your data. 
There is no need to create your own query mechanisms or object store. JQL will handle all of that for you. 
The database can be stored and retrieved from disk, and uses the familiar JSON format to store your data. 

However JQL supports the standard SQL commands such as  "Select", "Insert", "Update", "Delete", and "Drop" 
can be used to accomplish almost everything that one needs  to do with a relational database. 
Because JQL databases are object oriented,  SQL was modified slightly to accommodate the document within 
document (object within object) data structure so that inner objects can 
be queried easily.

Like other NoSQL databases, JQL does not support the concept of a JOIN. Instead embed the 
data you would expect on a JOIN, even if this results in duplicate data. The cost of memory is low, and the 
removal of the necessity of the JOIN greatly increases the speed of operations.

Diections on how to build, use and extend JQL is doumented in Users Guide.odt (or Users Guide.pdf.
