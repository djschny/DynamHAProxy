package org.schneider.DynamHAProxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HAProxyCfgCreator
 *
 * @author Eduard Martinescu <emartinescu@salsalabs.com>
 */
public class HAProxyCfgCreator {
  private static final Logger logger = LogManager.getLogger(HAProxyCfgCreator.class);
  private static final String MARKER = "####";
  private static final String HAPROXY_RELOAD_CMD = "sudo /etc/init.d/haproxy reload";
  
  public static final String TEMPLATE_NAME = "haproy.cfg.tmpl";
  public static final String HAPROXY_DEST_KEY = "haproxy.config.dest";

  private final StringWriter builder;
  private InputStream templateFile;
  private String destination;

  public HAProxyCfgCreator() {
    builder = new StringWriter(8192);
    templateFile = this.getClass().getClassLoader().getResourceAsStream(TEMPLATE_NAME);
    destination = System.getProperty(HAPROXY_DEST_KEY);
  }
  
  public HAProxyCfgCreator(InputStream template,String dest) {
	  builder = new StringWriter(8192);
	  templateFile = template;
	  destination = dest;
  }

  public void setupConfig(String[] addresses, boolean useSticky) {
    buildNewConfig(addresses,useSticky);
    File origFile = FileUtils.getFile(destination, TEMPLATE_NAME);
    File tmpFile = FileUtils.getFile(destination, TEMPLATE_NAME+".tmp");
    try {
      FileUtils.writeStringToFile(tmpFile, builder.toString());
      if (!FileUtils.contentEquals(tmpFile, origFile)) {
        logger.info("Detected difference in files {} and {}, updating haproxy",tmpFile,origFile);
        FileUtils.copyFile(tmpFile, origFile);
        reloadHaproxy();
      }
    }
    catch (Exception ex) {
      logger.warn("Failed to update HA Proxy configuration", ex);
    }

  }

  /**
   *
   */
  private void reloadHaproxy() {
    logger.info("kick haproxy");
    try {
      CommandLine cmdLine = CommandLine.parse(HAPROXY_RELOAD_CMD);
      DefaultExecutor executor = new DefaultExecutor();
      executor.setStreamHandler(new PumpStreamHandler(new OutputStreamLogger()));
      executor.execute(cmdLine);
    }
    catch (IOException ex) {
      logger.error("Failed to kick haproxy",ex);
    }
  }

  private void buildNewConfig(String[] addresses, boolean useSticky) {
    BufferedReader reader;
    reader = new BufferedReader(new InputStreamReader(templateFile));
    PrintWriter printer = new PrintWriter(builder);
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        if (MARKER.equals(line)) {
          String template = reader.readLine();
          for (String address : addresses) {
            String serverLine = String.format(template, address, address);
            printer.println(serverLine);
          }
        }
        else {
          printer.println(line);
        }
      }
      reader.close();
      //logger.info(builder.toString());
    }
    catch (IOException ex) {
      logger.warn("Failed to process file: {}", TEMPLATE_NAME, ex);
    }
  }

  private class OutputStreamLogger extends LogOutputStream {

    @Override
    protected void processLine(String line, int level) {
      Level l = Level.INFO;
      for (Level lv : Level.values()) {
        if (lv.intLevel() == level) {
          l = lv;
          break;
        }
      }
      logger.log(l, line);
    }

    public OutputStreamLogger() {
      super(Level.INFO.intLevel());
    }

  }
}
