package route.collator;

import org.apache.camel.builder.RouteBuilder;

public class CollatorRouteBuilder extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		from("jetty://http://localhost:8282/createPerfExcelAndReset")
		.to("bean:ExcelTransformerBean?method=toExcel")
		.to("bean:collatorDaemon?method=reset");
		
	}
	
}
