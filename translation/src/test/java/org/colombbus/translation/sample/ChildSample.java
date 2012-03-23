package org.colombbus.translation.sample;

import org.colombbus.build.Localize;

@Localize(value = "ChildSample", localizeParent = true)
public class ChildSample extends ParentSample {

	@Localize(value = "ChildSample.ChildSampleNoArg")
	public ChildSample() {
	}

	@Localize(value = "ChildSample.ChildSampleWithArg")
	public ChildSample(String name) {
	}

	@Localize(value = "ChildSample.method3")
	public String method3(String value) {
		return "child.method3";
	}

	@Localize(value = "ChildSample.method3NoArg")
	public String method3() {
		return "child.method3";
	}

}
