package service.performance;


public final class ServiceAndOperation {

	private final String service;
	private final String operation;
	public ServiceAndOperation(String service, String operation) {
		super();
		if(service==null || operation == null){
			throw new IllegalArgumentException("Service or Operation was null - service: "+service+" ,operation:"+operation);
		}
		this.service = service;
		this.operation = operation;
	}
	public String getService() {
		return service;
	}
	public String getOperation() {
		return operation;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((operation == null) ? 0 : operation.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceAndOperation other = (ServiceAndOperation) obj;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		return true;
	}
	
}
