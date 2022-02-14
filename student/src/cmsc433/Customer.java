package cmsc433;

import java.util.List;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order. When running, an
 * customer attempts to enter the Ratsie's (only successful if the
 * Ratsie's has a free table), place its order, and then leave the
 * Ratsie's when the order is complete.
 */
public class Customer implements Runnable {
	// JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	private final List<Food> order;
	private final int orderNum;

	private static int runningCounter = 0;

	// Used to communicate between cook and customer on whether their order is complete or not
	private boolean orderComplete;

	public static int maxCapacity;
	private static int currentCapacity = 0;
	private static Object customerLock = new Object();

	/**
	 * You can feel free modify this constructor. It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> order) {
		this.name = name;
		this.order = order;
		this.orderNum = ++runningCounter;
		this.orderComplete = false;
	}

	public String toString() {
		return name;
	}

	/** @return: a customers order */
	public List<Food> getOrder() {
		return this.order;
	}

	/** @return: a customers order number */
	public int getOrderNumber() {
		return this.orderNum;
	}

	// Changes a customers check on whether their order is finished from false to true
	public void orderCompleted() {
		this.orderComplete = true;
		Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, this.order, this.orderNum));
		Simulation.logEvent(SimulationEvent.customerLeavingRatsies(this));
		currentCapacity--;
		synchronized(customerLock) {
			customerLock.notifyAll();
		}
	}

	/**
	 * This method defines what an Customer does: The customer attempts to
	 * enter the Ratsie's (only successful when the Ratsie's has a
	 * free table), place its order, and then leave the Ratsie's
	 * when the order is complete.
	 */
	public void run() {
		// YOUR CODE GOES HERE...
		Simulation.logEvent(SimulationEvent.customerStarting(this));

		try {
			// Waits for an open table in the Ratsies
			synchronized(customerLock) {
				while (currentCapacity == maxCapacity) {
					customerLock.wait();
				}

				// Takes up one table in the Ratsies
				currentCapacity++;
				Simulation.logEvent(SimulationEvent.customerEnteredRatsies(this));
			}

			Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, this.order, this.orderNum));
			Simulation.nullCook.addCustomerInLine(this);

			synchronized(customerLock) {
				while (this.orderComplete != true) {
					customerLock.wait();
				}
			}
		} catch(InterruptedException e) { }
	}
}
