package com.simonbaars.githubjavacorpus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.github.javaparser.JavaToken;
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
        AtomicInteger loc = new AtomicInteger(0);
        System.out.println("System\tLine Size\tNumber of Classes\tLOC");
        for(File f : corpusFiles) {
        	Path p = Paths.get(f+"/src/main/java/");
        	int start = ordinal.get(), startLOC = loc.get();
        	AtomicInteger numberOfClasses = new AtomicInteger(0);
        	if(p.toFile().isDirectory()) {
				new SourceRoot(Paths.get(f+"/src/main/java/")).parse("", config, (Path localPath, Path absolutePath, ParseResult<CompilationUnit> result) -> {
					if(result.getResult().isPresent() && result.getResult().get().getRange().isPresent()) {
						int size = result.getResult().get().getRange().get().end.line+1;
						ordinal.addAndGet(size);
						numberOfClasses.getAndIncrement();
						loc.addAndGet(lineSize(StreamSupport.stream(result.getResult().get().getTokenRange().get().spliterator(), true)));
					}
					return Result.DONT_SAVE;
				});
        	}
        	System.out.println(f.getName()+"\t"+(ordinal.get()-start)+"\t"+numberOfClasses.get()+"\t"+(loc.get()-startLOC));
        }
        System.out.println("Lines = "+ordinal.get()+", LOC = "+loc.get());
	}
	
	public static Set<Integer> filledLines(Stream<JavaToken> stream) {
		return stream.map(t -> t.getRange()).filter(Optional::isPresent).map(r -> r.get().begin.line).collect(Collectors.toSet());
	}
	
	public static int lineSize(Stream<JavaToken> tokens) {
		return filledLines(tokens).size();
	}
}
