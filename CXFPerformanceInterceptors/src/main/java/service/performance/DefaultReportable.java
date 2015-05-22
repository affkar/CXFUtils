package service.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;

public class DefaultReportable implements Reportable {

	private List<Long> returnTimes = new ArrayList<Long>();
	private static final Logger LOG = LogUtils
			.getLogger(DefaultReportable.class);
	private List<Long> sortedReturnTimes = new ArrayList<Long>();

	@Override
	public Number getMinimum() {
		return getSorted().get(0);
	}

	@Override
	public Number getMaximum() {
		return getSorted().get(getSize() - 1);
	}

	@Override
	public Number getAverage() {
		return getSum() / getSize();
	}

	@Override
	public Number getMedian() {
		double medianElement;
		if (getSize() % 2 == 1) {
			medianElement = getSorted().get(
					(int) (Math.ceil(getSize() / 2) - 1));
		} else {
			medianElement = (getSorted().get((getSize() / 2 - 1)) + getSorted()
					.get(getSize() / 2)) / 2;
		}
		return medianElement;
	}

	@Override
	public Number getLine90Percent() {
		int line90PercentIndex = (int) (getSize() * 0.9);
		float fraction = (getSize() * 0.9f) - line90PercentIndex;
		return (getSorted().get(line90PercentIndex-1)*fraction + (getSorted().get(line90PercentIndex)*(1-fraction)));
	}

	public List<Long> getSorted() {
		Collections.sort(sortedReturnTimes);
		return sortedReturnTimes;
	}

	public int getSize() {
		return sortedReturnTimes.size() == 0 ? 1 : sortedReturnTimes.size();
	}

	public Long getSum() {
		long sum = 0;
		for (Long item : getSorted()) {
			sum = sum + item;
		}
		return sum;
	}

	public void addItem(Long item) {
		returnTimes.add(item);
		sortedReturnTimes.add(item);
	}

	public List<Long> getRawData() {
		return returnTimes;
	}

	@Override
	public String toString() {
		if (returnTimes.size() == 0) {
			return "[raw data=" + returnTimes + "]";
		}
		LOG.fine("[raw data=" + returnTimes + "]; sorted data ["+sortedReturnTimes+"]");
		return "[raw data=" + returnTimes + ", minimum=" + getMinimum()
				+ ", median=" + getMedian() + ", 90%line=" + getLine90Percent()
				+ ", maximum=" + getMaximum() + ", average=" + getAverage()
				+ "] ";
	}

}
