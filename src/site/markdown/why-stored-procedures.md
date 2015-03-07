# Why Stored Procedures

This framework is explicitly build for stored procedures and noting else (like SELECT, UPDATE, INSERT, DELETE). So some
people will wonder why the hell?

Stored Procedures are:

* the interface to the database
    * in Java there are interfaces everywhere to separate the interface from the implementation.
      No one would rely on the underling classes. Because they can change. But the interface is stable.
      The same is true for the database. Only that in this case the stored procedures are the interface.
    * the developer doesn't care how the data are stored. If the date is stored in one table or in 11 tables. Only the result is important.
    * if the schema changes  - e.g. some data from the user table is extracted into another table - than only the
      stored procedures need to take care about the change. It's not necessary to change the entities in Java code.
* the optimum for the performance
    * there is nothing to add. Stored procedures are simply the fastest way to execute SQL.
* easy to monitor (to see how often they are called and how long the execution times were)
    * with the data it's very easy to track down performance issues. Because you exactly know what's slow.
    * there are several possibilities how to implement the monitoring
        * directly in the Java code (spBee provides a hook to that)
        * with APO
        * with a kind of interceptor [log4jdbc](https://code.google.com/p/log4jdbc/)
* DBA experts can optimize them without the help of the developers
    * because the DBA expert can just redeploy the optimized stored procedure and doesn't need a developer to change
      something in the code.
* easy to use and understand (instead of JPA/Hibernate)
    * every backend developer knows SQL and about stored procedures. It's very easy to call the stored procedures and
      there is no magic behind. With JPA/Hibernate there is a huge framework which you need to be learned.
    * with JPA/Hibernate you need an DBA expert, who explains how to optimize the SQL statements, and a JPA/Hibernate
      expert to implement that changes.
    * it's very easy to run into serious issues with JPA/Hibernate. E.g. something is wrongly configured which causes
      that a SELECT statement also executes a DROP statement and an INSERT statement.
* easy to maintain instead of hundreds or thousands of SQL statements distributed in the application.
    * if prepared SQL statements are used
    * or even in JPA/Hibernate they can be spread over several classes
* safe: no SQL injection, no broken SQL statements in the application
    * if SQL statements are used
    * it's very hard to spot if a developer writes anywhere in the Java code a broken SQL statement.
      E.g. an UPDATE or a DELETE statement without a WHERE. If only stored procedures are used than it's easy to check
      changed and the newly created stored procedures.

The downside of Stored Procedures are:

* it makes it hard to change the database. Because all stored procedures needs to be rewritten.
  Therefore the stored procedures should be very easy and small.
* it's easy to put too much logic into them
* they can grow very complex so that you need a DBA expert.

If you disagree or want to share you thoughts than please <a href="mailto:rseiler.developer@gmail.com">let me know</a>.
