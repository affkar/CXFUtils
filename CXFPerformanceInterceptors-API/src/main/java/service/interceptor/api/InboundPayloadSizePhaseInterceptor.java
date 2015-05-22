package service.interceptor.api;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;
/**
 * Marker Interface
 * @author alagu
 *
 * @param <T>
 */
public interface InboundPayloadSizePhaseInterceptor<T extends Message> extends PhaseInterceptor<T> {

	static final String INBOUND_PAYLOAD_SIZE = "inbound-payload-size";
	
}
