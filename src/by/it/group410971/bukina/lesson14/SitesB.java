package by.it.group410971.bukina.lesson14;

import java.util.*;

public class SitesB {

    static class DSU {
        private final Map<String, String> parent;
        private final Map<String, Integer> rank;
        private final Map<String, Integer> size;

        public DSU() {
            parent = new HashMap<>();
            rank = new HashMap<>();
            size = new HashMap<>();
        }

        public void makeSet(String x) {
            if (!parent.containsKey(x)) {
                parent.put(x, x);
                rank.put(x, 0);
                size.put(x, 1);
            }
        }

        public String find(String x) {
            if (!parent.get(x).equals(x)) {
                parent.put(x, find(parent.get(x))); // Path compression
            }
            return parent.get(x);
        }

        public void union(String x, String y) {
            String rootX = find(x);
            String rootY = find(y);

            if (rootX.equals(rootY)) return;

            // Union by rank
            if (rank.get(rootX) < rank.get(rootY)) {
                parent.put(rootX, rootY);
                size.put(rootY, size.get(rootY) + size.get(rootX));
            } else if (rank.get(rootX) > rank.get(rootY)) {
                parent.put(rootY, rootX);
                size.put(rootX, size.get(rootX) + size.get(rootY));
            } else {
                parent.put(rootY, rootX);
                size.put(rootX, size.get(rootX) + size.get(rootY));
                rank.put(rootX, rank.get(rootX) + 1);
            }
        }

        public int getSize(String x) {
            return size.get(find(x));
        }

        public Collection<String> getAllRoots() {
            Set<String> roots = new HashSet<>();
            for (String site : parent.keySet()) {
                roots.add(find(site));
            }
            return roots;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DSU dsu = new DSU();

        while (true) {
            String line = scanner.nextLine();
            if (line.equals("end")) {
                break;
            }

            String[] sites = line.split("\\+");
            String site1 = sites[0];
            String site2 = sites[1];

            // Добавляем сайты в DSU если их еще нет
            dsu.makeSet(site1);
            dsu.makeSet(site2);

            // Объединяем сайты
            dsu.union(site1, site2);
        }

        // Собираем размеры кластеров
        Map<String, Integer> clusterSizes = new HashMap<>();
        for (String root : dsu.getAllRoots()) {
            clusterSizes.put(root, dsu.getSize(root));
        }

        // Получаем размеры и сортируем их в порядке убывания
        List<Integer> sizes = new ArrayList<>(clusterSizes.values());
        Collections.sort(sizes, Collections.reverseOrder());

        // Выводим результат
        for (int i = 0; i < sizes.size(); i++) {
            System.out.print(sizes.get(i));
            if (i < sizes.size() - 1) {
                System.out.print(" ");
            }
        }
    }
}