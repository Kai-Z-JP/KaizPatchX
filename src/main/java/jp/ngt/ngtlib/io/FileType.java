package jp.ngt.ngtlib.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileType {
	private static final List<FileType> TYPES = new ArrayList<>();
	public static final FileType OBJ = new FileType("obj", "Wavefront Obj");
	public static final FileType MQO = new FileType("mqo", "Metasequoia");
	public static final FileType MQOZ = new FileType("mqoz", "Metasequoia Zip");
	public static final FileType NGTO = new FileType("ngto", "NGTObject");
	public static final FileType CLASS = new FileType("class", "Java Class");
	public static final FileType ZIP = new FileType("zip", "Zip");
	public static final FileType JAR = new FileType("jar", "Java Archive");
	public static final FileType NGTZ = new FileType("ngtz", "NGTObject Zip");
	public static final FileType NPM = new FileType("npm", "NGT Polygon Model");
	public static final FileType JSON = new FileType("json", "JavaScript Object Notation");
	public static final FileType PNG = new FileType("png", "Portable Network Graphics");
	public static final FileType CSV = new FileType("csv", "Comma-Separated Values");

	private final String extension;
	private final String description;

	public FileType(String par1, String par2) {
		this.extension = par1;
		this.description = par2;
		TYPES.add(this);
	}

	public boolean match(File file) {
		return this.match(file.getName());
	}

	public boolean match(String fileName) {
		return fileName.endsWith(this.extension);
	}

	/**
	 * "."含まない
	 */
	public String getExtension() {
		return this.extension;
	}

	public String getDescription() {
		return this.description;
	}

	public static FileType getType(String fileName) {
		for (FileType type : TYPES) {
			if (type.match(fileName)) {
				return type;
			}
		}
		return null;
	}
}