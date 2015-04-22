package org.schneider.DynamHAProxy;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.schneider.DynamHAProxy.joiners.ElasticSearchJoiner;
import org.schneider.DynamHAProxy.joiners.Joiner;
import org.schneider.DynamHAProxy.joiners.LocalHostJoiner;


/**
 * Create and run a new DynamHAProxy instance to monitor a cluster.
 * 
 * @author <a href="ryan.schneider@gmail.com">Ryan Schneider</a>
 */
public class DynamHAProxy {

  Logger logger = LogManager.getLogger( MethodHandles.lookup().lookupClass() );

  private Class<? extends Joiner> joiner;
  private Joiner instance;
  private Map<String,String> properties;
  
  private static Map<String,Class<? extends Joiner>> availableJoiners = new HashMap<>();
  static {
    // TODO - classpath scan and register all classes implementing the interface
    availableJoiners.put( LocalHostJoiner.NAME, LocalHostJoiner.class );
    availableJoiners.put( ElasticSearchJoiner.NAME, ElasticSearchJoiner.class );
  }
  
  /**
   * Create a new DynamHAPRoxy instance with the specified joiner.
   * @param joiner
   */
  public DynamHAProxy( String joiner ) {
    this( joiner, null );
  }
  
  public DynamHAProxy( String joiner, Map<String,String> properties ) {
    this.joiner = availableJoiners.get( joiner );
    if( joiner == null ) {
      throw new IllegalArgumentException( "Joiner " + joiner + " not found. Make sure the joiner name " +
                                          "is correct or your custom joiner is on the classpath. Currently available " +
                                          "joiners: " ); // TODO print map once put in apach collection utils
    }
    this.properties = properties;
  }
  
  public void start() {
    logger.info( "Initializing using joiner " + joiner.getName() );
    try {
      instance = joiner.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException( "Problem instantating joiner " + joiner.getName(), e );
    }
    instance.setProperties( properties );
    instance.setClusterStateChange( new HAProxyClusterStateChangeReceiver() );
    logger.info( "Starting service using joiner " + joiner.getCanonicalName() );
    instance.join();
  }
  
  public void stop() {
    logger.info(  "Stopping service" );
    instance.leave();
  }
}
