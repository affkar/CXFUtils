package service.utils;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.message.Message;

import service.performance.ServiceAndOperation;

public class PerfUtils {

	private static final Logger LOG = LogUtils.getLogger(PerfUtils.class);
	
	public static ServiceAndOperation extractServiceAndOperation(Message message) {
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.finest("Exchange Properties");
			for (Iterator<Entry<String, Object>> iterator = message.getExchange()
					.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, Object> type = iterator.next();
				LOG.finest(type.getKey() + "=" + type.getValue());
			}
			LOG.finest("==================");
			LOG.finest("Message Properties");
			for (Iterator<Entry<String, Object>> iterator = message.entrySet()
					.iterator(); iterator.hasNext();) {
				Entry<String, Object> type = iterator.next();
				LOG.finest(type.getKey() + "=" + type.getValue());
			}
			LOG.finest("==================");
		}
		return new ServiceAndOperation(message.getExchange().getEndpoint()
				.getEndpointInfo().getService().getName().getLocalPart(),
				message.getExchange().getBindingOperationInfo()
						.getOperationInfo().getName().getLocalPart());
	}
	public static boolean amIMakingThisCallAsAClient(Message message) {
		return message.getExchange().get("org.apache.cxf.endpoint.Client") == null;
	}
}
