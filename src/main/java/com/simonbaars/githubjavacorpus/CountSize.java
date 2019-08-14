package com.simonbaars.githubjavacorpus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.utils.SourceRoot.Callback.Result;
import com.simonbaars.githubjavacorpus.utils.SavePaths;

public class CountSize {
	public static void main(String[] args) throws IOException {
        final ParserConfiguration config = new ParserConfiguration()
    			.setLexicalPreservationEnabled(false)
    			.setStoreTokens(false);
        int total = 0;
        File[] corpusFiles = new File(SavePaths.getApplicationDataFolder()+"git").listFiles();
        
		new SourceRoot(Paths.get(corpusFiles[i]+"src/main/java/")).parse("", config, (Path localPath, Path absolutePath, ParseResult<CompilationUnit> result) -> {
			if(result.getResult().isPresent()) {
				total+=result.getResult().get().getRange().get().end.line+1;
			}
			return Result.DONT_SAVE;
		});
	}
}
