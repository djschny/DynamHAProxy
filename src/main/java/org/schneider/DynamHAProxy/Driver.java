package org.schneider.DynamHAProxy;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

/**
 * CommandLine entry point to the application.
 * 
 * @author <a href="ryan.schneider@gmail.com">Ryan Schneider</a>
 */
public class Driver {
  
  public static final String JOINER_PROPERTY_PREFIX = "joiner.";
  
  @SuppressWarnings("static-access")
  public static void main( String[] args ) {
	  System.setProperty(DefaultConfiguration.DEFAULT_LEVEL, "DEBUG");
   
	  Logger logger = LogManager.getLogger( MethodHandles.lookup().lookupClass() );

    // create the command line parser
    CommandLineParser parser = new GnuParser();

    // create the Options
    Options options = new Options();
    
    options.addOption( OptionBuilder.withLongOpt("joiner")
    					.isRequired()
    					.hasArg()
    					.withArgName("joiner")
    					.withDescription("name of the joiner to use")
    					.create() );

    try {
      // parse the command line arguments
      CommandLine line = parser.parse( options, args );
      DynamHAProxy service = new DynamHAProxy( line.getOptionValue("joiner"), retrieveJoinerProperties() );
      service.start();
    }
    catch( ParseException e ) {
      logger.fatal( "Problem trying to parse command line", e );
      new HelpFormatter().printHelp( MethodHandles.lookup().lookupClass().getName(), options );
    }
    catch( Exception e ) {
      logger.error( "Unexpected error, exiting", e );
      e.printStackTrace();
    }
  }
  
  public static Map<String,String> retrieveJoinerProperties() {
    HashMap<String,String> properties = new HashMap<>();
    
    for( Object key : System.getProperties().keySet() ) {
      if( key.toString().startsWith(JOINER_PROPERTY_PREFIX) ) {
        properties.put( key.toString().replace(JOINER_PROPERTY_PREFIX, ""), System.getProperty(key.toString()) );
      }
    }
    return properties;
  }
}