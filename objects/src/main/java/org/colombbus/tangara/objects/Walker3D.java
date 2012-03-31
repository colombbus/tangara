package org.colombbus.tangara.objects;

import java.util.ArrayList;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;


@Localize(value = "Walker3D", localizeParent = true)
public class Walker3D extends Object3D {


	/**
	 * A boolean that indicates if this Object3D
	 * can fall or not. If it can fall, the method
	 * fall() in step() is called.
	 */
	private boolean mayFall = false;
	
	/**
	 * A boolean that indicates whether this Object3D
	 * is jumping or not. This boolean becomes true when
	 * the jump() method is called and becomes false when
	 * the jump is finished.
	 */
	private boolean jumping = false;
	
	/**
	 * A boolean that indicates whether this Object3D
	 * is falling or not. When it's falling, the method
	 * jump() does nothing.
	 */
	private boolean falling = false;
	
	/**
	 * The falling speed when the fallingStep is 20.
	 * This attribute can be modified using
	 * setFallSpeed(speed).
	 */
	private double fallSpeed = 0.1;

	protected static final double MAX_STEP = 0.05;
	
	/**
	 * The height of a jump. It can be modified
	 * using setJumpHeight(height).
	 */
	private double jumpHeight = 1;
	
	/**
	 * A double array describing the trajectory
	 * of a jump. This trajectory is calculated
	 * before a jump.
	 */
	private double[] jumpTrajectory;
	
	/**
	 * Two attributes used to manage a jump.
	 */
	private int jumpStep = 0;
	private int totalJumpStep = 0;
	
	/**
	 * This attributes represents the Object3D that
	 * is followed or escaped by this Object3D.
	 */
	private Object3D linkedObject;
	
	/**
	 * When an Object3D collides with an other, and if
	 * slideIfCollision is true, it will slide down
	 * this second Object3D. This attributes becomes
	 * true when the method anObject3D.slideIfCollision(true)
	 * is used.
	 */
	private boolean slideIfCollision = false;
	
	protected boolean verticalMove = false;
	protected boolean slideMove = false;
	
	protected boolean groundFound = false;
	
	protected double groundPosition;
	
	protected static final int FOLLOWING = 7;
	protected static final int ESCAPING = 8;
	
	
	protected ArrayList<Object3D> lastEncounteredVerticalObjects = new ArrayList<Object3D>();
	
	@Localize(value = "Walker3D")
	public Walker3D() {
		super();
		isSolid = true;
	}
	
	/**
	 * Sets the canFall boolean with the specified boolean b.
	 * 
	 * @param b
	 */
	@Localize(value = "Walker3D.mayFall")
	public void mayFall(boolean b) {
		synchronized(moveSync) {
			mayFall = b;
			// As this Walker3D can fall now, we have
			// to set the jump trajectory. (Because a
			// Walker3D can jump only if it can fall).
			setJumpTrajectory();
		}
	}

	/**
	 * This method is first called when an Walker3D.setCanFall(true)
	 * is used, and every time the fall speed or is jump height are
	 * changed, because the jump trajectory changes in those occasions.
	 */
	private void setJumpTrajectory() {
		int numberOfStep = (int) (jumpHeight / fallSpeed);
		if (numberOfStep % 2 == 0)
			numberOfStep++;
		totalJumpStep = numberOfStep;
		jumpTrajectory = new double[numberOfStep];
		double x = -1.0;
		double previousX = 0.0;
		jumpTrajectory[0] = 0;
		// The trajectory is parabolic.
		double st = 2.0 / (numberOfStep - 1);
		for (int i = 1; i < numberOfStep; i++) {
			x = x + st;
			jumpTrajectory[i] = jumpHeight * (1 - x * x) - previousX;
			previousX = jumpHeight * (1 - x * x);
		}
	}

