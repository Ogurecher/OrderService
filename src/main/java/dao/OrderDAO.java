package dao;

import dto.*;
import entity.*;
import service.*;
import org.hibernate.Session;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDAO {
	public List<OrderDTO> getOrders() {
		List<Order> orders = null;
		try (Session session = SessionFactoryService.getSessionFactory().openSession()) {
			session.beginTransaction();
			CriteriaQuery<Order> query = session.getCriteriaBuilder().createQuery(Order.class);
			query.from(Order.class);
			orders = session.createQuery(query).getResultList();
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return orders.stream().map(OrderDTO::new).collect(Collectors.toList());
	}

	public OrderItem getOrderItem(long orderId, long itemId) {
		OrderItem orderItem = null;
		try (Session session = SessionFactoryService.getSessionFactory().openSession()) {
			session.beginTransaction();
			CriteriaQuery<OrderItem> query = session.getCriteriaBuilder().createQuery(OrderItem.class);
			Root<OrderItem> root = query.from(OrderItem.class);
            query.select(root).where(
				root.get("id").get("order").get("id").in(orderId),
				root.get("id").get("item").get("id").in(itemId)
			);
			List<OrderItem> results = session.createQuery(query).getResultList();
            orderItem = !results.isEmpty() ? results.get(0) : null;
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return orderItem;
	}
}
