package moe.nea.frobjson.generator;

import com.palantir.javapoet.JavaFile;

import java.util.Comparator;
import java.util.List;

public interface Generatable {
	default int priority() {
		return 0;
	}

	Comparator<Generatable> COMPARATOR = Comparator.comparingInt(Generatable::priority).reversed();

	List<? extends JavaFile> emitFiles();
}