	/**
	 * Sets the value of this Walker3D's fall speed.
	 * 
	 * @param speed - The new speed of this Walker3D's fall speed.
	 */
	@Localize(value = "Walker3D.setFallSpeed")
	public void setFallSpeed(int speed) {
		synchronized(moveSync) {
			this.fallSpeed = (double) speed / 500;
			// As the fall speed has changed,
			// the jump trajectory changes too.
			setJumpTrajectory();
		}
	}

	/**
	 * Makes this Walker3D make a jump.
	 * <br/>
	 * It'll work only if this Walker3D is not
	 * already jumping, can fall but is not falling.
	 */
	@Localize(value = "Walker3D.jump")
	public void jump() {
		synchronized(moveSync) {
			if (mayFall && !jumping && !falling) {
				jumping = true;
				jumpStep = 0;
			}
		}
	}

	protected void performJump() {
		jumpStep++;
		boolean result = verticalMove(jumpTrajectory[jumpStep]);
		if ((!result)||(jumpStep == totalJumpStep - 1)) {
			jumping = false;
		}
	}
	
	/**
	 * Sets the jump height of this Walker3D.
	 * <br/>
	 * This method does nothing if <code>height</code> is < 0.
	 * 
	 * @param height - The new jump height of this Object3D,
	 * it has to be >= 0.
	 */
	@Localize(value = "Walker3D.setJumpHeight")
	public void setJumpHeight(double height) {
		synchronized(moveSync) {
			if(height < 0) {
	            Program.instance().writeMessage(getMessage("error.wrongHeightValue"));
				return;
			}
			jumpHeight = height;
			setJumpTrajectory();
		}
	}

	/**
	 * This method is called by the Space's movement timer.
	 */
	@Override
	public synchronized void step() {
		double stepX = 0;
		double stepY = 0;
		double stepZ = 0;
		double speedStepCopy;
		double angleCopy;
		int directionCopy;
		Vector3d destinationCopy;
		Vector3d translationCopy;
		Point3d ownPosition = null;
		Point3d othersPosition = null;
		boolean movement = true;
		boolean mayFallCopy = false;
		// make copies
		synchronized (moveSync) {
			speedStepCopy = speedStep;
			angleCopy = rotation.getAngle();
			directionCopy = direction;
			destinationCopy = new Vector3d(destination);
			translationCopy = new Vector3d(translation);
			if ((direction == FOLLOWING)||(direction == ESCAPING)) {
				ownPosition = getPosition();
				othersPosition = linkedObject.getPosition();
			}
			mayFallCopy = mayFall;
		}
		double slideStep = speedStepCopy/2;
		switch (directionCopy) {
			case FOLLOWING :
				double xdif = othersPosition.getX() - ownPosition.getX();
				double zdif = othersPosition.getZ() - ownPosition.getZ();
				synchronized (moveSync) {
					angleCopy = Math.PI+Math.atan2(xdif, zdif);
					rotation.setAngle(angleCopy);
				}
				stepX = - speedStepCopy * Math.sin(angleCopy);
				stepZ = - speedStepCopy * Math.cos(angleCopy);
				break;
			case ESCAPING :
				double xdif2 = othersPosition.getX() - ownPosition.getX();
				double zdif2 = othersPosition.getZ() - ownPosition.getZ();
				synchronized (moveSync) {
					angleCopy = Math.atan2(xdif2, zdif2);
					rotation.setAngle(angleCopy);
				}
				stepX = - speedStepCopy * Math.sin(angleCopy);
				stepZ = - speedStepCopy * Math.cos(angleCopy);
				break;
			case STOP :
				movement = false;
				break;
			case FORWARD :
				stepX = - speedStepCopy * Math.sin(angleCopy);
				stepZ = - speedStepCopy * Math.cos(angleCopy);
				break;
			case BACKWARD :
				stepX = speedStepCopy * Math.sin(angleCopy);
				stepZ = speedStepCopy * Math.cos(angleCopy);
				break;
			case LEFT :
				stepX = - speedStepCopy * Math.cos(angleCopy);
				stepZ = speedStepCopy * Math.sin(angleCopy);
				break;
			case RIGHT :
				stepX = speedStepCopy * Math.cos(angleCopy);
				stepZ = - speedStepCopy * Math.sin(angleCopy);
				break;
			case UP :
				stepY = speedStepCopy;
				break;
			case DOWN :
				stepY = - speedStepCopy;
				break;
			case NO_DIRECTION :
				if (!translationCopy.equals(destinationCopy)) {
					Vector3d difference = new Vector3d(destinationCopy);
					difference.sub(translationCopy);
					if ((difference.length())>speedStepCopy) {
						difference.normalize();
						difference.scale(speedStepCopy);
					} else {
						slideStep = difference.length();
					}
					stepX = difference.getX();
					if (!mayFallCopy)
						stepY = difference.getY();
					stepZ = difference.getZ();
				} else {
					// We have reached destination
					stop();
					movement = false;
				}
				break;
		}
		if (movement) {
			if (!innerTranslate(stepX,stepY,stepZ)) {
				// try to move with a little height (MAX_STEP)
				if (!slideMove(stepX, stepY+ MAX_STEP, stepZ)) {
					if (slideIfCollision) {
						// try to find a way to move anyway
						if (!slide(slideStep, stepX, stepZ)) {
							if ((directionCopy != FOLLOWING)&&(directionCopy != ESCAPING))
								stop();
							else
								initLastEncounteredObjects = true;
						}
					} else {
						if ((directionCopy != FOLLOWING)&&(directionCopy != ESCAPING))
							stop();
						else
							initLastEncounteredObjects = true;
					}
				}
			}
		}
		if (jumping)
			performJump();
		else if (mayFall)
			fall();
	}

