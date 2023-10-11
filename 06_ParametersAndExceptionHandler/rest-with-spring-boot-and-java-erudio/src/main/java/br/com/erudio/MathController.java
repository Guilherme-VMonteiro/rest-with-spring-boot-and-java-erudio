package br.com.erudio;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.erudio.validations.Operations;

@RestController
public class MathController {
		
	@Autowired
	Operations operations;
	
	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();
	
	@RequestMapping(value = "/sum/{n1}/{n2}", method = RequestMethod.GET)
	public Double sum(
			@PathVariable(value = "n1") String n1, 
			@PathVariable(value = "n2") String n2
		) throws Exception{
		
		return operations.sum(n1, n2);
	}
	
	@RequestMapping(value = "/sub/{n1}/{n2}", method = RequestMethod.GET)
	public Double sub(
			@PathVariable(value = "n1") String n1, 
			@PathVariable(value = "n2") String n2
		) throws Exception{
		
		return operations.sub(n1, n2);
	}
	
	@RequestMapping(value = "/mult/{n1}/{n2}", method = RequestMethod.GET)
	public Double mult(
			@PathVariable(value = "n1") String n1, 
			@PathVariable(value = "n2") String n2
		) throws Exception{
		
		return operations.mult(n1, n2);
	}
	
	@RequestMapping(value = "/div/{n1}/{n2}", method = RequestMethod.GET)
	public Double div(
			@PathVariable(value = "n1") String n1, 
			@PathVariable(value = "n2") String n2
		) throws Exception{
		
		return operations.div(n1, n2);
	}
	
	@RequestMapping(value = "/sqrt/{n1}", method = RequestMethod.GET)
	public Double sqrt(
			@PathVariable(value = "n1") String n1
			) throws Exception{
		
		return operations.sqrt(n1);
	}
	
	@RequestMapping(value = "/avg/{n1}", method = RequestMethod.GET)
	public Double avg(
			@PathVariable(value = "n1") List<String> values
			) throws Exception{
		
		return operations.avg(values);
	}

}
