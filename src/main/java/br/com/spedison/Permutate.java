package br.com.spedison;

import java.util.Arrays;

public class Permutate {
    private int[] index;   // Índices fixos representando as posições
    private int[] counters; // Contadores do Algoritmo de Heap
    private int i = 0;      // Índice de controle do algoritmo
    private boolean isInitialized = false;

    public void init(int arraySize) {
        index = new int[arraySize];
        counters = new int[arraySize]; // Vetor para contar permutações geradas
        for (int j = 0; j < arraySize; j++) {
            index[j] = j; // Índices fixos
            counters[j] = 0; // Inicializa os contadores
        }
        isInitialized = true;
        i = 0; // Garante que começamos do início
    }

    public boolean permutate() {
        if (!isInitialized) {
            throw new IllegalStateException("O método init() deve ser chamado antes de permutate().");
        }

        while (i < index.length) {
            if (counters[i] < i) {
                int swapIndex = (i % 2 == 0) ? 0 : counters[i];
                trocar(swapIndex, i); // Troca elementos (não índices)

                counters[i]++; // Atualiza o contador
                i = 0; // Reinicia para continuar gerando permutações
                return true;
            } else {
                counters[i] = 0;
                i++;
            }
        }

        return false; // Todas as permutações foram geradas
    }

    private void trocar(int i, int j) {
        int temp = index[i];
        index[i] = index[j];
        index[j] = temp;
    }

    @Override
    public String toString() {
        return "Permutate{index = " + Arrays.toString(index) + "}";
    }

    public int[] getIndex() {
        return Arrays.copyOf(index, index.length); // Evita modificações externas
    }

    public static void main(String[] args) {
        Permutate permutador = new Permutate();
        permutador.init(4); // Inicializa com 3 elementos

        System.out.println("Permutações geradas:");
        do {
            System.out.println(permutador);
        } while (permutador.permutate());
    }
}
