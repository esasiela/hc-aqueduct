package com.hedgecourt.aqueduct.sprite;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractEntitySprite implements EntitySprite {
  @JsonProperty(required = true)
  protected String type;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
