package moe.nea.frobmatrix.ui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.List;

public class CleanerHelper {
	public static final Cleaner CLEANER = Cleaner.create();
	private static final Logger log = LoggerFactory.getLogger(CleanerHelper.class);
	private static List<Cleaner.Cleanable> cleanables = new ArrayList<>();
	public static void debugLogOnClean(Object object, String cleanMessage) {
		cleanables.add(CLEANER.register(object, () -> log.info("{}", cleanMessage)));
	}
}
