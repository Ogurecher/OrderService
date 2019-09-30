package entity;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class Order {

	private long id;
	private Status status;
	private String username;
	private Set<Item> items = new HashSet<Item>();

	public Order() {}

	public Order(String username) {
		this.username = username;
		this.status = Status.COLLECTING;
		this.items = new HashSet<Item>();
	}

	public long calculateTotalCost() {
		return sumFields(item -> item.getAmount() * item.getPrice());
	}

	public long calculateTotalAmount() {
		return sumFields(item -> item.getAmount());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<Item> getItems() {
		return items;
	}

	public void setItems(Set<Item> items) {
		this.items = items;
	}

	private long sumFields(Function<Item, Long> mapper) {
		if (items.isEmpty()) {
			return 0;
		}
		return items.stream().map(mapper).reduce((long) 0, (a, b) -> a + b);
	}
} 
