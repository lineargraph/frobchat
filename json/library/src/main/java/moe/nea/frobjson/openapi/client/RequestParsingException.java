package moe.nea.frobjson.openapi.client;

public class RequestParsingException extends ApiException {
	String body;

	public String getBody() {
		return body;
	}

	public RequestParsingException(String message, String body, Throwable cause) {
		super(message + " with message body " + body, cause);
		this.body = body;
	}
}
