import java.io.*;

public class ClientIdServer implements Serializable {
  private static final long serialVersionUID = 1L;

  private int idCounter = 1;
  private static ClientIdServer server;

  private ClientIdServer() { }

  public static ClientIdServer instance() {
    if (server == null) {
      server = new ClientIdServer();
    }
    return server;
  }

  /** Returns next client id, e.g., C1, C2, ... */
  public synchronized String nextId() {
    return "C" + (idCounter++);
  }

  // Persistence helpers

  /** Called from Warehouse.retrieve(input) to restore this utility. */
  public static void retrieve(ObjectInputStream input) {
    try {
      Object obj = input.readObject();
      server = (ClientIdServer) obj;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Can also be called explicitly (Warehouse currently writes the instance directly). */
  public void save(ObjectOutputStream output) {
    try {
      output.writeObject(this);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
  }

  /** Keep the singleton consistent after deserialization. */
  private Object readResolve() throws ObjectStreamException {
    server = this;
    return this;
  }
}
