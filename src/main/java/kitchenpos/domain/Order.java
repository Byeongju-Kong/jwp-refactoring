package kitchenpos.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import kitchenpos.exception.AlreadyCompletionOrderStatusException;

@Entity(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_table_id")
    private OrderTable orderTable;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private LocalDateTime orderedTime;

    @OneToMany(mappedBy = "order")
    private List<OrderLineItem> orderLineItems = new ArrayList<>();

    public Order() {
    }

    public Order(OrderTable orderTable, OrderStatus orderStatus, List<OrderLineItem> orderLineItems) {
        this.orderTable = orderTable;
        this.orderStatus = orderStatus;
        this.orderedTime = LocalDateTime.now();
        this.orderLineItems = orderLineItems;
    }

    public Long getId() {
        return id;
    }

    public OrderTable getOrderTable() {
        return orderTable;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getOrderedTime() {
        return orderedTime;
    }

    public List<OrderLineItem> getOrderLineItems() {
        return orderLineItems;
    }

    public void changeOrderStatus(OrderStatus orderStatus) {
        validateCompletionStatus();
        this.orderStatus = orderStatus;
    }

    private void validateCompletionStatus() {
        if (orderStatus.isCompletion()) {
            throw new AlreadyCompletionOrderStatusException();
        }
    }

    public boolean isNotCompletionOrderStatus() {
        return !orderStatus.isCompletion();
    }

    public void addOrderLineItem(OrderLineItem orderLineItem) {
        orderLineItems.add(orderLineItem);
    }
}
