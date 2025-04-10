package dev.alortie.jorm.core;

import dev.alortie.jorm.metadata.TableMeta;
import dev.alortie.jorm.utils.DBInterface;
import dev.alortie.jorm.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 0.0.1
 * @author ALortie
 */
public class ORMEngine {
    private static ORMEngine instance;
    private DBInterface dbInterface;
    private List<TableMeta<?>> entities;


    private ORMEngine() {
        // Private constructor to prevent instantiation
    }

    // Initialize the ORMEngine with a DBInterface and a list of entities
    public static ORMEngine InitInstance(DBInterface dbInterface, List<Class<?>> entities) {
        if (instance != null) {
            throw new IllegalStateException("ORMEngine is already initialized.");
        }
        instance = new ORMEngine();
        instance.dbInterface = dbInterface;
        instance.entities = new ArrayList<>();
        for (Class<?> entity : entities) {
            TableMeta<?> tableMeta = ReflectionUtils.generateTableMeta(entity);
            instance.entities.add(tableMeta);
        }

        instance.initialize();
        return instance;
    }

    // Get the singleton instance of ORMEngine
    public static ORMEngine getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ORMEngine is not initialized. Call setInstance() first.");
        }
        return instance;
    }

    // Set the instance of ORMEngine
    public void initialize() {
        if (instance == null) {
            throw new IllegalStateException("ORMEngine is not initialized. Call setInstance() first.");
        }

        for(TableMeta<?> tableMeta : entities) {
            dbInterface.createTable(tableMeta);
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

    public void shutdown() {
        // Shutdown logic here
    }

    public DBInterface getDbInterface() {
        return dbInterface;
    }


    public List<TableMeta<?>> getEntities() {
        return entities;
    }
}
