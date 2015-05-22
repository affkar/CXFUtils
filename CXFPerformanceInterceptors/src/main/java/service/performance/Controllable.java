package service.performance;


public interface Controllable {

	public Statistics getStatistics();
	public void reset(Statistics statistics);
	
}
