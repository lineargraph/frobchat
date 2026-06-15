import moe.nea.frobchat.matrixapi.MatrixAuthentication;
import moe.nea.frobchat.matrixapi.MatrixClient;
import moe.nea.frobchat.matrixapi.operations.GetWellknown;
import moe.nea.frobjson.openapi.Operation;

import java.net.http.HttpClient;

void main() {
	var client = new MatrixClient(
		"https://nea.moe",
		HttpClient.newBuilder().build(),
		MatrixAuthentication.anon()
	);
	var result = client.executeOperation(GetWellknown.INSTANCE, new GetWellknown.Parameters(), Operation.EMPTY_BODY)
		.join();
	var body = result.body().body();
	System.out.println(body.mHomeserver());
}
