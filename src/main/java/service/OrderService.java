package service;

import dao.*;
import dto.*;
import entity.*;
import org.slf4j.*;
import java.util.*;

public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private OrderDAO orderDAO;
    private CommonDAO commonDAO;
    private ItemService itemService;

    public OrderService(OrderDAO orderDAO, CommonDAO commonDAO, ItemService itemService) {
        this.orderDAO = orderDAO;
        this.commonDAO = commonDAO;
        this.itemService = itemService;
    }

    public List<OrderDTO> getOrders() {
        return orderDAO.getOrders();
    }

    public Order createEmptyOrder(String username) {
        Order order = new Order(username);
        orderDAO.save(order);
        logger.info("Created on order for " + username);
        return order;
    }

    public OrderDTO getOrderDTOById(long orderId) {
        Order order = commonDAO.getById(orderId, Order.class);
        if (order != null) {
            return new OrderDTO(order);
        }
        logger.error("No order with orderId {} found!", orderId);
        throw new IllegalArgumentException("No order with orderId " + orderId + " found!");
    }

    public OrderDTO addItemToOrder(Long orderId, ItemAdditionParametersDTO itemAdditionParameters) {
        Item itemToAdd = commonDAO.getById(itemAdditionParameters.getId(), Item.class);
        if (itemToAdd == null) {
            logger.error("Cannot add items with id " + itemAdditionParameters.getId() + " - there is no item with such id!");
            throw new IllegalArgumentException("Cannot add items with id " + itemAdditionParameters.getId() + " - there is no item with such id!");
        }

        Order order;
        if (orderId != null) {
            order = commonDAO.getById(orderId, Order.class);
            if(order == null) {
                logger.error("Cannot reserve " + itemAdditionParameters.getAmount() + " items with id " +
                        itemAdditionParameters.getId() + " to order " + orderId + ". Such order does not exist!");
                throw new IllegalArgumentException("Cannot reserve " + itemAdditionParameters.getAmount() + " items with id " +
                        itemAdditionParameters.getId() + " to order " + orderId + ". Such order does not exist!");
            }
        } else {
            order = createEmptyOrder(itemAdditionParameters.getUsername());
        }

        if (!itemService.reserveItems(itemAdditionParameters.getId(), itemAdditionParameters.getAmount())) {
            logger.error(
                    "Cannot reserve " + itemAdditionParameters.getAmount() + " items with id " +
                        itemAdditionParameters.getId() + " to order " + orderId + ". ItemService rejected the operation."
            );
            throw new RuntimeException("Cannot reserve " + itemAdditionParameters.getAmount() + " items with id " +
                itemAdditionParameters.getId() + " to order " + orderId + ". ItemService rejected the operation.");
        }

        order.getOrderItems().add(new OrderItem(order, itemToAdd, itemAdditionParameters.getAmount()));
        orderDAO.update(order);
        logger.info(
                "Updated order " + order.getId() + " added " + itemAdditionParameters.getAmount() + " " +
                        itemToAdd.getName() + " (itemId " + itemToAdd.getId() + ")"
        );
        return new OrderDTO(order);
    }

    public OrderDTO changeOrderStatus(long orderId, Status newStatus) {
        Order order = commonDAO.getById(orderId, Order.class);
        if (order == null) {
            logger.error("No order with orderId " + orderId + " found!");
            throw new IllegalArgumentException("No order with orderId " + orderId + " found!");
        }
        Status oldStatus = order.getStatus();
        if (!oldStatus.nextStatus().contains(newStatus)) {
            logger.error("Cannot change state from " + oldStatus + " to " + newStatus + " for the order " + orderId);
            throw new RuntimeException("Cannot change state from " + oldStatus + " to " + newStatus + " for the order " + orderId);
        }
        order.setStatus(newStatus);
        orderDAO.update(order);
        logger.info("Updated state of order " + orderId + " from " + oldStatus + " to " + newStatus);
        if (newStatus.equals(Status.FAILED) || newStatus.equals(Status.CANCELLED)) {
            order.getOrderItems().forEach(
                orderItem -> itemService.releaseItems(orderItem.getId().getItem().getId(), orderItem.getAmount())
            );
        }
        return new OrderDTO(order);
    }
}
