package org.schneider.DynamHAProxy;

import java.lang.invoke.MethodHandles;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * CommandLine entry point to the application.
 * 
 * @author <a href="ryan.schneider@gmail.com">Ryan Schneider</a>
 */
public class Driver {
  
  @SuppressWarnings("static-access")
  public static void main( String[] args ) {
    
    Logger logger = LogManager.getLogger( MethodHandles.lookup().lookupClass() );

    // create the command line parser
    CommandLineParser parser = new BasicParser();

    // create the Options
    Options options = new Options();
    
    options.addOption( OptionBuilder.withLongOpt("joiner").isRequired().hasArg(false)
                       .withDescription("name of the joiner to use").create('j') );
    try {
      // parse the command line arguments
      CommandLine line = parser.parse( options, args );
    }
    catch( ParseException e ) {
      logger.fatal( "Problem trying to parse command line", e );
      new HelpFormatter().printHelp( MethodHandles.lookup().lookupClass().getName(), options );
    }
    catch( Exception e ) {
      logger.error( "Unexpected error, exiting", e );
    }
  }
}