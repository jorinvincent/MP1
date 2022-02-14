package cmsc433;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Simulation is the main class used to run the simulation. You may
 * add any fields (static or instance) or any methods you wish.
 */
public class Simulation {
	// List to track simulation events during simulation
	public static List<SimulationEvent> events;

	//Creates machines for the sake of simulation events
	public static Machines sodaMachine;
	public static Machines fryMachine;
	public static Machines grillMachine;
	public static Machines ovenMachine;

	//Creates a cook for calling methods used for simulation events
	public static Cook nullCook;

	/**
	 * Used by other classes in the simulation to log events
	 * 
	 * @param event
	 */
	public static void logEvent(SimulationEvent event) {
		events.add(event);
		System.out.println(event);
	}

	/**
	 * Function responsible for performing the simulation. Returns a List of
	 * SimulationEvent objects, constructed any way you see fit. This List will
	 * be validated by a call to Validate.validateSimulation. This method is
	 * called from Simulation.main(). We should be able to test your code by
	 * only calling runSimulation.
	 * 
	 * Parameters:
	 * 
	 * @param numCustomers the number of customers wanting to enter the restaurant
	 * @param numCooks the number of cooks in the simulation
	 * @param numTables the number of tables in the restaurant
	 * @param machineCapacity the count of all machine types in the restaurant
	 * @param randomOrders a flag say whether or not to give each customer a random
	 *        order
	 */
	public static List<SimulationEvent> runSimulation(
		int numCustomers, int numCooks,
		int numTables, int machineCapacity,
		boolean randomOrders) {

		// This method's signature MUST NOT CHANGE.

		/*
		 * We are providing this events list object for you.
		 * It is the ONLY PLACE where a concurrent collection object is
		 * allowed to be used.
		 */
		events = Collections.synchronizedList(new ArrayList<SimulationEvent>());

		// Start the simulation
		logEvent(SimulationEvent.startSimulation(numCustomers,
			numCooks,
			numTables,
			machineCapacity));

		// Set things up you might need
		sodaMachine = new Machines(Machines.MachineType.sodaMachines, FoodType.soda);
		fryMachine = new Machines(Machines.MachineType.fryers, FoodType.fries);
		grillMachine = new Machines(Machines.MachineType.grillPresses, FoodType.subs);
		ovenMachine = new Machines(Machines.MachineType.ovens, FoodType.pizza);

		nullCook = new Cook(null);

		// Initializes the maximum capacity of all machines
		Machines.maxCapacity = machineCapacity;
		// Initializes the List representing customers in line
		Cook.customersInLine = new ArrayList<Customer>();
		// Initializes the maximum seating capacity of the Ratsies
		Customer.maxCapacity = numTables;

		// Start up machines
		logEvent(SimulationEvent.machinesStarting(sodaMachine, sodaMachine.machineFoodType, machineCapacity));
		logEvent(SimulationEvent.machinesStarting(fryMachine, fryMachine.machineFoodType, machineCapacity));
		logEvent(SimulationEvent.machinesStarting(grillMachine, grillMachine.machineFoodType, machineCapacity));
		logEvent(SimulationEvent.machinesStarting(ovenMachine, ovenMachine.machineFoodType, machineCapacity));

		// Let cooks in
		Thread[] cookThreads = new Thread[numCooks];
		for (int i = 0; i < cookThreads.length; i++) {
			cookThreads[i] = new Thread(new Cook("Cook " + i));
			cookThreads[i].start();
		}

		// Build the customers.
		Thread[] customers = new Thread[numCustomers];
		LinkedList<Food> order;
		if (!randomOrders) {
			for (int i = 0; i < customers.length; i++) {
				order = new LinkedList<Food>();
				order.add(FoodType.fries);
				order.add(FoodType.pizza);
				order.add(FoodType.subs);
				order.add(FoodType.soda);
				customers[i] = new Thread(new Customer("Customer " + (i), order));
			}
		} else {
			for (int i = 0; i < customers.length; i++) {
				Random rnd = new Random();
				int friesCount = rnd.nextInt(4);
				int pizzaCount = rnd.nextInt(4);
				int subCount = rnd.nextInt(4);
				int sodaCount = rnd.nextInt(4);
				order = new LinkedList<Food>();
				for (int b = 0; b < friesCount; b++) {
					order.add(FoodType.fries);
				}
				for (int f = 0; f < pizzaCount; f++) {
					order.add(FoodType.pizza);
				}
				for (int f = 0; f < subCount; f++) {
					order.add(FoodType.subs);
				}
				for (int c = 0; c < sodaCount; c++) {
					order.add(FoodType.soda);
				}
				customers[i] = new Thread(
					new Customer("Customer " + (i), order));
			}
		}

		/*
		 * Now "let the customers know the shop is open" by starting them running in
		 * their own thread.
		 */
		for (int i = 0; i < customers.length; i++) {
			customers[i].start();
			/*
			 * NOTE: Starting the customer does NOT mean they get to go right into the shop.
			 * There has to be a table for them. The Customer class' run method has many
			 * jobs to do - one of these is waiting for an available table...
			 */
		}

		try {
			/*
			 * Wait for customers to finish
			 * -- you need to add some code here...
			 */
			int customersFinished = 0;
			while (customersFinished < customers.length) {
				if (customers[customersFinished].isAlive()) {
					continue;
				} else {
					customersFinished++;
				}
			}

			for(int i = 0; i < customers.length; i++) {
				customers[i].join();
			}

			/*
			 * Then send cooks home...
			 * The easiest way to do this might be the following, where we interrupt their
			 * threads. There are other approaches though, so you can change this if you
			 * want to.
			 */
			for (int i = 0; i < cookThreads.length; i++) {
				cookThreads[i].interrupt();
			}
			for (int i = 0; i < cookThreads.length; i++) {
				cookThreads[i].join();
			}

		} catch (InterruptedException e) {
			System.out.println("Simulation thread interrupted.");
		}

		// Shut down machines
		logEvent(SimulationEvent.machinesEnding(sodaMachine));
		logEvent(SimulationEvent.machinesEnding(fryMachine));
		logEvent(SimulationEvent.machinesEnding(grillMachine));
		logEvent(SimulationEvent.machinesEnding(ovenMachine));

		// Done with simulation
		logEvent(SimulationEvent.endSimulation());

		return events;
	}

