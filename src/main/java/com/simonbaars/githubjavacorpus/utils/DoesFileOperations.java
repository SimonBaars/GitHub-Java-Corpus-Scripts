package com.simonbaars.githubjavacorpus.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public interface DoesFileOperations {

	public default String getFileAsString(File file) throws IOException {
		return new String(getFileBytes(file), StandardCharsets.UTF_8);
	}
	
	public default String getFileAsString(String file) throws IOException {
		return getFileAsString(new File(file));
	}

	public default byte[] getFileBytes(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
	}
	
	public default void writeStringToFile(File file, String content) throws IOException {
		if (file.exists())
			Files.delete(file.toPath());
		else if (file.getParentFile() != null)
			file.getParentFile().mkdirs();
		if (file.createNewFile())
			Files.write(Paths.get(file.getAbsolutePath()), content.getBytes(StandardCharsets.UTF_8));
	}
	
	public default void copyFolder(Path src, Path dest) {
		try {
			Files.walk(src).forEach(source -> copy(source, dest.resolve(src.relativize(source))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public default void copy(Path source, Path dest) {
	    try {
	        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
	    } catch (IOException e) {
	        throw new RuntimeException(e.getMessage(), e);
	    }
	}

}