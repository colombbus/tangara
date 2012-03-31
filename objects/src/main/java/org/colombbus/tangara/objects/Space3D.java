package org.colombbus.tangara.objects;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.swing.Timer;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TGraphicalObject;

import com.sun.j3d.exp.swing.JCanvas3D;
import com.sun.j3d.utils.universe.SimpleUniverse;

@SuppressWarnings("serial")
@Localize(value = "Space3D", localizeParent = true)
public class Space3D extends TGraphicalObject {

	private JCanvas3D c3D = null;

	private SimpleUniverse universe;

	private static final double cameraBoundsRadius = 0.35;
	
	/**
	 * The main group, where all the created elements are added,
	 * it also contains the FPS behavior and the FPS text.
	 */
	private BranchGroup mainGroup;

	/**
	 * The camera has an absolute position and a relative position,
	 * when it follows an object, the absolute position is equal to
	 * the position of this object plus the camera relative position,
	 * else, the absolute position is equal to the relative position.
	 */

	/**
	 * The camera absolute attributes. The camera rotation is initialized
	 * to 0,1,0 in order to restricts the rotation to the Y axis.
	 * The forth argument is the angle, 0 at the beginning.
	 */
	private double cameraAbsoluteAngle = 0;
	private AxisAngle4d cameraRotation;
	private Point3d cameraAbsolutePosition;
	private Vector3d cameraTranslation;

	/** The camera relative attributes. */
	private Point3d cameraRelativePosition;
	private double cameraRelativeAngle = 0;

	/**
	 * The camera transform, containing the camera absolute translation
	 * and the camera absolute rotation. This attribute is updated each
	 * time the updateCamera method is called.
     */
	private Transform3D cameraTransform;
	
	/** A 3D text displaying the frame per second. */
	private Text3D fpsText;
	
	/** A group containing axes */
	private BranchGroup axes;
	
	/**
	 * In order to always have this text in the upper left corner of
	 * screen, its position is equal to the position of the camera
	 * plus a little gap, updated simultaneous with the camera
	 * position in the updateCamera method.
	 */
	private Point3d fpsPosition;
	
	/**
	 * FpsBehavior is a private class extending javax.media.j3d.Behavior
	 * that calculates the frame per second. As Behavior extends
	 * javax.media.j3d.Leaf, it has to be added to the mainGroup like
	 * all the nodes.
	 */
	private FpsBehavior fps;
	
	/** A boolean that indicates whether the fpsText has to be shown or not. */
	private boolean showFPS = false;

	/** A boolean that indicates whether the axis are displayed or not. */
	private boolean showAxes = false;
	
	/**
	 * We can indicate to the camera to follow an object, this object
	 * becomes then the followedObject.
	 */
	private Object3D followedObject;
	
	/**
	 * The movementTimer makes all the Object3D make their movement,
	 * like jumping or following an another Object3D, as they don't have
	 * their own timer.
	 */
	private Timer movementTimer;
	
	/**
	 * A list that contains all the Object3D added to the Space.
	 * It's used in the movementTimer and the wallTimer.
	 * It's also used in the willCollide method checking collision
	 * between an Object3D and all the others.
	 */
	private List<Object3D> objects3D = new ArrayList<Object3D>();

	/**
	 * A list that contains all the Lights added to the Space.
	 */
	private List<Light3D> lights = new ArrayList<Light3D>();
	
	/**
	 * Those two variables are used in the mouse listeners.
	 */
	private int startX;
	private float previousAngle;
	
	/*
	 * Axes
	 */
	Segment3D xAxis, yAxis, zAxis;

	
	protected boolean lightingEnabled = false;
	
	protected boolean shouldBeRestarted = false; 

