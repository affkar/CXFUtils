package service.performance;

import java.util.ArrayList;
import java.util.List;

public class CollatorDaemon {

	private List<Controllable> handlers=new ArrayList<Controllable>();
	private Statistics statistics=new Statistics();
	public void attach(Controllable handler){
		handler.reset(statistics);
		handlers.add(handler);
	}
	
	public List<String> log(){
		List<String> logs=new ArrayList<String>();
		for(PerformanceAndThroughputInfo info: statistics.getAllPerformanceAndThroughputInfo()){
			logs.add(info.toString());
		}
		return logs;
	}
	
	public void reset(){
		statistics=new Statistics();
		for(Controllable handler:handlers){
			handler.reset(statistics);
		}
	}
	
	private static CollatorDaemon collatorDaemon=new CollatorDaemon();
	
	private CollatorDaemon(){}
	
	public static CollatorDaemon getInstance() {
		return collatorDaemon;
	}

}
