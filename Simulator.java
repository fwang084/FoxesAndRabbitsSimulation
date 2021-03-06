import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import processing.core.PApplet;

/**
 * A simple predator-prey simulator, based on a field containing rabbits and
 * foxes.
 * 
 * @author David J. Barnes and Michael Kolling. Modified by David Dobervich
 *         2007-2013.
 * @version 2006.03.30
 * 
 */
public class Simulator {
	// The default width for the grid.
	private static final int DEFAULT_WIDTH = 80;

	// The default height of the grid.
	private static final int DEFAULT_HEIGHT = 80;

	// The probability that a lynx will be created in any given grid position.
	private static final double LYNX_CREATION_PROBABILITY = 0.04;

	// Lists of animals in the field. Separate lists are kept for ease of
	// iteration.
	private List<Animal> animals;
	private List<Lynx> lynxes;

	// The current state of the field.
	private Field field;

	// A second field, used to build the next stage of the simulation.
	private Field updatedField;

	// The current step of the simulation.
	private int step;

	// A graphical view of the simulation.
	private FieldDisplay view;

	// A graph of animal populations over time
	private Graph graph;

	// Processing Applet (the graphics window we draw to)
	private PApplet graphicsWindow;

	// Object to keep track of statistics of animal populations
	private FieldStats stats;

	/**
	 * Construct a simulation field with default size.
	 */
	public Simulator() {
		this(DEFAULT_HEIGHT, DEFAULT_WIDTH);
	}

	/**
	 * Create a simulation field with the given size.
	 * 
	 * @param depth
	 *          Depth of the field. Must be greater than zero.
	 * @param width
	 *          Width of the field. Must be greater than zero.
	 */
	public Simulator(int width, int height) {
		if (width <= 0 || height <= 0) {
			System.out.println("The dimensions must be greater than zero.");
			System.out.println("Using default values.");
			height = DEFAULT_HEIGHT;
			width = DEFAULT_WIDTH;
		}

		animals = new ArrayList<Animal>();
		lynxes = new ArrayList<Lynx>();
		field = new Field(width, height);
		updatedField = new Field(width, height);
		stats = new FieldStats();

		// Setup a valid starting point.
		reset();
	}

	public void setGUI(PApplet p, int x, int y, int display_width,
			int display_height) {
		this.graphicsWindow = p;

		// Create a view of the state of each location in the field.
		view = new FieldDisplay(p, this.field, x, y, display_width, display_height);
		view.setColor(Rabbit.class, p.color(155, 155, 155));
		view.setColor(Fox.class, p.color(200, 0, 255));
		view.setColor(Lynx.class, p.color(51,255, 51));
		graph = new Graph(p, 100, p.height - 30, p.width - 50, p.height - 110, 0,
				0, 500, field.getHeight() * field.getWidth());
		graph.title = "Fox, Rabbit, and Lynx Populations";
		graph.xlabel = "Time";
		graph.ylabel = "Pop.\t\t";
		graph.setColor(Rabbit.class, p.color(155, 155, 155));
		graph.setColor(Fox.class, p.color(200, 0, 255));
		graph.setColor(Fox.class, p.color(51, 255, 51));
	}

	public void setGUI(PApplet p) {
		setGUI(p, 10, 10, p.width - 10, 400);
	}

	/**
	 * Run the simulation from its current state for a reasonably long period,
	 * e.g. 500 steps.
	 */
	public void runLongSimulation() {
		simulate(500);
	}

	/**
	 * Run the simulation from its current state for the given number of steps.
	 * Stop before the given number of steps if it ceases to be viable.
	 * 
	 * @param numSteps
	 *          The number of steps to run for.
	 */
	public void simulate(int numSteps) {
		for (int step = 1; step <= numSteps && isViable(); step++) {
			simulateOneStep();
		}
	}

	/**
	 * Run the simulation from its current state for a single step. Iterate over
	 * the whole field updating the state of each fox and rabbit.
	 */
	public void simulateOneStep() {
		step++;

		// New List to hold newborn rabbits.
		List<Animal> newAnimals = new ArrayList<Animal>();

		// Loop through all Rabbits. Let each run around.
		for (int i = 0; i < animals.size(); i++) {
			Animal animal = animals.get(i);
			animal.act(field, updatedField, newAnimals);
			if (!animal.isAlive()) {
				animals.remove(i);
				i--;
			}
		}

		// Add new born rabbits to the main list of rabbits.
		animals.addAll(newAnimals);
		List<Lynx> newLynxes = new ArrayList<Lynx>();
		for (int i =0; i < lynxes.size(); i++) {
			Lynx lynx = lynxes.get(i);
			lynx.hunt(field, updatedField, newLynxes);
			if (!lynx.isAlive()) {
				lynxes.remove(i);
				i--;
			}
			
		}

		// Swap the field and updatedField at the end of the step.
		Field temp = field;
		field = updatedField;
		updatedField = temp;
		updatedField.clear();

		stats.generateCounts(field);
		updateGraph();
	}

