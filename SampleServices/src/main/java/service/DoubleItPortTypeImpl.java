package service;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.schema.doubleit.DoubleItRequest;
import org.example.schema.doubleit.DoubleItResponse;

public class DoubleItPortTypeImpl implements DoubleItPortType {

   @Resource
   private WebServiceContext context;

	@Override
	public DoubleItResponse doubleIt(DoubleItRequest parameters) {
		DoubleItResponse doubleItResponse = new DoubleItResponse();
		doubleItResponse.setDoubledNumber(parameters.getNumberToDouble()*2);
		return doubleItResponse;
	}
}

