import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ClientHandler extends Thread {

  private Socket socket;

  private String directory;
  private Utils utile;

  private HashMap<String, Consumer<String[]>> command;

  public ClientHandler(Socket socket, Utils utile) {
    this.socket = socket;
    this.directory = System.getProperty("user.dir");
    this.utile = utile;
    command = new HashMap<String, Consumer<String[]>>();
    command.put(
      "cd",
      cmd -> {
        // run command line
        try {
          this.cdCmd(cmd);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    );
    command.put(
      "ls",
      cmd -> {
        try {
          // run command line
          this.lsCmd();
        } catch (IOException e) {
          System.out.println("An error accured while executing the command");
        }
      }
    );
    command.put(
      "mkdir",
      cmd -> {
        try {
          // run command line
          this.mkdirCmd(cmd);
        } catch (IOException e) {
          System.out.println("An error accured while executing the command");
        }
      }
    );
    command.put(
      "upload",
      cmd -> {
        try {
          // Receive file from user
          if (cmd.length == 2) {
            // Protocol to receive file
            this.utile.getfile(socket, this.directory);
          }
        } catch (IllegalArgumentException e) {
          // add to logs of error
          //System.out.println("an error has occurred : "+e.getMessage());
        } catch (IOException e) {
          // add to logs of error
          System.out.println("an error has occurred : " + e.getMessage());
        }
      }
    );
    command.put(
      "download",
      cmd -> {
        try {
          if (cmd.length == 2) {
            // Protocol to send file
            this.utile.sendfile(socket, this.directory, cmd[1]);
          }
        } catch (IllegalArgumentException e) {
          // add to logs of error
          e.getMessage();
        } catch (IOException e) {
          // add to logs of error
          System.out.println("an error has occurred : " + e.getMessage());
        }
      }
    );
    command.put("exit", cmd -> {});
  }

  public void lsCmd() throws IOException {
    DataOutput out = new DataOutputStream(socket.getOutputStream());

    String Filelist = Stream
      .of(new File(this.directory).listFiles())
      .map(File::getName)
      .collect(Collectors.joining("\n"));
    out.writeUTF(Filelist);
  }

  public void cdCmd(String[] dir) throws IOException {
    DataOutput out = new DataOutputStream(socket.getOutputStream());
    if (dir.length == 2) {
      File newdir = new File(this.directory + "//" + dir[1]);
      Boolean dirExsiste = newdir.exists() && dir[1] != ".";
      out.writeBoolean(dirExsiste);
      if (dirExsiste) {
        String newDirectery = newdir.toString();
        if (dir[1].compareTo("..") == 0) {
          newDirectery = Paths.get(this.directory).getParent().toString();
        }
        this.directory = newDirectery;
        out.writeUTF(newDirectery);
      } else {
        out.writeBoolean(false);
      }
    } else {
      out.writeBoolean(false);
    }
  }

  public void mkdirCmd(String[] dirName) throws IOException {
    if (dirName.length == 2) {
      File newdir = new File(this.directory + "//" + dirName[1]);
      Boolean fileDontExsiste = !newdir.exists();
      if (fileDontExsiste) {
        Files.createDirectory(newdir.toPath());
      }
    }
  }

  public void logs(String input) {
    LocalDateTime myDateObj = LocalDateTime.now();
    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd@HH:mm:ss"
    );
    String formattedDate = myDateObj.format(myFormatObj);
    System.out.println(
      "[" +
      socket.getInetAddress().toString().replace("/", "") +
      ":" +
      socket.getPort() +
      "-" +
      formattedDate +
      "]:" +
      input
    );
  }

  public void run() {
    try {
      String userInput = "";
      do {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        userInput = in.readUTF();
        logs(userInput);
        String[] key = utile.getkey(userInput);
        command.get(key[0]).accept(key);
      } while (userInput.compareTo("exit") != 0);
    } catch (IOException e) {
      System.out.println("an error has occurred : " + e.getMessage());
    } finally {
      try {
        socket.close();
      } catch (IOException e) {}
    }
  }
}
