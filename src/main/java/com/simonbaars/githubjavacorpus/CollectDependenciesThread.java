package com.simonbaars.githubjavacorpus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;

import com.simonbaars.githubjavacorpus.utils.DoesFileOperations;
import com.simonbaars.githubjavacorpus.utils.SavePaths;

public class CollectDependenciesThread extends Thread implements DoesFileOperations {

	private static final int MAVEN_PROCESS_TIMEOUT = 150000;
	private String project;

	public CollectDependenciesThread(String project) {
		this.project = project;
	}

	public void run() {
		try {
			File location = new File(SavePaths.createDirectoryIfNotExists(SavePaths.getGitFolder()+project.substring(project.lastIndexOf('/')+1)));
			Git git = Git.cloneRepository()
					.setURI("https://github.com"+project+".git")
					.setDirectory(location)
					.call();
			StringBuffer buff = gatherMavenDependencies(location);
			git.getRepository().close();
			if(buff.toString().contains("[INFO] BUILD FAILURE")) {
				deleteRepo(location);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private StringBuffer gatherMavenDependencies(File workingDirectory) throws IOException {
		new File(workingDirectory.getAbsolutePath()+File.separator+"lib").mkdirs();
		String[] mvnInstall = new String[] {"mvn", "dependency:copy-dependencies", "-DoutputDirectory=lib"};
		Process proc = new ProcessBuilder(mvnInstall).directory(workingDirectory).start();
		return readProcessBuffer(proc);
	}

	private StringBuffer readProcessBuffer(Process proc) throws IOException {
		StringBuffer buff = new StringBuffer();
		long duration = System.currentTimeMillis();
		while(proc.isAlive() || proc.getInputStream().available()!=0) {
			if(proc.getInputStream().available()!=0) {
				byte[] readBytes = new byte[proc.getInputStream().available()];
				proc.getInputStream().read(readBytes);
				for(byte b : readBytes) buff.append((char)b);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			if(duration+MAVEN_PROCESS_TIMEOUT<System.currentTimeMillis()) {
				proc.destroy();
				buff.append("[INFO] BUILD FAILURE");
				break;
			}
		}
		return buff;
	}

	private void deleteRepo(File location) throws IOException {
		Files.walk(location.toPath())
		.map(Path::toFile)
		.sorted((o1, o2) -> -o1.compareTo(o2))
		.forEach(File::delete);
	}

}
