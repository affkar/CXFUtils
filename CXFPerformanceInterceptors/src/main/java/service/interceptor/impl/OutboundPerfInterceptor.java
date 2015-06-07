package service.interceptor.impl;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import service.interceptor.api.InboundPerfPhaseInterceptor;
import service.interceptor.api.OutboundPayloadSizePhaseInterceptor;
import service.interceptor.api.OutboundPerfPhaseInterceptor;
import service.performance.CollatorDaemon;
import service.performance.Controllable;
import service.performance.PerformanceAndThroughputInfo;
import service.performance.ServiceAndOperation;
import service.performance.Statistics;
import service.utils.PerfUtils;

public class OutboundPerfInterceptor extends AbstractPhaseInterceptor<Message> implements OutboundPerfPhaseInterceptor<Message>, Controllable {

	private CollatorDaemon collatorDaemon;
	private Statistics statistics;
	protected int limit = 100 * 1024;
	private static final Logger LOG = LogUtils.getLogger(OutboundPerfInterceptor.class);
	private int bucketInterval=60000;//default
	
	public OutboundPerfInterceptor() {
		super(Phase.PRE_STREAM, true);
		collatorDaemon = CollatorDaemon.getInstance();
		collatorDaemon.attach(this);
	}
	
	public OutboundPerfInterceptor(int bucketInterval) {
		this();
		this.bucketInterval=bucketInterval;
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		int bucket = statistics.getCurrentBucket(bucketInterval);
		ServiceAndOperation serviceAndOperation = PerfUtils.extractServiceAndOperation(message);
		PerformanceAndThroughputInfo performanceAndThroughputInfo = statistics
				.getPerformanceAndThroughputInfoFor(serviceAndOperation);
		if (MessageUtils.isOutbound(message)) {
			addResponseHeaders(message.getExchange());
			if (MessageUtils.isFault(message)) {
				performanceAndThroughputInfo.incrementFaultCount(bucket);
				performanceAndThroughputInfo.addFaultResponseTimes(bucket,
						getTimeDifference(message));
			} else {
				performanceAndThroughputInfo.incrementResponseCount(bucket);
				performanceAndThroughputInfo.addResponseTimes(bucket,
						getTimeDifference(message));
			}
		}
		
		final OutputStream os = message.getContent(OutputStream.class);
		final Writer iowriter = message.getContent(Writer.class);
		if (os == null && iowriter == null) {
			return;
		}
		if (os != null) {
			final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(
					os);
			message.setContent(OutputStream.class, newOut);
			newOut.registerCallback(new OutPayloadSizeCallback(bucket,performanceAndThroughputInfo, message, os));
		} else {
			message.setContent(Writer.class,
					new PayloadSizeExtractAndWriteWriter(bucket,performanceAndThroughputInfo, message, iowriter));
		}
	}

	@Override
	public void reset(Statistics statistics) {
		this.statistics=statistics;
	}

	@Override
	public Statistics getStatistics() {
		return statistics;
	}
	
	private Long getTimeDifference(Message message) {
		return getTimeDifference(
				(Date) message.getExchange().get(InboundPerfPhaseInterceptor.REQUESTED_TIME),
				(Date) message.getExchange().get(InboundPerfPhaseInterceptor.RESPONDED_TIME));
	}

	private Long getTimeDifference(Date date1, Date date2) {
		return (date2.getTime() - date1.getTime());
	}
	
	private void addResponseHeaders(Exchange exchange) {
		exchange.put(InboundPerfPhaseInterceptor.RESPONDED_TIME, new Date());
	}
	
	private class OutPayloadSizeCallback implements CachedOutputStreamCallback {

		private final Message message;
		private final OutputStream origStream;
		private final PerformanceAndThroughputInfo performanceAndThroughputInfo;
		private final int bucket;

		public OutPayloadSizeCallback(int bucket, PerformanceAndThroughputInfo performanceAndThroughputInfo, final Message msg, final OutputStream os) {
			this.message = msg;
			this.origStream = os;
			this.performanceAndThroughputInfo = performanceAndThroughputInfo;
			this.bucket=bucket;
		}

		public void onFlush(CachedOutputStream cos) {

		}

		public void onClose(CachedOutputStream cos) {
			try {
				LOG.fine("OutPayloadSizeCallback Available Output bytes:: "
						+ cos.getInputStream().available());
				message.getExchange().put(OutboundPayloadSizePhaseInterceptor.OUTBOUND_PAYLOAD_SIZE,
						cos.getInputStream().available());
				performanceAndThroughputInfo.addResponsePayloadSize(bucket, Long.valueOf(cos.getInputStream().available()));
			} catch (Exception ex) {
				// ignore
			}

			try {
				// empty out the cache
				cos.lockOutputStream();
				cos.resetOut(null, false);
			} catch (Exception ex) {
				// ignore
			}
			message.setContent(OutputStream.class, origStream);
		}
	}
	
	private class PayloadSizeExtractAndWriteWriter extends FilterWriter {
		int count;
		Message message;
		private final PerformanceAndThroughputInfo performanceAndThroughputInfo;
		private final int bucket;

		public PayloadSizeExtractAndWriteWriter(int bucket, PerformanceAndThroughputInfo performanceAndThroughputInfo, Message message, Writer writer) {
			super(writer);
			this.message = message;
			this.performanceAndThroughputInfo = performanceAndThroughputInfo;
			this.bucket=bucket;

		}

		public void write(int c) throws IOException {
			super.write(c);
			count++;
		}

		public void write(char[] cbuf, int off, int len) throws IOException {
			super.write(cbuf, off, len);
			count += len;
		}

		public void write(String str, int off, int len) throws IOException {
			super.write(str, off, len);
			count += len;
		}

		public void close() throws IOException {
			LOG.fine("Outbound count from PayloadSizeExtractAndWriteWriter is "+count);
			message.getExchange().put(OutboundPayloadSizePhaseInterceptor.OUTBOUND_PAYLOAD_SIZE, count);
			performanceAndThroughputInfo.addResponsePayloadSize(bucket, Long.valueOf(count));
			message.setContent(Writer.class, out);
			super.close();
		}
	}
	

}
