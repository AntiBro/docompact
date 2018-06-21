package pk.compact.docompact;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * 工具文件类，用于Maven打包文件生成tar文件
 * 
 * @author lihuai
 * 291015924@qq.com
 *
 */
public class FileTools {
	
	public static String cacheFileName="cachedMD5.temp";

	/**
	 * 计算文件的MD5值
	 * @param file
	 * @return
	 */
	public static String getFileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[8192];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer)) != -1) {
				digest.update(buffer, 0, len);
			}
			BigInteger bigInt = new BigInteger(1, digest.digest());
			return bigInt.toString(16);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 计算文件的sha1值
	 * @param file
	 * @return
	 */
	public static String getFileSha1(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[8192];
		int len;
		try {
			digest = MessageDigest.getInstance("SHA-1");
			in = new FileInputStream(file);
			while ((len = in.read(buffer)) != -1) {
				digest.update(buffer, 0, len);
			}
			BigInteger bigInt = new BigInteger(1, digest.digest());
			return bigInt.toString(16);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 写对象
	 * @param o
	 * @param file
	 */
	public static void write(Object o, String file) {
		try {
			ObjectOutputStream w = new ObjectOutputStream(new FileOutputStream(file));
			w.writeObject(o);
			w.flush();
			w.close();
		} catch (Exception e) {
			System.err.println(file+"write"+e);
		}
	}

	/**
	 * 读对象
	 * @param file
	 * @return
	 */
	public static Object Reader(String file) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			Object o = in.readObject();
			in.close();
			return o;
		} catch (Exception e) {
			System.err.println(file+"Reader"+e);
		}
		return null;
	}
	
	/**
	 * 获取CachedFileInf的文件
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static  HashMap<String, String> getCachedFileInfo(String path){
		return (HashMap<String, String>) Reader(path);
	}
	/**
	 * 写cache到文件
	 * @param cache
	 */
	@SuppressWarnings("rawtypes")
	public static void setCachedFileInfo(Map cache,String path){	
		write(cache,path);
	}

/**
 * 文件先打包成tar格式压缩包
 * @param sources
 * @param target
 * @param basepath
 * @return
 */
	public static File pack(File[] sources, File target,String basepath) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(target);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		TarArchiveOutputStream os = new TarArchiveOutputStream(out);
		for (File file : sources) {
			try {
				os.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU); 
				String orpath=file.getAbsolutePath();
				orpath=target.getName().substring(0,target.getName().lastIndexOf("."))+File.separator+orpath.replace(basepath+File.separator, "");			
				os.putArchiveEntry(new TarArchiveEntry(file,orpath));
				IOUtils.copy(new FileInputStream(file), os);
				os.closeArchiveEntry();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (os != null) {
			try {
				os.flush();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return target;
	}

/**
 * 压缩文件成gz格式
 * @param source
 * @return
 */
	public static File compress(File source) {
		File target = new File(source.getAbsolutePath() + ".gz");
		FileInputStream in = null;
		GZIPOutputStream out = null;
		try {
			in = new FileInputStream(source);
			out = new GZIPOutputStream(new FileOutputStream(target));
			byte[] array = new byte[1024];
			int number = -1;
			while ((number = in.read(array, 0, array.length)) != -1) {
				out.write(array, 0, number);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}

			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		source.delete();
		return target;
	}
	
	/**
	 * 压缩成tar.gz 文件格式
	 * @param sources
	 * @param target
	 * @return
	 */
	public static void getTarFile(File[] sources, File target,String basepath){
		 compress(pack(sources, target,basepath));
	}
	
}
