import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;

public class Calculator {
    private Map<String, String> variables = new HashMap<>();
    private Map<String, String> commands = Map.of("/exit", "Bye!",
            "/help","The program calculates the sum and difference of numbers");
    private Map<String, Integer> precedence = Map.of("+", 1,
            "-", 1,
            "*", 2,
            "/", 2);
    private final List<String> expression = new ArrayList<>();
    private final Deque<String> operators = new ArrayDeque<>();
    private final Deque<String> postfixExpression = new ArrayDeque<>();
    private static final Pattern OPERAND = compile("-*[0-9]+|[A-Za-z]+");
    private static final Pattern OPERATOR = compile("[*/+-]|[()]");
    private static final Pattern LETTERS = compile("[A-Za-z]+");
    private static final Pattern DIGITS = compile("\\d+");
    private static final Pattern MULTIPLEOPERATORS = compile("\\+\\+|--");
    private static final Pattern INVALIDOPERATORs = compile("[*][*]+|//+");
    private Scanner scanner = new Scanner(System.in);

    // Method starts the calculator
    public void start() {
        while (true) {
            String input = scanner.nextLine().replaceAll(" ", "");
            if (input.equals("/exit")) {
                System.out.println(commands.get(input));
                break;
            } else if (input.isEmpty()) {
                continue;
            }
            processInput(input);
        }
    }
    //Method processes the input
    private void processInput(String input) {
        if (input.startsWith("/")) {
            validateCommand(input);
        } else if (input.contains("=")) {
            assignVariables(input);
        } else if ((OPERATOR.matcher(input).find())) {
            validateExpression(input);
        } else if (LETTERS.matcher(input).matches() &&
                variables.containsKey(input)) {
            System.out.println(variables.get(input));
        } else if (LETTERS.matcher(input).matches() &&
                !variables.containsKey(input)) {
            System.out.println("Unknown variable");
        }
    }

    //Method checks if the expression entered by the user is valid
    private void validateExpression(String input) {
        if (INVALIDOPERATORs.matcher(input).find()) {
            System.out.println("Invalid expression");
        } else if (OPERAND.matcher(input).matches()) {
            System.out.println(input);
        } else if (hasValidBrackets(input)) {
            prepareExpression(input);
            populatePostfixExpression();
            System.out.println(evaluateExpression(postfixExpression));
        } else {
            System.out.println("Invalid expression");
        }
    }
    // Method checks if the commands entered by the user are valid
    private void validateCommand(String input) {
        if (!commands.containsKey(input)) {
            System.out.println("Unknown command");
        } else {
            System.out.println(commands.get(input));
        }
    }
    // Method checks if the variables and values provided by the user are correct and
    // stores them in a Map
    private void assignVariables(String input) {
        String[] values = new String[input.length()];
        values = input.replaceAll("\\s+", "")
                .split("=");
        if (values.length > 2) {
            System.out.println("Invalid assignment");
        } else if (values.length == 2) {
            if (!LETTERS.matcher(values[0]).matches()) {
                System.out.println("Invalid identifier");
            } else if (LETTERS.matcher(values[0]).find() &&
                    DIGITS.matcher(values[0]).find()) {
                System.out.println("Invalid identifier");
            } else if (LETTERS.matcher(values[1]).find() &&
                    DIGITS.matcher(values[1]).find()) {
                System.out.println("Invalid assignment");
            } else if (LETTERS.matcher(values[1]).matches()) {
                if (variables.containsKey(values[1])) {
                    variables.put(values[0], variables.get(values[1]));
                } else {
                    System.out.println("Unknown variable");
                }
            } else {
                variables.put(values[0], values[1]);
            }
        }
    }
    // Method replaces multiple operators with a single, valid operator,
    // splits the input and stores it in a List
    private void prepareExpression(String input) {
        while (MULTIPLEOPERATORS.matcher(input).find()) {
            input = input.replaceAll("\\+\\+", "+")
                    .replaceAll("--", "+")
                    .replaceAll("\\+-|-+", "-");
        }
        Pattern pattern = compile("[*/+-]|[()]|[0-9]+|[A-Za-z]+");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            expression.add(matcher.group());
        }
    }
    // Method checks if the expression with brackets contains both opening and closing brackets
    private boolean hasValidBrackets(String input) {
        Pattern pattern = compile("[()]+");
        Matcher matcher = pattern.matcher(input);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            builder.append(matcher.group());
        }
        if (builder.length() % 2 != 0) {
            return false;
        }
        Map<Character, Character> brackets = Map.of('(', ')',
                '[', ']',
                '{', '}');
        Deque<Character> stack = new ArrayDeque<>();
        for (Character ch : builder.toString().toCharArray()) {
            if (brackets.containsKey(ch)) {
                stack.push(ch);
            } else {
                if (stack.isEmpty()) {
                    return false;
                }
                var openCh = brackets.get(stack.pop());
                if (ch != openCh) {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }
    // Method populates the postfix expression and the operators deque
    private void populatePostfixExpression() {
        for (int i = 0; i < expression.size(); i++) {
            if (OPERAND.matcher(expression.get(i)).matches()) {
                postfixExpression.offer(expression.get(i));
            } else if (OPERATOR.matcher(expression.get(i)).matches()) {
                if (operators.isEmpty()) {
                    operators.push(expression.get(i));
                    continue;
                } else if (expression.get(i).equals("(")) {
                    operators.push(expression.get(i));
                    continue;
                } else if (expression.get(i).equals(")")) {
                    appendOperator(operators,postfixExpression);
                    continue;
                }
                boolean higherPrecedence = operators.peek().equals("(") ?
                        true : precedence.get(expression.get(i)) >
                        precedence.get(operators.peek());
                if (higherPrecedence) {
                    operators.push(expression.get(i));
                } else if (!higherPrecedence) {
                    appendOperator(operators,postfixExpression);
                    operators.push(expression.get(i));
                }
            }
        }
        appendOperator(operators,postfixExpression);
    }
    // Method adds the rest of the operators to the postfix expression
    public void appendOperator(Deque<String> operators, Deque<String> postfixExpression) {
        while (!operators.isEmpty()) {
            if (operators.peek().equals("(")){
                operators.pop();
                break;
            }
            postfixExpression.offer(operators.pop());
        }
    }
    // Method performs calculations
    public static void calculate(String operator, Deque<String> values) {
        BigInteger a = new BigInteger(values.pop());
        BigInteger b = new BigInteger(values.pop()) ;
        switch (operator) {
            case "+" -> values.push(String.valueOf(b.add(a)));
            case "-" -> values.push(String.valueOf(b.subtract(a)));
            case "*" -> values.push(String.valueOf(b.multiply(a)));
            case "/" -> values.push(String.valueOf(b.divide(a)));
        }
    }
    //Method evaluates the expression and calls the calculate method
    public String evaluateExpression(Deque<String> postfixExpression) {
        Deque<String> evaluation = new ArrayDeque<>();
        while (!postfixExpression.isEmpty()) {
            if (DIGITS.matcher(postfixExpression.peek()).matches()) {
                evaluation.push(postfixExpression.pop());
            } else if (LETTERS.matcher(postfixExpression.peek()).matches()) {
                evaluation.push(variables.get(postfixExpression.pop()));
            } else if (OPERATOR.matcher(postfixExpression.peek()).matches()) {
                calculate(postfixExpression.pop(), evaluation);
            }
        }
        expression.clear();
        return evaluation.pop();
    }
}

