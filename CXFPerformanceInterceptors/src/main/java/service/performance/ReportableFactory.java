package service.performance;

public final class ReportableFactory {

	public static Reportable create(){
		return new DefaultReportable();
	}
	
}
