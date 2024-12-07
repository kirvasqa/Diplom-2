package praktikum.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String name;
    private OrderDetails order;
    private boolean success;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderDetails {
        private int number;
    }
}