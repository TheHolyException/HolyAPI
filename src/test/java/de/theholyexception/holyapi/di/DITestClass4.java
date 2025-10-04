package de.theholyexception.holyapi.di;

public class DITestClass4 {


	@DIInject
	public DITestClass3 testClass3;

	public DITestClass4(DITestClass3 testClass3) {
		this.testClass3 = testClass3;
	}

}
