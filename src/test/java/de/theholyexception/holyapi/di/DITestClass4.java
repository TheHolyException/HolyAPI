package de.theholyexception.holyapi.di;

public class DITestClass4 {


	@DIInject
	private DITestClass3 testClass3;

	public DITestClass4(DITestClass3 testClass3) {
		this.testClass3 = testClass3;
	}

	public DITestClass3 getTestClass3() {
		return testClass3;
	}
}
