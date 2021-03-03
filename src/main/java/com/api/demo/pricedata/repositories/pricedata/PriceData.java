package com.api.demo.pricedata.repositories.pricedata;

import com.api.demo.mongorepositories.BasicEntity;
import com.api.demo.pricedata.repositories.equations.Equation;
import com.api.demo.pricedata.repositories.equations.EquationData;
import com.api.demo.pricedata.repositories.models.Model;
import com.api.demo.pricedata.repositories.variables.Variable;
import com.api.demo.pricedata.repositories.variables.VariableData;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.api.demo.pricedata.KeyConstants.*;
import static com.api.demo.pricedata.repositories.pricedata.PriceDataStatus.*;

@Getter
// @Setter
@Document(collection = "pricedata")
public class PriceData extends BasicEntity {
	private static Logger logger = LoggerFactory.getLogger(PriceData.class);

	@Indexed
	private String modelId;
	private Model model;
	@Indexed
	private String locationId;
	@Indexed
	private String status;		// pending, active, retired
	@Indexed
	private String createdFromPriceDataId;

	@Indexed
	private String activeDate;
	@Indexed
	private String expirationDate;

	@Indexed
	private boolean dataUpdated;	// if the model, variables, equations, etc are changed, set this flag true

	@Indexed
	private boolean error;
	private String errorDetails;

	// list of variables and equations used to calculate the subsequent variables
	private List<VariableData> variables;
	private List<EquationData> equations;

	@Indexed
	private String name;
	@Indexed
	private String seriesName;

	// calculated variables
	// this value is calculated.  To change the actual base price, you need to change the *variableDTO*
	private BigDecimal basePrice;
	private BigDecimal factoryTotalCost;
	private BigDecimal msrp;
	private BigDecimal factoryDirectPrice;
	private BigDecimal firstHalfAdvertisingPrice;
	private BigDecimal secondHalfAdvertisingPrice;

	public PriceData() {
		this.modelId = "";
		this.locationId = "";
		this.status = DRAFT;
		this.createdFromPriceDataId = "";
		this.activeDate = "";
		this.expirationDate = "";

		this.error = false;
		this.dataUpdated = false;
		this.errorDetails = "";

		this.variables = new ArrayList<>();
		this.equations = new ArrayList<>();

		this.name = "";
		this.seriesName = "";
		this.basePrice = new BigDecimal(0);
		this.factoryTotalCost = new BigDecimal(0);
		this.msrp  = new BigDecimal(0);
		this.factoryDirectPrice = new BigDecimal(0);
		this.firstHalfAdvertisingPrice = new BigDecimal(0);
		this.secondHalfAdvertisingPrice = new BigDecimal(0);
	}

	public void setToActive() {
		if(!this.error && !this.dataUpdated) {
			this.status = ACTIVE;
		}
	}

	public boolean isActive() {
		return this.status.equals(ACTIVE);
	}

	public void setToPending() {
		if(!this.error && !this.dataUpdated) {
			this.status = PENDING;
		}
	}

	public boolean isPending() {
		return this.status.equals(PENDING);
	}

	public void setToExpired() {
		this.status = EXPIRED;
	}

	public boolean isExpired() {
		return this.status.equals(EXPIRED);
	}

	public void expireData(String date) {	// mm/dd/yyyy
		this.setExpirationDate(date);
		this.status = EXPIRED;
	}

	public void setToDraft() { this.status = DRAFT; }

	public boolean isDraft() {
		return this.status.equals(DRAFT);
	}

	public void addVariable(VariableData newVariable) {
		// check that variable does not already exist
		if(this.getVariable(newVariable.getId()) == null) {
			this.variables.add(newVariable);
			this.dataUpdated = true;
		} else {
			this.setError("Variable " + newVariable.getKey() + " (" + newVariable.getId() + ") already exists");
		}
	}

	public void removeVariable(String id) {
		VariableData var = this.getVariable(id);
		if(var != null) {
			boolean removed = this.variables.remove(var);
			if(removed) {
				removeKeyFromEquations(var.getKey());
				this.dataUpdated = true;
			}
		}
	}

