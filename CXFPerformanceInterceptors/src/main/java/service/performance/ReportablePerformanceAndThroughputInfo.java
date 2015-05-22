package service.performance;

import java.util.Map;

public interface ReportablePerformanceAndThroughputInfo {

	public ServiceAndOperation getServiceAndOperation();

	public Map<Integer, ReportablePerformance> getReportablePerformance();
}
