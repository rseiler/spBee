# spBee - Demo

The thought behind this module is to demonstrate the usage of the spBee framework and to show the Maven configuration.
In addition it's used as integration test for the spBee framework. If a unit test fails then something is fishy.

There is not much to say. The complete project is kept as simple as possible.

* an in-memory HSQLDB is used
* with init-db.sql some data and some stored procedures are created
* with clear-db.sql the data is cleared
* with context.xml a very simple spring context is defined
    * the DataSource is defined
    * the component-scan for the package ```at.rseiler.spbee.demo.dao``` is defined. So that Spring can load the generated implementation classes
* ```at.rseiler.spbee.demo.Main``` demonstrates some simple usage
* ```at.rseiler.spbee.demo.AbstractUserDaoTest``` and ```at.rseiler.spbee.demo.UserDaoTest``` demonstrate all the features of spBee
* a stored procedure is configured: ```at.rseiler.spbee.demo.SpLogger```
    * logs all stored procedure calls (before and after) with the log level ```TRACE```
    * logs long running stored procedure calls with the log level ```WARN```
    * the configuration is in ```spbee.properties```
    * the logger is configured in ```log4j.properties```

To see how much code so generated for you take a look into the ```target/generated-sources``` directory after the module
is build.
