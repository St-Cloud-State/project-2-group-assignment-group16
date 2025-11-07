import java.util.*;
import java.io.*;

public class ProductCatalog implements Serializable {
  private static final long serialVersionUID = 1L;

  // Keep insertion order stable for listing in the UI
  private final List<Product> products = new LinkedList<>();

  // Simple ID counter (P1, P2, ...)
  private int nextId = 1;

  // Singleton
  private static ProductCatalog productCatalog;

  private ProductCatalog() { }

  public static ProductCatalog instance() {
    if (productCatalog == null) {
      productCatalog = new ProductCatalog();
    }
    return productCatalog;
  }

  /** Inserts a product; assigns an id if missing. Returns true if added. */
  public boolean insertProduct(Product product) {
    if (product == null) return false;
    if (product.getId() == null || product.getId().isBlank()) {
      product.setId("P" + (nextId++));
    }
    products.add(product);
    return true;
  }

  /** Iterator used by Warehouse.getProducts() */
  public Iterator getProducts() {
    return products.iterator(); 
  }

  /** Helper for lookups */
  public Product get(String productId) {
    if (productId == null) return null;
    for (Product p : products) {
      if (productId.equals(p.getId())) return p;
    }
    return null;
  }

  public int size() { return products.size(); }

  @Override
  public String toString() {
    return products.toString();
  }

  // Serialization helpers to keep the singleton consistent after load 
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
  }

  /** Ensure the deserialized instance also becomes the process-wide singleton. */
  private Object readResolve() throws ObjectStreamException {
    productCatalog = this;
    return this;
  }
}
