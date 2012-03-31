package org.colombbus.tangara.objects;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;

import org.apache.log4j.Logger;
import org.colombbus.build.Localize;
import org.colombbus.tangara.FileUtils;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TObject;

import com.microcrowd.loader.java3d.max3ds.Loader3DS;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

@Localize(value = "Object3D", localizeParent = true)
public class Object3D extends TObject {

	private static final Logger LOG = Logger.getLogger(Object3D.class);


	/*
	 * should be changed by any child object that does not support expansion functions
	 * (addObject and loadFile).
	 */
	protected boolean isExpandable = true;
	protected boolean initLastEncounteredObjects = false;

	public BranchGroup innerObject;
	public BranchGroup voidCollisionBounds;
	public BranchGroup collisionBounds;

	/**
	 * The created geometry will be added in this transform
	 * group and this is this transform group that will be
	 * added to the Space.
	 */
	protected TransformGroup tg;


	/** The transform group's transformation */
	protected Transform3D t3D;

	/** The transformation's translation */
	protected Vector3d translation;

	/** The Object3D's destination */
	protected Vector3d destination;

	private Object3D parentObject = null;

	private ArrayList<Object3D> children = null;


	/**
	 * The transformation's rotation. The rotation is initialized
	 * to 0,1,0 in order to restricts the rotation to the Y axis.
	 * The rotation axis can be changed using
	 * anObject3D.setRotationAxis("X") , ("Y") or ("Z").
	 *
	 */
	protected AxisAngle4d rotation;

	/**
	 * Scale of the object.
	 */
	protected double scale = 1;

	/**
	 * The space in which the transform group will be added.
	 * The space is initialized when the method
	 * aSpace.addObject(anObject3D) is used.
	 */
	protected Space3D space;

	/**
	 * A boolean that indicates whether this Object3D
	 * is followed by the camera or not. It is initialized
	 * to false but becomes true when the method
	 * aSpace.followObject(anObject3D) is used, and
	 * becomes false when aSpace.stopFollowObject() is used.
	 */
	protected boolean isFollowed = false;

	/**
	 * When an Object3D is created, using this class
	 * or one of its children classes, it's not yet
	 * visible cause it has not been added to the
	 * space yet. The isVisible attribute of anObject3D
	 * becomes true when the method aSpace.addElement(anObject3D)
	 * is used, and becomes false when you use
	 * aSpace.removeObject(anObject3D).
	 */
	private boolean isVisible = false;

	/**
	 * A boolean that indicates whether the commands
	 * associated to this Object3D (for example
	 * the command to execute when a collision append)
	 * have to be displayed on the screen or not when
	 * they are executed.
	 */
	protected boolean displayEvents = true;

	/**
	 * A boolean that indicates whether this Object3D
	 * is transparent or not. It's transparent when the
	 * wall hiding manager have been activated and if this
	 * Object3D is between the camera and the Object3D
	 * followed by the camera.
	 */
	protected boolean isHidden = false;

	/**
	 * A boolean that indicates whether the collision area
	 * of this Object3D have to be shown or not.
	 */
	protected boolean displayCollisionArea = false;

	/**
	 * A boolean that indicates whether this Object3D is
	 * "collidable" or not. If it is "collidable", his movement
	 * will be stopped when a collision with an other "collidable"
	 * Object3D occurs.
	 */
	boolean isSolid = false;

	/**
	 * A boolean that indicates whether this Object3D
	 * has collision actions or not. If it has collision
	 * action, they will be executed when a collision occurs.
	 */
	protected boolean testCollisions = false;

	/**
	 * A boolean that indicates whether this Object3D
	 * has collision actions or not. If it has collision
	 * action, they will be executed when a collision occurs.
	 */
	protected boolean testCollisionsWith = false;

	/**
	 * Four booleans indicating if this Object3D
	 * always move forward, backward, left or right.
	 * If true, the corresponding method is called
	 * in the step() method. Those boolean can be set
	 * using the following methods :
	 * - anObject3D.alwaysMoveForward(boolean)
	 * - anObject3D.alwaysMoveBackward(boolean)
	 * - anObject3D.alwaysMoveLeft(boolean)
	 * - anObject3D.alwaysMoveRight(boolean)
	 */


	protected ArrayList<Object3D> lastEncounteredObjects = new ArrayList<Object3D>();