	public void updateDataFromVariable(Variable variable) {
		VariableData data = this.getVariable(variable.getId());
		if(data != null) {
			data.updateFromVariable(variable);
			this.dataUpdated = true;
		}
	}

	public void updateVariableData(VariableData newData) {
		int index = getIndexOfVariable(newData.getId());
		if(index != -1) {
			this.variables.set(index, newData);
			this.dataUpdated = true;
		}
	}

	public VariableData getVariable(String id) {
		// logger.info("Looking for variable by ID: " + id);
		for(int i = 0; i < this.variables.size(); i++) {
			VariableData variable = this.variables.get(i);
			if(variable.getId().equals(id)) {
				return variable;
			}
		}
		logger.warn("Could not find variable by ID: " + id);
		return null;
	}

	public VariableData getVariableByKey(String variableKey) {
		// logger.info("Looking for variable by key: " + variableKey);
		for(VariableData var : this.variables) {
			if(var.getKey().equals(variableKey)) {
				return var;
			}
		}
		logger.warn("Could not find variable by key: " + variableKey);
		return null;
	}

	public int getIndexOfVariable(String id) {
		for(int i = 0; i < this.variables.size(); i++) {
			VariableData data = this.variables.get(i);
			if(data.getId().equals(id)) {
				return i;
			}
		}
		return -1;
	}



	public void addEquation(EquationData newEquation) {
		// check that the equation does not already exist
		if(this.getEquation(newEquation.getId()) == null) {
			this.equations.add(newEquation);
			this.dataUpdated = true;
		} else {
			this.setError("Equation " + newEquation.getKey() + " (" + newEquation.getId() + ") already exists");
		}
	}

	public void removeEquation(String id) {
		EquationData eqn = this.getEquation(id);
		if(eqn != null) {
			this.equations.remove(eqn);
			this.dataUpdated = true;
		}
	}

	public void updateDataFromEquation(Equation equation) {
		EquationData data = this.getEquation(equation.getId());
		if(data != null) {
			data.updateFromEquation(equation);
			this.dataUpdated = true;
		}
	}

	public void updateEquationData(EquationData data) {
		int index = getIndexOfEquation(data.getId());
		if(index != -1) {
			this.equations.set(index, data);
			this.dataUpdated = true;
		}
	}

	public EquationData getEquation(String id) {
		// logger.info("Looking for equation by ID: " + id);
		for(int i = 0; i < this.equations.size(); i++) {
			EquationData data = this.equations.get(i);
			if(data.getId().equals(id)) {
				return data;
			}
		}
		logger.warn("Could not find equation by ID: " + id);
		return null;
	}

	public int getIndexOfEquation(String id) {
		// logger.info("Getting index of equation : " + id);
		for(int i = 0; i < this.equations.size(); i++) {
			EquationData data = this.equations.get(i);
			if(data.getId().equals(id)) {
				return i;
			}
		}
		logger.warn("Could not find equation by index: " + id);
		return -1;
	}

	public EquationData getEquationDataByKey(String equationKey) {
		// logger.info("Looking for equation by key: " + equationKey);
		for(int i = 0; i < this.equations.size(); i++) {
			EquationData data = this.equations.get(i);
			if(data.getKey().equals(equationKey)) {
				return data;
			}
		}
		logger.warn("Could not find equation by key: " + equationKey);
		return null;
	}

	public void setError(String _errorMessage) {
		this.error = true;
		this.errorDetails = _errorMessage;
		logger.error(_errorMessage);
	}

	public void clearError() {
		this.error = false;
		this.errorDetails = "";
	}

	public void clearUpdate() {
		this.dataUpdated = false;
	}

	public void setVariables(List<VariableData> _variables) {
		this.variables = _variables;
		this.dataUpdated = true;
	}
	public void setEquations(List<EquationData> _equations) {
		this.equations = _equations;
		this.dataUpdated = true;
	}

