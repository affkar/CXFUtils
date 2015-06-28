package service.interceptor.impl;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.XMLUtils;
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
import service.performance.Configuration;
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
	private  Configuration configuration;
	
	public OutboundPerfInterceptor(Configuration configuration) {
		super(Phase.PRE_STREAM, true);
		collatorDaemon = CollatorDaemon.getInstance();
		collatorDaemon.attach(this);
		this.configuration=configuration;
	}
	

	@Override
	public void handleMessage(Message message) throws Fault {
		if(configuration.isPerformanceInterceptorsEnabled()){
			int bucket = statistics.getCurrentBucket(configuration.getBucketInterval());
			ServiceAndOperation serviceAndOperation = PerfUtils.extractServiceAndOperation(message);
			PerformanceAndThroughputInfo performanceAndThroughputInfo = statistics
					.getPerformanceAndThroughputInfoFor(serviceAndOperation);
			if (MessageUtils.isOutbound(message)) {
				addResponseHeaders(message.getExchange());
				
				if (MessageUtils.isFault(message)) {
					if(configuration.isRecordCount())
						performanceAndThroughputInfo.incrementFaultCount(bucket);
					if(configuration.isRecordTimes())
						performanceAndThroughputInfo.addFaultResponseTimes(bucket,
							getTimeDifference(message));
				} else {
					if(configuration.isRecordCount())
						performanceAndThroughputInfo.incrementResponseCount(bucket);
					if(configuration.isRecordTimes())
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
				newOut.registerCallback(new OutPayloadSizeCallback(bucket,performanceAndThroughputInfo, message, os,configuration));
			} else {
				message.setContent(Writer.class,
						new PayloadSizeExtractAndWriteWriter(bucket,performanceAndThroughputInfo, message, iowriter, configuration));
			}
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
		Configuration configuration;
		public OutPayloadSizeCallback(int bucket, PerformanceAndThroughputInfo performanceAndThroughputInfo, final Message msg, final OutputStream os, Configuration configuration) {
			this.message = msg;
			this.origStream = os;
			this.performanceAndThroughputInfo = performanceAndThroughputInfo;
			this.bucket=bucket;
			this.configuration=configuration;
		}

		public void onFlush(CachedOutputStream cos) {

		}

		public void onClose(CachedOutputStream cos) {
			if(configuration.isRecordPayload() || configuration.isRecordPayloadSize()){
				try {
					LOG.fine("OutPayloadSizeCallback Available Output bytes:: "
							+ cos.getInputStream().available());
					message.getExchange().put(OutboundPayloadSizePhaseInterceptor.OUTBOUND_PAYLOAD_SIZE,
							cos.getInputStream().available());
					message.getExchange().get(OutboundPayloadSizePhaseInterceptor.OUTBOUND_PAYLOAD_SIZE);
					performanceAndThroughputInfo.addResponsePayloadSize(bucket, Long.valueOf(cos.getInputStream().available()));
					StringBuilder stringBuilder=new StringBuilder();
					if(configuration.isRecordPayload()){
						String contentType = (String)message.get(Message.CONTENT_TYPE);
						if (configuration.isPrettyLogging() && (contentType != null && contentType.indexOf("xml") >= 0 
					            && contentType.toLowerCase().indexOf("multipart/related") < 0) && cos.size() > 0) {
				            Transformer serializer = XMLUtils.newTransformer(2);
				            // Setup indenting to "pretty print"
				            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
				            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

				            StringWriter swriter = new StringWriter();
				            serializer.transform(new StreamSource(cos.getInputStream()), new StreamResult(swriter));
				            String result = swriter.toString();
				            if (result.length() < limit || limit == -1) {
				            	stringBuilder.append(swriter.toString());
				            } else {
				            	stringBuilder.append(swriter.toString().substring(0, limit));
				            }

				        } else {
				        	cos.writeCacheTo(stringBuilder, limit);
				        }
						if(MessageUtils.isFault(message))
							performanceAndThroughputInfo.getReportablePerformance().get(bucket).getExchanges().get(((Integer)message.getExchange().get("REQUEST_ID"))).setFaultPayload(stringBuilder.toString());
						else
							performanceAndThroughputInfo.getReportablePerformance().get(bucket).getExchanges().get(((Integer)message.getExchange().get("REQUEST_ID"))).setResponsePayload(stringBuilder.toString());
				   }
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
			}
			message.setContent(OutputStream.class, origStream);
		}
	}
	
	private class PayloadSizeExtractAndWriteWriter extends FilterWriter {
		int count;
		Message message;
		private final PerformanceAndThroughputInfo performanceAndThroughputInfo;
		private final int bucket;
		Configuration configuration;

		public PayloadSizeExtractAndWriteWriter(int bucket, PerformanceAndThroughputInfo performanceAndThroughputInfo, Message message, Writer writer, Configuration configuration) {
			super(writer);
			this.message = message;
			this.performanceAndThroughputInfo = performanceAndThroughputInfo;
			this.bucket=bucket;
			this.configuration=configuration;

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
			if(configuration.isRecordPayloadSize()){
				LOG.fine("Outbound count from PayloadSizeExtractAndWriteWriter is "+count);
				message.getExchange().put(OutboundPayloadSizePhaseInterceptor.OUTBOUND_PAYLOAD_SIZE, count);
				performanceAndThroughputInfo.addResponsePayloadSize(bucket, Long.valueOf(count));
			}
			message.setContent(Writer.class, out);
			super.close();
		}
	}
	

}