	protected static final int NO_DIRECTION = -1;
	protected static final int STOP = 0;
	protected static final int FORWARD = 1;
	protected static final int BACKWARD = 2;
	protected static final int LEFT = 3;
	protected static final int RIGHT = 4;
	protected static final int UP = 5;
	protected static final int DOWN = 6;

	protected int direction = STOP;
	/**
	 * The speed of the movement of this Object3D.
	 * When an Object3D always moves forward, backward,
	 * left, right or follows or escapes an other one,
	 * it moves with this speed. It can be modified with
	 * the method anObject3D.setMovingSpeed(speed).
	 */
	protected double speedStep = 0.1;

	/**
	 * Default extension for 3D object files
	 */
	private static final String[] DEFAULT_EXTENSIONS = {"obj", "3ds"}; //$NON-NLS-1$


	/**
	 * List of collision events for "if collision with"
	 */
	private final Map<Object3D,String> specificCollisionEvents = new HashMap<Object3D,String>();

	protected boolean lightingEnabled = false;

	protected Bounds ownBounds = null;

	protected Object moveSync = new Object();

	protected boolean isTextured = false;
	protected Texture texture;
	protected Color currentColor;
	protected double currentTransparency;


	/**
	 * The only constructor of this class.
	 * <br/>
	 * It's called by its children classes when you create
	 * a Sphere or a Box for example.
	 * <br/>
	 * When you use it directly, you create a group and other
	 * Object3D can be added to this one.
	 */
	@Localize(value = "Object3D")
	public Object3D() {
		super();
		// Initialization
		innerObject = new BranchGroup();
		voidCollisionBounds = new BranchGroup();
		collisionBounds = new BranchGroup();
		tg = new TransformGroup();
		t3D = new Transform3D();
		translation = new Vector3d();
		destination = new Vector3d(0,0,0);
		rotation = new AxisAngle4d(0, 1, 0, 0);

		// We set some capability to this BranchGroup
		innerObject.setCapability(BranchGroup.ALLOW_DETACH);
		innerObject.setCapability(Group.ALLOW_COLLISION_BOUNDS_WRITE);

		// We set some capability to the transform group.
		tg.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.setCapability(Group.ALLOW_CHILDREN_WRITE);


		// The child with index zero is reserved for collision bounds
		// we have to know the index of the collision bounds, 0,
		// because we use tg.setChild(collisionBounds,0) in the
		// setBoundingElement() method.
		collisionBounds.setCapability(BranchGroup.ALLOW_DETACH);
		voidCollisionBounds.setCapability(BranchGroup.ALLOW_DETACH);

		tg.addChild(voidCollisionBounds);

		// We add this transform group to the branch group.
		innerObject.addChild(tg);
		registerEvent("collision");
	}

	/**
	 * If this Object3D is a group, which means it was
	 * created using its own constructor, other Object3D
	 * can be added to this one.
	 * <br/>
	 * If this Object3D is not a group which means it was created
	 * with one of its subclasses, this method does nothing.
	 *
	 * @param obj - The Object3D to be added to this group.
	 */
	@Localize(value = "Object3D.addObject")
	public void addObject(Object3D obj) {
		if (!isExpandable) {
            Program.instance().writeMessage(getMessage("error.notSupported.addObject"));
			return;
		}
		// We add the specified Object3D to the transform group.
		tg.addChild(obj.getInnerObject());
		obj.setParent(this);
		if (children == null) {
			children = new ArrayList<Object3D>();
		}
		children.add(obj);
		// As this group contains a new Object3D, its collision
		// bounds have to be recombined.
		computeCollisionBounds();
	}

	public BranchGroup getInnerObject() {
		return innerObject;
	}

	protected void computeCollisionBounds() {
		if (displayCollisionArea) {
			hideBoundingObject();
		}
		collisionBounds.removeAllChildren();
		if (ownBounds != null) {
			// Bounds have been specified for this object: we use them
			if (isFollowed && space !=null ) {
				Bounds followedBounds = space.getCameraBounds();
				followedBounds.combine(ownBounds);
				innerObject.setCollisionBounds(followedBounds);
				addBoundingObject(followedBounds);
			} else {
				innerObject.setCollisionBounds(ownBounds);
				addBoundingObject(ownBounds);
			}
		} else if (children != null) {
			// No bounds specified and object has children: we use bounds of children objects
			Bounds bounds = null;
			for (Object3D childObject:children) {
				Bounds childBounds = childObject.getCollisionBounds();
				if (childBounds != null) {
					if (bounds == null) {
						innerObject.setCollisionBounds(childBounds);
						bounds = innerObject.getCollisionBounds();
					} else {
						bounds.combine(childBounds);
					}
				}
			}
			if (isFollowed && space !=null ) {
				Bounds followedBounds = space.getCameraBounds();
				bounds.combine(followedBounds);
			}
			innerObject.setCollisionBounds(bounds);
			addBoundingObject(bounds);
		}
		if (displayCollisionArea) {
			displayBoundingObject();
		}
		updateParentBounds();
	}

