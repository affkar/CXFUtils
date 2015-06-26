package service.collator;

import java.io.IOException;

import org.apache.cxf.common.util.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.example.contract.collator.CollatorPortType;
import org.example.schema.collator.GetLogsRequest;
import org.example.schema.collator.GetLogsResponse;
import org.example.schema.collator.ProduceExcelRequest;
import org.example.schema.collator.ProduceExcelResponse;

import service.output.ExcelTransformer;
import service.performance.CollatorDaemon;

public class CollatorPortTypeImpl implements CollatorPortType{

	private CollatorDaemon collatorDaemon=CollatorDaemon.getInstance();
	private ExcelTransformer excelTransformer;
	
	public CollatorPortTypeImpl(ExcelTransformer excelTransformer) {
		super();
		this.excelTransformer = excelTransformer;
	}


	@Override
	public GetLogsResponse getLogs(GetLogsRequest parameters) {
		
		GetLogsResponse printLogsResponse = new GetLogsResponse();
		printLogsResponse.getLogs().addAll(collatorDaemon.log());
		if(parameters.isAndReset()){
			collatorDaemon.reset();
		}
		return printLogsResponse;
	}


	@Override
	public ProduceExcelResponse produceExcel(ProduceExcelRequest parameters) {
		ProduceExcelResponse response=new ProduceExcelResponse();
		try {
			if(parameters.getExcelIdentifier()==null || parameters.getExcelIdentifier().equals(""))
				excelTransformer.toExcel();
			else
				excelTransformer.toExcel(parameters.getExcelIdentifier());
			if(parameters.isAndReset()){
				collatorDaemon.reset();
			}
			response.setSuccess(true);
		} catch (IOException e) {
			// success false by default
		} catch (InvalidFormatException e) {
			// success false by default
		}
		return response;
	}
	
}