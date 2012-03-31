package org.colombbus.tangara.objects;

import java.awt.Color;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Light;
import javax.media.j3d.PointLight;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import org.colombbus.build.Localize;
import org.colombbus.tangara.TColor;
import org.colombbus.tangara.TObject;

@Localize(value = "Light3D", localizeParent = true)
public class Light3D extends TObject {

	protected static final int AMBIENT_LIGHT = 0;
	protected static final int POINT_LIGHT = 1;
	protected static final int DIRECTIONAL_LIGHT = 2;

	protected Light light;
	protected Color3f lightColor;
	protected BoundingSphere influencingBounds;

	protected BranchGroup innerObject;

	protected int lightType = -1;

	protected Space3D space = null;

	@Localize(value="Light3D")
	public Light3D() {
		this(""); //$NON-NLS-1$
	}

	@Localize(value="Light3D")
	public Light3D(String colorName) {
		super();
		init(colorName);
		setAmbient();
	}

	@Localize(value="Light3D")
	public Light3D(String colorName, Point3D location) {
		super();
		init(colorName);
		setLocation(location);
	}

	@Localize(value="Light3D")
	public Light3D(String colorName, Vector3D direction) {
		super();
		init(colorName);
		setDirection(direction);
	}

	protected void init(String colorName) {
		innerObject = new BranchGroup();
		innerObject.setCapability(BranchGroup.ALLOW_DETACH);
		setColor(colorName);
		setInfluencingBounds(new Point3d(),100);
	}

	@Localize(value="Light3D.setAmbient")
	public void setAmbient() {
		if (lightType != AMBIENT_LIGHT) {
			removeLight();
			light = new AmbientLight();
			light.setColor(lightColor);
			light.setInfluencingBounds(influencingBounds);
			light.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
			light.setCapability(Light.ALLOW_COLOR_WRITE);
			light.setCapability(Light.ALLOW_STATE_WRITE);
			innerObject.removeAllChildren();
			innerObject.addChild(light);
			addLight();
			lightType = AMBIENT_LIGHT;
		}
	}

	@Localize(value="Light3D.setDirection2")
	public void setDirection(Vector3D v) {
		setDirection(v.getX(), v.getY(), v.getZ());
	}

	@Localize(value="Light3D.setDirection")
	public void setDirection(double x, double y, double z) {
		if (lightType != DIRECTIONAL_LIGHT) {
			removeLight();
			light = new DirectionalLight();
			light.setColor(lightColor);
			light.setInfluencingBounds(influencingBounds);
			light.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
			light.setCapability(Light.ALLOW_COLOR_WRITE);
			light.setCapability(DirectionalLight.ALLOW_DIRECTION_WRITE);
			light.setCapability(Light.ALLOW_STATE_WRITE);
			innerObject.removeAllChildren();
			innerObject.addChild(light);
			addLight();
			lightType = DIRECTIONAL_LIGHT;
		}
		((DirectionalLight)light).setDirection((float)x,(float)y,(float)z);
	}

	@Localize(value="Light3D.setLocation")
	public void setLocation(double x, double y, double z) {
		if (lightType != POINT_LIGHT) {
			removeLight();
			light = new PointLight();
			light.setColor(lightColor);
			light.setInfluencingBounds(influencingBounds);
			light.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
			light.setCapability(Light.ALLOW_COLOR_WRITE);
			light.setCapability(PointLight.ALLOW_POSITION_WRITE);
			light.setCapability(Light.ALLOW_STATE_WRITE);
			light.setCapability(PointLight.ALLOW_ATTENUATION_WRITE);
			innerObject.removeAllChildren();
			innerObject.addChild(light);
			addLight();
			lightType = POINT_LIGHT;
		}
		((PointLight)light).setPosition((float)x, (float)y,(float)z);
	}

	@Localize(value="Light3D.setLocation2")
	public void setLocation(double x, double y, double z, int attenuation) {
		setLocation(x,y,z);
		if (attenuation < 0 ) {
			attenuation = 0;
		}
		if (attenuation > 100) {
			attenuation = 100;
		}
		((PointLight)light).setAttenuation((1-(attenuation/100)),0,0);
	}

	@Localize(value="Light3D.setLocation3")
	public void setLocation(Point3D location) {
		setLocation(location.getX(),location.getY(),location.getZ());
	}

	@Localize(value="Light3D.setLocation4")
	public void setLocation(Point3D location, int attenuation) {
		setLocation(location.getX(),location.getY(),location.getZ(), attenuation);
	}


	@Localize(value="Light3D.setInfluencingBounds")
	public void setInfluencingBounds(Point3D center, double radius) {
		setInfluencingBounds(new Point3d(center.getX(), center.getY(), center.getZ()), radius);
	}

	public void setInfluencingBounds(Point3d center, double radius) {
		influencingBounds = new BoundingSphere(center, radius);
		if (light!=null) {
			light.setInfluencingBounds(influencingBounds);
		}
	}

	@Localize(value="Light3D.setColor")
	public void setColor(String colorName) {
		Color color = TColor.translateColor(colorName, Color.WHITE);
		lightColor = new Color3f(color);
		if (light!=null)
			light.setColor(lightColor);
	}

	@Localize(value="Light3D.setColor2")
	public void setColor(int red, int green, int blue) {
		if (red<0)
			red = 0;
		if (red>255)
			red = 255;
		if (green<0)
			green = 0;
		if (green>255)
			green = 255;
		if (blue<0)
			blue = 0;
		if (blue>255)
			blue = 255;
		lightColor = new Color3f(red/255f, green/255f, blue/255f);

		if (light!=null)
			light.setColor(lightColor);
	}

	protected void removeLight() {
		if (space != null) {
			space.removeLight(this, false);
		}
	}

	protected void addLight() {
		if (space != null) {
			space.addLight(this);
		}
	}

	public BranchGroup getInnerObject() {
		return innerObject;
	}

	/**
	 * This method is called when this Light3D is added to a Space.
	 * @param space - The Space in which this Light3D was added.
	 */
	public void setSpace(Space3D space) {
		this.space = space;
	}

	@Override
	public void deleteObject() {
		if (space != null) {
			space.removeLight(this);
		}
		super.deleteObject();
	}

	@Localize(value="Light3D.switchOn")
	public void switchOn() {
		light.setEnable(true);
	}

	@Localize(value="Light3D.switchOff")
	public void switchOff() {
		light.setEnable(false);
	}

}
