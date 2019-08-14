package com.simonbaars.githubjavacorpus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.utils.SourceRoot.Callback.Result;
import com.simonbaars.githubjavacorpus.utils.SavePaths;

public class CountSize {
	public static void main(String[] args) throws IOException {
        final ParserConfiguration config = new ParserConfiguration()
    			.setLexicalPreservationEnabled(false)
    			.setStoreTokens(true);
        File[] corpusFiles = new File(SavePaths.getApplicationDataFolder()+"git").listFiles();
        AtomicInteger ordinal = new AtomicInteger(0);
        for(File f : corpusFiles) {
        	Path p = Paths.get(f+"/src/main/java/");
        	int start = ordinal.get();
        	AtomicInteger numberOfClasses = new AtomicInteger(0);
        	if(p.toFile().isDirectory()) {
				new SourceRoot(Paths.get(f+"/src/main/java/")).parse("", config, (Path localPath, Path absolutePath, ParseResult<CompilationUnit> result) -> {
					if(result.getResult().isPresent() && result.getResult().get().getRange().isPresent()) {
						int size = result.getResult().get().getRange().get().end.line+1;
						ordinal.addAndGet(size);
						numberOfClasses.getAndIncrement();
					}
					return Result.DONT_SAVE;
				});
        	}
        	System.out.println(f.getName()+"\t"+(ordinal.get()-start)+"\t"+numberOfClasses.get());
        }
        System.out.println("Combined size = "+ordinal.get());
	}
}
