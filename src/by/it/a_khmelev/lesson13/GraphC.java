package by.it.a_khmelev.lesson13;

import java.util.*;

public class GraphC {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        // Парсинг входной строки и построение графа
        Map<String, List<String>> graph = new HashMap<>();
        Map<String, List<String>> reversedGraph = new HashMap<>();

        String[] edges = input.split(",\\s*");

        for (String edge : edges) {
            String[] parts = edge.split("->");
            String from = parts[0].trim();
            String to = parts[1].trim();

            // Добавляем ребро в прямой граф
            graph.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
            graph.putIfAbsent(to, new ArrayList<>());

            // Добавляем ребро в обратный граф
            reversedGraph.computeIfAbsent(to, k -> new ArrayList<>()).add(from);
            reversedGraph.putIfAbsent(from, new ArrayList<>());
        }

        // Алгоритм Косарайю для нахождения компонент сильной связности
        List<Set<String>> scc = kosaraju(graph, reversedGraph);

        // Сортируем компоненты по порядку обхода (первый - исток, последний - сток)
        // и сортируем вершины внутри каждой компоненты лексикографически
        List<String> result = new ArrayList<>();
        for (Set<String> component : scc) {
            List<String> sortedComponent = new ArrayList<>(component);
            Collections.sort(sortedComponent);
            StringBuilder sb = new StringBuilder();
            for (String vertex : sortedComponent) {
                sb.append(vertex);
            }
            result.add(sb.toString());
        }

        // Вывод результата
        for (String component : result) {
            System.out.println(component);
        }
    }

    private static List<Set<String>> kosaraju(Map<String, List<String>> graph,
                                              Map<String, List<String>> reversedGraph) {
        // Первый проход DFS - получаем порядок завершения обработки
        Stack<String> stack = new Stack<>();
        Set<String> visited = new HashSet<>();

        for (String vertex : graph.keySet()) {
            if (!visited.contains(vertex)) {
                dfsFirstPass(vertex, graph, visited, stack);
            }
        }

        // Второй проход DFS по обратному графу в порядке из стека
        visited.clear();
        List<Set<String>> scc = new ArrayList<>();

        while (!stack.isEmpty()) {
            String vertex = stack.pop();
            if (!visited.contains(vertex)) {
                Set<String> component = new HashSet<>();
                dfsSecondPass(vertex, reversedGraph, visited, component);
                scc.add(component);
            }
        }

        return scc;
    }

    private static void dfsFirstPass(String vertex, Map<String, List<String>> graph,
                                     Set<String> visited, Stack<String> stack) {
        visited.add(vertex);

        if (graph.containsKey(vertex)) {
            for (String neighbor : graph.get(vertex)) {
                if (!visited.contains(neighbor)) {
                    dfsFirstPass(neighbor, graph, visited, stack);
                }
            }
        }

        stack.push(vertex);
    }

    private static void dfsSecondPass(String vertex, Map<String, List<String>> reversedGraph,
                                      Set<String> visited, Set<String> component) {
        visited.add(vertex);
        component.add(vertex);

        if (reversedGraph.containsKey(vertex)) {
            for (String neighbor : reversedGraph.get(vertex)) {
                if (!visited.contains(neighbor)) {
                    dfsSecondPass(neighbor, reversedGraph, visited, component);
                }
            }
        }
    }
}
