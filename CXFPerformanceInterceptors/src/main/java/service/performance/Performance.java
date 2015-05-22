package service.performance;

import java.util.concurrent.atomic.AtomicInteger;

public class Performance implements PerformanceInput, ReportablePerformance{

	private AtomicInteger requestsIn=new AtomicInteger(0);
	private AtomicInteger responsesOut=new AtomicInteger(0);
	private AtomicInteger faultsOut=new AtomicInteger(0);
	private Reportable requestPayloadSizes=ReportableFactory.create();
	private Reportable responsePayloadSizes=ReportableFactory.create();
	private Reportable faultPayloadSizes=ReportableFactory.create();
	private Reportable responseTimesInMs=ReportableFactory.create();
	private Reportable faultOutTimesInMs=ReportableFactory.create();
	@Override
	public void incrementRequestsCount() {
		requestsIn.incrementAndGet();
	}
	@Override
	public void incrementResponsesCount() {
		responsesOut.incrementAndGet();
	}
	@Override
	public void incrementFaultsCount() {
		faultsOut.incrementAndGet();
	}
	@Override
	public void addResponseTime(Long reponseTimes) {
		responseTimesInMs.addItem(reponseTimes);
	}
	@Override
	public void addResponseTimeForFault(Long faultreponseTimes) {
		faultOutTimesInMs.addItem(faultreponseTimes);
		
	}
	@Override
	public void addRequestPayloadSize(Long payloadSize) {
		requestPayloadSizes.addItem(payloadSize);
	}
	@Override
	public void addResponsePayloadSize(Long payloadSize) {
		responsePayloadSizes.addItem(payloadSize);
	}
	@Override
	public void addFaultPayloadSize(Long payloadSize) {
		faultPayloadSizes.addItem(payloadSize);
	}
	public AtomicInteger getRequestsIn() {
		return requestsIn;
	}
	public AtomicInteger getResponsesOut() {
		return responsesOut;
	}
	public AtomicInteger getFaultsOut() {
		return faultsOut;
	}
	public Reportable getRequestPayloadSizes() {
		return requestPayloadSizes;
	}
	public Reportable getResponsePayloadSizes() {
		return responsePayloadSizes;
	}
	public Reportable getFaultPayloadSizes() {
		return faultPayloadSizes;
	}
	public Reportable getResponseTimesInMs() {
		return responseTimesInMs;
	}
	public Reportable getFaultOutTimesInMs() {
		return faultOutTimesInMs;
	}
	@Override
	public String toString() {
		return "Performance [requestsIn=" + requestsIn + ", responsesOut="
				+ responsesOut + ", faultsOut=" + faultsOut
				+ ", requestPayloadSizes=" + requestPayloadSizes
				+ ", responsePayloadSizes=" + responsePayloadSizes
				+ ", faultPayloadSizes=" + faultPayloadSizes
				+ ", responseTimesInMs=" + responseTimesInMs
				+ ", faultOutTimesInMs=" + faultOutTimesInMs + "]";
	}
	
	
	
	
	
	
}
