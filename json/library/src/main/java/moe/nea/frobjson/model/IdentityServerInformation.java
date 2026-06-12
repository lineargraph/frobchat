package moe.nea.frobjson.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@SuppressWarnings("unused")
public final class IdentityServerInformation {
  private @Nullable JsonElement $json;

  private final String base_url;

  public IdentityServerInformation(String base_url) {
    this.base_url = base_url;
  }

  public String base_url() {
    return this.base_url;
  }

  public JsonElement generateJson() {
    JsonObject $json = new JsonObject();
     {
      JsonElement $jsonField;
      $jsonField = new JsonPrimitive(this.base_url);
      $json.add("base_url", $jsonField);
    }
    return $json;
  }

  public JsonElement asJson() {
    if (this.$json == null) {
      return this.$json = this.generateJson();
    }
    return this.$json;
  }

  public static IdentityServerInformation fromJson(JsonElement $json) {
    JsonObject $json$object = $json.getAsJsonObject();
    String base_url;
     {
      JsonElement $jsonField = $json$object.get("base_url");
      base_url = $jsonField.getAsString();
    }
    IdentityServerInformation $constructed = new IdentityServerInformation(
        base_url);
    $constructed.$json = $json;
    return $constructed;
  }

  @Override
  @SuppressWarnings("StringBufferReplaceableByString")
  public String toString() {
    StringBuilder $string = new StringBuilder();
    $string.append("IdentityServerInformation { ");
    $string.append("base_url=").append(this.base_url).append(", ");
    $string.append("... }");
    return $string.toString();
  }
}