	/**
	 * Perform the next falling step.
	 */
	protected void fall() {
		groundPosition = -10000;
		falling = verticalMove(-fallSpeed);
		if (falling) {
			groundFound = false;
		}
		synchronized(moveSync) {
			if (!falling && !groundFound && groundPosition > -1000 && translation.getY()!= groundPosition) {
				translation.set(translation.getX(), groundPosition, translation.getZ());
				updatePosition();
				groundFound = true;
			}
		}
	}

	/**
	 * A method called by the fall() and performJump methods.
	 * 
	 * @param y - The value of the moving.
	 * @return <code>true</code> if no collision occurred,
	 * else <code>false</code>.
	 */
	protected synchronized boolean verticalMove(double y) {
		if (space != null) {
			if (initLastEncounteredObjects) {
				initLastEncounteredObjects = false;
				lastEncounteredObjects.clear();
			}
			lastEncounteredVerticalObjects.addAll(lastEncounteredObjects);
			verticalMove = true;
			boolean solidObjectEncountered = space.willCollide(this, 0, y , 0,  lastEncounteredVerticalObjects, true);
			verticalMove = false;
			if (solidObjectEncountered) {
				return false;
			}
		}
		synchronized (moveSync) {
			translation.set(translation.getX(), translation.getY() + y, translation.getZ());
			updatePosition();
		}
		return true;
	}

	protected synchronized boolean slideMove(double x, double y, double z) {
		if (space != null) {
			if (initLastEncounteredObjects) {
				initLastEncounteredObjects = false;
				lastEncounteredObjects.clear();
			}
			slideMove = true;
			boolean solidObjectEncountered = space.willCollide(this, x, y , z,  lastEncounteredObjects, true);
			slideMove = false;
			if (solidObjectEncountered) {
				return false;
			}
		}
		synchronized (moveSync) {
			// direction may have changed during collision processing, hence we test it again
			if (direction != STOP) {
				translation.set(translation.getX() + x, translation.getY() +y, translation.getZ() +z);
				updatePosition();
			}
		}
		return true;
	}
	
	/**
	 * Indicates that this Walker3D has to follow
	 * the specified Walker3D.
	 * 
	 * @param e - The Walker3D to follow.
	 */
	@Localize(value = "Walker3D.followObject")
	public void followObject(Object3D o) {
		synchronized (moveSync) {
			direction = FOLLOWING;
			linkedObject = o;
		}
	}

