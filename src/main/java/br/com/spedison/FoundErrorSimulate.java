package br.com.spedison;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class FoundErrorSimulate {

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    List<String> operations;
    List<String> errors = new LinkedList<String>();

    public static void main(String[] args) {
        new FoundErrorSimulate()
                .createExpressions(0, 12, 10)
                .showExpressions()
                .simulate()
                .shutdown()
                .printErros()
        ;
    }

    private void printErros() {

        if (errors.isEmpty()) {
            System.out.println("Todos os testes terminaram com sucesso.");
        }

        for (String error : errors) {
            System.out.println(error);
        }
    }

    static class RunTask implements Callable<String> {
        String expression;

        public RunTask(String expression) {
            this.expression = expression;
        }

        public String call() {
            MaximumNaiveMethod solver1 = new MaximumNaiveMethod();
            MaximumValueArithmeticExpression solver2 = new MaximumValueArithmeticExpression();
            solver1.setExpression(expression);
            solver2.setExpression(expression);

            solver1.solve();
            solver2.solve();

            if (!solver1.getResult().equals(solver2.getResult())) {
                return
                        "Erro com a expressão [ %s ]. Solver1 = %d Solver2 = %d"
                                .formatted(expression, solver1.getResult(), solver2.getResult());
            } else {
                return "Sucesso com a expressão [ %s ] !!".formatted(expression);
            }
        }
    }


    private FoundErrorSimulate simulate() {
        List<Future<String>> listOfTasks = new ArrayList<>();

        // Submete as tarefas ao executor
        for (String op : operations) {
            listOfTasks.add(executorService.submit(new RunTask(op)));
        }

        int count = 0;
        while (count < listOfTasks.size()) {
            for (Future<String> task : listOfTasks) {
                try {
                    String result = task.get(2000, TimeUnit.MILLISECONDS); // Bloqueia até que a tarefa esteja concluída
                    errors.add(result);
                    count++;
                } catch (TimeoutException e) {
                    System.out.println("Verificando...");
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Erro ao processar uma tarefa: " + e.getMessage());
                }
                System.out.println("Quantidade de tarefas executadas : " + count);
            }
        }
        System.out.println("+++++++++++++++++++++++++++++++++");
        return this;
    }

    private FoundErrorSimulate showExpressions() {
        for (String expression : operations) {
            System.out.println(expression);
        }
        System.out.println("--------------------------------\n\n");
        return this;
    }

    public FoundErrorSimulate shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        return this;
    }

    private String getSingleOperator(Random random) {
        String operator;
        switch (random.nextInt(3)) {
            case 0:
                operator = "+";
                break;
            case 1:
                operator = "-";
                break;
            case 2:
                operator = "*";
                break;
            default:
                operator = "+";
        }
        return operator;
    }

    private String makeOneExpression(int numberOperations, Random random) {
        StringBuilder sb = new StringBuilder();

        sb.append(Math.abs(random.nextInt() % 1000)).append(" ");

        for (int i = 0; i < numberOperations; i++) {
            sb.append(getSingleOperator(random)).append(" ");
            sb.append(Math.abs(random.nextInt() % 1000)).append(" ");
        }

        return sb.toString();
    }

    private FoundErrorSimulate createExpressions(int minOperations, int maxOperations, int repetitions) {

        Random random = new Random();
        this.operations = new LinkedList<>();

        for (int numOperations = minOperations; numOperations <= maxOperations; numOperations++) {
            for (int repeat = 0; repeat < repetitions; repeat++) {
                String expression = makeOneExpression(numOperations, random);
                operations.add(expression);
            }
        }
        return this;
    }
}
