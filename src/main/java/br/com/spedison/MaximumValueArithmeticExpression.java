package br.com.spedison;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MaximumValueArithmeticExpression {

    static class Value {
        long minValue;
        long maxValue;

        public Value(List<Value> values) {
            this.minValue = values.get(0).minValue;
            this.maxValue = values.get(0).maxValue;
            for (Value v : values) {
                this.minValue = Math.min(this.minValue, v.minValue);
                this.maxValue = Math.max(this.maxValue, v.maxValue);
            }
        }

        public Value extremum(Value value) {
            return new Value(Math.min(minValue, value.minValue), Math.max(maxValue, value.maxValue));
        }

        public Value(long minValue, long maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public Value(long value) {
            this.minValue = value;
            this.maxValue = value;
        }

        public Value operator(Value value, String operator) {
            return switch (operator) {
                case "+" -> sum(value);
                case "-" -> sub(value);
                case "*" -> mult(value);
                default -> throw new IllegalArgumentException("Invalid operator: " + operator);
            };
        }

        public Value sum(Value value) {
            long v1 = this.minValue + value.minValue;
            long v2 = this.maxValue + value.minValue;
            long v3 = this.minValue + value.maxValue;
            long v4 = this.maxValue + value.maxValue;
            long max = Math.max(Math.max(v1, v2), Math.max(v3, v4));
            long min = Math.min(Math.min(v1, v2), Math.min(v3, v4));
            return new Value(min, max);
        }

        public Value mult(Value value) {
            long v1 = this.minValue * value.minValue;
            long v2 = this.maxValue * value.minValue;
            long v3 = this.minValue * value.maxValue;
            long v4 = this.maxValue * value.maxValue;
            long max = Math.max(Math.max(v1, v2), Math.max(v3, v4));
            long min = Math.min(Math.min(v1, v2), Math.min(v3, v4));
            return new Value(min, max);
        }

        public Value sub(Value value) {
            long v1 = this.minValue - value.minValue;
            long v2 = this.maxValue - value.minValue;
            long v3 = this.minValue - value.maxValue;
            long v4 = this.maxValue - value.maxValue;
            long max = Math.max(Math.max(v1, v2), Math.max(v3, v4));
            long min = Math.min(Math.min(v1, v2), Math.min(v3, v4));
            return new Value(min, max);
        }

        @Override
        public String toString() {
            return "(min=" + minValue +
                    " ; max=" + maxValue + ')';
        }
    }

    private String expression;
    private String[] tokens;
    private Integer[] numbers;
    private String[] operators;
    private Value[][] dp;
    private Long result;

    public Long getResult() {
        return result;
    }

    public static void main(String[] args) {
        new MaximumValueArithmeticExpression()
                .read()
                .solve()
                .printResult();
    }

    public void setExpression(String expression) {
        this.expression = expression;

        tokens = extractTokens(this.expression);
        numbers = new Integer[tokens.length / 2 + 1];
        operators = new String[tokens.length / 2];

        if (tokens.length == 0) {
            throw new IllegalArgumentException("Expression must contain an odd number of operands. Now is empty");
        } else if (tokens.length == 1) {
            result = Long.parseLong(tokens[0]);
        } else if (tokens.length % 2 == 0) {
            throw new IllegalArgumentException("Expression must contain an odd number of operands");
        } else if (tokens.length == 3) {
            Value value1 = new Value(Long.parseLong(tokens[0]));
            Value value2 = new Value(Long.parseLong(tokens[2]));
            result = value1.operator(value2, tokens[1]).minValue;
        } else {
            dp = new Value[numbers.length][numbers.length];
            result = null;
        }
    }

    private void printResult() {
        System.out.println(result);
    }

    public MaximumValueArithmeticExpression solve() {

        if (result != null) return this;

        loadNumbersAndOperators();
        processPrincipalDiag();
        processSecondDiag();
        processOtherDiag();

        result = this.dp[0][dp.length - 1].maxValue;
        return this;
    }

    private void processOtherDiag() {
        for (int s = 2; s < numbers.length; s++) {
            for (int line = 0; line < numbers.length - s; line++) {
                int j = line + s;  // Define a posição final do intervalo
                Value minMax = new Value(Integer.MAX_VALUE, Integer.MIN_VALUE);  // Para armazenar o mínimo e máximo dessa posição

                // List<Value> values = new LinkedList<>();
                // Percorre todos os pontos de divisão dentro do intervalo [i, j]
                for (int k = line; k < j; k++) {
                    // Obtém os valores das subexpressões
                    Value left = dp[line][k];       // dp[i][k]: Parte esquerda da divisão
                    Value right = dp[k + 1][j];  // dp[k+1][j]: Parte direita da divisão
                    String op = operators[k];   // Operador que está entre as duas partes
                    // Calcula os valores possíveis (min e max) da expressão
                    minMax = minMax.extremum(left.operator(right, op));
                }
                // Preenche dp[i][j] com os extremos valores calculados
                dp[line][j] = minMax;
            }
        }
    }

    private void processSecondDiag() {
        for (int i = 0; i < numbers.length - 1; i++) {
            dp[i][i + 1] = dp[i][i].operator(dp[i + 1][i + 1], operators[i]);
        }
    }

    private MaximumValueArithmeticExpression read() {
        Scanner sc = new Scanner(System.in);
        setExpression(sc.nextLine().trim());
        return this;
    }

    private void processPrincipalDiag() {
        for (int i = 0; i < numbers.length; i++) {
            dp[i][i] = new Value(numbers[i]);
        }
    }

    private void loadNumbersAndOperators() {
        int posNumbers = 0;
        int posOperators = 0;
        for (int i = 0; i < tokens.length; i++) {
            if (i % 2 == 0) numbers[posNumbers++] = Integer.parseInt(tokens[i]);
            else operators[posOperators++] = tokens[i];
        }
    }

    private String[] extractTokens(String expression) {

        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (char ch : expression.toCharArray()) {
            if (Character.isDigit(ch)) {
                sb.append(ch);
            } else if (ch == '+' || ch == '-' || ch == '*') {
                tokens.add(sb.toString());
                sb.setLength(0);
                tokens.add(String.valueOf(ch));
            } else if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
                if (!tokens.isEmpty()) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
            }
        }
        if (!sb.isEmpty()) {
            tokens.add(sb.toString());
        }

        return tokens.stream().filter(s -> !s.isBlank()).toArray(String[]::new);
    }
}

