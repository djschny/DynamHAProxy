package org.schneider.DynamHAProxy;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implement me or make another existing class implement me.
 * 
 * @author <a href="mailto:ryan.schneider@gmail.com">Ryan Schneider</a>
 */
public class HAProxyClusterStateChangeReceiver implements ClusterStateChangeReceiver {

  Logger logger = LogManager.getLogger( MethodHandles.lookup().lookupClass() );
  
  @Override
  public boolean memberJoined( ClusterMember member ) {
    logger.info( "Adding " + member );
    return false;
  }

  @Override
  public boolean memberTerminated( ClusterMember member ) {
    logger.info( "Removing " + member );
    return false;
  }

  @Override
  public boolean refresh( Collection<ClusterMember> members ) {
    logger.info( "Refreshing config with new list of members {}", members );
    return false;
  }

}
