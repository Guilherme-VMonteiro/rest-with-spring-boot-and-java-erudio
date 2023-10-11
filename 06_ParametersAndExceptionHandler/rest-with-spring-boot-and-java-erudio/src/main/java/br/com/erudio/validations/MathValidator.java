package br.com.erudio.validations;

import org.springframework.stereotype.Component;

@Component
public class MathValidator {

	public Double convertToDouble(String strNumber) {
		if(strNumber == null) return 0D;
		
		String strTemp = strNumber.replaceAll(",",".");
		
		if(isNumeric(strTemp)) return  Double.parseDouble(strTemp);
		return 0D;
	}
	
	boolean isNumeric(String strNumber) {
		if(strNumber == null) return false;
		
		String strTemp = strNumber.replaceAll(",",".");
		
		return strTemp.matches("[+-]?[0-9]*\\.?[0-9]+");
	}
}
