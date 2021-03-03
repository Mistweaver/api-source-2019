package com.api.demo.pricedata.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Stack;

public class ShuntingYardAlgo {
	private static Logger logger = LoggerFactory.getLogger(ShuntingYardAlgo.class);

	public ShuntingYardAlgo() {}

	public String calculateEquationValue(String infix) {
		// logger.info("SHUNTING YARD CALCULATION");
		// logger.info("String to eval: " + infix);

		// preprocess the equation string

		infix = infix.replace("+", " + ");
		infix = infix.replace("-", " - ");
		infix = infix.replace("/", " / ");
		infix = infix.replace("*", " * ");
		infix = infix.replace("^", " ^ ");
		infix = infix.replace(")", " ) ");
		infix = infix.replace("(", " ( ");

		// logger.info("Preprocessed : " + infix);


		// src https://rosettacode.org/wiki/Parsing/Shunting-yard_algorithm#Java
		 /* To find out the precedence, we take the index of the
           token in the ops string and divide by 2 (rounding down).
           This will give us: 0, 0, 1, 1, 2 */
		final String ops = "-+/*^";

		StringBuilder sb = new StringBuilder();
		Stack<Integer> s = new Stack<>();

		for (String token : infix.split("\\s")) {

			// logger.info("Token: " + token);

			if (token.isEmpty())
				continue;
			char c = token.charAt(0);
			int idx = ops.indexOf(c);

			// check for operator
			if (idx != -1) {
				// logger.info("Operator found");
				if (s.isEmpty()) {
					// logger.info("Is empty");
					s.push(idx);
				} else {
					// logger.info("Not empty");
					while (!s.isEmpty()) {
						int prec2 = s.peek() / 2;
						int prec1 = idx / 2;
						if (prec2 > prec1 || (prec2 == prec1 && c != '^'))
							sb.append(ops.charAt(s.pop())).append(' ');
						else break;
					}
					s.push(idx);
				}
			}
			else if (c == '(') {
				// logger.info("Parenthesis left");
				s.push(-2); // -2 stands for '('
			}
			else if (c == ')') {
				// logger.info("Parenthesis right");

				// until '(' on stack, pop operators.
				while (s.peek() != -2)
					sb.append(ops.charAt(s.pop())).append(' ');
				s.pop();
			}
			else {
				// logger.info("Idk?");
				sb.append(token).append(' ');
			}
		}
		while (!s.isEmpty())
			sb.append(ops.charAt(s.pop())).append(' ');

		// process the postfix
		// create an empty stack
		// Stack<Integer> stack = new Stack<>();
		Stack<String> stack = new Stack<>();
		// traverse the given expression
		// src https://www.techiedelight.com/evaluate-given-postfix-expression/
		// logger.info("Postfix: " + sb.toString());
		String[] equationArray = sb.toString().split("\\s+");
		// for (char c: sb.toString().toCharArray()) {
		for (String c : equationArray) {
			// logger.info("String " + c);
			// if current char is an operand, push it to the stack
			// if (Character.isDigit(c)) {
			try {
				BigDecimal value = new BigDecimal(c);
				// logger.info("Numeric value found" );
				// logger.info("Value: " + value);
				//stack.push(c - '0');
				stack.push(value.toString());
				// logger.info("Stack");
				// logger.info(stack.toString());
			} catch (NumberFormatException e) {
				// if current char is an operator

				// logger.info("Current character is an operator");
				// logger.info("CHARACTER: " + c);
				// logger.info("Stack");
				// logger.info(stack.toString());
				// pop top two elements from the stack
				// int x = stack.pop();
				String x = stack.pop();
				// int y = stack.pop();
				String y = stack.pop();

				// evaluate the expression x op y, and push the
				// result back to the stack
				if (c.equals("+")) {
					// logger.info("Adding values");
					BigDecimal bigX = new BigDecimal(x);
					BigDecimal bigY = new BigDecimal(y);
					BigDecimal result = bigY.add(bigX, MathContext.DECIMAL128);

					stack.push(result.toString());
				} else if (c.equals("-")) {
					// logger.info("Subtract values");

					BigDecimal bigX = new BigDecimal(x);
					BigDecimal bigY = new BigDecimal(y);
					BigDecimal result = bigY.subtract(bigX, MathContext.DECIMAL128);

					stack.push(result.toString());
				} else if (c.equals("*")) {
					// logger.info("Multiply values");

					BigDecimal bigX = new BigDecimal(x);
					BigDecimal bigY = new BigDecimal(y);
					BigDecimal result = bigY.multiply(bigX, MathContext.DECIMAL128);

					stack.push(result.toString());
				} else if (c.equals("/")) {
					// logger.info("Divide values");

					BigDecimal bigX = new BigDecimal(x);
					BigDecimal bigY = new BigDecimal(y);
					BigDecimal result = bigY.divide(bigX, MathContext.DECIMAL128);

					stack.push(result.toString());
				}

			}
		}

		// At this point, the stack is left with only one element i.e.
		// expression result
		return stack.pop();
	}
}
