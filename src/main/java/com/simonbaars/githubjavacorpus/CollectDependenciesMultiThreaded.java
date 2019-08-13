	package com.simonbaars.githubjavacorpus;

import java.io.IOException;
import java.util.Arrays;

import com.simonbaars.githubjavacorpus.utils.DoesFileOperations;
import com.simonbaars.githubjavacorpus.utils.SavePaths;

public class CollectDependenciesMultiThreaded implements DoesFileOperations {

	private static final int THREADS = Runtime.getRuntime().availableProcessors();

	public static void main(String[] args) throws IOException {
		new CollectDependenciesMultiThreaded().collectDependencies();
	}
	
	private void collectDependencies() throws IOException {
		String file = getFileAsString(SavePaths.getApplicationDataFolder()+"filtered_projects.txt");
		String[] githubProjects = file.split("\n");
		Thread[] threads = new Thread[THREADS];
		for(int i = 0; i<githubProjects.length; i++) {
			for(int j = 0; j<threads.length; j++) {
				if(threads[j] == null || !threads[j].isAlive()) {
			Thread t = new CollectDependenciesThread(githubProjects[i]);
			t.start();
			threads[j] = t;
			break;
			}}
			System.out.println(githubProjects[i]+" ("+(i+1)+"/"+githubProjects.length+")");
			while(Arrays.stream(threads).allMatch(e -> e!=null && e.isAlive())) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
			}
		}
		while(Arrays.stream(threads).anyMatch(e -> e!=null && e.isAlive())) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
