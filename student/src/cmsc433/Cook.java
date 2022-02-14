package cmsc433;

import java.util.List;

/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Customer and process them.
 */
public class Cook implements Runnable {
	private final String name;

	public static List<Customer> customersInLine;
	private static Object cookLock = new Object();

	/**
	 * You can feel free modify this constructor. It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful.
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	// Adds customers to the line to later "place" their order to the cooks
	public void addCustomerInLine(Customer customer) {
		synchronized(cookLock) {
			customersInLine.add(customer);
			cookLock.notify();
		}
	}

	/**
	 * When a cook is freed up, they will take the order from the first customer
	 * in line. The entire customer is being added and removed to/from a "line" 
	 * instead of just their order because the simulation events need both the 
	 * order and the order number; in my mind, adding the entire customer made
	 * the most sense seeing that I can simply use getter methods to access a 
	 * customers order and order number, making things easier for myself.
	 * 
	 * @return: the customer at the front of the line
	 */
	public Customer takeCustomerOrder() throws InterruptedException {
		synchronized(cookLock){
			while (customersInLine.isEmpty()) {
				cookLock.wait();
			}

			Customer tempCustomer = customersInLine.remove(0);
			return tempCustomer;
		}
	}

	/**
	 * This method executes as follows. The cook tries to retrieve
	 * orders placed by Customers. For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine type, by calling makeFood(). Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified. The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	public void run() {

		Simulation.logEvent(SimulationEvent.cookStarting(this));
		try {
			while (true) {
				// YOUR CODE GOES HERE..

				// Gets all the necessary information from the customers order
				Customer tempCustomer = takeCustomerOrder();
				List<Food> order = tempCustomer.getOrder();
				int orderNumber = tempCustomer.getOrderNumber();
				Thread[] foodThreads = new Thread[order.size()];
				int itemsCooked = 0;

				Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, order, orderNumber));

				// Sends each food item in the order off to the machines to cook
				for (int i = 0; i < order.size(); i++) {
					Food food = order.get(i);

					if (food.equals(FoodType.soda)) {
						foodThreads[i] = Simulation.sodaMachine.makeFood();
					} else if (food.equals(FoodType.fries)) {
						foodThreads[i] = Simulation.fryMachine.makeFood();
					} else if (food.equals(FoodType.subs)) {
						foodThreads[i] = Simulation.grillMachine.makeFood();
					} else {
						foodThreads[i] = Simulation.ovenMachine.makeFood();
					}

					Simulation.logEvent(SimulationEvent.cookStartedFood(this, food, orderNumber));
				}

				// Waits for each food item in the order to finish cooking
				while (itemsCooked < order.size()) {
					if (foodThreads[itemsCooked].isAlive()) {
						continue;
					} else {
						Simulation.logEvent(SimulationEvent.cookFinishedFood(this, order.get(itemsCooked), orderNumber));
						itemsCooked++;
					}
				}

				Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, orderNumber));
				tempCustomer.orderCompleted();

				// throw new InterruptedException(); // REMOVE THIS
			}
		} catch (InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		}
	}
}