	/**
	 * A getter used by Space to know if this Object3D is followed
	 * by the camera or not.
	 *
	 * @return <code>true</code> if this Object3D is followed by the camera,
	 * else <code>false</code>.
	 */
	boolean isFollowed() {
		return isFollowed;
	}

	/**
	 * Sets the isFollowed boolean with the specified boolean b.
	 * <br/>
	 * This method is called by the Space when the method
	 * space.followElement(anObject3D) is called.
	 *
	 * @param b - <code>true</code> if this Object3D is followed by the camera,
	 * else <code>false</code>.
	 */
	public void setFollowed(boolean b) {
		isFollowed = b;
		// update object bounds to take camera bounds into account
		computeCollisionBounds();
	}

	/**
	 * Sets the isSolid value with the specified
	 * boolean b.
	 * <br/>
	 * Determines whether this Object3D can be considered
	 * for collision purposes.
	 *
	 * @param b - <code>true</code> if this Object3D have
	 * to be considered for collision purposes, else false;
	 */
	@Localize(value = "Object3D.setSolid")
	public void setSolid(boolean b) {
		this.isSolid = b;
	}

	/**
	 * Sets the displayCollisionArea value with the specified
	 * boolean b.
	 * <br/>
	 * Determines whether the collision area of this Object3D
	 * has to be shown.
	 *
	 * @param b - <code>true</code> if the collision area of
	 * this Object3D has to be shown, else false;
	 */
	@Localize(value = "Object3D.displayCollisionArea")
	public void displayCollisionArea(boolean b) {
		if (displayCollisionArea != b) {
			displayCollisionArea = b;
			// If the collision area has to be shown,
			// it has to be computed first.
			if(displayCollisionArea)
				displayBoundingObject();
			else
				hideBoundingObject();
		}
	}

	/**
	 * Translates this Object3D with the specified arguments.
	 * <br/>
	 * If the translation succeed, <code>false</code> is returned.
	 * <br/>
	 * <code>true</code> is returned if a collision occurs.
	 *
	 * @param x - The moving value on the X axis.
	 * @param y - The moving value on the Y axis.
	 * @param z - The moving value on the Z axis.
	 *
	 * @return <code>true</code> if the translation succeed,
	 * <code>false</code> if a collision occurred.
	 */
	@Localize(value = "Object3D.translate")
	public void translate(double x, double y, double z) {
		setPosition(translation.getX() + x, translation.getY() + y, translation.getZ() + z);
	}

	protected boolean innerTranslate(double x, double y, double z) {
		if (isVisible && (isSolid|testCollisions|testCollisionsWith) && space != null) {
			if (initLastEncounteredObjects) {
				initLastEncounteredObjects = false;
				lastEncounteredObjects.clear();
			}
			boolean solidObjectEncountered = space.willCollide(this, x, y, z, lastEncounteredObjects);
			if (isSolid && solidObjectEncountered)
				return false;
		}
		synchronized (moveSync) {
			// direction may have changed during collision processing, hence we test it again
			if (direction != STOP) {
				translation.set(translation.getX() + x, translation.getY() + y, translation.getZ() + z);
				updatePosition();
			}
		}
		return true;
	}

	/**
	 * Translate this Object3D in the direction it's "looking".
	 *
	 * @param distance - the value of the moving.
	 */
	@Localize(value = "Object3D.moveForward")
	public void moveForward(double distance) {
		synchronized (moveSync) {
			direction = NO_DIRECTION;
			destination.add(new Point3d(-distance * Math.sin(rotation.angle), 0, -distance * Math.cos(rotation.angle)));
		}
	}

	/**
	 * Translate this Object3D in the opposed direction it's "looking".
	 *
	 * @param distance - the value of the moving.
	 */
	@Localize(value = "Object3D.moveBackward")
	public void moveBackward(double distance) {
		synchronized (moveSync) {
			direction = NO_DIRECTION;
			destination.add(new Point3d(distance * Math.sin(rotation.angle), 0, distance * Math.cos(rotation.angle)));
		}
	}

