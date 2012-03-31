package org.colombbus.tangara.objects;

import javax.vecmath.Point3d;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TObject;

@Localize(value = "Point3D", localizeParent = true)
public class Point3D extends TObject {

	protected Point3d innerObject;


	@Localize(value="Point3D")
	public Point3D() {
		super();
		innerObject = new Point3d();
	}

	@Localize(value="Point3D")
	public Point3D(double x, double y, double z) {
		super();
		innerObject = new Point3d(x,y,z);
	}

	@Localize(value="Point3D")
	public Point3D(int x, int y, int z) {
		super();
		innerObject = new Point3d(x,y,z);
	}

	@Localize(value="Point3D.translate")
	public void translate(double x, double y, double z) {
		innerObject.set(innerObject.x + x, innerObject.y + y, innerObject.z + z);
	}

	@Localize(value="Point3D.setCoordinates")
	public void setCoordinates(double x, double y, double z) {
		innerObject.set(x, y, z);
	}

	@Localize(value="Point3D.setCoordinates2")
	public void setCoordinates(Point3D point) {
		innerObject.set(point.getX(), point.getY(), point.getZ());
	}

	@Localize(value="Point3D.getX")
	public double getX() {
		return innerObject.x;
	}

	@Localize(value="Point3D.getY")
	public double getY() {
		return innerObject.y;
	}

	@Localize(value="Point3D.getZ")
	public double getZ() {
		return innerObject.z;
	}

}
