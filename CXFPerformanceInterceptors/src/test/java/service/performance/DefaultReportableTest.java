package service.performance;

public class DefaultReportableTest {

	public static void main(String[] args) {
		
		DefaultReportable r=new DefaultReportable();
		r.toString();
		
		r=new DefaultReportable();
		r.getMinimum();
		r.getMaximum();
		r.getAverage();
		r.getMedian();
		r.getLine90Percent();
		r.addItem(1l);
		
		r.toString();
	}
	
}
