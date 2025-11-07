import java.util.*;
import java.io.*;

public class ClientList implements Serializable {
  private static final long serialVersionUID = 1L;

  // Keep insertion order so listings are stable
  private final List<Client> clients = new LinkedList<>();

  private static ClientList clientList;

  private ClientList() { }

  public static ClientList instance() {
    if (clientList == null) {
      clientList = new ClientList();
    }
    return clientList;
  }

  /** Insert a client; returns true if added. */
public boolean insertClient(Client client) {
  if (client == null) return false;
  if (client.getId() == null || client.getId().isBlank()) {
    client.setId(ClientIdServer.instance().nextId()); // <-- assign if missing
  }
  return clients.add(client);
}

  /** Iterator used by Warehouse.getClients(). */
  public Iterator getClients() {
    return clients.iterator(); // raw Iterator to match rest of design
  }

  /** helper for direct lookup. */
  public Client get(String clientId) {
    if (clientId == null) return null;
    for (Client c : clients) {
      if (clientId.equals(c.getId())) return c;
    }
    return null;
  }

  public int size() { return clients.size(); }

  @Override
  public String toString() {
    return clients.toString();
  }

  // --- Serialization helpers to keep the singleton consistent after load ---
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
  }

  /** Ensure the deserialized instance also becomes the process-wide singleton. */
  private Object readResolve() throws ObjectStreamException {
    clientList = this;
    return this;
  }
}
