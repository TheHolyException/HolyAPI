package de.theholyexception.holyapi.di;

public class DITestClass3 {

	@DIInject
	private DITestClass4 testClass4;

	public DITestClass4 getTestClass4() {
		return testClass4;
	}
}
