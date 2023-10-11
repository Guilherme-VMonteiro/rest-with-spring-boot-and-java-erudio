package br.com.erudio.validations;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.erudio.exceptions.UnsupportedMathOperationException;

@Component
public class Operations {

	@Autowired
	MathValidator mathValidator;

	public Double sum(String s1, String s2) {

		if (!mathValidator.isNumeric(s1) || !mathValidator.isNumeric(s2)) {
			throw new UnsupportedMathOperationException("Please set a numeric value");
		}

		return mathValidator.convertToDouble(s1) + mathValidator.convertToDouble(s2);
	}

	public Double sub(String s1, String s2) {

		if (!mathValidator.isNumeric(s1) || !mathValidator.isNumeric(s2)) {
			throw new UnsupportedMathOperationException("Please set a numeric value");
		}

		return mathValidator.convertToDouble(s1) - mathValidator.convertToDouble(s2);
	}

	public Double mult(String s1, String s2) {

		if (!mathValidator.isNumeric(s1) || !mathValidator.isNumeric(s2)) {
			throw new UnsupportedMathOperationException("Please set a numeric value");
		}

		return mathValidator.convertToDouble(s1) * mathValidator.convertToDouble(s2);
	}

	public Double div(String s1, String s2) {

		if (!mathValidator.isNumeric(s1) || !mathValidator.isNumeric(s2)) {
			throw new UnsupportedMathOperationException("Please set a numeric value");
		}

		return mathValidator.convertToDouble(s1) / mathValidator.convertToDouble(s2);
	}

	public Double sqrt(String s1) {

		if (!mathValidator.isNumeric(s1)) {
			throw new UnsupportedMathOperationException("Please set a numeric value");
		}

		return Math.sqrt(mathValidator.convertToDouble(s1));
	}

	public Double avg(List<String> values) {

		if (!values.stream().allMatch(num -> mathValidator.isNumeric(num))) {
			throw new UnsupportedMathOperationException("Please set a numeric value");
		}

		List<Double> nums = values.stream().map(str -> {
			try {
				return Double.parseDouble(str);
			} catch (Exception e) {
				throw new NumberFormatException("Error parsing double value: " + e.getMessage());
			}
		}).collect(Collectors.toList());

		BinaryOperator<Double> somar = (ac, n) -> ac + n;
		Double allValues = nums.stream().reduce(somar).get();

		return allValues / nums.size();
	}

}
