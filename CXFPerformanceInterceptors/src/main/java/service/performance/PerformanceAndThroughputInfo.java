package service.performance;

import java.util.HashMap;
import java.util.Map;

public class PerformanceAndThroughputInfo implements
		ReportablePerformanceAndThroughputInfo {

	private final ServiceAndOperation serviceAndOperation;
	private Map<Integer, PerformanceInput> performanceByBucket;
	private Map<Integer, ReportablePerformance> reportablePerformance;

	public PerformanceAndThroughputInfo(ServiceAndOperation serviceAndOperation) {
		super();
		this.serviceAndOperation = serviceAndOperation;
		performanceByBucket = new HashMap<Integer, PerformanceInput>();
		reportablePerformance = new HashMap<Integer, ReportablePerformance>();
	}

	@Override
	public String toString() {
		return "PerformanceAndThroughputInfo For operationName =["
				+ serviceAndOperation.getOperation() + "] serviceName = ["
				+ serviceAndOperation.getService() + "]"
				+ "[reportablePerformance=" + reportablePerformance + "]";
	}

	public ServiceAndOperation getServiceAndOperation() {
		return serviceAndOperation;
	}

	public Map<Integer, ReportablePerformance> getReportablePerformance() {
		return reportablePerformance;
	}

	public void initialiseBucket(int bucket) {
		if (performanceByBucket.get(bucket) == null) {
			Performance create = PerformanceFactory.create();
			performanceByBucket.put(bucket, create);
			reportablePerformance.put(bucket, create);
		}
	}

	public void incrementRequestCount(int bucket) {
		initialiseBucket(bucket);
		performanceByBucket.get(bucket).incrementRequestsCount();
	}

	public void incrementResponseCount(int bucket) {
		initialiseBucket(bucket);
		performanceByBucket.get(bucket).incrementResponsesCount();
	}

	public void incrementFaultCount(int bucket) {
		initialiseBucket(bucket);
		performanceByBucket.get(bucket).incrementFaultsCount();
	}

	public void addRequestPayloadSize(int bucket, Long payloadSize) {
		initialiseBucket(bucket);
		performanceByBucket.get(bucket).addRequestPayloadSize(payloadSize);
	}

	public void addResponsePayloadSize(int bucket, Long payloadSize) {
		initialiseBucket(bucket);
		performanceByBucket.get(bucket).addResponsePayloadSize(payloadSize);
	}

	public void addFaultPayloadSize(int bucket, Long payloadSize) {
		initialiseBucket(bucket);
		performanceByBucket.get(bucket).addFaultPayloadSize(payloadSize);
	}

	public void addFaultResponseTimes(int bucket, Long timeDifference) {
		initialiseBucket(bucket);
		performanceByBucket.get(bucket).addResponseTimeForFault(timeDifference);
	}

	public void addResponseTimes(int bucket, Long timeDifference) {
		initialiseBucket(bucket);
		performanceByBucket.get(bucket).addResponseTime(timeDifference);
	}

}
