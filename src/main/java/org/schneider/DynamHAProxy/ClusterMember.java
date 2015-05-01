package org.schneider.DynamHAProxy;

import java.net.InetSocketAddress;

/**
 * Represents metadata about a member that essentially maps to a <code>server</code> line
 * in an HAProxy config file.
 * 
 * @author <a href="ryan.schneider@gmail.com">Ryan Schneider</a>
 */
public class ClusterMember {

  /** The actual address (dns/ip and port) to use to connect to the host */
  private InetSocketAddress address;

  /** Optional descriptive name for this host (what will display in the HAProxy status page).
   *  If one is not supplied the dns/ip + port string will be used. */
  private String name;
  
  /** Optional weight that this node should receive when load balancing is performed. */
  private Double weight;
  
  public InetSocketAddress getAddress() { return address; }
  public ClusterMember setAddress( InetSocketAddress address ) { this.address = address; return this; }
  
  public String getName() { return name; }
  public ClusterMember setName( String name ) { this.name = name; return this; }
  
  public Double getWeight() { return weight; }
  public ClusterMember setWeight( Double weight ) { this.weight = weight; return this; }
  @Override
  public String toString() {
	return String.format("ClusterMember [address=%s, name=%s, weight=%s]",
			address, name, weight);
  }
 
}
