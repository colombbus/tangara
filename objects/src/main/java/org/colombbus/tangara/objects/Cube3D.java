package org.colombbus.tangara.objects;

import org.colombbus.build.Localize;

@Localize(value = "Cube3D", localizeParent = true)
public class Cube3D extends Box3D {
	
	@Localize(value="Cube3D")
	public Cube3D(double size, String colorName) {
		super(size,size,size, colorName);
	}
	
	@Localize(value="Cube3D")
	public Cube3D(double size) {
		this(size,"");
	}

	@Localize(value="Cube3D")
	public Cube3D() {
		this(1);
	}
	
	@Localize(value="Cube3D.setDimension")
	public void setDimension(double d) {
		super.setDimensions(d, d, d);
	}

}
