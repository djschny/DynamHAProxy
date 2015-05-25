package org.schneider.DynamHAProxy.joiners;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.schneider.DynamHAProxy.ClusterMember;
import org.schneider.DynamHAProxy.ClusterStateChangeReceiver;
import org.schneider.DynamHAProxy.DynamHAProxy;

/**
 * Joiner for the ElasticSearch Zen discovery protocol. This will join
 * elasticsearch as a client node only. Therefore no data, searches, or indexing
 * will be performed by this node.
 * <p/>
 * TODO: see if there is a way to get notification about callback events as opposed
 * to a timer and poll
 * 
 * @author <a href="mailto:ryan.schneider@gmail.com">Ryan Schneider</a>
 */
public class ElasticSearchJoiner implements Joiner {

  public static final String NAME = "ElasticSearchJoiner";
  
  /* Properties */
  private String clusterName;
  
  private ClusterStateChangeReceiver receiver;
  private Node node;
  private ScheduledThreadPoolExecutor clusterMemberRefreshTimer;
  private int refreshPollIntervalSeconds = 30;
  
  public interface Properties {
    public static final String CLUSTER_NAME = "clusterName";
    public static final String REFRESH_POLL_INTERVAL_SECONDS = "refreshPollIntervalSeconds";
  }
  
  @Override
  public void setProperties( Map<String, String> properties ) {
    if( properties != null ) {
      clusterName = StringUtils.trimToNull(properties.get(Properties.CLUSTER_NAME)) != null ?
          StringUtils.trimToNull(properties.get(Properties.CLUSTER_NAME)) : clusterName;
      refreshPollIntervalSeconds = StringUtils.trimToNull(properties.get(Properties.REFRESH_POLL_INTERVAL_SECONDS)) != null ?
          Integer.parseInt(properties.get(Properties.REFRESH_POLL_INTERVAL_SECONDS)) : refreshPollIntervalSeconds;
    }
  }

  @Override
  public void setClusterStateChange( ClusterStateChangeReceiver receiver ) {
    this.receiver = receiver;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void join() {
    node = joinElasticsearch();
    clusterMemberRefreshTimer = new ScheduledThreadPoolExecutor(1);
    clusterMemberRefreshTimer.scheduleAtFixedRate( new ClusterRefreshTask(), 0, refreshPollIntervalSeconds, TimeUnit.SECONDS );
  }

  @Override
  public void leave() {
    clusterMemberRefreshTimer.shutdown();
    leaveElasticsearch();
  }
  
  protected Node joinElasticsearch() {
	NodeBuilder nBuilder = NodeBuilder.nodeBuilder().clusterName(clusterName).client(true).data(false);
	nBuilder.settings(nBuilder.settings().put("node.name",DynamHAProxy.class.getSimpleName() + "-" + NetworkUtils.getLocalHostName("")));
	return nBuilder.node();
  }

  protected void leaveElasticsearch() {
    node.close();
  }
  
  public static Collection<ClusterMember> getClusterMembers( Node node ) {
    NodesInfoResponse response = node.client().admin().cluster().nodesInfo( new NodesInfoRequest() ).actionGet();
    
    HashSet<ClusterMember> members = new HashSet<ClusterMember>();
    for( NodeInfo ni : response.getNodes() ) {
      
      // TDODO - do not add ourself; cannot figure out how to get unique node ID for ourself
      if( !ni.getNode().getId().equals(node) ) {
        // TODO need to find out from ES folks the following items
        // 1) How is best way to get the proper address to use (in a multiple NIC machine, we want
        //    the one that ES is bound to and we also ideally want IP address to make sure we don't
        //    use a DNS name that might not be resolvable from the HAProxy node.
        // 2) Best way to modify ES code base so clients getting settings like below don't have to duplicate
        //    magic strings and default logic.
        int httpPort = ni.getSettings().getAsInt("http.netty.port", ni.getSettings().getAsInt("http.port",9200));
        members.add( new ClusterMember().setAddress( new InetSocketAddress(ni.getHostname(), httpPort) ) );
      }
    }
    return members;
  }
  
  /**
   * Quick inner class to adapt to the TimerTask concept. Delegate to logic inside wrapper class so that
   * logic in here is very limited. Intentionally private so we can have access to wrapper class variables.
   * 
   * @author <a href="mailto:ryan.schneider@gmail.com">Ryan Schneider</a>
   */
  private class ClusterRefreshTask extends TimerTask {
    @Override
    public void run() {
      System.out.println( Thread.currentThread().getName() + "Entering run" );
      receiver.refresh( getClusterMembers(node) );
      System.out.println( Thread.currentThread().getName() + "Leaving run" );
    }
    
  }
}
