package fr.profi.mzdbconverter;

import fr.profi.mzdb.server.MzdbServerMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Thermo2Mzdb {

  private static final Logger LOGGER = LoggerFactory.getLogger(MzDBConverterMain.class);

  public static void main(String[] argv) {
    // look for a specific port
    int port = 8090;
    int portSpecifiedIndex = -1;
    for (int i=0;i<argv.length-1;i++) {
      String param = argv[i];
      if (param.equals("-p") || param.equals("--port")) {
        portSpecifiedIndex = i+1;
        port = Integer.parseInt(argv[portSpecifiedIndex]);

      }
    }


    // --- look for an available port between port=8090 (or specified port as argument) and port+100
    int portMax = port+100;
    boolean available = false;
    for (;port<=portMax;port++) {
      available = isPortAvailable(port);
      if (available) {
        break;
      }
    }

    if (! available) {
      LOGGER.error("No port available between"+port+"and "+portMax);
      return;
    }



    // --- mzdb server start

    String[] args = { Integer.toString(port)};
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        MzdbServerMain.main(args);
      }
    });
    t.setDaemon(false); // the main thread will wait for the ending of the server thread
    t.start();

    // --- start ThermoAccess

    // prepare command with parameters

    // check localisation of the ThermoAccess.exe

    String dirName = "";

    try {
      Properties properties = new Properties();
      properties.load(MzDBConverterMain.class.getResourceAsStream("mzdbServerConverter.properties"));
      String version = properties.getProperty("thermoaccess.version", "");
      String classifier = properties.getProperty("thermoaccess.classifier", "");
      dirName = "ThermoAccess-"+version+"-"+classifier;


    } catch (Exception e) {
      LOGGER.warn("error in addMzdbMetaData : can not get current version");
    }

    File pathFile = new File(".\\target\\unzip-dependencies\\"+dirName+"\\"); // path for debugging with IDE.
    if (! pathFile.exists()) {
      pathFile = new File(".\\"+dirName+"\\");
    }

    String absolutePath = pathFile.getAbsolutePath()+"\\ThermoAccess.exe";
    System.out.println("Thermo Access : "+absolutePath);

    ArrayList<String> cmds = new ArrayList<>(3+argv.length);
    cmds.add(absolutePath);

    if (portSpecifiedIndex != -1) {
      argv[portSpecifiedIndex] = String.valueOf(port);
    } else {
      cmds.add("-p");
      cmds.add(String.valueOf(port));
    }
    cmds.addAll(Arrays.asList(argv));

    // Prepare process builder, we redirect error stream to ouput stream to be able to read both
    ProcessBuilder pb = new ProcessBuilder(cmds);
    pb.redirectErrorStream(true);

    pb.directory(pathFile);



    try {
      Process p = pb.start();
      BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        LOGGER.info(line);
      }

      int exitCode = p.waitFor();
      if (exitCode != 0) {

        LOGGER.error(cmds+" abnormally finished with exitCode "+exitCode);
        LOGGER.info("Interrupt socket server");
        MzdbServerMain.interrupt();
        System.exit(exitCode);
      }

    } catch (Exception e ) {
      LOGGER.error(cmds+" abnormally finished");
      LOGGER.error(e.getMessage(), e);
      LOGGER.info("Interrupt socket server");
      MzdbServerMain.interrupt();
      System.exit(-1);
    }

  }

  private static boolean isPortAvailable(int port) {

    ServerSocket serverSocket = null;
    DatagramSocket datagramSocket = null;
    try {
      serverSocket = new ServerSocket(port);
      serverSocket.setReuseAddress(true);
      datagramSocket = new DatagramSocket(port);
      datagramSocket.setReuseAddress(true);

      return true;

    } catch (IOException ignored) {
    } finally {
      if (datagramSocket != null) {
        datagramSocket.close();
      }

      if (serverSocket != null) {
        try {
          serverSocket.close();
        } catch (IOException ignored) {
        }
      }
    }

    return false;
  }
}
