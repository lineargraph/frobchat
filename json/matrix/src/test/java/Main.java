import com.google.gson.Gson;
import moe.nea.frobjson.openapi.Operation;
import moe.nea.frobjson.openapi.client.JavaHttpClient;
import moe.nea.operations.GetWellknown;

import java.net.http.HttpClient;

void main() {
	var client = new JavaHttpClient(
		"https://matrix.nea.moe",
		HttpClient.newBuilder().build()
	);
	var result = client.executeOperation(GetWellknown.INSTANCE, new GetWellknown.Parameters(), Operation.EMPTY_BODY)
		.join();
	System.out.println(result.body());
	System.out.println(((GetWellknown.Status200) result.body()).body().asJson());
}
