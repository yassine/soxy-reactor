package com.github.yassine.soxychains.subsystem.layer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

import java.io.Serializable;

@Getter
@JsonTypeInfo(
  use      = JsonTypeInfo.Id.NAME,
  include  = JsonTypeInfo.As.PROPERTY,
  property = "type"
)
public abstract class AbstractLayerContext implements Serializable{
  protected int maxNodes    = 50;
  protected int clusterServicePort = 1080;
  protected int localServicePort   = 1081;
  protected int healthCheckPort    = 1082;
  protected double readyRatio = 0.5;
  protected Integer healthCheckInterval = 120;//seconds
  protected Integer healthCheckTimeout  = 120;//seconds
}