package service.interceptor.impl;

import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.DelegatingInputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import service.interceptor.api.InboundPayloadSizePhaseInterceptor;
import service.interceptor.api.InboundPerfPhaseInterceptor;
import service.performance.Configuration;

public class InboundPayloadSizeInterceptor extends
		AbstractPhaseInterceptor<Message> implements
		InboundPayloadSizePhaseInterceptor<Message> {

	protected int limit = 100 * 1024;
	private static final Logger LOG = LogUtils
			.getLogger(InboundPayloadSizeInterceptor.class);
	private Configuration configuration;
	private AtomicInteger messageId=new AtomicInteger(1);

	public InboundPayloadSizeInterceptor(Configuration configuration) {
		super(Phase.RECEIVE);
		this.configuration = configuration;
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		if (configuration.isPerformanceInterceptorsEnabled()) {
			message.getExchange().put("REQUEST_ID", messageId.getAndIncrement());
			if (configuration.isRecordPayload() || configuration.isRecordPayloadSize()) {
				InputStream is = message.getContent(InputStream.class);
				if (is != null) {
					try {
						// use the appropriate input stream and restore it later
						InputStream bis = is instanceof DelegatingInputStream ? ((DelegatingInputStream) is)
								.getInputStream() : is;
						int available = bis.available();
						LOG.fine("Available Bytes:: " + available);
						message.getExchange().put(INBOUND_PAYLOAD_SIZE,
								available);
						if (configuration.isRecordPayload()) {
							String encoding = (String) message
									.get(Message.ENCODING);
							StringBuilder stringBuilder = new StringBuilder();
							CachedOutputStream bos = new CachedOutputStream();
							IOUtils.copyAndCloseInput(bis, bos);
							bos.flush();
							bis = bos.getInputStream();

							if (StringUtils.isEmpty(encoding)) {
								bos.writeCacheTo(stringBuilder, limit);
							} else {
								bos.writeCacheTo(stringBuilder, encoding, limit);
							}
							message.getExchange().put("REQUEST", stringBuilder.toString());
							
							bos.close();
							// restore the delegating input stream or the input
							// stream
							if (is instanceof DelegatingInputStream) {
								((DelegatingInputStream) is)
										.setInputStream(bis);
							} else {
								message.setContent(InputStream.class, bis);
							}
						}
					} catch (Exception e) {
						throw new Fault(e);
					}
				} else {
					Reader reader = message.getContent(Reader.class);
					if (reader != null) {
						try {
							LOG.fine("Bummer. It was a reader, not an InputStream content");
							message.getExchange()
									.put("request payload size", 0);
						} catch (Exception e) {
							throw new Fault(e);
						}
					}
				}
			}
		}
	}

	
}
