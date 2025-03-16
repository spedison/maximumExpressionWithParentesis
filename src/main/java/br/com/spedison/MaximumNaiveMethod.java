package br.com.spedison;

import java.util.*;

public class MaximumNaiveMethod {

    static class Token {
        Long value;
        Character op;
        Token prevToken;
        Token nextToken;

        public Token(Token cloneToken) {
            this.value = cloneToken.value;
            this.op = cloneToken.op;
            this.prevToken = new Token(cloneToken.prevToken);
            this.nextToken = new Token(cloneToken.nextToken);
        }

        public Token(Token cloneToken, Token prevToken, Token nextToken) {
            this.value = cloneToken.value;
            this.op = cloneToken.op;
            this.prevToken = prevToken;
            this.nextToken = nextToken;
        }

        public Token() {
            this.value = null;
            this.op = null;
            this.prevToken = null;
            this.nextToken = null;
        }

        public Token(String item, Token prevToken, Token nextToken) {
            if (item.matches("\\d+")) {
                this.value = Long.parseLong(item);
            } else {
                this.op = item.trim().charAt(0);
            }
            this.prevToken = prevToken;
            this.nextToken = nextToken;
        }

        public Token operator() {
            if (op == null) {
                throw new IllegalArgumentException("Invalid operator");
            }

            switch (op) {
                case '+':
                    return sum();
                case '-':
                    return sub();
                case '*':
                    return mult();
                default:
                    throw new IllegalArgumentException("Invalid operator: " + op);
            }
        }

        public Token sum() {
            Token ret = new Token();
            ret.value = prevToken.value + nextToken.value;
            return ret;
        }

        public Token mult() {
            Token ret = new Token();
            ret.value = prevToken.value * nextToken.value;
            return ret;
        }

        public Token sub() {
            Token ret = new Token();
            ret.value = prevToken.value - nextToken.value;
            return ret;
        }

        @Override
        public String toString() {
            return "(Val = [" + (op == null ? value : String.valueOf(op)) + "])";
        }

        public boolean isOperate() {
            return op != null;
        }

        public Token getNextToken() {
            return nextToken;
        }

        public Token getPrevToken() {
            return prevToken;
        }

        public Token delete() {
            if (prevToken != null) {
                prevToken.nextToken = nextToken;
            }
            if (nextToken != null) {
                nextToken.prevToken = prevToken;
            }
            return nextToken;
        }

        public Token getAt(int pos) {
            Token current = this;
            while (pos > 0 && current.nextToken != null) {
                current = current.nextToken;
            }
            return current;
        }
    }

    static public class ListTokens {

        Token rootItem;
        Token lastItem;
        int lasPosition;
        int countItens;


        // Use for Clone.
        public ListTokens(ListTokens otherlist) {

            rootItem = null;
            lastItem = null;
            countItens = 0;

            if (otherlist.countItens == 0)
                return;

            Token current = otherlist.rootItem;
            Token lastCreated = null;
            do {
                Token newItem = new Token(current, lastCreated, null);

                if (rootItem == null)
                    rootItem = newItem;

                if (lastItem != null)
                    lastItem.nextToken = newItem;

                lastItem = newItem;
                lastCreated = newItem;
                current = current.getNextToken();
            } while (current != null);
        }


        public ListTokens(String expression) {
            rootItem = null;
            lastItem = null;
            lasPosition = 0;
            countItens = 0;

            Token lastOp = null;

            StringBuilder sb = new StringBuilder();
            for (char ch : expression.toCharArray()) {
                if (Character.isDigit(ch)) {
                    sb.append(ch);
                } else if (ch == '+' || ch == '-' || ch == '*') {
                    Token tokenNumber = new Token(sb.toString(), lastItem, null);

                    if (lastOp != null)
                        lastOp.nextToken = tokenNumber;

                    lastItem = tokenNumber;
                    countItens++;

                    if (rootItem == null)
                        rootItem = lastItem;

                    Token op = new Token(String.valueOf(ch), lastItem, null);
                    lastOp = op;

                    countItens++;
                    lastItem.nextToken = op;
                    lastItem = op;
                    sb.setLength(0);
                } else {
                    continue;
                }
            }
            if (sb.length() > 0) {
                Token number = new Token(sb.toString(), lastOp, null);

                if (lastOp != null)
                    lastOp.nextToken = number;

                countItens++;
                lastItem = number;

                if (rootItem == null)
                    rootItem = number;
            }
        }

        public void solveInOrder(int[] order) {
            int[] s = order.clone();
            for (int i = 0; i < order.length; i++) {
                solve(s[i]);
                //System.out.println(this);
                //System.out.println("---------------");
                for (int j = i + 1; j < order.length; j++) {
                    if (s[j] > s[i])
                        s[j] -= 2;
                }
            }
        }

        public boolean solve(int pos) {
            Token current = rootItem;

            while (current != null && pos > 0) {
                current = current.getNextToken();
                pos--;
            }

            if (current == null)
                return false;

            // Delete current token
            if (!current.isOperate() || pos != 0)
                return false;

            // Create a new token. I need put in corret position
            Token t = current.operator();
            t.prevToken = current.prevToken.prevToken;
            if (current.prevToken.prevToken != null)
                current.prevToken.prevToken.nextToken = t;

            if (current.nextToken.nextToken != null)
                current.nextToken.nextToken.prevToken = t;

            current.prevToken.prevToken = t;

            t.nextToken = current.nextToken.nextToken;

            countItens -= 2;

            if (t.prevToken == null)
                rootItem = t;

            if (t.nextToken == null)
                lastItem = t;

            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Token current = rootItem;
            sb.append("ListTokens{\n");
            while (current != null) {
                sb.append(current).append(", \n");
                current = current.getNextToken();
            }
            sb.append("}\n");
            return sb.toString();
        }

