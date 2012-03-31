package org.colombbus.tangara.objects;

import javax.vecmath.Vector3d;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TObject;

@Localize(value = "Vector3D", localizeParent = true)
public class Vector3D extends TObject {

	protected Vector3d innerObject;

	@Localize(value="Vector3D")
	public Vector3D() {
		this(1,0,0);
	}

	@Localize(value="Vector3D")
	public Vector3D(double x, double y, double z) {
		super();
		innerObject = new Vector3d(x,y,z);
	}

	@Localize(value="Vector3D")
	public Vector3D(int x, int y, int z) {
		super();
		innerObject = new Vector3d(x,y,z);
	}

	@Localize(value="Vector3D.setComponents")
	public void setComponents(double x, double y, double z) {
		innerObject.set(x, y, z);
	}

	@Localize(value="Vector3D.getX")
	public double getX() {
		return innerObject.x;
	}

	@Localize(value="Vector3D.getY")
	public double getY() {
		return innerObject.y;
	}

	@Localize(value="Vector3D.getZ")
	public double getZ() {
		return innerObject.z;
	}



}