	/**
	 * Translate this Object3D in the left of the direction it's "looking".
	 *
	 * @param distance - the value of the moving.
	 */
	@Localize(value = "Object3D.moveLeft")
	public void moveLeft(double distance) {
		synchronized (moveSync) {
			direction = NO_DIRECTION;
			destination.add(new Point3d(-distance * Math.cos(rotation.angle), 0, distance * Math.sin(rotation.angle)));
		}
	}

	/**
	 * Translate this Object3D in the right of the direction it's "looking".
	 *
	 * @param distance - the value of the moving.
	 */
	@Localize(value = "Object3D.moveRight")
	public void moveRight(double distance) {
		synchronized (moveSync) {
			direction = NO_DIRECTION;
			destination.add(new Point3d(distance * Math.cos(rotation.angle), 0, -distance * Math.sin(rotation.angle)));
		}
	}

	/**
	 * Translate this Object3D in the "up" direction
	 *
	 * @param distance - the value of the moving.
	 */
	@Localize(value = "Object3D.moveUp")
	public void moveUp(double distance) {
		synchronized (moveSync) {
			direction = NO_DIRECTION;
			destination.add(new Point3d(0, distance,0));
		}
	}

	/**
	 * Translate this Object3D in the "down" direction
	 *
	 * @param distance - the value of the moving.
	 */
	@Localize(value = "Object3D.moveDown")
	public void moveDown(double distance) {
		synchronized (moveSync) {
			direction = NO_DIRECTION;
			destination.add(new Point3d(0,-distance,0));
		}
	}

	/**
	 * Sets the position of this Object3D with the
	 * specified position.
	 * <br/>
	 * This method does the same thing than
	 * setPosition(p.x,p.y,p.z).
	 *
	 * @param p - The new position of this Object3D.
	 */
	@Localize(value = "Object3D.setPosition2")
	public void setPosition(Point3D p) {
		setPosition(p.getX(), p.getY(), p.getZ());
	}

	/**
	 * Sets the position of this Object3D with the
	 * specified position.
	 * <br/>
	 * This method does the same thing than
	 * setPosition(p.x,p.y,p.z).
	 *
	 * @param p - The new position of this Object3D.
	 */
	public void setPosition(Point3d p) {
		setPosition(p.x, p.y, p.z);
	}


	/**
	 * Sets the position of this Object3D with the
	 * specified position.
	 *
	 * @param x - The new value of the position on the X axis.
	 * @param y - The new value of the position on the Y axis.
	 * @param z - The new value of the position on the Z axis.
	 */
	@Localize(value = "Object3D.setPosition")
	public void setPosition(double x, double y, double z) {
		synchronized (moveSync) {
			direction = STOP;
			destination = new Vector3d(x,y,z);
			translation.set(x, y, z);
			updatePosition();
		}
	}

	/**
	 * Rotates this Object3D with the specified angle.
	 *
	 * @param angle - The angle to rotate this Object3D.
	 */
	@Localize(value = "Object3D.rotate")
	public void rotate(double angle) {
		double angleRadians = Math.toRadians(angle);
		setRadianAngle(rotation.angle - angleRadians);
	}

	/**
	 * Sets the angle of this Object3D with the
	 * specified angle.
	 *
	 * @param angle - The new angle of this Object3D.
	 */
	@Localize(value = "Object3D.setAngle")
	public void setAngle(double angle) {
		setRadianAngle(Math.toRadians(angle));
	}

	public void setRadianAngle(double angle) {
		synchronized (moveSync) {
			rotation.setAngle(angle);
			updatePosition();
		}
	}

	/**
	 * Returns the angle of this Object3D.
	 *
	 * @return - The angle of this Object3D.
	 */
	public double getRotationAngle() {
		return rotation.angle;
	}

	/**
	 * Sets the scale of the Object3D with the
	 * specified value.
	 *
	 * @param scaleValue - The new scale of this Object3D.
	 */
	@Localize(value = "Object3D.setScale")
	public void setScale(double scaleValue) {
		scale = scaleValue;
		updatePosition();
	}

	/**
	 * Called when an Object3D is moved.
	 * <br/>
	 * This method checks if this Object3D's parent is
	 * a group. If it is, its parent collision bounds is
	 * updated.
	 */
	protected void updateParentBounds() {
		if (parentObject != null)
			parentObject.computeCollisionBounds();
	}

