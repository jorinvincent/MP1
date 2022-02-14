package cmsc433;

/**
 * Machines are used to make different kinds of Food. Each Machine type makes
 * just one kind of Food. Each machine type has a count: the set of machines of
 * that type can make that many food items in parallel. If the machines are
 * asked to produce a food item beyond its count, the requester blocks. Each
 * food item takes at least item.cookTime10S seconds to produce. In this
 * simulation, use Thread.sleep(item.cookTime10S) to simulate the actual cooking
 * time.
 */
public class Machines {

	public enum MachineType {
		sodaMachines, fryers, grillPresses, ovens
	};

	// Converts Machines instances into strings based on MachineType.
	public String toString() {
		switch (machineType) {
			case sodaMachines:
				return "Soda Machines";
			case fryers:
				return "Fryers";
			case grillPresses:
				return "Grill Presses";
			case ovens:
				return "Ovens";
			default:
				return "INVALID MACHINE TYPE";
		}
	}

	public final MachineType machineType;
	public final Food machineFoodType;

	// YOUR CODE GOES HERE...
	public static int maxCapacity;
	private int currentCapacity = 0;
	private Object machineLock = new Object();

	/**
	 * The constructor takes at least the name of the machines, the Food item they
	 * make, and their count. You may extend it with other arguments, if you wish.
	 * Notice that the constructor currently does nothing with the count; you must
	 * add code to make use of this field (and do whatever initialization etc. you
	 * need).
	 */
	public Machines(MachineType machineType, Food foodIn) {
		this.machineType = machineType;
		this.machineFoodType = foodIn;
	}

	/**
	 * This method is called by a Cook in order to make the Machines' food item. You
	 * can extend this method however you like, e.g., you can have it take extra
	 * parameters or return something other than Object. You will need to implement
	 * some means to notify the calling Cook when the food item is finished.
	 */
	public Thread makeFood() throws InterruptedException {
		// YOUR CODE GOES HERE...
		Thread cookFood = new Thread(new CookAnItem(this, this.machineFoodType));
		cookFood.start();

		return cookFood;
	}

	// THIS MIGHT BE A USEFUL METHOD TO HAVE AND USE BUT IS JUST ONE IDEA
	private class CookAnItem implements Runnable {

		private final Machines machine;
		private final Food food;

		public CookAnItem(Machines machine, Food food) {
			this.machine = machine;
			this.food = food;
		}

		public void run() {
			try {
				//YOUR CODE GOES HERE...

				// Waits for room in the machine
				synchronized(machineLock) {
					while(currentCapacity == maxCapacity){
						machineLock.wait();
					}

					// Takes up one cooking space in the machine
					currentCapacity++;
				}

				Simulation.logEvent(SimulationEvent.machinesCookingFood(this.machine, this.food));

				// "Cooks" for the foods required time
				Thread.sleep(this.food.cookTime10S);
				// Frees up one cooking space in the machine once cooked
				currentCapacity--;
				synchronized(machineLock) {
					machineLock.notify();
				}

				Simulation.logEvent(SimulationEvent.machinesDoneFood(this.machine, this.food));

				// throw new InterruptedException(); // REMOVE THIS
			} catch(InterruptedException e) { }
		}
	}
}
