/*
 *  CoMinds is a Java framework for collaborative robotic scenarios with
 *  Lego Mindstorms based leJOS. A complete documentation can be found
 *  in docs/thesis.pdf.
 *
 *  Copyright (C) 2010  Annabelle Klarl
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 */
package robots.linefollower.pid;

import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.ColorSensor.Color;
import logging.Logger;
import logging.Logger.LogLevel;
import robots.abstractRobots.AbstractCar;
import robots.abstractRobots.properties.BotProperties;
import sensorwrappers.color.OwnColorSensorWrapper;
import behaviourmodel.StoppableBehavior;
import behaviourmodel.linefollower.FindLineBehavior;
import behaviourmodel.linefollower.PIDBehavior;

/**
 * A robot with this program will follow a black line on the floor and can be
 * stopped by pressing ESCAPE. It uses therefore Behavior and Pilot classes. It
 * requires tracked vehicle with two independently controlled tracks with motors
 * connected to motor ports A and C and a color light sensor mounted forwards
 * and pointing down connected to sensor port 2. Or it can handle a trike robot.
 * Its motors will run backward. Press ENTER to start the robot. This
 * LineFollower is controlled via a PID-Controller.
 * 
 * @author Annabelle Klarl
 */
public class PIDLineFollower extends AbstractCar {

	protected static final Logger logger = Logger.getLogger("log_car.txt",
			LogLevel.DEBUG, 500, true);

	// default values for white and black
	public static final float DEFAULT_WHITE = 50f;
	public static final float DEFAULT_BLACK = 20f;

	protected static final SensorPort COLOR_LIGHT_SENSOR = SensorPort.S2;
	private final ColorSensor light;
	private final PIDProperties pidProperties;

	/**
	 * Constructor that only initializes a color light sensor and the pilot with
	 * the parameters of a shooter bot
	 */
	public PIDLineFollower() {
		this(new TrackyPIDProperties(), new TrackyPIDProperties());
	}

	/**
	 * Constructor that only initializes a color light sensor and the pilot
	 * 
	 * @param properties
	 *            the properties of this car
	 * @param pidProperties
	 *            the pid properties (KI, KD, KP and corresponding travel speed
	 *            -> will override the speed property in properties)
	 */
	public PIDLineFollower(BotProperties properties, PIDProperties pidProperties) {
		super(properties);
		this.light = new ColorSensor(COLOR_LIGHT_SENSOR, Color.BLUE);
		this.pidProperties = pidProperties;
		this.pilot.setTravelSpeed(pidProperties.getTravelSpeed());
	}

	/**
	 * initializes the line following und searching for the line. It will also
	 * calibrate the black and white values
	 * 
	 * @return an array of StoppableBehaviors which are initialized
	 */
	@Override
	protected StoppableBehavior[] initializeBehaviors() {
		OwnColorSensorWrapper lightWrapper = OwnColorSensorWrapper
				.initLightWrapper(this.light, this.monitor);

		final PIDBehavior pid = new PIDBehavior(this.pilot, this.properties
				.getTrackWidth(), lightWrapper, this.pidProperties
				.getNormalizedLineLightValue(), this.pidProperties.getKP(),
				this.pidProperties.getKI(), this.pidProperties.getKD());
		final FindLineBehavior findLine = new FindLineBehavior(this.pilot,
				lightWrapper, this.pidProperties.getNormalizedLineLightValue(),
				this.pidProperties.getNormalizedOfflineLightValue(),
				this.pidProperties.getSweep(), this.pidProperties
						.getTravelDistance());

		StoppableBehavior[] behaviors = { pid, findLine };
		return behaviors;
	}

	@Override
	public void stop() {
		this.light.setFloodlight(false);
		super.stop();
	}

	/**
	 * turns off the floodlight
	 * 
	 * @param floodlight
	 */
	public void setFloodlight(boolean floodlight) {
		this.light.setFloodlight(floodlight);
	}

	/**
	 * Main routine for setting up a PIDLineFollower
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TrackyPIDProperties props = new TrackyPIDProperties();
		TrikePIDProperties props = new TrikePIDProperties();
		main(new PIDLineFollower(props, props));
	}
}