	public void setLocationId(String _locationId) { this.locationId = _locationId; }
	public void setSeriesName(String _seriesName) { this.seriesName = _seriesName; }
	public void setName(String _name) { this.name = _name; }

	public void setBasePrice(BigDecimal _basePrice) {
		try {
			/*try {
				// Update the variable...
				VariableData basePriceVariable = this.getVariableByKey(BASE_PRICE_KEY);
				basePriceVariable.setValue(_basePrice);
			} catch(Exception e) {
				logger.warn("Base price variable does not exist");
			}*/

			//...as well as the base price value
			this.basePrice = _basePrice;
			this.dataUpdated = true;
		} catch(Exception e) {
			e.printStackTrace();
			this.setError("Failed to update base price from input: " + _basePrice);
		}
	}

	public void setActiveDate(String _date) {
		this.activeDate = _date;
	}
	public void setExpirationDate(String _date) {
		this.expirationDate = _date;
	}

	/**
	 * Sets the model for the price data.  This will set the price data state to updated
	 * @param _model	Model to update the data with
	 */
	public void setModel(Model _model) {
		this.model = _model;
		this.modelId = _model.getId();
		this.dataUpdated = true;
	}

	public void updateValues() {
		// set error to false
		this.clearError();
		calculateFactoryDirectPrice();
		calculateFactoryTotalCost();
		calculateFirstHalfDiscount();
		calculateMSRP();
		calculateSecondHalfDiscount();
		// if no error occured, remove the dataUpdated flag
		if(!this.error) {
			this.dataUpdated = false;
		}
	}


	public void calculateFactoryTotalCost() {
		EquationData equation = getEquationDataByKey(FACTORY_COST_KEY);
		if(equation == null) {
			VariableData variable = getVariableByKey(FACTORY_COST_KEY);
			if(variable == null) {
				this.setError(FACTORY_COST_KEY + " is neither a variable nor equation");
			} else {
				this.factoryTotalCost = variable.getValue();
			}
		} else {
			this.factoryTotalCost = this.solveEquation(equation);
		}
	}

	public void calculateMSRP() {
		EquationData equation = getEquationDataByKey(MSRP_KEY);
		if(equation == null) {
			VariableData variable = getVariableByKey(MSRP_KEY);
			if(variable == null) {
				this.setError(MSRP_KEY + " is neither a variable nor equation");
			} else {
				this.msrp = variable.getValue();
			}
		} else {
			this.msrp = this.solveEquation(equation);
		}
	}

	public void calculateFactoryDirectPrice() {
		EquationData equation = getEquationDataByKey(FACTORY_DIRECT_PRICE_KEY);
		if(equation == null) {
			VariableData variable = getVariableByKey(FACTORY_DIRECT_PRICE_KEY);
			if(variable == null) {
				this.setError(FACTORY_DIRECT_PRICE_KEY + " is neither a variable nor equation");
			} else {
				this.factoryDirectPrice = variable.getValue();
			}
		} else {
			this.factoryDirectPrice = this.solveEquation(equation);
		}
	}

	public void calculateFirstHalfDiscount() {
		EquationData equation = getEquationDataByKey(FIRST_HALF_DISCOUNT_KEY);
		if(equation == null) {
			VariableData variable = getVariableByKey(FIRST_HALF_DISCOUNT_KEY);
			if(variable == null) {
				this.setError(FIRST_HALF_DISCOUNT_KEY + " is neither a variable nor equation");
			} else {
				this.firstHalfAdvertisingPrice = variable.getValue();
			}
		} else {
			this.firstHalfAdvertisingPrice = this.solveEquation(equation);
		}
	}

	public void calculateSecondHalfDiscount() {
		EquationData equation = getEquationDataByKey(SECOND_HALF_DISCOUNT_KEY);
		if(equation == null) {
			VariableData variable = getVariableByKey(SECOND_HALF_DISCOUNT_KEY);
			if(variable == null) {
				this.setError(SECOND_HALF_DISCOUNT_KEY + " is neither a variable nor equation");
			} else {
				this.secondHalfAdvertisingPrice = variable.getValue();
			}
		} else {
			this.secondHalfAdvertisingPrice = this.solveEquation(equation);
		}
	}

