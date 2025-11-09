import java.io.Serializable;
import java.util.*;

/** Wishlist is owned by a single Client. */
public class Wishlist implements Serializable {
  private static final long serialVersionUID = 1L;

  // Keep insertion order for stable listing in a simple UI
  private final Map<String, WishlistItem> items = new LinkedHashMap<>();

  public Wishlist() { }

  /** Add a product or increase its quantity. Returns the (updated) WishlistItem. */
  public WishlistItem addOrUpdate(String productId, int qty) {
    if (productId == null || productId.trim().isEmpty()) {
      throw new IllegalArgumentException("productId required");
    }
    if (qty <= 0) {
      throw new IllegalArgumentException("qty must be > 0");
    }
    WishlistItem existing = items.get(productId);
    if (existing == null) {
      WishlistItem created = new WishlistItem(productId, qty, new Date());
      items.put(productId, created);
      return created;
    } else {
      existing.setQty(existing.getQty() + qty); // merge behavior
      return existing;
    }
  }

  /**
   * Set the quantity for an existing product. If qty <= 0, removes the item.
   * @return true if the item existed (and was updated/removed), false if not found.
   */
  public boolean updateQty(String productId, int qty) {
    WishlistItem existing = items.get(productId);
    if (existing == null) return false;
    if (qty <= 0) {
      items.remove(productId);
    } else {
      existing.setQty(qty);
    }
    return true;
  }

  /** Remove a product from the wishlist. */
  public boolean remove(String productId) {
    return items.remove(productId) != null;
  }

  public boolean contains(String productId) {
    return items.containsKey(productId);
  }

  /** Desired quantity for a product, or 0 if not present. */
  public int getQty(String productId) {
    WishlistItem existing = items.get(productId);
    return existing == null ? 0 : existing.getQty();
  }

  /** Raw iterator (to match your template style). */
  public Iterator getItems() {
    return Collections.unmodifiableCollection(items.values()).iterator();
  }

  /** snapshot list for UIs if prefer lists. */
  public List<WishlistItem> asList() {
    return Collections.unmodifiableList(new ArrayList<>(items.values()));
  }

  public int size() { return items.size(); }
  public boolean isEmpty() { return items.isEmpty(); }
  public void clear() { items.clear(); }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Wishlist:\n");
    for (WishlistItem it : items.values()) {
      sb.append("  ").append(it.getProductId())
        .append(" x ").append(it.getQty())
        .append(" (added ").append(it.getTime()).append(")\n");
    }
    return sb.toString();
  }
}
