package service.interceptor.api;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;
/**
 * Marker Interface
 * @author alagu
 *
 * @param <T>
 */
public interface InboundPerfPhaseInterceptor<T extends Message> extends PhaseInterceptor<T> {

	static final String REQUEST_ID = "RequestId";
	static final String RESPONDED_TIME = "responded-time";
	static final String REQUESTED_TIME = "requested-time";
	
}