	public BigDecimal solveEquation(EquationData equation) {
		try {
			// pass the equation string and equation key, NOT the equation
			String equationString = evaluateVariablesInEquation(equation.getEquation(), false);
			// System.out.println("Equation String going to Shunting Yard");
			// System.out.println(equationString);
			return new BigDecimal(calculateEquationValue(equationString));
		} catch (Exception e) {
			this.setError("Failed to solve equation: " + equation.getKey() + " : " + equation.getEquation());
			logger.error("Failed to solve equation: " + equation.getKey());
			EquationDebug();
			return new BigDecimal(0);
		}
	}

	public void EquationDebug() {
		logger.error("EQUATION DEBUG");
		for(EquationData equation : this.equations) {
			logger.error("-----------------------------");
			logger.error("Solving " + equation.getEquation());
			String equationString = evaluateVariablesInEquation(equation.getEquation(), true);
			logger.error("= " + equationString);
			logger.error(calculateEquationValue(equationString));
		}
	}

	private String evaluateVariablesInEquation(String equationString, boolean debug) {
		// System.out.println("Evaluating String");
		// System.out.println(equationString);
		try {
			for(VariableData variable : this.variables) {
				// System.out.println("Inserting [ " + variable.getKey() + ", " + variable.getValue().toString() + "]");
				equationString = equationString.replace(variable.getKey(), variable.getValue().toString());
			}

			// System.out.println("Variables Swapped");
			// System.out.println(equationString);

			// replace the core price data variables
			equationString = equationString.replace(BASE_PRICE_KEY, this.basePrice.toString());
			equationString = equationString.replace(SQUARE_FEET_KEY, this.model.getEstimatedSquareFeet().toString());

			// System.out.println("Core Price Data Swapped");
			// System.out.println(equationString);

			// does equation key exist in string
			for(EquationData existingEquation : equations) {
				// is the equation key blank or equal to the equation in question?
				// not sure why I originally wrote this check.  Leaving it out for now and seeing what happens
				// if(!equationKey.equals("") && !equationKey.equals(existingEquation.getKey())) {
				// does the equation key exist in the equation string?
				boolean equationKeyFound = equationString.contains(existingEquation.getKey());
				if(equationKeyFound && !this.error) {
					// System.out.println("Equation string: " + equationString + " contained equation key " + existingEquation.getKey());
					// System.out.println("Existing equation: " + existingEquation.getEquation());
					equationString = " ( " + equationString.replace(existingEquation.getKey(), " ( " + existingEquation.getEquation() + " ) ") + " ) ";
					// if an equation exists in the equation string, use some recursion here
					equationString = evaluateVariablesInEquation(equationString,debug);
				}
			}



		} catch (Exception e) {
			this.setError("Failed to evaluate variables in equation string: " + equationString);
			return "";
		}

		if(debug) {
			logger.error(equationString);
		}
		System.out.println("Returning string");
		System.out.println(equationString);
		return "( " + equationString +  " )";
	}

