package io.github.cdfn.skyblock.commons.message;

import io.github.cdfn.skyblock.commons.message.api.serializer.AnnotationMessageSerializer;
import io.github.cdfn.skyblock.commons.message.api.serializer.MessagePackField;

public class ConfigMessages {

  public static class ConfigRequest implements AnnotationMessageSerializer {

    @MessagePackField
    private Integer id;

    public ConfigRequest(int id) {
      this.id = id;
    }

    public int getId() {
      return id;
    }
  }

  public static class ConfigResponse implements AnnotationMessageSerializer {

    @MessagePackField
    private Integer id;
    @MessagePackField
    private String data;

    public ConfigResponse(int id, String data) {
      this.id = id;
      this.data = data;
    }

    public int getId() {
      return id;
    }

    public String getData() {
      return data;
    }
  }
}
