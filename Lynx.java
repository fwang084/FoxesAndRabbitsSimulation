import java.io.Serializable;
import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a lynx. Lynxes age, move, eat foxes, and die.
 * 
 * @author David J. Barnes and Michael Kolling.  Modified by David Dobervich 2007-2013.
 * @version 2006.03.30
 */
public class Lynx implements Serializable {
	// Characteristics shared by all lynxes (static fields).
	private int BREEDING_AGE;
	// The age to which a lynx can live.
	private static final int MAX_AGE = 50;
	// The likelihood of a lynx breeding.
	private static final double BREEDING_PROBABILITY = 0.15;
	// The maximum number of births.
	private static final int MAX_LITTER_SIZE = 6;
	// The food value of a single fox. In effect, this is the
	// number of steps a lynx can go before it has to eat again.
	private static final int FOX_FOOD_VALUE = 6;
	// A shared random number generator to control breeding.
	private static final Random rand = new Random();

	// Individual characteristics (instance fields).

	// The lynx's age.
	private int age;
	// Whether the lynx is alive or not.
	private boolean alive;
	// The lynx's position
	private Location location;
	// The lynx's food level, which is increased by eating foxes.
	private int foodLevel;

	/**
	 * Create a lynx. A lynx can be created as a new born (age zero and not
	 * hungry) or with random age.
	 * 
	 * @param randomAge
	 *            If true, the lynx will have random age and hunger level.
	 */
	public Lynx(boolean randomAge) {
		age = 0;
		alive = true;
		if (randomAge) {
			age = rand.nextInt(MAX_AGE);
			foodLevel = rand.nextInt(FOX_FOOD_VALUE);
		} else {
			// leave age at 0
			foodLevel = FOX_FOOD_VALUE;
		}
		BREEDING_AGE = 3;
	}

	/**
	 * This is what the lynx does most of the time: it hunts for foxes. In the
	 * process, it might breed, die of hunger, or die of old age.
	 * 
	 * @param currentField
	 *            The field currently occupied.
	 * @param updatedField
	 *            The field to transfer to.
	 * @param newFoxes
	 *            A list to add newly born lynxes to.
	 */
	public void hunt(Field currentField, Field updatedField, List<Lynx> newLynxes) {
		incrementAge();
		incrementHunger();
		if (alive) {
			// New lynxes are born into adjacent locations.
			int births = breed();
			for (int b = 0; b < births; b++) {
				Lynx newLynx = new Lynx(false);
				newLynx.setFoodLevel(this.foodLevel);
				newLynxes.add(newLynx);
				Location loc = updatedField.randomAdjacentLocation(location);
				newLynx.setLocation(loc);
				updatedField.put(newLynx, loc);
			}
			// Move towards the source of food if found.
			Location newLocation = findFood(currentField, location);
			if (newLocation == null) { // no food found - move randomly
				newLocation = updatedField.freeAdjacentLocation(location);
			}
			if (newLocation != null) {
				setLocation(newLocation);
				updatedField.put(this, newLocation);
			} else {
				// can neither move nor stay - overcrowding - all locations
				// taken
				alive = false;
			}
		}
	}

	/**
	 * Increase the age. This could result in the lynx's death.
	 */
	private void incrementAge() {
		age++;
		if (age > MAX_AGE) {
			alive = false;
		}
	}

	/**
	 * Make this lynx more hungry. This could result in the lynx's death.
	 */
	private void incrementHunger() {
		foodLevel--;
		if (foodLevel <= 0) {
			alive = false;
		}
	}

	/**
	 * Tell the lynx to look for foxes adjacent to its current location. Only
	 * the first live fox is eaten.
	 * 
	 * @param field
	 *            The field in which it must look.
	 * @param location
	 *            Where in the field it is located.
	 * @return Where food was found, or null if it wasn't.
	 */
	private Location findFood(Field field, Location location) {
		List<Location> adjacentLocations = field.adjacentLocations(location);

		for (Location where : adjacentLocations) {
			Object animal = field.getObjectAt(where);
			if (animal instanceof Fox) {
				Fox fox = (Fox) animal;
				if (fox.isAlive()) {
					fox.setEaten();
					foodLevel = FOX_FOOD_VALUE;
					return where;
				}
			}
		}

		return null;
	}

	/**
	 * Generate a number representing the number of births, if it can breed.
	 * 
	 * @return The number of births (may be zero).
	 */
	private int breed() {
		int births = 0;
		if (canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
			births = rand.nextInt(MAX_LITTER_SIZE) + 1;
		}
		return births;
	}

	/**
	 * A lynx can breed if it has reached the breeding age.
	 */
	private boolean canBreed() {
		return age >= BREEDING_AGE;
	}

	/**
	 * Check whether the lynx is alive or not.
	 * 
	 * @return True if the lynx is still alive.
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Set the animal's location.
	 * 
	 * @param row
	 *            The vertical coordinate of the location.
	 * @param col
	 *            The horizontal coordinate of the location.
	 */
	public void setLocation(int row, int col) {
		this.location = new Location(row, col);
	}

	/**
	 * Set the lynx's location.
	 * 
	 * @param location
	 *            The lynx's location.
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	public void setFoodLevel(int fl) {
		this.foodLevel = fl;
	}
}
