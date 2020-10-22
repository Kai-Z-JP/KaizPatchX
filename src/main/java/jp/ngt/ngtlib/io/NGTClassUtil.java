package jp.ngt.ngtlib.io;

import jp.ngt.ngtlib.NGTCore;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class NGTClassUtil {
	private static final String PATH_HEAD = "jp/ngt/";
	private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

	public static Class<?> loadClassFromFile(File file) throws ClassNotFoundException {
		ClassLoader classLoader = NGTCore.class.getClassLoader();
		String s1 = pathStartFrom(file.getPath(), "zip");
		String filename = resourceNameToClassName(pathStartWith(s1, PATH_HEAD));
		Class<?> clazz = classLoader.loadClass(filename);
		return clazz;
	}

	public static String fileNameToClassName(String name) {
		return name.substring(0, name.length() - ".class".length());
	}

	public static String resourceNameToClassName(String resourceName) {
		return fileNameToClassName(resourceName).replace('\\', '.');
	}

	/**
	 * パスを指定された文字列で開始されるように変更
	 */
	public static String pathStartWith(String path, String start) {
		int index = path.indexOf(start);
		return index >= 0 ? path.substring(index) : path;
	}

	public static String pathStartFrom(String path, String start) {
		int index = path.indexOf(start);
		if (index > 0) {
			index += start.length();
			return path.substring(index);
		}
		return path;
	}

	public static <T> T getInstance(String name, String source) throws ReflectiveOperationException {
		Class<T> clazz = compile(name, source);
		return clazz.newInstance();
	}

	public static <T> Class<T> compile(String name, String source) throws ReflectiveOperationException {
		if (COMPILER == null) {
			NGTLog.debug("*** Compiler not found ***");
			return null;
		}

		final String source2 = source.replaceAll("\t", "");
		URI uri = URI.create("string:///" + name.replaceAll(".", "/") + Kind.SOURCE.extension);
		JavaFileObject sourceObj = new SimpleJavaFileObject(uri, Kind.SOURCE) {
			@Override
			public CharSequence getCharContent(boolean par1) throws IOException {
				return source2;
			}
		};

		List<? extends JavaFileObject> src = Arrays.asList(sourceObj);
		String classPath = System.getProperty("java.class.path");
		List<String> options = Arrays.asList("-classpath", classPath);
		NGTLog.debug("Set class path : " + classPath);
		DiagnosticCollector<JavaFileObject> listner = new DiagnosticCollector<JavaFileObject>();
		VirtualFileManager manager = new VirtualFileManager(COMPILER, listner);

		CompilationTask compilerTask = COMPILER.getTask(null, manager, listner, options, null, src);
		if (!compilerTask.call()) {
			for (Diagnostic diag : listner.getDiagnostics()) {
				NGTLog.debug("Error on line %d in %s", diag.getLineNumber(), diag);
			}
			throw new RuntimeException("Error on parse source : " + name);
		}

		//ClassLoader cl = manager.getClassLoader(null);
		//クラスローダー違うとエラー出るので、マイクラ用クラスローダーに登録
		ClassLoader cl = Launch.classLoader;
		Map<String, byte[]> map = (Map<String, byte[]>) NGTUtil.getField(LaunchClassLoader.class, cl, "resourceCache");
		map.put(name, manager.getByteData(name));

		try {
			Class<T> c = (Class<T>) cl.loadClass(name);
			return c;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}