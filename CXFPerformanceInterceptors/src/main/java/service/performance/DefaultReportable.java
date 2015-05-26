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
		if(getActualSize()==0){
			return 0;
		}
		return getSorted().get(0);
	}

	@Override
	public Number getMaximum() {
		if(getActualSize()==0){
			return 0;
		}
		return getSorted().get(getViewableSize() - 1);
	}

	@Override
	public Number getAverage() {
		if(getActualSize()==0){
			return 0;
		}
		return getSum() / getViewableSize();
	}

	@Override
	public Number getMedian() {
		double medianElement;
		if(getActualSize()==0){
			return 0;
		}
		if(getViewableSize() == 1){
			return getSorted().get(0);
		}else if (getViewableSize() % 2 == 1) {
			medianElement = getSorted().get(
					(int) (Math.ceil(getViewableSize() / 2) - 1));
		} else {
			medianElement = (getSorted().get((getViewableSize() / 2 - 1)) + getSorted()
					.get(getViewableSize() / 2)) / 2;
		}
		return medianElement;
	}

	@Override
	public Number getLine90Percent() {
		if(getActualSize()==0){
			return 0;
		}
		if(getViewableSize()==1){
			return getSorted().get(0);
		}
		int line90PercentIndex = (int) (getViewableSize() * 0.9);
		float fraction = (getViewableSize() * 0.9f) - line90PercentIndex;
		return (getSorted().get(line90PercentIndex-1)*fraction + (getSorted().get(line90PercentIndex)*(1-fraction)));
	}

	public List<Long> getSorted() {
		Collections.sort(sortedReturnTimes);
		return sortedReturnTimes;
	}

	public int getViewableSize() {
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

	public int getActualSize(){
		return returnTimes.size();
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
