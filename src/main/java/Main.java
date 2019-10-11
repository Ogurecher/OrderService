import static spark.Spark.*;

import com.google.gson.Gson;
import dao.CommonDAO;
import dao.OrderDAO;
import dto.*;
import entity.Status;
import org.slf4j.LoggerFactory;
import service.ItemService;
import service.OrderService;
import service.SessionFactoryService;

public class Main {
	private static OrderService orderService = new OrderService(
			new OrderDAO(new SessionFactoryService()),
			new CommonDAO(new SessionFactoryService()),
			new ItemService(LoggerFactory.getLogger(OrderService.class)),
			LoggerFactory.getLogger(OrderService.class)
	);

    public static void main(String[] args) {
    	port(1809);

		get("/api/orders", (req, res) -> orderService.getOrders());

		post("/api/orders/:username", (req, res) ->
				new OrderDTO(orderService.createEmptyOrder(req.params("username")))
		);

		get("/api/orders/:orderId", (req, res) ->
			orderService.getOrderDTOById(Long.parseLong(req.params("orderId")))
		);

		put("/api/orders/:orderId/status/:status", (req, res) ->
			orderService.changeOrderStatus(
				Long.parseLong(req.params("orderId")),
				Status.valueOf(req.params("status"))
			)
		);

		post("/api/orders/:orderId/item", (req, res) ->
			orderService.addItemToOrder(
				parseLong(req.params("orderId")),
				new Gson().fromJson(req.body(), ItemAdditionParametersDTO.class)
			)
		);
    }

	private static Long parseLong(String s) {
		if (s == null || s.equals("") || s.toLowerCase().equals("null")) {
			return null;
		}
		return Long.parseLong(s);
	}
}
