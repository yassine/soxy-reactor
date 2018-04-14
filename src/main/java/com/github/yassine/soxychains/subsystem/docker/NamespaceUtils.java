package com.github.yassine.soxychains.subsystem.docker;

import com.github.yassine.soxychains.SoxyChainsModule;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.github.yassine.soxychains.subsystem.layer.LayerProvider;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class NamespaceUtils {

  private static final String SYSTEM_NAMESPACE    = "soxy_chains";
  private static final String IMAGE_SEPARATOR     = "__";
  private static final String CONTAINER_SEPARATOR = "__";
  private static final String NETWORK_SEPARATOR   = "__";
  public static final String SOXY_DRIVER_NAME    = "soxy-driver";
  public static final String SYSTEM_LABEL         = SoxyChainsModule.class.getPackage().getName();
  public static final String ORIGINAL_LABEL       = SYSTEM_LABEL+".original_name";
  public static final String NAMESPACE_LABEL      = SYSTEM_LABEL+".namespace";
  public static final String RANDOM_LABEL         = SYSTEM_LABEL+".random";
  public static final String LAYER_PROVIDER_LABEL = SYSTEM_LABEL+".layer.provider";
  public static final String LAYER_PROVIDER_INDEX = SYSTEM_LABEL+".layer.index";
  public static final String REPLICA_LABEL        = SYSTEM_LABEL+".replica_num";

  public static String nameSpaceImage(DockerConfiguration configuration, String userImageName){
    return userImageName.startsWith(Joiner.on(IMAGE_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace()))
      ? userImageName
      : Joiner.on(IMAGE_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace(), userImageName);
  }

  public static String nameSpaceNetwork(DockerConfiguration configuration, String userNetworkName){
    return userNetworkName.startsWith(Joiner.on(NETWORK_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace()))
      ? userNetworkName
      : Joiner.on(NETWORK_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace(), userNetworkName);
  }

  public static String nameSpaceLayerNetwork(DockerConfiguration configuration, int layerIndex){
    return nameSpaceNetwork(configuration, String.format("layer__%s", layerIndex));
  }

  public static String namespaceLayerNode(DockerConfiguration configuration, int layerIndex, String name){
    return nameSpaceContainer(configuration, String.format("layer-%s__%s", layerIndex, name));
  }

  public static String nameSpaceContainer(DockerConfiguration configuration, String userContainerName){
    return userContainerName.startsWith(Joiner.on(CONTAINER_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace()))
      ? userContainerName
      : Joiner.on(CONTAINER_SEPARATOR).join(SYSTEM_NAMESPACE, configuration.getNamespace(), userContainerName);
  }

  public static Map<String, String> labelizeLayerNode(Class<? extends LayerProvider> providerClass, int layerLevel, DockerConfiguration dockerConfiguration){
    return ImmutableMap.of(
      NamespaceUtils.LAYER_PROVIDER_INDEX, layerLevel+"",
      NamespaceUtils.LAYER_PROVIDER_LABEL, providerClass.getName(),
      NamespaceUtils.SYSTEM_LABEL, "",
      NamespaceUtils.NAMESPACE_LABEL, dockerConfiguration.getNamespace()
    );
  }

  public static Map<String, String> labelizeNamedEntity(String name, DockerConfiguration dockerConfiguration){
    return ImmutableMap.of(
      NamespaceUtils.ORIGINAL_LABEL, name,
      NamespaceUtils.SYSTEM_LABEL, "",
      NamespaceUtils.NAMESPACE_LABEL, dockerConfiguration.getNamespace()
    );
  }

  public static String soxyDriverName(DockerConfiguration dockerConfiguration){
    return Joiner.on("__").join(dockerConfiguration, SOXY_DRIVER_NAME);
  }

}
