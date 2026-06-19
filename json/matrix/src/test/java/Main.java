import moe.nea.frobchat.matrixapi.MatrixAuthentication;
import moe.nea.frobchat.matrixapi.MatrixClient;
import moe.nea.frobchat.matrixapi.operations.GetLoginFlows;
import moe.nea.frobchat.matrixapi.operations.Login;
import moe.nea.frobchat.matrixapi.operations.GetWellknown;
import moe.nea.frobjson.openapi.Operation;

import java.net.http.HttpClient;

void main() {
	var client = new MatrixClient(
		"https://nea.moe",
		HttpClient.newBuilder().build(),
		MatrixAuthentication.anon("https://nea.moe")
	);
	var result = client.executeOperation(GetWellknown.INSTANCE, new GetWellknown.Parameters(), Operation.EMPTY_BODY)
		.join();
	var body = result.body().body();
	System.out.println(body.mHomeserver());
	client = new MatrixClient(
		"https://matrix.nea.moe",
		HttpClient.newBuilder().build(),
		MatrixAuthentication.anon("https://matrix.nea.moe")
	);
	System.out.println(((GetLoginFlows.Status200) client.executeOperation(GetLoginFlows.INSTANCE,
		new GetLoginFlows.Parameters(),
		GetLoginFlows.EMPTY_BODY).join().body()).body().asJson());
}
