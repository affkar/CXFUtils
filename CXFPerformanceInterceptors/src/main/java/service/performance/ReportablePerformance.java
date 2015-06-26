package service.performance;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public interface ReportablePerformance {

	public AtomicInteger getRequestsIn();

	public AtomicInteger getResponsesOut();

	public AtomicInteger getFaultsOut();

	public Map<Integer, Exchange> getExchanges();
	
	public Reportable getRequestPayloadSizes();

	public Reportable getResponsePayloadSizes();

	public Reportable getFaultPayloadSizes();

	public Reportable getResponseTimesInMs();

	public Reportable getFaultOutTimesInMs();

}