        public int size() {
            return countItens;
        }
    }

    private Long result;

    private String expression;

    public MaximumNaiveMethod setExpression(String expression) {
        this.expression = expression;
        result = null;
        return this;
    }

    public Long getResult() {
        return result;
    }

    public static void main(String[] args) {
        new MaximumNaiveMethod()
                .readInput()
                //.setExpression("3 * 8 + 5 * 2 - 9 * 7 + 1 * 4")
                .solve()
                .printResult()
                .printOrder();
    }

    private void printOrder() {

    }

    private MaximumNaiveMethod readInput() {
        Scanner scanner = new Scanner(System.in);
        setExpression(scanner.nextLine().trim());
        return this;
    }

    private MaximumNaiveMethod printResult() {
        System.out.println(result);
        return this;
    }

    private int[] getSort(int[] values, Permutate permutate) {
        int[] p = new int[values.length];
        for (int i = 0; i < p.length; i++) {
            p[i] = values[permutate.getIndex()[i]];
        }
        return p;
    }

    public MaximumNaiveMethod solve() {
        ListTokens tk1 = new ListTokens(expression);

        if (tk1.size() == 1){
            result = tk1.rootItem.value;
            return this;
        }

        if (tk1.size() %2 == 0 ){
            throw  new RuntimeException("Quantidade de parâmetros inválidos");
        }

        int[] operators = makeOperators(tk1);

        Permutate permutate = new Permutate();
        permutate.init(tk1.size() / 2);

        ListTokens tkAtual = null;
        long maxResult = Long.MIN_VALUE;
        long minResult = Long.MAX_VALUE;
        int [] minOrder = null;
        int [] maxOrder = null;

        do {
            int[] operatorExec = getSort(operators, permutate);
            tkAtual = new ListTokens(tk1);
            tkAtual.solveInOrder(operatorExec);
            long resultAtual = tkAtual.rootItem.value;

            if (resultAtual > maxResult){
                maxResult = resultAtual;
                maxOrder = operatorExec.clone();
            }

            if (resultAtual < minResult){
                minResult = resultAtual;
                minOrder = operatorExec.clone();
            }

        } while (permutate.permutate());

        //System.out.println("Máximo: " + maxResult + " - Ordem: " + Arrays.toString(maxOrder));
        //System.out.println("Mínimo: " + minResult + " - Ordem: " + Arrays.toString(minOrder));
        result = maxResult;

        return this;
    }

    private int[] makeOperators(ListTokens tk1) {
        int[] operators = new int[tk1.size() / 2];
        int pos = 0;
        for (int i =1 ; i < tk1.size(); i+=2) {
                operators[pos++] = i;
        }
        return operators;
    }

    private void test() {
        //                                           1   3   5   7   9   11  13
        ListTokens tk1 = new ListTokens("3 * 8 + 5 * 2 - 9 * 7 + 1 * 4");
        ListTokens tk2 = new ListTokens(tk1);
        //Permutate p = new Permutate();
        //p.init(tk1.size()/2);
        tk1.solveInOrder(new int[]{11, 13, 1, 3, 5, 7, 9});
        System.out.println(tk1);


        System.out.println(tk2);
        System.out.println("--------------------------------");
        //while (tk.size() > 1) {
        tk1.solve(1);
        tk2.solve(5);
        System.out.println(tk1);
        System.out.println("--------------------------------");
        System.out.println(tk2);
        System.out.println("--------------------------------");
        //}
        System.out.println(tk1);
    }

    private void test2() {
        int[] vals = {10, 20, 30, 50, 90, 100};
        Permutate a = new Permutate();
        a.init(vals.length);
        for (int i = 0; i < 10; i++) {
            a.permutate();
            System.out.println("");
            for (int val : a.getIndex()) {
                System.out.print(vals[val] + " ; ");
            }
            System.out.println("---\n");
        }
    }

/*
    private void printResult() {

        System.out.println(result);

    }

    private MaximumNaiveMethod solve() {

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

        expression = sc.nextLine();
        tokens = extractTokens(expression);
        numbers = new Integer[tokens.length / 2 + 1];
        operators = new String[tokens.length / 2];

        if (tokens.length == 0) {
            throw new IllegalArgumentException("Expression must contain an odd number of operands. Now is empty");
        } else if (tokens.length == 1) {
            result = Integer.parseInt(tokens[0]);
            return this;
        } else if (tokens.length % 2 == 0) {
            throw new IllegalArgumentException("Expression must contain an odd number of operands");
        } else if (tokens.length == 3) {
            Value value1 = new Value(Integer.parseInt(tokens[0]));
            Value value2 = new Value(Integer.parseInt(tokens[2]));
            result = value1.operator(value2, tokens[1]).minValue;
            return this;
        } else {
            dp = new Value[numbers.length][numbers.length];
            result = null;
            return this;
        }
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
    }*/
}
