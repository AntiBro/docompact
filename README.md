# docompact
```
Maven打包插件,可以用于对web项目进行增量打包，极大的缩小打包的体积
项目优点
可以对jar包增量打包
可以对源代码增量打包
打包成gz格式的压缩包,便于linux的解压操作和Jenkins的集成
体积小，源代码量小，便于二次开发
```
使用：
```
<plugin>
<groupId>pk.compact</groupId>
<artifactId>docompact</artifactId>
<version>0.0.1-SNAPSHOT</version>
<configuration>
<compactJarType>1</compactJarType>
<useCached>0</useCached>
<includesFileTypes>
<include>sql</include>
<include>class</include>
<include>xml</include>
<include>properties</include>
<include>jsp</include>
<include>html</include>
<include>txt</include>
</includesFileTypes>
<Includefiles> 
 <include>dubbo-2.5.3.jar</include> 
</Includefiles> 
<tarFileName>
docompact
</tarFileName>
</configuration>
<executions>
<execution>
<goals>
	<goal>compact</goal>
</goals>
</execution>
</executions>
</plugin>
```

与jenkins结合使用:
```
cd /opt/tomcat/webapps
tar zxvf docompact.tar.gz
chmod 777 docompact/delShell.sh
cd docompact
./delShell.sh
```













