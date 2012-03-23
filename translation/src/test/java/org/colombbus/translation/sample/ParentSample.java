package org.colombbus.translation.sample;

import org.colombbus.build.Localize;

@Localize(value = "ParentSample", localizeParent=true)
public class ParentSample {

	@Localize(value="ParentSample.method1")
	public String method1() {
		return "parent.method1";
	}

	public String method2() {
		return "parent.method2";
	}

}
