# Troubleshooting


#### IllegalStateException: endPosTable already set

If this error occurs than clean your target director (```mvn clean```) or define an auto-clean-task for spBee (see blow).

    [ERROR] COMPILATION ERROR :
    [INFO] -------------------------------------------------------------
    [ERROR] An unknown compilation problem occurred
    [INFO] 1 error
    [INFO] -------------------------------------------------------------
    An exception has occurred in the compiler (1.8.0). Please file a bug at the Java Developer Connection (http://java.sun.com/webapps/bugreport)  after checking the Bug Parade for duplicates. Include your program and the following diagnostic in your report.  Thank you.
    java.lang.IllegalStateException: endPosTable already set
    	at com.sun.tools.javac.util.DiagnosticSource.setEndPosTable(DiagnosticSource.java:136)
    	at com.sun.tools.javac.util.Log.setEndPosTable(Log.java:350)
    	at com.sun.tools.javac.main.JavaCompiler.parse(JavaCompiler.java:670)
    	at com.sun.tools.javac.main.JavaCompiler.parseFiles(JavaCompiler.java:953)
    	at com.sun.tools.javac.processing.JavacProcessingEnvironment$Round.<init>(JavacProcessingEnvironment.java:892)
    	at com.sun.tools.javac.processing.JavacProcessingEnvironment$Round.next(JavacProcessingEnvironment.java:921)
    	at com.sun.tools.javac.processing.JavacProcessingEnvironment.doProcessing(JavacProcessingEnvironment.java:1187)
    	at com.sun.tools.javac.main.JavaCompiler.processAnnotations(JavaCompiler.java:1173)
    	at com.sun.tools.javac.main.JavaCompiler.compile(JavaCompiler.java:859)
    	at com.sun.tools.javac.main.Main.compile(Main.java:523)
    	at com.sun.tools.javac.api.JavacTaskImpl.doCall(JavacTaskImpl.java:129)
    	at com.sun.tools.javac.api.JavacTaskImpl.call(JavacTaskImpl.java:138)
    	at org.codehaus.plexus.compiler.javac.JavaxToolsCompiler.compileInProcess(JavaxToolsCompiler.java:125)
    	at org.codehaus.plexus.compiler.javac.JavacCompiler.performCompile(JavacCompiler.java:169)
    	at org.apache.maven.plugin.compiler.AbstractCompilerMojo.execute(AbstractCompilerMojo.java:823)
    	at org.apache.maven.plugin.compiler.CompilerMojo.execute(CompilerMojo.java:129)
    	at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo(DefaultBuildPluginManager.java:132)
    	at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:208)
    	at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:153)
    	at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:145)
    	at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject(LifecycleModuleBuilder.java:116)
    	at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject(LifecycleModuleBuilder.java:80)
    	at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build(SingleThreadedBuilder.java:51)
    	at org.apache.maven.lifecycle.internal.LifecycleStarter.execute(LifecycleStarter.java:120)
    	at org.apache.maven.DefaultMaven.doExecute(DefaultMaven.java:355)
    	at org.apache.maven.DefaultMaven.execute(DefaultMaven.java:155)
    	at org.apache.maven.cli.MavenCli.execute(MavenCli.java:584)
    	at org.apache.maven.cli.MavenCli.doMain(MavenCli.java:216)
    	at org.apache.maven.cli.MavenCli.main(MavenCli.java:160)
    	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    	at java.lang.reflect.Method.invoke(Method.java:483)
    	at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced(Launcher.java:289)
    	at org.codehaus.plexus.classworlds.launcher.Launcher.launch(Launcher.java:229)
    	at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode(Launcher.java:415)
    	at org.codehaus.plexus.classworlds.launcher.Launcher.main(Launcher.java:356)
    	at org.codehaus.classworlds.Launcher.main(Launcher.java:47)
    	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    	at java.lang.reflect.Method.invoke(Method.java:483)
    	at com.intellij.rt.execution.application.AppMain.main(AppMain.java:134)

<div class="source">
<pre class="prettyprint lang-xml">
&lt;plugin&gt;
    &lt;artifactId&gt;maven-clean-plugin&lt;/artifactId&gt;
    &lt;version&gt;${maven-clean-plugin.version}&lt;/version&gt;
    &lt;configuration&gt;
        &lt;filesets&gt;
            &lt;fileset&gt;
                &lt;directory&gt;
                    ${project.build.outputDirectory}/generated-sources/annotations/
                &lt;/directory&gt;
                &lt;includes&gt;
                    &lt;include&gt;**/*&lt;/include&gt;
                &lt;/includes&gt;
            &lt;/fileset&gt;
        &lt;/filesets&gt;
    &lt;/configuration&gt;
    &lt;executions&gt;
        &lt;execution&gt;
            &lt;id&gt;auto-clean-spBee&lt;/id&gt;
            &lt;phase&gt;initialize&lt;/phase&gt;
            &lt;goals&gt;
                &lt;goal&gt;clean&lt;/goal&gt;
            &lt;/goals&gt;
        &lt;/execution&gt;
    &lt;/executions&gt;
&lt;/plugin&gt;
</pre>
</div>

#### ```@MappingConstructor``` - names don't match

If this error occurs if the name of the ```@MappingConstructor``` in the entity doesn't match the name in the @Dao or @ResultSet.

    [ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.2:compile (default-compile) on project demo: Compilation failure: Compilation failure:
    [ERROR] /spBee/demo/target/generated-sources/annotations/at/rseiler/spbee/demo/dao/storedprocedure/SpGetUserWithSpGetSimpleUsers.java:[8,43] cannot find symbol
    [ERROR] symbol:   class UserSpGetSimpleUsersMapper
    [ERROR] location: package at.rseiler.spbee.demo.entity.mapper

I recommend to use constants for the ```@MappingConstructor``` names (see ```McName``` in the demo module).

#### ```@MappingConstructor``` - constructor missing

    More than one public constructor and none of these has a @MappingConstructor annotation for 'at.rseiler.spbee.demo.entity.Permission'

To fix this annotate one constructor in the ```@Entity``` class with ```@MappingConstructor```.

#### The mapped objects have no state (everything is null)

Probably the ```@Entity``` class doesn't have any constructor. Because of that the default constructor kicks in and
therefore no data will be mapped.

#### FilerException: Attempt to recreate a file for type

This error occurs if there is a name conflict. A possible cause is that an ```@Entity``` has
two ```@MappingConstructors``` with the same name.

    [ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.2:compile (default-compile) on project demo: Compilation failure
    [ERROR] javax.annotation.processing.FilerException: Attempt to recreate a file for type at.rseiler.spbee.demo.entity.mapper.UserSpGetSimpleUsersMapper

#### Unknown array type: int[]

It's necessary to use the class kind of the array. Meaning ```Integer[]``` must be used as array parameter type.

#### incompatible types: ...  cannot be converted to ...

If this happens in a ```*DaoImpl``` by creating a new ```ResultSet``` than the fields of the ```ResultSet`` and the
parameters in the constructor doesn't match.

#### Sql Exceptions

Probably the Java code doesn't match exactly the stored procedure. Check again if somewhere is a bug in the Java classes
(e.g. wrong order or the variables).

#### Strange Errors

Try to recompile the project. Maybe this helps.
