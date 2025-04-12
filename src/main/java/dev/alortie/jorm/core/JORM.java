package dev.alortie.jorm.core;

import dev.alortie.jorm.metadata.TableMeta;
import dev.alortie.jorm.utils.JORMAdapter;
import dev.alortie.jorm.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class that manages the ORM engine.
 * It initializes the database connection and manages the entities.
 */
public class JORM {
    private static JORM instance;
    private JORMAdapter JORMAdapter;
    private List<TableMeta<?>> entities;


    private JORM() {
        // Private constructor to prevent instantiation
    }

    // Initialize the ORMEngine with a DBInterface and a list of entities
    public static JORM InitInstance(JORMAdapter JORMAdapter, List<Class<?>> entities) {
        if (instance != null) {
            throw new IllegalStateException("ORMEngine is already initialized.");
        }
        instance = new JORM(); // Create a new instance
        instance.JORMAdapter = JORMAdapter; // Set the DBInterface
        instance.entities = new ArrayList<>(); // Initialize the entities list
        for (Class<?> entity : entities) { // Generate TableMeta for each entity
            TableMeta<?> tableMeta = ReflectionUtils.generateTableMeta(entity);
            instance.entities.add(tableMeta);
        }

        instance.initialize(); // Initialize the database
        return instance; // Return the instance
    }

    public static JORM getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ORMEngine is not initialized. Call setInstance() first.");
        }
        return instance;
    }

    // Initialize db tables
    public void initialize() {
        if (instance == null) {
            throw new IllegalStateException("ORMEngine is not initialized. Call setInstance() first.");
        }
        List<TableMeta<?>> sorted = SchemaManager.sortByDependency(entities);
        for(TableMeta<?> tableMeta : sorted) {
            JORMAdapter.createTable(tableMeta);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TableMeta<T> repository(Class<T> entity) {
        for (TableMeta<?> tableMeta : entities) {
            if (tableMeta.getEntityClass().equals(entity)) {
                return (TableMeta<T>) tableMeta;
            }
        }
        throw new IllegalArgumentException("Entity not found: " + entity.getSimpleName());
    }

    // Placeholder for later use
    public void shutdown() {
        return;
    }


    public JORMAdapter getAdapter() {
        return JORMAdapter;
    }


    public List<TableMeta<?>> getEntities() {
        return entities;
    }
}
