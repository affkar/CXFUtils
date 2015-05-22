package service.performance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Statistics {

	
	private long startTime;
	private Map<ServiceAndOperation, PerformanceAndThroughputInfo> performanceAndThroughputInfoMap;
	
	public Statistics() {
		this.startTime= System.currentTimeMillis();
		this.performanceAndThroughputInfoMap=new HashMap<ServiceAndOperation, PerformanceAndThroughputInfo>();
	}
	
	public PerformanceAndThroughputInfo getPerformanceAndThroughputInfoFor(ServiceAndOperation serviceAndOperation){
		if(performanceAndThroughputInfoMap.get(serviceAndOperation)==null){
			performanceAndThroughputInfoMap.put(serviceAndOperation, new PerformanceAndThroughputInfo(serviceAndOperation));
		}
		return performanceAndThroughputInfoMap.get(serviceAndOperation);
	}
	
	public Collection<PerformanceAndThroughputInfo> getAllPerformanceAndThroughputInfo() {
		return performanceAndThroughputInfoMap.values();
	}
	
	
	public int getCurrentBucket() {
		long current_time = System.currentTimeMillis();
		return (int) (current_time - startTime) / 10000;
	}
}