package moe.nea.frobjson.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@SuppressWarnings("unused")
public final class DiscoveryInformation {
  private @Nullable JsonElement $json;

  private final HomeserverInformation mhomeserver;

  private final @Nullable IdentityServerInformation midentity_server;

  public DiscoveryInformation(HomeserverInformation mhomeserver,
      @Nullable IdentityServerInformation midentity_server) {
    this.mhomeserver = mhomeserver;
    this.midentity_server = midentity_server;
  }

  public HomeserverInformation mhomeserver() {
    return this.mhomeserver;
  }

  public @Nullable IdentityServerInformation midentity_server() {
    return this.midentity_server;
  }

  public JsonElement generateJson() {
    JsonObject $json = new JsonObject();
     {
      JsonElement $jsonField;
      $jsonField = this.mhomeserver.asJson();
      $json.add("m.homeserver", $jsonField);
    }
    if (this.midentity_server != null) {
      JsonElement $jsonField;
      $jsonField = this.midentity_server.asJson();
      $json.add("m.identity_server", $jsonField);
    }
    return $json;
  }

  public JsonElement asJson() {
    if (this.$json == null) {
      return this.$json = this.generateJson();
    }
    return this.$json;
  }

  public static DiscoveryInformation fromJson(JsonElement $json) {
    JsonObject $json$object = $json.getAsJsonObject();
    HomeserverInformation mhomeserver;
     {
      JsonElement $jsonField = $json$object.get("m.homeserver");
      mhomeserver = HomeserverInformation.fromJson($jsonField);
    }
    IdentityServerInformation midentity_server;
     {
      JsonElement $jsonField = $json$object.get("m.identity_server");
      if ($jsonField == null) {
        midentity_server = null;
      } else {
        midentity_server = IdentityServerInformation.fromJson($jsonField);
      }
    }
    DiscoveryInformation $constructed = new DiscoveryInformation(
        mhomeserver,
        midentity_server);
    $constructed.$json = $json;
    return $constructed;
  }

  @Override
  @SuppressWarnings("StringBufferReplaceableByString")
  public String toString() {
    StringBuilder $string = new StringBuilder();
    $string.append("DiscoveryInformation { ");
    $string.append("m.homeserver=").append(this.mhomeserver).append(", ");
    $string.append("m.identity_server=").append(this.midentity_server).append(", ");
    $string.append("... }");
    return $string.toString();
  }
}
