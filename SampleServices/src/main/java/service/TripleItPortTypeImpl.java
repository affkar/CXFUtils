package service;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.example.contract.tripleit.TripleItFault;
import org.example.contract.tripleit.TripleItPortType;
import org.example.schema.tripleit.TripleItException;
import org.example.schema.tripleit.TripleItRequest;
import org.example.schema.tripleit.TripleItResponse;

public class TripleItPortTypeImpl implements TripleItPortType {

	@Resource
	private WebServiceContext context;
	
	@Override
	public TripleItResponse tripleIt(TripleItRequest parameters) throws TripleItFault{
		TripleItResponse tripleItResponse = new TripleItResponse();
		if(parameters.getNumberToTriple()==9){
			TripleItException tripleItException = new TripleItException();
			tripleItException.setNumberThatCantBeTripled(parameters.getNumberToTriple());
			throw new TripleItFault("can't triple", tripleItException);
		}
		tripleItResponse.setTripledNumber(parameters.getNumberToTriple()*3);
		return tripleItResponse;
	}

}