	public void updateGraph() {
		Counter count;
		for (Counter c : stats.getCounts()) {
			graph.plotPoint(step, c.getCount(), c.getClassName());
		}
	}

	/**
	 * Reset the simulation to a starting position.
	 */
	public void reset() {
		step = 0;
		animals.clear();
		lynxes.clear();
		field.clear();
		updatedField.clear();
		initializeBoard(field);

		if (graph != null)
			graph.clear();

		// Show the starting state in the view.
		// view.showStatus(step, field);
	}

	/**
	 * Populate a field with lynxes, foxes and rabbits.
	 * 
	 * @param field
	 *          The field to be populated.
	 */
	private void initializeBoard(Field field) {
		Random rand = new Random();
		field.clear();
		for (int row = 0; row < field.getHeight(); row++) {
			for (int col = 0; col < field.getWidth(); col++) {
				if (rand.nextDouble() <= LYNX_CREATION_PROBABILITY) {
					Lynx lynx = new Lynx(true);
					lynx.setLocation(col, row);
					lynxes.add(lynx);
					field.put(lynx, col, row);
				} else if (rand.nextDouble() <= CREATION_PROBABILITY) {
					Animal animal = new Animal(true);
					animal.setLocation(col, row);
					animals.add(animal);
					field.put(animal, col, row);
			}
		}
		Collections.shuffle(animals);
		Collections.shuffle(lynxes);}
	}

	private boolean isViable() {
		return stats.isViable(field);
	}

	public Field getField() {
		return this.field;
	}

	// Draw field if we have a gui defined
	public void drawField() {
		if ((graphicsWindow != null) && (view != null)) {
			view.drawField(this.field);
		}
	}

	public void drawGraph() {
		graph.draw();
	}

	public void writeToFile(String writefile) {
		try {
			Record r = new Record(animals, this.field, this.step);
			FileOutputStream outStream = new FileOutputStream(writefile);
			ObjectOutputStream objectOutputFile = new ObjectOutputStream(outStream);
			objectOutputFile.writeObject(r);
			objectOutputFile.close();
		} catch (Exception e) {
			System.out.println("Something went wrong: " + e.getMessage());
		}
	}

	public void readFile(String readfile) {
		try {
			FileInputStream inputStream = new FileInputStream(readfile);
			ObjectInputStream objectInputFile = new ObjectInputStream(inputStream);
			Record r = (Record) objectInputFile.readObject();
			setAnimals(r.getAnimals());
			setField(r.getField());
			setStep(r.getSteps());
			objectInputFile.close();
			// clear field
		} catch (Exception e) {
			System.out.println("Something went wrong: " + e.getMessage());
		}
	}


	private void setField(Field field2) {
		// TODO Auto-generated method stub
		
	}

	private void setStep(int steps) {
		// TODO Auto-generated method stub
		
	}

	private void setAnimals(List<Animal> animals2) {
		animals = animals2;
	}

	private void setLynxes(List<Lynx> lynxes2) {
		lynxes = lynxes2;
	}

	// Perform an action when the mouse was clicked.
	// parameters are the x, y screen coordinates the user clicked on.
	// Note: you probably want to modify handleMouseClick(Location) which
	// gives you the location they clicked on in the grid.
	public void handleMouseClick(float mouseX, float mouseY) {
		Location loc = view.gridLocationAt(mouseX, mouseY); // get grid at
		// click.

		for (int x = loc.getCol() - 8; x < loc.getCol() + 8; x++) {
			for (int y = loc.getRow() - 8; y < loc.getRow() + 8; y++) {
				Location locToCheck = new Location(x, y);
				if (field.isInGrid(locToCheck)) {
					Object animal = field.getObjectAt(locToCheck);
					if (animal instanceof Animal)
						animals.remove((Animal) animal);
					if (animal instanceof Lynx)
						lynxes.remove((Lynx) animal);
					field.put(null, locToCheck);
					updatedField.put(null, locToCheck);
				}
			}
		}
	}

	private void handleMouseClick(Location l) {
		System.out.println("Change handleMouseClick in Simulator.java to do something!");
	}

	public void handleMouseDrag(int mouseX, int mouseY) {
		Location loc = this.view.gridLocationAt(mouseX, mouseY); // get grid at
		// click.
		if (loc == null)
			return; // if off the screen, exit
		handleMouseDrag(loc);
	}

	private void handleMouseDrag(Location l) {
		System.out.println("Change handleMouseDrag in Simulator.java to do something!");
	}
}