package route.collator;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.example.schema.collator.PrintLogsRequest;
import org.example.schema.collator.PrintLogsResponse;
import org.example.schema.collator.ResetRequest;

public class CollatorRouteBuilder extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		from("jetty://http://localhost:8282/createPerfExcelAndReset")
		.to("direct:createPerfExcelAndReset");
		
		from("direct:printAndReset")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				PrintLogsRequest printLogsRequest = new PrintLogsRequest();
				printLogsRequest.setSomeBoolean(true);
				exchange.getIn().setBody(printLogsRequest);
			}
		})
		.to("bean:collatorDaemon?method=printLogs")
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				PrintLogsResponse response = exchange.getIn().getBody(PrintLogsResponse.class);
				exchange.getIn().setBody(response.getLogs());
			}
		})
		.to("bean:ExcelTransformerBean?method=toExcel")
		.process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				ResetRequest req=new ResetRequest();
				req.setSomeBoolean(true);
				exchange.getIn().setBody(req);
			}
		})
		.to("bean:collatorDaemon?method=reset");
		
	}
	
}
