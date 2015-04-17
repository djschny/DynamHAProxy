package org.schneider.DynamHAProxy.joiners;

import java.util.Map;

import org.schneider.DynamHAProxy.ClusterStateChangeReceiver;

public interface Joiner {

  /** Properties that can be configured on this Joiner. */
  public void setProperties( Map<String,String> properties );
  
  /** A ClusterStateChangeReceiver that will be notified as members are added, removed, etc. */
  public void setClusterStateChange( ClusterStateChangeReceiver receiver );
  
  /** The name of this joiner which maps to configuration of which one should be used. */
  public String getName();
  
  // -- join actions
  public void join();
  
  public void leave();
}
