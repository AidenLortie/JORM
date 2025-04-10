package dev.alortie.jorm.core;

import dev.alortie.jorm.metadata.ColumnMeta;
import dev.alortie.jorm.metadata.TableMeta;

import java.util.*;

public class SchemaManager {
    public static List<TableMeta<?>> sortByDependency(List<TableMeta<?>> tables){
        Map<String, TableMeta<?>> tableMap = new HashMap<>();
        Map<String, Set<String>> dependencyGraph = new HashMap<>();

        // Initialize the table map and dependency graph
        for (TableMeta<?> table : tables) {
            tableMap.put(table.getTableName(), table);
            dependencyGraph.put(table.getTableName(), new HashSet<>());
        }

        // Build the dependency graph
        for (TableMeta<?> table : tables) {
            for(ColumnMeta column : table.getColumns()){
                if (column.isForeignKey()) {
                    String dependsOn = getTableNameForEntity(column.getReferencedEntity());
                    dependencyGraph.get(table.getTableName()).add(dependsOn);
                }
            }
        }

        // Perform topological sort
        List<TableMeta<?>> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> tempMarked = new HashSet<>();

        for (String table : dependencyGraph.keySet()){
            if (!visited.contains(table)){
                dfs(table, dependencyGraph, tableMap, visited, tempMarked, sorted);
            }
        }

        return sorted;
    }

    private static void dfs(
            String table,
            Map<String, Set<String>> graph,
            Map<String, TableMeta<?>> tableMap,
            Set<String> visited,
            Set<String> visiting,
            List<TableMeta<?>> sorted
    ) {
        if (visiting.contains(table)) {
            throw new RuntimeException("Cycle detected in dependency graph for table: " + table);
        }

        if (visited.contains(table)) return;

        visiting.add(table);
        for (String dep : graph.getOrDefault(table, Collections.emptySet())){
            dfs(dep, graph, tableMap, visited, visiting, sorted);
        }
        visiting.remove(table);
        visited.add(table);
        sorted.add(tableMap.get(table));
    }

    public static String getTableNameForEntity(Class<?> clazz) {
        // Optional: cache this
        if (!clazz.isAnnotationPresent(dev.alortie.jorm.annotations.Entity.class)) {
            throw new RuntimeException("Referenced class " + clazz.getName() + " is not an @Entity");
        }
        String name = clazz.getAnnotation(dev.alortie.jorm.annotations.Entity.class).tableName();
        return name.isEmpty() ? clazz.getSimpleName().toLowerCase() : name;
    }
}
