package service;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.example.contract.doubleit.DoubleItFault;
import org.example.contract.doubleit.DoubleItPortType;
import org.example.schema.doubleit.DoubleItException;
import org.example.schema.doubleit.DoubleItRequest;
import org.example.schema.doubleit.DoubleItResponse;

public class DoubleItPortTypeImpl implements DoubleItPortType {

   @Resource
   private WebServiceContext context;

	@Override
	public DoubleItResponse doubleIt(DoubleItRequest parameters)throws DoubleItFault {
		DoubleItResponse doubleItResponse = new DoubleItResponse();
		if(parameters.getNumberToDouble()==10){
			DoubleItException doubleItException = new DoubleItException();
			doubleItException.setNumberThatwasntdoubled(parameters.getNumberToDouble());
			throw new DoubleItFault("Can't double. ", doubleItException);
		}
		doubleItResponse.setDoubledNumber(parameters.getNumberToDouble()*2);
		return doubleItResponse;
	}
}

