import java.io.Serializable;
import java.util.Objects;

/** Product-side waitlist entry (FIFO). */
public class WaitList implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String clientId;
  private int qty;
  private final long createdAt = System.currentTimeMillis(); // FIFO tie-breaker

  public WaitList(String clientId, int qty) {
    this.clientId = clientId; this.qty = qty;
  }

  public String getClientId() { return clientId; }
  public int getQty() { return qty; }
  public void consume(int n) { qty -= n; }
  public boolean cleared() { return qty <= 0; }
  public long getCreatedAt() { return createdAt; }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof WaitList)) return false;
    WaitList b = (WaitList) o;
    return createdAt == b.createdAt && Objects.equals(clientId, b.clientId);
  }
  @Override public int hashCode() { return Objects.hash(clientId, createdAt); }
}
