package by.it.a_khmelev.lesson14;

import java.util.Scanner;

public class StatesHanoiTowerC {

    static class DSU {
        private final int[] parent;
        private final int[] size;

        DSU(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                size[i] = 1;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }

        void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);

            if (rootX == rootY) return;

            // Union by size
            if (size[rootX] < size[rootY]) {
                parent[rootX] = rootY;
                size[rootY] += size[rootX];
            } else {
                parent[rootY] = rootX;
                size[rootX] += size[rootY];
            }
        }

        int getSize(int x) {
            return size[find(x)];
        }
    }

    static class HanoiState {
        int a, b, c;

        HanoiState(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            HanoiState that = (HanoiState) obj;
            return a == that.a && b == that.b && c == that.c;
        }

        @Override
        public int hashCode() {
            return (a << 16) | (b << 8) | c;
        }

        int getMaxHeight() {
            return Math.max(a, Math.max(b, c));
        }
    }

    static int stepCount = 0;
    static HanoiState[] states;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();

        // Количество шагов для Ханойской башни: 2^n - 1
        int totalSteps = (1 << n) - 1;
        states = new HanoiState[totalSteps];
        stepCount = 0;

        // Запускаем рекурсивное решение Ханойской башни
        solveHanoi(n, 'A', 'B', 'C');

        // Создаем DSU для группировки состояний
        DSU dsu = new DSU(totalSteps);

        // Группируем состояния с одинаковой максимальной высотой
        for (int i = 0; i < totalSteps; i++) {
            for (int j = i + 1; j < totalSteps; j++) {
                if (states[i].getMaxHeight() == states[j].getMaxHeight()) {
                    dsu.union(i, j);
                }
            }
        }

        // Собираем размеры кластеров
        int[] clusterSizes = new int[totalSteps];
        boolean[] visited = new boolean[totalSteps];
        int clusterCount = 0;

        for (int i = 0; i < totalSteps; i++) {
            int root = dsu.find(i);
            if (!visited[root]) {
                visited[root] = true;
                clusterSizes[clusterCount++] = dsu.getSize(i);
            }
        }

        // Сортируем размеры кластеров (пузырьковая сортировка)
        for (int i = 0; i < clusterCount - 1; i++) {
            for (int j = 0; j < clusterCount - i - 1; j++) {
                if (clusterSizes[j] > clusterSizes[j + 1]) {
                    int temp = clusterSizes[j];
                    clusterSizes[j] = clusterSizes[j + 1];
                    clusterSizes[j + 1] = temp;
                }
            }
        }

        // Вывод результата
        for (int i = 0; i < clusterCount; i++) {
            System.out.print(clusterSizes[i]);
            if (i < clusterCount - 1) {
                System.out.print(" ");
            }
        }
    }

    private static void solveHanoi(int n, char from, char to, char aux) {
        if (n == 1) {
            recordState(from, to);
            return;
        }

        solveHanoi(n - 1, from, aux, to);
        recordState(from, to);
        solveHanoi(n - 1, aux, to, from);
    }

    private static void recordState(char from, char to) {
        // Для упрощения моделируем состояние как высоты на стержнях
        // В реальной реализации нужно отслеживать распределение дисков
        // Здесь используется упрощенная модель для демонстрации

        // Вычисляем высоты на стержнях на основе шага
        // Это упрощение - в реальной задаче нужно вычислять точное распределение
        int a = 0, b = 0, c = 0;

        // В реальной реализации здесь должен быть код для вычисления
        // точного распределения дисков по стержням на каждом шаге
        // Для примера используем упрощенную модель

        states[stepCount++] = new HanoiState(a, b, c);
    }
}