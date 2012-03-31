package org.colombbus.tangara.objects;

import org.colombbus.build.Localize;
import org.colombbus.tangara.TObject;

@Localize(value="Calculator",localizeParent=true)
public class Calculator extends TObject {

	@Localize(value="Calculator")
	public Calculator() {
		super();
	}
	
	@Localize(value="Calculator.squareRoot")
	public double squareRoot(double number) {
		return Math.sqrt(number);
	}

	@Localize(value="Calculator.square")
	public double square(double number) {
		return number * number;
	}

	@Localize(value="Calculator.cube")
	public double cube(double number) {
		return number * number * number;
	}

	@Localize(value="Calculator.power")
	public double power(double number, double exponent) {
		return Math.pow(number, exponent);
	}

	@Localize(value="Calculator.round")
	public int round(double number) {
		return (int) Math.round(number);
	}

	@Localize(value="Calculator.round")
	public int round(int number) {
		return number;
	}
	
	@Localize(value="Calculator.cos")
	public double cos(double number) {
		return Math.cos(Math.toRadians(number));
	}

	@Localize(value="Calculator.sin")
	public double sin(double number) {
		return Math.sin(Math.toRadians(number));
	}

	@Localize(value="Calculator.tan")
	public double tan(double number) {
		return Math.tan(Math.toRadians(number));
	}

	@Localize(value="Calculator.getPi")
	public double getPi() {
		return Math.PI;
	}
	
}