	public String calculateEquationValue(String infix) {
		if(!this.error) {


			try {
				// HACK TO FIX THE LINGERING []
				infix = infix.replaceAll("\\[", "");
				infix = infix.replaceAll("\\]", "");

				// System.out.println("SHUNTING YARD CALCULATION");
				// System.out.println("String to eval: " + infix);

				// preprocess the equation string

				infix = infix.replace("+", " + ");
				infix = infix.replace("-", " - ");
				infix = infix.replace("/", " / ");
				infix = infix.replace("*", " * ");
				infix = infix.replace("^", " ^ ");
				infix = infix.replace(")", " ) ");
				infix = infix.replace("(", " ( ");

				// System.out.println("Preprocessed : " + infix);


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
					} else if (c == '(') {
						// logger.info("Parenthesis left");
						s.push(-2); // -2 stands for '('
					} else if (c == ')') {
						// logger.info("Parenthesis right");

						// until '(' on stack, pop operators.
						while (s.peek() != -2)
							sb.append(ops.charAt(s.pop())).append(' ');
						s.pop();
					} else {
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
						switch (c) {
							case "+": {
								// logger.info("Adding values");
								BigDecimal bigX = new BigDecimal(x);
								BigDecimal bigY = new BigDecimal(y);
								BigDecimal result = bigY.add(bigX, MathContext.DECIMAL128);

								stack.push(result.toString());
								break;
							}
							case "-": {
								// logger.info("Subtract values");

								BigDecimal bigX = new BigDecimal(x);
								BigDecimal bigY = new BigDecimal(y);
								BigDecimal result = bigY.subtract(bigX, MathContext.DECIMAL128);

								stack.push(result.toString());
								break;
							}
							case "*": {
								// logger.info("Multiply values");

								BigDecimal bigX = new BigDecimal(x);
								BigDecimal bigY = new BigDecimal(y);
								BigDecimal result = bigY.multiply(bigX, MathContext.DECIMAL128);

								stack.push(result.toString());
								break;
							}
							case "/": {
								// logger.info("Divide values");

								BigDecimal bigX = new BigDecimal(x);
								BigDecimal bigY = new BigDecimal(y);
								BigDecimal result = bigY.divide(bigX, MathContext.DECIMAL128);

								stack.push(result.toString());
								break;
							}
						}

					}
				}

				// At this point, the stack is left with only one element i.e.
				// expression result
				return stack.pop();
			} catch (Exception e) {
				this.setError("Shunting Yard Algo Failure: " + infix);
				e.printStackTrace();
				return "";
			}
		} else {
			return "";
		}
	}


	private void updateKeyInEquations(String oldKey, String newKey) {
		// if the key has been updated, reflect this in all the equations
		for(int j = 0; j < this.equations.size(); j++) {
			EquationData equation = this.equations.get(j);
			/*while(equation.getEquation().contains(oldKey)) {
				String equationString = equation.getEquation();
				String newEquationString = equationString.replaceAll(oldKey, newKey);
				equation.setEquation(newEquationString);
			}*/
			String equationString = equation.getEquation();

			Pattern keyPattern = Pattern.compile(Pattern.quote(oldKey), Pattern.DOTALL);
			Matcher keyMatcher = keyPattern.matcher(equationString);
			List<String> allMatches = new ArrayList<String>();

			while (keyMatcher.find()) {
				allMatches.add(keyMatcher.group(0));
				// System.out.println("MATCHES " + allMatches.toString());
				for (String match : allMatches) {
					// System.out.println("Matched: " + match + " in [" + equation.getEquation() + "]");
					// for each match, swap with the variable value
					// logger.warn(equation.getEquation());
					String replacementString = equation.getEquation().replace(match, newKey);
					equation.setEquation(replacementString);
					// logger.warn(equation.getEquation());

				}
			}

		}
	}

	private void removeKeyFromEquations(String key) {
		// check each equation for the variable key
		for(int j = 0; j < this.equations.size(); j++) {
			EquationData equation = this.equations.get(j);
			while(equation.getEquation().contains(key)) {
				String equationString = equation.getEquation();
				// System.out.println("Variable found in equation string");
				// System.out.println(equationString);
				String newEquationString = this.removeKey(key, equationString);
				equation.setEquation(newEquationString);
				// System.out.println("New equation string");
				// System.out.println(equation.getEquation());
				this.dataUpdated = true;
			}
		}
	}

	private String removeKey(String key, String equation) {
		StringBuilder equationString = new StringBuilder(equation);
		int variableIndex = equationString.indexOf(key);
		// System.out.println(variableIndex);
		if(variableIndex != -1) {
			// System.out.println("Variable exists in equation [" + equationString + "] at index " + variableIndex);
			// find the first character to the left and the first character to the right of the variable
			int idxLeft = this.firstNonWhiteSpaceCharLeft(equationString.toString(), variableIndex);

			if(idxLeft != -1) {
				char characterLeft = equationString.charAt(idxLeft);
				// System.out.println("Character Left: " + characterLeft + " ( index: " + idxLeft + " )");
				// character to the left is an operator, remove the operator
				// System.out.println("Deleting chars");
				//for(int i = idxLeft; i < variableIndex; i++) {
					//System.out.println("Index: " + i);
					//System.out.println("Deleting " + equationString.charAt(idxLeft) + " at index " + idxLeft);
					//equationString.deleteCharAt(idxLeft);
				//}
				equationString.deleteCharAt(idxLeft);
			} else {
				// else, get the character to the right
				int idxRight = this.firstNonWhiteSpaceCharRight(equationString.toString(), variableIndex + key.length());

				// if operator, remove variable and the operator
				if(idxRight != -1) {
					char characterRight = equationString.charAt(idxRight);
					// System.out.println("Character Right: " + characterRight + " ( index: " + idxRight + " )");
					// System.out.println("Deleting chars right");
					// character to the right is an operator, remove the operator
					//int indexAnchor = variableIndex + variableToRemove.length();
					//for(int i = indexAnchor; i <= idxRight; i++) {
						//equationString.deleteCharAt(i);
					//}
					equationString.deleteCharAt(idxRight);
				}
			}
			// remove the variable
			equationString = new StringBuilder(equationString.toString().replace(key, ""));
			variableIndex = equationString.indexOf(key);
			// System.out.println("New equation [" + equationString.toString().replaceAll("\\s\\s+", " ") + "]");

			// check for variable occurring more than once
			if(variableIndex != -1) {
				equationString = new StringBuilder(this.removeKey(key, equation));
			}

		}

		return equationString.toString().replaceAll("\\s\\s+", "");

	}

	private int firstNonWhiteSpaceCharLeft(String equationString, int variableIndex) {
		for(int i = 1; i < variableIndex; i++) {
			char characterLeft = equationString.charAt(variableIndex - i);
			// System.out.println("Character " + i + " indexes left of " + variableIndex + " : " + characterLeft);
			if(!Character.isWhitespace(characterLeft)) {
				return variableIndex - i;
			}
		}
		return -1;
	}

	private int firstNonWhiteSpaceCharRight(String equationString, int searchIndex) {
		for(int i = searchIndex; i < equationString.length(); i++) {
			char characterRight = equationString.charAt(i);
			// System.out.println("Character " + i + " indexes right of " + searchIndex + " : " + characterRight);
			if(!Character.isWhitespace(characterRight)) {
				return i;
			}
		}
		return -1;
	}

	public PriceData createDraftFromSelf(String draftDate) {
		PriceData copy = copyData(draftDate);
		return copy;
	}

	public PriceData createDraftFromSelfForLocation(String locationId, String draftDate) {
		PriceData copy = copyData(draftDate);
		copy.setLocationId(locationId);
		return copy;
	}


	public PriceData copyData(String draftDate) {
		PriceData copy = new PriceData();

		copy.modelId = this.modelId;
		copy.model = this.model;
		copy.locationId = locationId;
		copy.status = DRAFT;
		copy.createdFromPriceDataId = this.getId();

		copy.activeDate = draftDate;
		copy.expirationDate = "";

		copy.error = this.error;
		copy.errorDetails = this.errorDetails;
		copy.dataUpdated = this.dataUpdated;

		copy.variables = this.variables;
		copy.equations = this.equations;

		copy.name = this.name;
		copy.seriesName = this.seriesName;
		copy.basePrice = this.basePrice;
		copy.factoryTotalCost = this.factoryTotalCost;
		copy.msrp  = this.msrp;
		copy.factoryDirectPrice = this.factoryDirectPrice;
		copy.firstHalfAdvertisingPrice = this.firstHalfAdvertisingPrice;
		copy.secondHalfAdvertisingPrice = this.secondHalfAdvertisingPrice;

		return copy;
	}
}
