package service.interceptor.api;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;
/**
 * Marker Interface
 * @author alagu
 *
 * @param <T>
 */
public interface OutboundPayloadSizePhaseInterceptor<T extends Message> extends PhaseInterceptor<T> {

	static final String OUTBOUND_PAYLOAD_SIZE = "outbound-payload-size";
}