	@Localize(value = "Space3D")
	public Space3D() {
		super();
		c3D = new JCanvas3D();
		// The Canvas3D has Dimension to be set for it to work,
		// even if it takes the JPanel dimension afterwards.
		//c3D.setSize(new Dimension(100, 100));
		c3D.setResizeMode(JCanvas3D.RESIZE_IMMEDIATELY);

		setLayout(new BorderLayout());
		Dimension dim = new Dimension(300,200);
		add(c3D, BorderLayout.CENTER);
		c3D.setSize(dim);
		setSize(dim);

		// A SimpleUniverse is created and associated
		// to the Canvas3D.
		Canvas3D c = c3D.getOffscreenCanvas3D();
		universe = new SimpleUniverse(c);

		universe.getViewingPlatform().setNominalViewingTransform();

		c.getView().setDepthBufferFreezeTransparent(false);

		mainGroup = new BranchGroup();
		axes  = new BranchGroup();
		cameraTransform = new Transform3D();
		cameraRotation = new AxisAngle4d(0, 1, 0, 0);
		cameraAbsolutePosition = new Point3d();
		cameraTranslation = new Vector3d();
		cameraRelativePosition = new Point3d();
		fpsPosition = new Point3d();
		
		// We set some capability to the Main Group.
		mainGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		mainGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		mainGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		// fpsText is initialized.
		fpsText = new Text3D("FPS : ", 0.01); //$NON-NLS-1$

		// The FpsBehavior is created and added to the Main Group.
		fps = new FpsBehavior();
		fps.setSchedulingBounds(new BoundingSphere());
		mainGroup.addChild(fps);

		// Axes
		buildAxes();
		axes.setCapability(BranchGroup.ALLOW_DETACH);

		//mainGroup.compile();
		
		// The Main Group is added to the SimpleUniverse.
		universe.addBranchGraph(mainGroup);
		// Init timer
		initTimer();

		updateFPSLocation();

		// The mouseListener and the mouseMotionListener are used to rotate the followed Object3D
		// if there is one, to rotate the camera else.
		c3D.addMouseListener(new MouseAdapter() {
			/**
			 * Once the mouse is pressed, we have to keep the initial angle and the initial
			 * location on screen until the mouse is released.
			 */
			@Override
			public void mousePressed(MouseEvent e) {
				startX = e.getLocationOnScreen().x;
				// If an Object3D is followed, this Object3D's angle is saved,
				// else the camera's angle is saved.
				if (followedObject == null)
					previousAngle = (float) cameraRotation.angle;
				else
					previousAngle = (float) followedObject.getRotationAngle();
			}
		});

		c3D.addMouseMotionListener(new MouseMotionAdapter() {
			/**
			 * When the mouse is dragged, we update the camera's or the followed
			 * Object3D's angle using the saved informations.
			 */
			@Override
			public void mouseDragged(MouseEvent e) {
				int newX = e.getLocationOnScreen().x;
				if (followedObject == null)
					setCameraRadianAngle(previousAngle + (float) (newX - startX) / 100);
				else 
					followedObject.setRadianAngle(previousAngle + (float) (newX - startX) / 100);
			}
		});
		displayObject();
	}
	
	
	
	private void buildAxes() {
		Point3d origin = new Point3d(0,0,0);
		xAxis = new Segment3D(origin, new Point3d(5,0,0), Color.red);
		yAxis = new Segment3D(origin, new Point3d(0,5,0), Color.green);
		zAxis = new Segment3D(origin, new Point3d(0,0,5), Color.blue);
		axes.addChild(xAxis.getInnerObject());
		axes.addChild(yAxis.getInnerObject());
		axes.addChild(zAxis.getInnerObject());
	}

