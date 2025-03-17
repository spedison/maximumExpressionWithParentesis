package br.com.spedison;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FoundErrorSimulate {

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    List<String> operations;
    List<String> errors = null;

    public static void main(String[] args) {
        new FoundErrorSimulate()
                .createExpressions(0, 15, 15)
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
        AtomicInteger counter;
        List<String> results;

        public RunTask(String expression, AtomicInteger counter, List<String> results) {
            this.expression = expression;
            this.counter = counter;
            this.results = results;
        }

        public String call() {

            MaximumNaiveMethod solver1 = new MaximumNaiveMethod();
            MaximumValueArithmeticExpression solver2 = new MaximumValueArithmeticExpression();

            solver1.setExpression(expression);
            solver2.setExpression(expression);

            solver1.solve();
            solver2.solve();

            int a = counter.incrementAndGet();
            String result = null;
            if (!solver1.getResult().equals(solver2.getResult())) {
                result =
                        "Erro com a expressão [ %s ]. Solver1(Naive) = %d Solver2(Dinamic Programming) = %d"
                                .formatted(expression, solver1.getResult(), solver2.getResult());
            } else {
                result = "Sucesso com a expressão [ %s ] !!".formatted(expression);
            }

            results.add(result);
            return result;
        }
    }


    private FoundErrorSimulate simulate() {
        List<Future<String>> listOfTasks = new ArrayList<>();

        AtomicInteger counter = new AtomicInteger(0);
        errors = Collections.synchronizedList(new LinkedList<>());

        // Submete as tarefas ao executor
        for (String op : operations) {
            listOfTasks.add(executorService.submit(new RunTask(op, counter, errors)));
        }

        while (counter.get() < listOfTasks.size()) {
            for (Future<String> task : listOfTasks) {
                try {
                    task.get(1500, TimeUnit.MILLISECONDS); // Bloqueia até que a tarefa esteja concluída
                } catch (TimeoutException e) {
                    System.out.println(Instant.now() + " - Verificando...");
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println(Instant.now() + " - Erro ao processar uma tarefa: " + e.getMessage());
                    counter.incrementAndGet();
                }
                System.out.println(Instant.now() + " - Quantidade de tarefas executadas : " + counter.get());
            }
        }

        System.out.println("+++++++++++++++++++++++++++++++++");
        return this;
    }

    private FoundErrorSimulate showExpressions() {
        System.out.println("================  Expression to Calculate ===============");
        for (String expression : operations) {
            System.out.println(expression);
        }
        System.out.println("==========================================================\n\n");
        return this;
    }

    public FoundErrorSimulate shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(20, TimeUnit.SECONDS)) {
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

        sb.append(Math.abs(random.nextInt() % 100)).append(" ");

        for (int i = 0; i < numberOperations; i++) {
            sb.append(getSingleOperator(random)).append(" ");
            sb.append(Math.abs(random.nextInt() % 100)).append(" ");
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