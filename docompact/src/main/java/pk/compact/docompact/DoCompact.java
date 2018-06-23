package pk.compact.docompact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

/**
 * 
 * @author lihuai Maven编译压缩工具，可以灵活的配置需要打包的文件，加速部署的效率 291015924@qq.com
 *
 */
@Mojo(name = "compact", defaultPhase = LifecyclePhase.PACKAGE)
public class DoCompact extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project.build.finalName}")
	private String finalName;

	@Parameter(defaultValue = "delShell")
	private String delShellFileName;

	/**
	 * 默认需要打包的文件类型
	 */
	@Parameter(defaultValue = "class,xml,properties,jsp,html,txt,png,jpg,gif,css")
	private String[] includesFileTypes;

	/**
	 * cache文件名
	 */
	@Parameter(defaultValue = "compactCache")
	private String CacheFileName;

	@Parameter(defaultValue = "")
	private String[] Includefiles;

	private HashSet<String> filetypes;

	private HashMap<String, String> cachefileMD5;

	@Parameter(defaultValue = "${project.name}")
	private String tarFileName;

	@Parameter(defaultValue = "${project.artifactId}")
	private String targetProjectNameArtifactId;

	@Parameter(defaultValue = "${project.version}")
	private String targetProjectNameversion;

	@Parameter(defaultValue = "0")
	private Integer compactJarType;

	/**
	 * 是否开启源代码的增量打包
	 */
	@Parameter(defaultValue = "0")
	private Integer useCached;

	private HashMap<String, String> mapMD5;

	private HashMap<String, String> tempmapMD5 = new HashMap<String, String>();

	private Set<String> pathkey;

	private String basePath;

	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException {
		this.getLog().info("-----docompact Start-----");
		long startTime = System.currentTimeMillis();
		filetypes = new HashSet<String>(Arrays.asList(includesFileTypes));
		if (compactJarType.equals(0))
			filetypes.add("jar");
		for (String e : filetypes) {
			this.getLog().info("-----传入filetypes参数:[" + e + "]");
		}

		this.getLog().info("compactJarType:" + this.compactJarType.toString());

		Set<String> collections = new HashSet<String>();

		String maventargetpath = outputDirectory.getAbsoluteFile() + File.separator + targetProjectNameArtifactId + "-"
				+ targetProjectNameversion;
		if (!StringUtils.isEmpty(finalName))
			maventargetpath = outputDirectory.getAbsoluteFile() + File.separator + finalName;
		this.getLog().info("target path" + maventargetpath);
		File maventargetfile = new File(maventargetpath);
		basePath = maventargetfile.getAbsolutePath();
		String cachepath = FileTools.cacheFileName;

		if (useCached.equals(1)) {
			this.getLog().info("读取缓存文件");
			cachefileMD5 = FileTools.getCachedFileInfo(cachepath);
			
		}

		if (cachefileMD5 == null) {
			this.getLog().info("不使用缓存文件");
			mapMD5 = new HashMap<String, String>();
			cachefileMD5 = new HashMap<String, String>();
		} else {
			mapMD5 = cachefileMD5;
		}
		pathkey = mapMD5.keySet();

		getoutclasses(maventargetfile, collections);

		File delShell = getDelShell();

		generateTarFile(collections, maventargetfile, delShell);

		cachefileMD5 = (HashMap<String, String>) tempmapMD5.clone();

		FileTools.setCachedFileInfo(cachefileMD5, cachepath);

		for (String e : collections) {
			this.getLog().debug("-----被打包的文件:[" + e + "]");
		}
		if (delShell.isFile()) {
			this.getLog().debug("-----本地delshell 文件路径:[" + delShell.getAbsolutePath() + "]");
			this.getLog().debug(delShell.delete() ? "删除delshell文件成功" : "删除失败");

		}

		long endTime = System.currentTimeMillis();
		this.getLog().info("-----docompact End-----");
		this.getLog().info("-----docompact 打包插件耗时:[" + (endTime - startTime) + "ms" + "]");

	}

	/**
	 * 遍历编译后的文件
	 * 
	 * @param entry
	 * @param collections
	 */
	public void getoutclasses(File entry, Set<String> collections) {

		File[] fs = entry.listFiles();

		int lastindex = 0;
		for (int i = 0; i < fs.length; i++) {
			if (fs[i].isFile()) {
				lastindex = fs[i].getAbsolutePath().lastIndexOf(".");
				String path = fs[i].getAbsolutePath().replace(basePath, "");
				String MD5 = FileTools.getFileMD5(fs[i]);
				this.getLog().info(path + ":" + MD5);
				tempmapMD5.put(path, FileTools.getFileMD5(fs[i]));

				if (filetypes.contains(fs[i].getAbsolutePath().substring(lastindex + 1))) {

					if (!pathkey.contains(path)) {
						this.getLog().info("加入新增文件:" + path);
						collections.add(fs[i].getAbsolutePath());
					} else {
						this.getLog().debug(path + ":" + mapMD5.get(path));
						if (!MD5.equals(mapMD5.get(path))) {
							collections.add(fs[i].getAbsolutePath());
							this.getLog().info("加入修改文件:" + path);
						}
					}

				}

				if (Includefiles == null || Includefiles.length == 0) {
					continue;
				}

				for (String jar : Includefiles) {
					if (jar.equals(fs[i].getName()))
						collections.add(fs[i].getAbsolutePath());
				}
			} else {
				getoutclasses(fs[i], collections);
			}
		}

	}

	/**
	 * 产生delshell的脚本文件
	 * 
	 * @return
	 */
	public File getDelShell() {
		File delfile = new File(delShellFileName + ".sh");
		for (String e : tempmapMD5.keySet()) {
			this.getLog().debug("新遍历文件：[" + e + "]");
		}
		for (String e : pathkey) {
			this.getLog().debug("缓存文件：[" + e + "]");
		}
		try {
			if (!delfile.exists())
				delfile.createNewFile();
			FileOutputStream out = new FileOutputStream(delfile, true); // 如果追加方式用true
			StringBuffer sb = new StringBuffer();
			for (String e : pathkey) {
				if (!tempmapMD5.containsKey(e)) {	
					e=e.substring(1);
					e=e.replaceAll("\\\\", "/");
					this.getLog().info("待删除文件：[" + e + "]");
					sb.append("rm -f " + e + "\n");
				}
			}
			out.write(sb.toString().getBytes("utf-8"));// 注意需要转换对应的字符集
			out.close();
		} catch (IOException ex) {
			this.getLog().error(ex);
		}
		return delfile;

	}

	/**
	 * 压缩成tar.gz 格式
	 * 
	 * @param collections
	 * @param base
	 */
	public void generateTarFile(Set<String> collections, File base, File delshell) {
		File[] files = new File[collections.size()];
		int i = 0;
		for (String e : collections) {
			files[i++] = new File(e);
		}
		File output = new File(
				outputDirectory.getAbsolutePath() + File.separator + "tar" + File.separator + tarFileName + ".tar");
		File fileParent = output.getParentFile();
		if (!fileParent.exists()) {
			fileParent.mkdir();
		}
		if (!output.exists()) {
			try {
				output.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		FileTools.getTarFile(files, output, delshell, base.getAbsolutePath());
	}
}
