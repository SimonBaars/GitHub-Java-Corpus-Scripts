package com.simonbaars.githubjavacorpus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.jgit.api.Git;

import com.simonbaars.githubjavacorpus.utils.DoesFileOperations;
import com.simonbaars.githubjavacorpus.utils.SavePaths;

import me.tongfei.progressbar.ProgressBar;

public class CollectDependenciesSources implements DoesFileOperations {

	private static final int MAVEN_PROCESS_TIMEOUT = 60000;

	public static void main(String[] args) throws IOException {
		new CollectDependenciesSources().collectDependencySources();
	}
	
	private void collectDependencySources() throws IOException{
		String file = getFileAsString(SavePaths.getApplicationDataFolder()+"filtered_projects.txt");
		FileOutputStream fos = new FileOutputStream(new File(SavePaths.getApplicationDataFolder()+"valid_projects_sources.txt"));
		for(String s : ProgressBar.wrap(Arrays.asList(file.split("\n")), "Cloning Git Projects")) {
			try {
				File location = new File(SavePaths.createDirectoryIfNotExists(SavePaths.getGitSourcesFolder()+s.substring(s.lastIndexOf('/')+1)));
				Git git = Git.cloneRepository()
						  .setURI("https://github.com"+s+".git")
						  .setDirectory(location)
						  .call();
				StringBuffer buff = gatherMavenDependencies(location);
				git.getRepository().close();
				if(buff.toString().contains("[INFO] BUILD FAILURE")) {
					deleteRepo(location);
				} else {
					fos.write(s.getBytes());
					fos.write(System.lineSeparator().getBytes());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		fos.close();
	}

	private StringBuffer gatherMavenDependencies(File workingDirectory) throws IOException {
		new File(workingDirectory.getAbsolutePath()+File.separator+"lib").mkdirs();
		String[] mvnInstall = new String[] {"/usr/local/bin/mvn", "dependency:copy-dependencies", "-Dclassifier=sources", "-DoutputDirectory=lib"};
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
				e.printStackTrace();
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
