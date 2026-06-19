package moe.nea.frobjson.internal;

public class OperationUtil {
	public static RuntimeException errorForResponse(Object response) {
		throw new RuntimeException("Non success response: " + response);
	}
}