	private void initTimer() {
		// The movementTimer calls each Object3D's step method
		// if it's visible.
		movementTimer = new Timer(20, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<Object3D> currentList;
				synchronized(objects3D)
				{			
					currentList = new ArrayList<Object3D>(objects3D);
				}
				for (Object3D object : currentList) {
					if (object.isVisible())
						object.step();
				}
			}
		});
		movementTimer.start();
	}
	
	
	/**
	 * Adds an Object3D to the mainGroup.
	 * <br/>
	 * If the Object3D is already in the mainGroup,
	 * this method does nothing.
	 * 
	 * @param object - The Object3D to be added.
	 */
	@Localize(value = "Space3D.addObject")
	public void addObject(Object3D object) {
		synchronized(objects3D) {
			if (objects3D.contains(object)) 
				return;
			mainGroup.addChild(object.getInnerObject());
			object.setVisible(true);
			object.setSpace(this);
			objects3D.add(object);
			if (lightingEnabled) {
				object.enableLighting();
			}
		}
	}
	
	/**
	 * Adds a Light3D to the mainGroup.
	 * <br/>
	 * If the Light3D is already in the mainGroup,
	 * this method does nothing.
	 * 
	 * @param bg - The Light3D to be added.
	 */
	@Localize(value = "Space3D.addLight")
	public void addLight(Light3D light) {
		synchronized(lights) {
			if (lights.contains(light)) 
				return;
			mainGroup.addChild(light.getInnerObject());
			light.setSpace(this);
			lights.add(light);
			if (!lightingEnabled) {
				enableLighting();
			}
		}
	}

	
	/**
	 * Removes an Object3D from the mainGroup.
	 * <br/>
	 * If the Object3D is not in the mainGroup,
	 * this method does nothing.
	 * 
	 * @param bg - The Object3D to be removed.
	 */
	@Localize(value = "Space3D.removeObject")
	public void removeObject(Object3D bg) {
		synchronized(objects3D) {
			if(!objects3D.contains(bg)) return;
			mainGroup.removeChild(bg.getInnerObject());
			objects3D.remove(bg);
			bg.setVisible(false);
		}
	}

	
	/**
	 * Removes a Light3D from the mainGroup.
	 * <br/>
	 * 
	 * @param light - The Light3D to be removed.
	 */
	@Localize(value = "Space3D.removeLight")
	public void removeLight(Light3D light) {
		removeLight(light, true);
	}

	public void removeLight(Light3D light, boolean permanent) {
		synchronized(lights) {
			if(!lights.contains(light))
				return;
			mainGroup.removeChild(light.getInnerObject());
			lights.remove(light);
			if (permanent && lights.isEmpty()) {
				disableLighting();
			}
		}
	}

	
	/**
	 * Enable or disable the display of the FPS.
	 * 
	 * @param b - <code>true</code> to enable, <code>false</code> to disable.
	 */
	protected void displayFPS(boolean b) {
		if (b && !showFPS)
			mainGroup.addChild(fpsText.getInnerObject());
		else if (!b && showFPS)
			mainGroup.removeChild(fpsText.getInnerObject());
		showFPS = b;
	}
	
	@Localize(value = "Space3D.showFPS")
	public void showFPS() {
		displayFPS(true);
	}
	
	@Localize(value = "Space3D.hideFPS")
	public void hideFPS() {
		displayFPS(false);
	}
	
	protected void displayAxes(boolean b) {
		if (b && !showAxes)
			mainGroup.addChild(axes);
		else if (!b && showAxes)
			mainGroup.removeChild(axes);
		showAxes = b;
	}

	@Localize(value = "Space3D.showAxes")
	public void showAxes() {
		displayAxes(true);
	}
	
	@Localize(value = "Space3D.hideAxes")
	public void hideAxes() {
		displayAxes(false);
	}
	
	/**
	 * Enable or disable the moving of the Object3Ds
	 * 
	 * @param b - <code>true</code> to enable, <code>false</code> to disable.
	 */
	protected void enableMoving(boolean b) {
		if (b)
			movementTimer.start();
		else
			movementTimer.stop();
	}

	/**
	 * Enable the moving of the Object3Ds
	 * 
	 */
	@Localize(value = "Space3D.start")
	public void start() {
		enableMoving(true);
	}

	/**
	 * Disable the moving of the Object3Ds
	 * 
	 */
	@Localize(value = "Space3D.stop")
	public void stop() {
		enableMoving(false);
	}

	
	/**
	 * Checks if an Object3D collide with one of the
	 * mainGroup's Object3Ds, excepting itself, when
	 * it's moved with the given arguments.
	 * 
	 * @param obj - The Object3D to check.
	 * @param x - The moving value on the X axis.
	 * @param y - The moving value on the Y axis.
	 * @param z - The moving value on the Z axis.
	 * 
	 * @return <code>true</code> if the Object3D will collide
	 * <code>false</code> otherwise.
	 */
	public boolean willCollide(Object3D obj, double x, double y, double z, ArrayList<Object3D> ignoredObjects) {
		return willCollide(obj, x, y, z, ignoredObjects, false);
	}
	
	public boolean willCollide(Object3D obj, double x, double y, double z, ArrayList<Object3D> ignoredObjects, boolean forceNotify) {
		Bounds b = obj.getCollisionBounds();
		// If the Object3D has no CollisionBounds, the method return false.
		if (b == null) return false;
		// We transform the CollisionBounds with the Object3D's transformation.
		Vector3d v = new Vector3d(x, y, z);
		Transform3D t = new Transform3D();
		t.setTranslation(v);
		b.transform(t);
		ArrayList<Object3D> foundObjects = new ArrayList<Object3D>();
		boolean solidObjectEncountered = false;
		ArrayList<Object3D> currentList;
		synchronized(objects3D)
		{			
			currentList = new ArrayList<Object3D>(objects3D);
		}
		for (Object3D object : currentList) {
			// If element isn't the tested Object3D and is visible we 
			// test if its bounds intersects with the tested Object3D's bounds :
			if (!object.equals(obj) && object.isVisible()) {
				if (object.intersects(b)) {
					// Add object to the foundObjects list
					foundObjects.add(object);
					// If object is not in ignored list
					// we informed the tested Object3D that a collision will happen and vice versa,
					if (!ignoredObjects.contains(object)) {
						obj.collisionHappened(object);
						object.collisionHappened(obj);
						if (!solidObjectEncountered)
							solidObjectEncountered = object.isSolid();
					} else if (forceNotify) {
						obj.collisionHappened(object);
						if (!solidObjectEncountered)
							solidObjectEncountered = object.isSolid();
					}
				}
			}
		}
		// Update ignoredObjects list
		ignoredObjects.clear();
		ignoredObjects.addAll(foundObjects);
		
		return solidObjectEncountered;
	}

	/**
	 * Indicates that the camera has to follow an Object3D.
	 * <br/>
	 * When this method is used, and if the camera has no
	 * relativePosition, its position will become the followed
	 * Object3D's position and it will not be visible.
	 * <br/>
	 * To see correctly the followed Object3D, use the <code>setCameraPosition</code>
	 * or the <code>translateCamera</code> method  in order to give
	 * the camera a relative position.
	 * <br/>
	 * If the main group doesn't contain this Object3D, or if this
	 * Object3D is not visible, or if it's already the
	 * followed Object3D, this method does nothing.
	 * 
	 * @param e - The Object3D the camera has to follow.
	 */
	@Localize(value = "Space3D.followObject")
	public void followObject(Object3D e) {
		// If the main group doesn't contain this Object3D, or if this
		// Object3D is not visible, or if it's already the followed
		// Object3D, we return.
		if (!objects3D.contains(e) || !e.isVisible() || e.equals(followedObject)) return;
		// If there is already a followed Object3D, we indicates that
		// it's not followed any more.
		if (followedObject != null) followedObject.setFollowed(false);
		// initialize camera position and rotation
		cameraRelativePosition.set(0,0,0);
		cameraRelativeAngle = 0;
		// We indicated that the Object3D is now followed.
		e.setFollowed(true);
		// We set the followed Object attributes.
		followedObject = e;
		// Finally we updates the cameraPosition.
		updateCamera();
	}

	/**
	 * Stops the camera following the followed Object3D.
	 * <br/>
	 * When this method is used, the relative attributes of
	 * the camera becomes its absolute attributes.
	 * <br/>
	 * If there is no followed Object3D, this method does nothing.
	 */
	@Localize(value = "Space3D.stopFollowObject")
	public void stopFollowObject() {
		// If there is no followed Object3D, we return.
		if (followedObject == null) return;
		// If there is one, we indicates that it's not
		// followed any more.
		followedObject.setFollowed(false);
		// We set the followedObject attribute as null.
		followedObject = null;
		
		cameraRelativePosition.set(cameraAbsolutePosition);
		cameraRelativeAngle = cameraRotation.getAngle();
		updateCamera();
	}

	/**
	 * Sets the camera's relative position to the given arguments.
	 * 
	 * @param x - The position on the X axis.
	 * @param y - The position on the Y axis.
	 * @param z - The position on the Z axis.
	 * 
	 * @see #setCameraPosition
	 * @see #translateCamera
	 */
	@Localize(value = "Space3D.setCameraPosition")
	public void setCameraPosition(double x, double y, double z) {
		cameraRelativePosition.set(x, y, z);
		updateCamera();
	}
	
	/**
	 * Sets the camera's relative position to the given argument.
	 * 
	 * @param p - The new relative position.
	 * 
	 * @see #setCameraPosition
	 * @see #translateCamera
	 */
	@Localize(value = "Space3D.setCameraPosition2")
	public void setCameraPosition(Point3D p) {
		setCameraPosition(p.getX(), p.getY(), p.getZ());
	}

	/**
	 * Translates the camera with the given arguments.
	 * 
	 * @param x - The moving value on the X axis.
	 * @param y - The moving value on the Y axis.
	 * @param z - The moving value on the Z axis.
	 */
	@Localize(value = "Space3D.translateCamera")
	public void translateCamera(double x, double y, double z) {
		cameraRelativePosition.add(new Vector3d(x,y,z));
		setCameraPosition(cameraRelativePosition.x, cameraRelativePosition.y, cameraRelativePosition.z);
	}

	/**
	 * Sets the camera relative angle to the given argument.
	 * 
	 * @param angle - The angle to set.
	 */
	@Localize(value = "Space3D.setCameraAngle")
	public void setCameraAngle(double angle) {
		setCameraRadianAngle(Math.toRadians(angle));
	}

	public void setCameraRadianAngle(double angle) {
		cameraRelativeAngle = angle;
		updateCamera();
	}
	
	
	/**
	 * Rotates the camera with the given argument.
	 * 
	 * @param angle - The rotation value.
	 */
	@Localize(value = "Space3D.rotateCamera")
	public void rotateCamera(double angle) {
		double angleRadians = Math.toRadians(angle);
		setCameraRadianAngle(cameraRelativeAngle + angleRadians);
	}

	/**
	 * Moves the camera forward using camera's absolute angle.
	 * 
	 * @param step - The distance to move.
	 */
	@Localize(value = "Space3D.moveCameraForward")
	public void moveCameraForward(double step) {
		translateCamera(-step * Math.sin(cameraAbsoluteAngle), 0, -step * Math.cos(cameraAbsoluteAngle));
	}

	/**
	 * Moves the camera backward using camera's absolute angle.
	 * 
	 * @param step - The distance to move.
	 */
	@Localize(value = "Space3D.moveCameraBackward")
	public void moveCameraBackward(double step) {
		translateCamera(step * Math.sin(cameraAbsoluteAngle),0,step * Math.cos(cameraAbsoluteAngle));
	}

	/**
	 * Moves the camera to the left using camera's absolute angle.
	 * 
	 * @param step - The distance to move.
	 */
	@Localize(value = "Space3D.moveCameraLeft")
	public void moveCameraLeft(double step) {
		translateCamera(-step * Math.cos(cameraAbsoluteAngle),0, step * Math.sin(cameraAbsoluteAngle));
	}

	/**
	 * Moves the camera to the right using camera's absolute angle.
	 * 
	 * @param step - The distance to move.
	 */
	@Localize(value = "Space3D.moveCameraRight")
	public void moveCameraRight(double step) {
		translateCamera(step * Math.cos(cameraAbsoluteAngle), 0, -step * Math.sin(cameraAbsoluteAngle));
	}


	/**
	 * Moves the camera up
	 * 
	 * @param step - The distance to move.
	 */
	@Localize(value = "Space3D.moveCameraUp")
	public void moveCameraUp(double step) {
		translateCamera(0, step, 0);
	}

	/**
	 * Moves the camera down
	 * 
	 * @param step - The distance to move.
	 */
	@Localize(value = "Space3D.moveCameraDown")
	public void moveCameraDown(double step) {
		translateCamera(0, -step, 0);
	}	
	
	/**
	 * Updates the absolute position of the camera and the position
	 * of the text that indicates the fps.
	 * <br/>
	 * This method is called every time the followed Object3D changed
	 * its position or its angle and every time the camera's relative
	 * attributes have been changed. 
	 */
	public void updateCamera() {
		// If there is not a followed Object3D, the camera's
		// relative attributes becomes its absolute attributes.
		if (followedObject == null) {
			cameraAbsolutePosition.set(cameraRelativePosition);
			cameraAbsoluteAngle = cameraRelativeAngle;
		}
		// If there is a followed Object3D, the absolute attributes
		// are equal to the relative attributes plus the followed
		// Object3D's attributes. The relative attributes are added
		// considering the angle of the followed Object3D.
		else {
			cameraAbsolutePosition.set(followedObject.getPosition());
			cameraAbsolutePosition.setX(cameraAbsolutePosition.x
					- cameraRelativePosition.x * Math.cos(followedObject.getRotationAngle())
					+ cameraRelativePosition.z * Math.sin(followedObject.getRotationAngle()));
			cameraAbsolutePosition.setY(cameraAbsolutePosition.y + cameraRelativePosition.y);
			cameraAbsolutePosition.setZ(cameraAbsolutePosition.z
					+ cameraRelativePosition.z * Math.cos(followedObject.getRotationAngle())
					+ cameraRelativePosition.x * Math.sin(followedObject.getRotationAngle()));
			cameraAbsoluteAngle = followedObject.getRotationAngle() + cameraRelativeAngle;
		}

		// Once we have the camera's absolute position and angle,
		// we do the transformations.
		setAbsoluteCameraPosition(cameraAbsolutePosition);
		setAbsoluteCameraAngle(cameraAbsoluteAngle);

		updateFPSLocation();
	}

	protected void updateFPSLocation() {
		// First, we set the position of the FPS text as
		// the camera absolute position,
		fpsPosition.set(cameraAbsolutePosition);
		// and then we add the gap, considering the
		// camera absolute angle.
		fpsPosition.setX(fpsPosition.x - 0.43 * Math.sin(cameraAbsoluteAngle) - 0.15 * Math.cos(cameraAbsoluteAngle));
		fpsPosition.setY(fpsPosition.y + 0.1);
		fpsPosition.setZ(fpsPosition.z - 0.43 * Math.cos(cameraAbsoluteAngle) + 0.15 * Math.sin(cameraAbsoluteAngle));
		fpsText.setPosition(fpsPosition);
		// The fps text's angle is the same than the camera's absolute angle.
		fpsText.setRadianAngle(cameraAbsoluteAngle);
	}
	
	/**
	 * Sets the camera's absolute position to the given arguments.
	 * <br/>
	 * This method is private because users cannot set the absolute
	 * attributes of the camera but just its relative attributes.
	 * 
	 * @param x - The position on the X axis.
	 * @param y - The position on the Y axis.
	 * @param z - The position on the Z axis.
	 * 
	 * @see #setCameraPosition
	 * @see #translateCamera
	 */
	private void setAbsoluteCameraPosition(double x, double y, double z) {
		cameraTranslation.set(x, y, z);
		cameraTransform.setTranslation(cameraTranslation);
		universe.getViewingPlatform().getViewPlatformTransform().setTransform(cameraTransform);
	}

	/**
	 * Sets the camera's absolute position to the given argument.
	 * <br/>
	 * This method is private because users cannot set the absolute
	 * attributes of the camera but just its relative attributes.
	 * 
	 * @param p - The new position of the camera.
	 * 
	 * @see #setCameraPosition
	 * @see #translateCamera
	 */
	private void setAbsoluteCameraPosition(Point3d p) {
		setAbsoluteCameraPosition(p.x, p.y, p.z);
	}

	/**
	 * Sets the camera's absolute angle to the given argument.
	 * <br/>
	 * This method is private because users cannot set the absolute
	 * attributes of the camera but just its relative attributes.
	 * 
	 * @param angle - The new angle of the camera.
	 * 
	 * @see #setCameraAngle
	 * @see #rotateCamera
	 */
	private void setAbsoluteCameraAngle(double angle) {
		cameraRotation.setAngle(angle);
		cameraTransform.setRotation(cameraRotation);
		universe.getViewingPlatform().getViewPlatformTransform().setTransform(cameraTransform);
	}
	
	// The class used to calculate the FPS
	private class FpsBehavior extends Behavior {
		protected WakeupCondition wakeupCondition = null;
		protected long startTime = 0;
		protected final int nbImageEcoulees = 10;

		public FpsBehavior() {
			wakeupCondition = new WakeupOnElapsedFrames(nbImageEcoulees);
		}

		@Override
		public void initialize() {
			wakeupOn(wakeupCondition);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void processStimulus(Enumeration criteria) {
			WakeupCriterion criterion;
			long interval;
			while (criteria.hasMoreElements()) {
				criterion = (WakeupCriterion) criteria.nextElement();
				if (criterion instanceof WakeupOnElapsedFrames) {
					interval = System.currentTimeMillis() - startTime;
					if ((startTime > 0) && (interval != 0))
						fpsText.setText("FPS : " + ((nbImageEcoulees * 1000) / interval)); //$NON-NLS-1$
					startTime = System.currentTimeMillis();
				}
			}
			wakeupOn(wakeupCondition);
		}
	}
	
	@Override
	public void deleteObject() {
		stop();
		if (xAxis != null)
			xAxis.deleteObject();
		if (yAxis != null)
			yAxis.deleteObject();
		if (zAxis != null)
			zAxis.deleteObject();
		super.deleteObject();
	}

	@Override
	public void freeze(boolean value) {
		if (value) {
			if (movementTimer.isRunning()) {
				stop();
				shouldBeRestarted = true;
			} else {
			}
		} else {
			if (shouldBeRestarted) {
				start();
				shouldBeRestarted = false;
			}
		}
	}
	
	public void enableLighting(){
		for (Object3D obj:objects3D) {
			obj.enableLighting();
		}
		lightingEnabled = true;
	}
	
	public void disableLighting(){
		for (Object3D obj:objects3D) {
			obj.disableLighting();
		}
		lightingEnabled = false;
	}
	
	public BoundingBox getCameraBounds() {
		Point3d lower = new Point3d(cameraRelativePosition.x - cameraBoundsRadius, cameraRelativePosition.y - 0.01, cameraRelativePosition.z - cameraBoundsRadius);
		Point3d upper = new Point3d(cameraRelativePosition.x + cameraBoundsRadius, cameraRelativePosition.y + 0.01, cameraRelativePosition.z + cameraBoundsRadius);
		return new BoundingBox(lower, upper);
	}
	

}