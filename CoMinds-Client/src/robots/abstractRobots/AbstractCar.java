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
package robots.abstractRobots;

import observers.ButtonMonitor;
import observers.ButtonObserver;
import robots.abstractRobots.properties.BotProperties;
import robots.abstractRobots.properties.TrackyProperties;
import behaviourmodel.StoppableBehavior;
import behaviourmodel.BehaviorUtils;
import actuatorwrappers.ExtendedDifferentialPilot;
import lejos.nxt.Button;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Arbitrator;
import logging.Logger;
import logging.Logger.LogLevel;


/**
 * This is an abstract car. For that car a pilot will be initialized (according
 * to the parameters of a shooter bot). In the method {@link #startBehavior()}
 * the behavior of this car will be started and button listener will be added
 * (to stop the behavior). To give this car a special behavior, just implement
 * the method {@link #initializesBehaviors()} and use some special
 * initializations in the Constructor. For starting a car only the method
 * {@link #main(AbstractCar)} must be called.
 * 
 * @author Annabelle Klarl
 */
public abstract class AbstractCar {

	protected static final Logger logger = Logger.getLogger("log_car.txt",
			LogLevel.DEBUG, 500, true);

	protected final BotProperties properties;
	protected final DifferentialPilot pilot;
	protected final ButtonMonitor monitor = new ButtonMonitor();

	/**
	 * Constructor that initializes the car with the parameters of a shooter bot
	 */
	public AbstractCar() {
		this(new TrackyProperties());
	}

	/**
	 * Constructor that initializes the car with the parameters set int the bot
	 * properties
	 * 
	 * @param properties
	 *            the properties of this car
	 */
	public AbstractCar(BotProperties properties) {
		this(properties, false);
	}

	/**
	 * Constructor that initializes the car with the parameters set int the bot
	 * properties
	 * 
	 * @param properties
	 *            the properties of this car
	 * @param reverse
	 *            whether the driving direction should be reversed
	 */
	public AbstractCar(BotProperties properties, boolean reverse) {
		logger.info("car started");
		System.out.println("car start");

		this.properties = properties;

		// initialize pilot
		this.pilot = new ExtendedDifferentialPilot(properties
				.getWheelDiameter(), properties.getTrackWidth(), properties
				.getLeftMotor(), properties.getRightMotor(), reverse,
				properties.getScaleFactor());
		// for pilot.travel()
		// -> motorspeed = Math.round(0.5f * travelSpeed *
		// (_leftDegPerDistance + _rightDegPerDistance))
		// motorspeed for pilot.forward(), backward(), steer(), arc()
		this.pilot.setTravelSpeed(properties.getTravelSpeed());
		// for pilot.rotate()
		this.pilot.setRotateSpeed(properties.getRotateSpeed());
		this.pilot.setAcceleration(properties.getAcceleration());

		// start listening for buttons
		this.monitor.start();
	}

	/**
	 * starts the behavior for that car
	 */
	public void startBehavior() {
		// initializes behaviors
		final StoppableBehavior[] behaviors = this.initializeBehaviors();

		Arbitrator arbitrator = new Arbitrator(behaviors, true);

		this.monitor.register(new ButtonObserver() {
			@Override
			public void notifyForEvent(Button button) {
				if (button.equals(Button.ESCAPE)) {
					BehaviorUtils.stopAllBehaviors(behaviors);
				}
			}
		}, Button.ESCAPE);

		logger.info("behav started");
		arbitrator.start();
	}

	/**
	 * initializes the behaviors of this car
	 * 
	 * @return the behaviors for this car
	 */
	protected abstract StoppableBehavior[] initializeBehaviors();

	/**
	 * stops the car. It therefore stops the logging
	 */
	public void stop() {
		try {
			// stop logging
			logger.stopLogging();
		}
		catch (Throwable e) {
			System.out.println("AbstractCar-Exc " + e.getClass());
		}
	}

	/**
	 * initializes the car, starts and stops the behavior (if ESCAPE is
	 * pressed). This is the method to be used by subclasses.
	 * 
	 * @param car
	 */
	public static void main(AbstractCar car) {
		try {
			car.startBehavior();
		}
		catch (Throwable e) {
			System.out.println("AbstractCar-Exc " + e.getClass());
		}
		finally {
			// stopping connection and logging
			System.out.println("stopping...");
			car.stop();
		}
	}

}
