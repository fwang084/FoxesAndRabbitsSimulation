import java.io.Serializable;
import java.util.List;
import java.util.Iterator;
import java.util.Random;
public class Animal {
	protected int BREEDING_AGE;
	protected int MAX_AGE;
	protected double BREEDING_PROBABILITY;
	protected int MAX_LITTER_SIZE;
	protected int age;
	protected boolean alive;
	protected Location location;
	protected static final Random rand = new Random();
	protected double CREATION_PROBABILITY;
	public Animal (boolean randomAge){
		age = 0;
		alive = true;
		if(randomAge) {
			age = rand.nextInt(MAX_AGE);}
		}
	public void act(Field field, Field updatedField, List<Animal> newAnimals) {
		
	}
	protected void incrementAge() {
		age++;
		if (age > MAX_AGE) {
			alive = false;
		}
	}
	protected int breed() {
		int births = 0;
		if (canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
			births = rand.nextInt(MAX_LITTER_SIZE) + 1;
		}
		return births;
	}
	protected boolean canBreed() {
		return age >= BREEDING_AGE;
	}
	protected boolean isAlive() {
		return alive;
	}
	protected void setLocation(int row, int col) {
		this.location = new Location(row, col);
	}
	protected void setLocation(Location location) {
		this.location = location;
	}
	protected void setEaten()
    {
        alive = false;
    }
}
