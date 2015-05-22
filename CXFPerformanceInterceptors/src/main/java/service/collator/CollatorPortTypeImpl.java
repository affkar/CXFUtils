package service.collator;

import org.example.contract.collator.CollatorPortType;
import org.example.schema.collator.PrintLogsRequest;
import org.example.schema.collator.PrintLogsResponse;
import org.example.schema.collator.ResetRequest;
import org.example.schema.collator.ResetResponse;

import service.performance.CollatorDaemon;

public class CollatorPortTypeImpl implements CollatorPortType{

	private CollatorDaemon collatorDaemon=CollatorDaemon.getInstance();
	
	@Override
	public ResetResponse reset(ResetRequest parameters) {
		collatorDaemon.reset();
		return new ResetResponse();
	}

	@Override
	public PrintLogsResponse printLogs(PrintLogsRequest parameters) {
		
		PrintLogsResponse printLogsResponse = new PrintLogsResponse();
		printLogsResponse.getLogs().addAll(collatorDaemon.log());
		return printLogsResponse;
	}
	
}