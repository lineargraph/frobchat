import moe.nea.frobchat.matrixapi.operations.GetWellknown;
import moe.nea.frobjson.openapi.Operation;
import moe.nea.frobjson.openapi.client.JavaHttpClient;

import java.net.http.HttpClient;

void main() {
	var client = new JavaHttpClient(
		"https://nea.moe",
		HttpClient.newBuilder().build()
	);
	var result = client.executeOperation(GetWellknown.INSTANCE, new GetWellknown.Parameters(), Operation.EMPTY_BODY)
		.join();
	var body = ((GetWellknown.Status200) result.body()).body();
	System.out.println(body.mHomeserver());
}
