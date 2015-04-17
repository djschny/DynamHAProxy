package org.schneider.DynamHAProxy.joiners;

import java.net.InetSocketAddress;
import java.util.Map;

import org.schneider.DynamHAProxy.ClusterMember;
import org.schneider.DynamHAProxy.ClusterStateChangeReceiver;

public class LocalHostJoiner implements Joiner {

  public static final String NAME = "LocalHostJoiner";
  private Integer port = 8080;
  private ClusterStateChangeReceiver receiver;
  
  public interface Properties {
    public static final String PORT = "port";
  }

  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public void setProperties( Map<String, String> properties ) {
    port = properties.get(Properties.PORT) != null ? new Integer(properties.get(Properties.PORT)) : port;
  }

  @Override
  public void setClusterStateChange( ClusterStateChangeReceiver receiver ) {
    this.receiver = receiver;
  }

  @Override
  public void join() {
    receiver.memberJoined( new ClusterMember().setAddress(new InetSocketAddress("localhost", port)) );
  }
  
  @Override
  public void leave() {
  }
}
