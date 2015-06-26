package service.performance;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class Configuration implements ManagedService{

	private static final String CONFIG_PID = "org.karthick.performance";
	private static final String RECORD_COUNT_KEY = CONFIG_PID + ".recordCount";
	private static final String RECORD_TIMES_KEY = CONFIG_PID + ".recordTimes";
	private static final String RECORD_PAYLOAD_SIZE_KEY = CONFIG_PID
			+ ".recordPayloadSize";
	private static final String RECORD_PAYLOAD_KEY = CONFIG_PID
			+ ".recordPayload";
	private static final String PERFORMANCE_INTERCEPTORS_ENABLED_KEY = CONFIG_PID
			+ ".performanceInterceptorsEnabled";
	private static final String BUCKET_INTERVAL_KEY = CONFIG_PID+".bucketInterval";
	private boolean recordPayloadSize;
	private boolean recordPayload;
	private boolean recordTimes;
	private Integer bucketInterval;

	private boolean recordCount;
	private boolean performanceInterceptorsEnabled;
//	private BundleContext blueprintBundleContext;
//	private BlueprintContainer blueprintContainer;
	private Hashtable defaults = new Hashtable() {
		{
			put(PERFORMANCE_INTERCEPTORS_ENABLED_KEY, false);
			put(RECORD_COUNT_KEY, false);
			put(RECORD_TIMES_KEY, false);
			put(RECORD_PAYLOAD_SIZE_KEY, false);
			put(RECORD_PAYLOAD_KEY, false);
			put(BUCKET_INTERVAL_KEY, false);
		}
	};

	public Configuration(BundleContext blueprintBundleContext,BlueprintContainer blueprintContainer) {
		super();
//		this.blueprintBundleContext=blueprintBundleContext;
//		this.blueprintContainer=blueprintContainer;
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put(Constants.SERVICE_PID, CONFIG_PID);
		blueprintBundleContext.registerService(ManagedService.class.getName(), this,
				properties);

		//initialiseWithDefaults();
	}

	public void initialiseWithDefaults() {
		initialise(null);
	}

	public void initialise(Dictionary config1) {
		//DictionaryWithDefaults config = new DictionaryWithDefaults(config1,
		//		defaults);
		MyDictionary config=new MyDictionary(config1); 
		performanceInterceptorsEnabled = config
				.getBoolean(PERFORMANCE_INTERCEPTORS_ENABLED_KEY);
		recordCount = config.getBoolean(RECORD_COUNT_KEY);
		recordTimes = config.getBoolean(RECORD_TIMES_KEY);
		recordPayloadSize = config.getBoolean(RECORD_PAYLOAD_SIZE_KEY);
		recordPayload = config.getBoolean(RECORD_PAYLOAD_KEY);
		bucketInterval = config.getInteger(BUCKET_INTERVAL_KEY);
	}

	@Override
	public void updated(Dictionary config1) throws ConfigurationException {
		initialise(config1);
	}


	public boolean isRecordPayloadSize() {
		return recordPayloadSize;
	}

	public boolean isRecordPayload() {
		return recordPayload;
	}

	public boolean isRecordTimes() {
		return recordTimes;
	}

	public boolean isRecordCount() {
		return recordCount;
	}

	public boolean isPerformanceInterceptorsEnabled() {
		return performanceInterceptorsEnabled;
	}
	

	public void setRecordPayloadSize(boolean recordPayloadSize) {
		this.recordPayloadSize = recordPayloadSize;
	}

	public void setRecordPayload(boolean recordPayload) {
		this.recordPayload = recordPayload;
	}

	public void setRecordTimes(boolean recordTimes) {
		this.recordTimes = recordTimes;
	}

	public void setRecordCount(boolean recordCount) {
		this.recordCount = recordCount;
	}

	public void setPerformanceInterceptorsEnabled(
			boolean performanceInterceptorsEnabled) {
		this.performanceInterceptorsEnabled = performanceInterceptorsEnabled;
	}

	
	public void setBucketInterval(Integer bucketInterval) {
		this.bucketInterval = bucketInterval;
	}

	public Integer getBucketInterval() {
		return bucketInterval;
	}

	
	abstract class MyAbstractDictionary{
		public abstract Object get(Object key);
		public Boolean getBoolean(Object key) {
			Object value = get(key);
			return value instanceof Boolean ? (Boolean) value : Boolean
					.valueOf((String) value);
		}

		public Integer getInteger(Object key) {
			Object value = get(key);
			return value instanceof Integer ? (Integer) value : Integer
					.valueOf((String) value);
		}

		public String getString(Object key) {
			return (String) get(key);
		}
	}
	
	class MyDictionary extends MyAbstractDictionary{
		private Dictionary dictionary;
		public MyDictionary(Dictionary dictionary) {
			this.dictionary=dictionary;
		}
		@Override
		public Object get(Object key) {
			return dictionary.get(key);
		}
		
	}
	class DictionaryWithDefaults extends MyAbstractDictionary{

		private Dictionary dictionary;
		private Dictionary defaults;

		public DictionaryWithDefaults(Dictionary dictionary, Dictionary defaults) {
			super();
			if (dictionary == null) {
				System.out
						.println("No Properties supplied. Will try to use defaults");
			}
			this.dictionary = dictionary;
			this.defaults = defaults;
		}

		public Object get(Object key) {
			return dictionary != null ? dictionary.get(key) == null ? defaults
					.get(key) : dictionary.get(key) : defaults.get(key);
		}

		

	}

}
