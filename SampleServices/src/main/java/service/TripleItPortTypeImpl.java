package service;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.example.contract.tripleit.TripleItPortType;
import org.example.schema.tripleit.TripleItRequest;
import org.example.schema.tripleit.TripleItResponse;

public class TripleItPortTypeImpl implements TripleItPortType {

	@Resource
	private WebServiceContext context;
	
	@Override
	public TripleItResponse tripleIt(TripleItRequest parameters) {
		TripleItResponse tripleItResponse = new TripleItResponse();
		tripleItResponse.setTripledNumber(parameters.getNumberToTriple()*3);
		return tripleItResponse;
	}

}