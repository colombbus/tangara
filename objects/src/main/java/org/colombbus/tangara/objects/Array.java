package org.colombbus.tangara.objects;

import java.io.Serializable;
import java.util.ArrayList;

import org.colombbus.build.Localize;

@Localize(value="Array",localizeParent=true)
public class Array implements Serializable
{
	private static final long serialVersionUID = 7237295972472179181L;
	ArrayList<Byte> byteValues = new ArrayList<Byte>();
	ArrayList<Integer> intValues = new ArrayList<Integer>();
	ArrayList<Double> doubleValues = new ArrayList<Double>();
	private static final int BYTE_ARRAY = 1;
	private static final int INT_ARRAY = 2;
	private static final int DOUBLE_ARRAY = 3;
	private int arrayType;


	@Localize(value="Array")
	public Array(int size) {
		this();
		for(int i = 0; i < size; i++) {
			byte number = 0;
			byteValues.add(number);
		}
	}

	@Localize(value="Array")
	public Array() {
		arrayType = BYTE_ARRAY;
	}

	@Localize(value="Array.setValueAt")
	public void setValueAt(int index, double value) {
		int actualIndex = index-1;
		switch (arrayType) {
			case BYTE_ARRAY:
				if (Math.round(value) == value) {
					if (value<256) {
						byteValues.set(actualIndex,(byte)value);
					} else {
						// Switch to int values
						setArrayType(INT_ARRAY);
						intValues.set(actualIndex,(int)value);
					}
				} else {
					// Switch to double values
					setArrayType(DOUBLE_ARRAY);
					doubleValues.set(actualIndex, value);
				}
				break;
			case INT_ARRAY:
				if (Math.round(value) == value) {
					intValues.set(actualIndex,(int)value);
				} else {
					// Switch to double values
					setArrayType(DOUBLE_ARRAY);
					doubleValues.set(actualIndex, value);
				}
				break;
			case DOUBLE_ARRAY:
				doubleValues.set(actualIndex, value);
				break;
		}
	}

	@Localize(value="Array.addValue")
	public void addValue(double value) {
		int index = 0;
		switch(arrayType) {
			case BYTE_ARRAY:
				byteValues.add((byte)0);
				index = byteValues.size();
				break;
			case INT_ARRAY:
				intValues.add(0);
				index = intValues.size();
				break;
			case DOUBLE_ARRAY:
				doubleValues.add(0.0);
				index = doubleValues.size();
				break;
		}
		setValueAt(index, value);
	}

	@Localize(value="Array.getValueAt")
	public Number getValueAt(int index) {
		int actualIndex = index-1;
		switch (arrayType) {
			case BYTE_ARRAY:
				return byteValues.get(actualIndex);
			case INT_ARRAY:
				return intValues.get(actualIndex);
			case DOUBLE_ARRAY:
				return doubleValues.get(actualIndex);
		}
		return 0;
	}

	private void setArrayType(int newType) {
		switch (arrayType) {
			case BYTE_ARRAY:
				if (newType == INT_ARRAY) {
					for (int i=0; i<byteValues.size(); i++) {
						intValues.add((int)byteValues.get(i));
					}
					byteValues.clear();
				} else if (newType == DOUBLE_ARRAY) {
					for (int i=0; i<byteValues.size(); i++) {
						doubleValues.add((double)byteValues.get(i));
					}
					byteValues.clear();
				}
				break;
			case INT_ARRAY:
				if (newType == BYTE_ARRAY) {
					for (int i=0; i<intValues.size(); i++) {
						byteValues.add(intValues.get(i).byteValue());
					}
					intValues.clear();
				} else if (newType == DOUBLE_ARRAY) {
					for (int i=0; i<intValues.size(); i++) {
						doubleValues.add((double)intValues.get(i));
					}
					intValues.clear();
				}
				break;
			case DOUBLE_ARRAY:
				if (newType == BYTE_ARRAY) {
					for (int i=0; i<doubleValues.size(); i++) {
						byteValues.add(doubleValues.get(i).byteValue());
					}
					doubleValues.clear();
				} else if (newType == INT_ARRAY) {
					for (int i=0; i<doubleValues.size(); i++) {
						intValues.add(doubleValues.get(i).intValue());
					}
					doubleValues.clear();
				}
				break;
		}
		arrayType = newType;
	}

}