	/**
	 * Entry point for the simulation.
	 *
	 * @param args the command-line arguments for the simulation. There
	 *        should be exactly four arguments: the first is the number of
	 *        customers, the second is the number of cooks, the third is the number
	 *        of tables in the restaurant, and the fourth is the number of items
	 *        each set of machines can make at the same time.
	 */
	public static void main(String args[]) throws InterruptedException {
		// Command line argument parser

// @formatter:off
/*
 * 		if (args.length != 4) {
 * 			System.err.println("usage: java Simulation <#customers> <#cooks> <#tables> <count> <randomorders");
 * 			System.exit(1);
 * 		}
 * 		int numCustomers = new Integer(args[0]).intValue();
 * 		int numCooks = new Integer(args[1]).intValue();
 * 		int numTables = new Integer(args[2]).intValue();
 * 		int machineCapacity = new Integer(args[3]).intValue();
 * 		boolean randomOrders = new Boolean(args[4]);
 */
// @formatter: on
		
		// Parameters to the simulation
		int numCustomers = 10;
		int numCooks = 1;
		int numTables = 5;
		int machineCapacity = 4;
		boolean randomOrders = false;

		/* Run the simulation and then feed the result into the method to validate simulation. */
		System.out.println("Did it work? " +
			Validate.validateSimulation(
				runSimulation(
					numCustomers, numCooks,
					numTables, machineCapacity,
					randomOrders)));
	}

}
