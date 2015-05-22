package service.interceptor.impl;

import java.io.InputStream;
import java.io.Reader;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.DelegatingInputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import service.interceptor.api.InboundPayloadSizePhaseInterceptor;

public class InboundPayloadSizeInterceptor extends
		AbstractPhaseInterceptor<Message> implements 
		InboundPayloadSizePhaseInterceptor<Message> {

	private static final Logger LOG = LogUtils
			.getLogger(InboundPayloadSizeInterceptor.class);

	public InboundPayloadSizeInterceptor() {
		super(Phase.RECEIVE);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		InputStream is = message.getContent(InputStream.class);
        if (is != null) {
            try {
                // use the appropriate input stream and restore it later
                InputStream bis = is instanceof DelegatingInputStream 
                    ? ((DelegatingInputStream)is).getInputStream() : is;
                    int available = bis.available();
                    LOG.fine("Available Bytes:: "+available);
                    message.getExchange().put(INBOUND_PAYLOAD_SIZE, available);
            } catch (Exception e) {
                throw new Fault(e);
            }
        } else {
            Reader reader = message.getContent(Reader.class);
            if (reader != null) {
                try {
                    LOG.fine("Bummer. It was a reader, not an InputStream content");
                    message.getExchange().put("request payload size", 0);
                } catch (Exception e) {
                    throw new Fault(e);
                }
            }
        }

	}

}
