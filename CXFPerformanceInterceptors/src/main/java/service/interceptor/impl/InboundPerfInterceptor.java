package service.interceptor.impl;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import service.interceptor.api.InboundPayloadSizePhaseInterceptor;
import service.interceptor.api.InboundPerfPhaseInterceptor;
import service.performance.CollatorDaemon;
import service.performance.Configuration;
import service.performance.Controllable;
import service.performance.PerformanceAndThroughputInfo;
import service.performance.ServiceAndOperation;
import service.performance.Statistics;
import service.utils.PerfUtils;

public class InboundPerfInterceptor extends AbstractPhaseInterceptor<Message>
		implements Controllable, InboundPerfPhaseInterceptor<Message> {

	private static final Logger LOG = LogUtils.getLogger(InboundPerfInterceptor.class);
	
	private Statistics statistics;
	private CollatorDaemon collatorDaemon;
	
	private  Configuration configuration;

	public InboundPerfInterceptor(Configuration configuration) {
		super(Phase.USER_LOGICAL, true);
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
			if (! MessageUtils.isOutbound(message)) {
				addRequestHeaders(message.getExchange());
				if(configuration.isRecordCount())
					performanceAndThroughputInfo.incrementRequestCount(bucket);
				if(configuration.isRecordPayloadSize())
					performanceAndThroughputInfo
						.addRequestPayloadSize(
								bucket,
								Long.valueOf((Integer) message
										.getExchange()
										.get(InboundPayloadSizePhaseInterceptor.INBOUND_PAYLOAD_SIZE)));
				if(configuration.isRecordPayload())
					performanceAndThroughputInfo.getReportablePerformance().get(bucket).getExchanges().put(((Integer)message.getExchange().get("REQUEST_ID")), service.performance.Exchange.getExchange((String)message.getExchange().get("REQUEST")));
			}
		}
	}

	private void addRequestHeaders(Exchange exchange) {
		exchange.put(REQUESTED_TIME, new Date());
	}

	public void reset(Statistics statistics) {
		this.statistics = statistics;
	}

	@Override
	public Statistics getStatistics() {
		return statistics;
	}

}