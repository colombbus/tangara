package org.colombbus.tangara.net;

import org.colombbus.tangara.Usage;

public class MyBean {

	public MyBean() {
		super();
	}

	@Usage(value = "getValue()", prototype = "getValue()")
	public int getValue() {
		return this.value;
	}

	@Usage(value = "setValue()", prototype = "setValue(200)")
	public void setValue(int value) {
		this.value = value;
	}

	private int value = 100;

}
