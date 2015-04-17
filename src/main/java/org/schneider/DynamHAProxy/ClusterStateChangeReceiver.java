package org.schneider.DynamHAProxy;

import java.util.Collection;

/**
 * Interface that defines activities that happen in a cluster that HAProxy should
 * be aware of and adjust its configuration.
 * 
 * @author <a href="mailto:ryan.schneider@gmail.com">Ryan Schneider</a>
 */
public interface ClusterStateChangeReceiver {
  
  /* Cluster specific methods */
  public boolean memberJoined( ClusterMember member );
  public boolean memberTerminate( ClusterMember member );
  public boolean refresh( Collection<ClusterMember> members );
}