	public Object3D getParent() {
		return parentObject;
	}

	public void setParent(Object3D obj) {
		parentObject = obj;
	}

	/**
	 * Sets the rotation axis of this Object3D with the
	 * specified axis, "X", "Y" or "Z".
	 *
	 * @param axis - The new rotation axis of this Object3D.
	 */
	@Localize(value = "Object3D.setRotationAxis")
	public void setRotationAxis(String axis) {
		if (axis.length()!=1) {
	        Program.instance().writeMessage(getMessage("error.wrongAxis"));
	        return;
		}

		switch (axis.charAt(0)) {
		case 'X':
		case 'x':
			rotation.set(1, 0, 0, rotation.angle);
			break;
		case 'Y':
		case 'y':
			rotation.set(0, 1, 0, rotation.angle);
			break;
		case 'z':
		case 'Z':
			rotation.set(0, 0, 1, rotation.angle);
			break;
		default:
	        Program.instance().writeMessage(getMessage("error.wrongAxis"));
		}
	}

	/**
	 * Sets the rotation axis of this Object3D with the
	 * specified values.
	 *
	 * @param x - The proportion of the "X" axis in the
	 * new rotation axis.
	 * @param y - The proportion of the "Y" axis in the
	 * new rotation axis.
	 * @param z - The proportion of the "Z" axis in the
	 * new rotation axis.
	 */
	@Localize(value = "Object3D.setRotationAxis")
	public void setRotationAxis(double x, double y, double z) {
		synchronized (moveSync) {
			rotation.set(x, y, z, rotation.angle);
			updatePosition();
		}
	}

	/**
	 * Returns the collision bounds of this Object3D, used to check
	 * for collisions.
	 *
	 * @return - The collision bounds of this Object3D.
	 */
	public Bounds getCollisionBounds() {
		Bounds b = innerObject.getCollisionBounds();
		if (b == null)
			return null;
		b.transform(t3D);
		return b;
	}

	/**
	 * Checks if this Object3D's collision bounds intersects
	 * with the specified bounds.
	 *
	 * @param b - the bounds to check collision with.
	 * @return <code>true</code> if the specified bounds intersects
	 * with this Object3D's collision bounds, else <code>false</code>.
	 */
	public boolean intersects(Bounds b) {
		Bounds myBounds = getCollisionBounds();
		if (myBounds == null)
			return false;
		return myBounds.intersect(b);
	}

	protected void addBoundingObject(Bounds bounds) {
		if (bounds instanceof BoundingBox) {
			addBoundingBoxObject((BoundingBox)bounds);
		} else if (bounds instanceof BoundingSphere) {
			addBoundingSphereObject((BoundingSphere)bounds);
		}
	}

	protected void addBoundingBoxObject(BoundingBox bounds) {
		Point3d p1 = new Point3d();
		Point3d p2 = new Point3d();
		bounds.getLower(p1);
		bounds.getUpper(p2);
		Box box = new Box((float) Math.abs(p1.x - p2.x)/2, (float) Math.abs(p1.y- p2.y)/2, (float) Math.abs(p1.z - p2.z)/2,Primitive.GENERATE_NORMALS, Texture.getColorAppearance(Color.RED, 0.2));
		TransformGroup boundingTransform = new TransformGroup();
		Transform3D t = new Transform3D();
		Vector3d v = new Vector3d((p1.x + p2.x)/2, (p1.y + p2.y)/2, (p1.z + p2.z)/2);
		t.setTranslation(v);
		boundingTransform.setTransform(t);
		boundingTransform.addChild(box);
		collisionBounds.addChild(boundingTransform);
	}

	protected void addBoundingSphereObject(BoundingSphere bounds) {
		Sphere sphere = new Sphere((float) bounds.getRadius(),Texture.getColorAppearance(Color.RED, 0.2));
		TransformGroup boundingTransform = new TransformGroup();
		Transform3D t = new Transform3D();
		Point3d center = new Point3d();
		bounds.getCenter(center);
		t.setTranslation(new Vector3d(center.getX(),center.getY(),center.getZ()));
		boundingTransform.setTransform(t);
		boundingTransform.addChild(sphere);
		collisionBounds.addChild(boundingTransform);
	}


	/**
	 * Calculates and creates the bounding object that represents the
	 * collision bounds of this Object3D.
	 */
	public void displayBoundingObject() {
		tg.setChild(collisionBounds, 0);
	}

