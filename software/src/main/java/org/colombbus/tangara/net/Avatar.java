/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008 Colombbus (http://www.colombbus.org)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.colombbus.tangara.net;

/**
 * @author gwen
 *
 */
public class Avatar {
	
	/**
	 * Create a new avatar
	 * 
	 * @param name
	 *            the name of the user associated to the avatar
	 * @param image
	 *            the image file in binary format
	 * @param imgFormat
	 *            the (file) format of the <code>image</code>
	 */
	public Avatar(final String name, final byte[] image, final String imgFormat) {
		super();
		this.name = name;
		this.image = image.clone();
		this.imgFormat = imgFormat;
	}
	
	/**
	 * Get the name of the user associated the avatar
	 * 
	 * @return user's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the image file content
	 * 
	 * @return binary file content
	 */
	public byte[] getImage() {
		return image.clone();
	}
	
	/**
	 * Get the avatar file format
	 * 
	 * @return a file format
	 */
	public String getImageFormat() {
		return imgFormat;
	}
	
	/** User associated to the avatar */
	private final transient String name;
	
	/** binary file content */
	private final transient byte[] image;
	
	/** image file format */
	private final transient String imgFormat;
}
