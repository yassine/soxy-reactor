package com.github.yassine.soxychains.subsystem.service.consul;

import com.github.yassine.soxychains.plugin.ConfigKey;
import com.github.yassine.soxychains.subsystem.service.ServicesPluginConfiguration;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration.ID;

@SuppressWarnings("FieldCanBeLocal")
@ConfigKey(ID) @Accessors(fluent = true)
public class ConsulConfiguration implements ServicesPluginConfiguration {
  static final String ID = "consul";
  @NotNull
  private String image = ID;
  @NotNull @Getter
  private String  serviceName    = ID;
  @NotNull
  private Integer servicePort    = 7090;
  @NotNull
  private Integer managementPort = 7091;

  @Override
  public String imageName() {
    return image;
  }

  @Override
  public List<Integer> servicePorts() {
    return ImmutableList.of(servicePort);
  }
}
