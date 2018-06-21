# docompact
Maven打包插件,可以用于对web项目进行增量打包，极大的缩小打包的体积

项目优点
可以对jar包增量打包
可以对源代码增量打包
打包成gz格式的压缩包,便于linux的解压操作和Jenkins的集成
体积小，源代码量小，便于二次开发

使用：
<plugin>
				<groupId>pk.compact</groupId>
				<artifactId>docompact</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<configuration>
				   <!-- jar包的压缩类型,默认0(包含jar包)-->
					<compactJarType>1</compactJarType>
					<!--是否使用缓存文件 用于增量更新打包  默认0 不使用-->
					<useCached>0</useCached>
					<!--设置需要打包的文件类型 默认类型有 class,xml,properties,jsp,html,txt,jar(默认包含)  -->
					<includesFileTypes>
						<include>sql</include>
						<include>class</include>
						<include>xml</include>
						<include>properties</include>
						<include>jsp</include>
						<include>html</include>
						<include>txt</include>
					</includesFileTypes>
                    <!--设置需要打包的jar文件 -->
					<!-- <Includefiles> -->
						<!-- <include>dubbo-2.5.3.jar</include> -->
					<!-- </Includefiles> -->

					<tarFileName>
						docompact
					</tarFileName>
				</configuration>
				<executions>
					<execution>
						<goals>
							<goal>compact</goal>
						</goals>
						<!-- 默认在package截断使用 -->
						<!-- <phase>package</phase> -->
					</execution>
				</executions>
			</plugin>



