	public void hideBoundingObject() {
		tg.setChild(voidCollisionBounds, 0);
	}

	/**
	 * This method is called when this Object3D is added to a Space.
	 * @param space - The Space in which this Object3D was added.
	 */
	public void setSpace(Space3D space) {
		this.space = space;
	}

	/**
	 * Returns the position of this Object3D.
	 *
	 * @return - the position of this Object3D.
	 */
	public Point3d getPosition() {
		return new Point3d(translation.x, translation.y, translation.z);
	}

	/**
	 * This method is called by the Space's movement timer.
	 */
	public synchronized void step() {
		double stepX = 0;
		double stepY = 0;
		double stepZ = 0;
		double speedStepCopy;
		double angleCopy;
		int directionCopy;
		Vector3d destinationCopy;
		Vector3d translationCopy;
		boolean movement = true;
		// make copies
		synchronized (moveSync) {
			speedStepCopy = speedStep;
			angleCopy = rotation.angle;
			directionCopy = direction;
			destinationCopy = new Vector3d(destination);
			translationCopy = new Vector3d(translation);
		}
		switch (directionCopy) {
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
					if ((difference.length())<speedStepCopy) {
						stepX = destinationCopy.getX()-translationCopy.getX();
						stepY = destinationCopy.getY()-translationCopy.getY();
						stepZ = destinationCopy.getZ()-translationCopy.getZ();
					} else {
						difference.normalize();
						difference.scale(speedStepCopy);
						stepX = difference.getX();
						stepY = difference.getY();
						stepZ = difference.getZ();
					}
				} else {
					stop();
					movement = false;
				}
				break;
		}
		if (movement) {
			if (!innerTranslate(stepX,stepY,stepZ)) {
				stop();
			}
		}
	}

	@Localize(value="Object3D.ifCollision")
    public void ifCollision(String command) {
    	addHandler("collision",command); //$NON-NLS-1$
    	testCollisions = true;
    }


	/**
	 * Determines the command to executes when this Object3D collides
	 * with the specified Object3D.
	 *
	 * @param e - The Object3D that this Object3D has to collide with
	 * to execute the specified command.
	 * @param command - The command that is executed when this Object3D
	 * collides with the specified Object3D.
	 */
	@Localize(value = "Object3D.ifCollisionWith")
	public void ifCollisionWith(Object3D obj, String command) {
	    String eventCode;
	    if (specificCollisionEvents.containsKey(obj))
	    {
	        eventCode = specificCollisionEvents.get(obj);
	        addHandler(eventCode, command);
	    }
	    else
	    {
	        eventCode = "collision_"+UUID.randomUUID(); //$NON-NLS-1$
	        specificCollisionEvents.put(obj,eventCode);
	        registerEvent(eventCode);
            addHandler(eventCode, command);
	    }
	    testCollisionsWith = true;
	}

	/**
	 * Indicates that a collision between this Object3D and the specified
	 * Object3D append.
	 * <br/>
	 * If a command was attached to this collision, it is then executed.
	 *
	 * @param element
	 */
	public void collisionHappened(Object3D o) {
	    if (testCollisionsWith)
	    {
	        if (specificCollisionEvents.containsKey(o))
	        {
                HashMap <String,String> info = new HashMap<String,String>();
                info.put("x", Double.toString(translation.getX()));
                info.put("y", Double.toString(translation.getY()));
                info.put("z", Double.toString(translation.getZ()));
                info.put("lastCollision", Program.instance().getObjectName(o));
                processEvent(specificCollisionEvents.get(o),info);
	        }
	    }
		if (testCollisions)
		{
			HashMap <String,String> info = new HashMap<String,String>();
            info.put("x", Double.toString(translation.getX()));
            info.put("y", Double.toString(translation.getY()));
            info.put("z", Double.toString(translation.getZ()));
			info.put("lastCollision", Program.instance().getObjectName(o));
			processEvent("collision",info);
		}
	}

	/**
	 * Determines whether the commands attached to this Object3D have
	 * to be displayed when executed.
	 *
	 * @param b - <code>true</code> if the commands have to be displayed,
	 * else <code>false</code>.
	 */
	@Localize(value = "Object3D.displayCommands")
	public void displayCommands(boolean b) {
		displayEvents = b;
	}


	/**
	 * Sets the moving speed of this Object3D.
	 *
	 * @param speedStep - The new moving speed of this Object3D.
	 */
	@Localize(value = "Object3D.setSpeed")
	public void setSpeed(int newSpeed) {
		synchronized(moveSync) {
			speedStep = (double) newSpeed / 100;
		}
	}

	/**
	 * Hides this Object3D by removing it from the Space.
	 */
	@Localize(value = "Object3D.hide")
	public void hide() {
		if(space != null)
			space.removeObject(this);
		setVisible(false);
	}

	/**
	 * Shows this Object3D by adding it to the Space.
	 */
	@Localize(value = "Object3D.show")
	public void show() {
		if(space != null)
			space.addObject(this);
		setVisible(true);
	}

	/**
	 * Sets the visibility of this Object3D.
	 *
	 * @param b - <code>true</code> if this Object3D is visible,
	 * else <code>false</code>.
	 */
	public void setVisible(boolean b) {
		isVisible = b;
	}

	/**
	 * Returns the visibility or this Object3D.
	 *
	 * @return <code>true</code> if this Object3D is visible,
	 * else <code>false</code>.
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Returns the solid characteristics or this Object3D.
	 *
	 * @return <code>true</code> if this Object3D is solid,
	 * else <code>false</code>.
	 */
	public boolean isSolid() {
		return isSolid;
	}

	/**
	 * Sets the collision area of this Object3D.
	 * <br/>
	 * As there are 3 arguments, the collision area will
	 * be a box.
	 *
	 * @param x - The dimension of the new bounding box on the "X" axis.
	 * @param y - The dimension of the new bounding box on the "Y" axis.
	 * @param z - The dimension of the new bounding box on the "Z" axis.
	 */
	@Localize(value = "Object3D.setCollisionArea")
	public void setCollisionArea(double x, double y, double z) {
		setBounds(new BoundingBox(new Point3d(-x/2, -y/2, -z/2), new Point3d(x/2, y/2, z/2)));
	}

	/**
	 * Sets the collision area of this Object3D.
	 * <br/>
	 * As there is just one argument, the collision area will
	 * be a sphere.
	 *
	 * @param radius - The radius of the new bounding sphere.
	 */
	@Localize(value = "Object3D.setCollisionArea2")
	public void setCollisionArea(double radius) {
		setBounds(new BoundingSphere(new Point3d(), radius));
	}

	/**
	 * Makes this Object3D to always
	 * move forward.
	 *
	 */
	@Localize(value = "Object3D.alwaysMoveForward")
	public void alwaysMoveForward() {
		synchronized(moveSync) {
			direction = FORWARD;
		}
	}

	/**
	 * Makes this Object3D to always
	 * move backward.
	 *
	 */
	@Localize(value = "Object3D.alwaysMoveBackward")
	public void alwaysMoveBackward() {
		synchronized(moveSync) {
			direction = BACKWARD;
		}
	}

	/**
	 * Makes this Object3D to always
	 * move left.
	 *
	 */
	@Localize(value = "Object3D.alwaysMoveLeft")
	public void alwaysMoveLeft() {
		synchronized(moveSync) {
			direction = LEFT;
		}
	}

	/**
	 * Makes this Object3D to always
	 * move right.
	 *
	 */
	@Localize(value = "Object3D.alwaysMoveRight")
	public void alwaysMoveRight() {
		synchronized(moveSync) {
			direction = RIGHT;
		}
	}

	/**
	 * Makes this Object3D to always
	 * move up.
	 *
	 */
	@Localize(value = "Object3D.alwaysMoveUp")
	public void alwaysMoveUp() {
		synchronized(moveSync) {
			direction = UP;
		}
	}

	/**
	 * Makes this Object3D to always
	 * move up.
	 *
	 */
	@Localize(value = "Object3D.alwaysMoveDown")
	public void alwaysMoveDown() {
		synchronized(moveSync) {
			direction = DOWN;
		}
	}

	/**
	 * Makes this Object3D to stop
	 *
	 */
	@Localize(value = "Object3D.stop")
	public void stop() {
		synchronized(moveSync) {
			if (direction != STOP) {
				direction = STOP;
				destination.set(translation);
				initLastEncounteredObjects = true;
			}
		}
	}

	public void setBounds(Bounds object) {
		ownBounds = object;
		computeCollisionBounds();
	}

	@Localize(value="Object3D.loadFile")
	public void loadFile(String fileName) {
		loadFile(fileName, 0, 1);
	}

	@Localize(value="Object3D.loadFile2")
	public void loadFile(String name, double angle, double scale) {
		if (!isExpandable) {
            Program.instance().writeMessage(getMessage("error.notSupported.loadFile"));
			return;
		}
		TransformGroup transformGroup = new TransformGroup();
		AxisAngle4d rotation = new AxisAngle4d(0, 1, 0, Math.toRadians(angle));
		Transform3D t = new Transform3D();
		t.setRotation(rotation);
		t.setScale(scale);
		transformGroup.setTransform(t);
		try {
			String fileName = getCompleteFileName(name);
			if (fileName.endsWith(".obj")) { //$NON-NLS-1$
				Scene s;
				ObjectFile f = new ObjectFile();
				f.setFlags(ObjectFile.RESIZE | ObjectFile.TRIANGULATE
						| ObjectFile.STRIPIFY);
				try {
					s = f.load(fileName);
					transformGroup.addChild(s.getSceneGroup());
					tg.removeAllChildren();
					tg.addChild(transformGroup);
				} catch (FileNotFoundException e) {
					LOG.error("Error loading file (file not found)",e);
					throw new Exception();
				} catch (IncorrectFormatException e) {
					LOG.error("Error loading file (incorrect format)",e);
					throw new Exception();
				} catch (ParsingErrorException e) {
					LOG.error("Error loading file (parsing error)",e);
					throw new Exception();
				}
			}
			else if (fileName.endsWith(".3ds")) { //$NON-NLS-1$
				Loader3DS loader3DS = new Loader3DS();
				Scene s;
				try {
					s = loader3DS.load(fileName);
					transformGroup.addChild(s.getSceneGroup());
					tg.addChild(transformGroup);
				} catch (FileNotFoundException e) {
					LOG.error("Error loading file (file not found)",e);
					throw new Exception();
				}
			}
		}
		catch (Exception e) {
            String message = MessageFormat.format(getMessage("error.load")+" ("+e.getMessage()+")", name);
            Program.instance().writeMessage(message);
		}
	}

	public void enableLighting() {
		if (!lightingEnabled) {
			lightingEnabled = true;
			if(isTextured)
				setTexture(texture);
			else
				setColor(currentColor, currentTransparency);
		}
	}

	public void disableLighting() {
		if (lightingEnabled) {
			lightingEnabled = false;
			if(isTextured)
				setTexture(texture);
			else
				setColor(currentColor, currentTransparency);
		}
	}

	protected String getCompleteFileName(String fileName) throws Exception {
		File file = FileUtils.findFile(fileName, DEFAULT_EXTENSIONS);
		if (file == null)
			throw new Exception("file not found"); //$NON-NLS-1$
		return file.getAbsolutePath();
	}

	@Override
	public void deleteObject() {
		if (space != null) {
			space.removeObject(this);
		}
		super.deleteObject();
	}

	protected void updatePosition() {
		t3D.setIdentity();
		t3D.setScale(scale);
		t3D.setRotation(rotation);
		t3D.setTranslation(translation);
		tg.setTransform(t3D);
		updateParentBounds();
		// If this Object3D is followed by the camera, it has
		// to be updated too.
		if (isFollowed) {
			space.updateCamera();
		}
	}

	@Localize(value="Object3D.setColor")
	public void setColor(String colorName) {
		setColor(colorName, 0);
	}

	@Localize(value="Object3D.setColor2")
	public void setColor(String colorName, double transparency) {
		setColor(TColor.translateColor(colorName, Color.WHITE), transparency);
	}

	protected void setColor(Color color) {
		setColor(color, 0);
	}

	protected void setColor(Color color, double transparency) {
		isTextured = false;
		currentColor = color;
		currentTransparency = transparency;
		if (isExpandable&&children!=null) {
			for (Object3D child:children) {
				child.setColor(color, transparency);
			}
		}
	}

	@Localize(value="Object3D.setTexture")
	public void setTexture(Texture imageTexture) {
		texture = imageTexture;
		isTextured = true;
		if (isExpandable&&children!=null) {
			for (Object3D child:children) {
				child.setTexture(imageTexture);
			}
		}
	}

	@Localize(value="Object3D.removeTexture")
	public void removeTexture() {
		texture = null;
		isTextured = false;
		if (isExpandable&&children!=null) {
			for (Object3D child:children) {
				child.removeTexture();
			}
		}
		setColor(currentColor, currentTransparency);
	}



}