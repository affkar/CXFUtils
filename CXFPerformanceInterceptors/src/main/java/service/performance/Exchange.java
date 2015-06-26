package service.performance;

public class Exchange {

	private String requestPayload;
	private String responsePayload;
	public String getRequestPayload() {
		return requestPayload;
	}
	public void setRequestPayload(String requestPayload) {
		this.requestPayload = requestPayload;
	}
	public String getResponsePayload() {
		return responsePayload;
	}
	public void setResponsePayload(String responsePayload) {
		this.responsePayload = responsePayload;
	}
	
	public static Exchange getExchange(String requestPayload){
		Exchange exchange=new Exchange();
		exchange.setRequestPayload(requestPayload);
		return exchange;
	}
	
	
}
