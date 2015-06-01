# spBee - Model Generator

This module is very basic and not polished at all. It just demonstrates how you could generate the spBee model classes.
Basically you just let the code execute the stored procedure with the right parameters. Then the result set will be
analysed and the spBee model classes will be written into the std-out. But the getters aren't generated yet.
With some work this module could be improved so that it generates Java files which can be just dropped into your 
project. Then everything is generated for you and you don't have to write any database code :-)