	/**
	 * Indicates that this Walker3D has to escape
	 * from the specified Walker3D.
	 * 
	 * @param e - The Walker3D to escape from.
	 */
	@Localize(value = "Walker3D.escapeObject")
	public void escapeObject(Object3D o) {
		synchronized (moveSync) {
			direction = ESCAPING;
			linkedObject = o;
		}
	}

	/**
	 * Indicates whether this Walker3D has to slide
	 * when a collision occurs.
	 * 
	 * @param b - <code>true</code> if this Object3D has
	 * to slide when a collision occurs. Else <code>false</code>.
	 */
	@Localize(value = "Walker3D.slideIfCollision")
	public void slideIfCollision(boolean b) {
		synchronized (moveSync) {
			slideIfCollision = b;
		}
	}
	
	/**
	 * This method is called when a collision occurs and this Object3D
	 * is set to slide when a collision occurs.
	 * <br/>
	 * This method will try to translate the Object3D down the Z axis.
	 * If there is still a collision, it'll try to translate it down the
	 * X axis.
	 * 
	 * @param step - The value of the moving.
	 * @param angle - The angle of the Object3D's direction
	 * before the collision.
	 */
	private synchronized boolean slide(double step, double x, double z) {
		// We check in which direction the Object3D was going.
		//lastEncounteredSlideObjects.addAll(lastEncounteredObjects);

		// upper west
		if(x < 0 && z < 0) {
			return (slideMove(0, 0, -step) || slideMove(-step, 0, 0));
		}
		
		// lower west
		else if(x < 0 && z > 0) {
			return (slideMove(0, 0, step) || slideMove(-step, 0, 0));
		}
		
		// lower east
		else if(x > 0 && z > 0) {
			return (slideMove(0, 0, step)||slideMove(step, 0, 0));
		}
		
		// upper east
		else {
			return (slideMove(0, 0, -step) || slideMove(step, 0, 0));
		}
	}
	
	@Override
	public void collisionHappened(Object3D o) {
		if (verticalMove) {
			if (!lastEncounteredVerticalObjects.contains(o)) {
				super.collisionHappened(o);
			}
			if (o.isSolid()) {
				if (!jumping&&!groundFound) {
					Bounds encounteredBounds = o.getCollisionBounds();
					Bounds myBounds = innerObject.getCollisionBounds();
					Point3d p1 = new Point3d();
					Point3d p2 = new Point3d();
					double yDiff = 0;
					double newGroundPosition = groundPosition;
					if (myBounds instanceof BoundingBox) {
						((BoundingBox)myBounds).getUpper(p1);
						((BoundingBox)myBounds).getLower(p2);
						yDiff = p2.y;
					} else if (myBounds instanceof BoundingSphere) {
						((BoundingSphere)myBounds).getCenter(p1);
						yDiff = p1.getY()-((BoundingSphere)myBounds).getRadius();
					} else {
					}
					if (encounteredBounds instanceof BoundingBox) {
						((BoundingBox)encounteredBounds).getUpper(p1);
						newGroundPosition = p1.y-yDiff;
					} else if (encounteredBounds instanceof BoundingSphere) {
						((BoundingSphere)encounteredBounds).getCenter(p1);
						newGroundPosition = p1.y+((BoundingSphere)encounteredBounds).getRadius()-yDiff;
					}
					if ((newGroundPosition>groundPosition)&&(newGroundPosition<translation.getY()))
						// add 0.01 in order to be sure that the walker won't be stopped by the ground next time
						groundPosition = newGroundPosition+0.01;
				}
			}
		} else if (slideMove){
			if (!lastEncounteredObjects.contains(o)) {
				super.collisionHappened(o);
			}
		} else {
			super.collisionHappened(o);
		}
	}
	
	@Override
	@Localize(value = "Object3D.setSolid")
	public void setSolid(boolean b) {
		this.isSolid = true;
        Program.instance().writeMessage(getMessage("error.setSolid"));
	}
	
}