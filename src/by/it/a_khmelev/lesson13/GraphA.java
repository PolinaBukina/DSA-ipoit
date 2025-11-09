package by.it.a_khmelev.lesson13;

import java.util.*;

public class GraphA {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        // Парсинг входной строки
        Map<String, List<String>> graph = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Set<String> allVertices = new HashSet<>();

        String[] edges = input.split(", ");

        for (String edge : edges) {
            String[] parts = edge.split(" -> ");
            String from = parts[0];
            String to = parts[1];

            // Добавляем ребро в граф
            graph.putIfAbsent(from, new ArrayList<>());
            graph.get(from).add(to);

            // Инициализируем для всех вершин
            graph.putIfAbsent(to, new ArrayList<>());

            // Обновляем полустепень захода
            inDegree.put(to, inDegree.getOrDefault(to, 0) + 1);
            inDegree.putIfAbsent(from, 0);

            allVertices.add(from);
            allVertices.add(to);
        }

        // Топологическая сортировка алгоритмом Кана
        List<String> result = topologicalSortKahn(graph, inDegree, allVertices);

        // Вывод результата
        for (int i = 0; i < result.size(); i++) {
            System.out.print(result.get(i));
            if (i < result.size() - 1) {
                System.out.print(" ");
            }
        }
    }

    private static List<String> topologicalSortKahn(Map<String, List<String>> graph,
                                                    Map<String, Integer> inDegree,
                                                    Set<String> allVertices) {
        List<String> result = new ArrayList<>();

        // PriorityQueue для лексикографического порядка
        PriorityQueue<String> queue = new PriorityQueue<>();

        // Добавляем вершины с нулевой полустепенью захода
        for (String vertex : allVertices) {
            if (inDegree.getOrDefault(vertex, 0) == 0) {
                queue.offer(vertex);
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);

            // Уменьшаем полустепень захода для всех соседей
            for (String neighbor : graph.get(current)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // Проверка на циклы
        if (result.size() != allVertices.size()) {
            return new ArrayList<>(); // Есть цикл
        }

        return result;
    }
}