package io.github.cybergrid.simulation.engine.collision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.math.Rectangle;
import io.github.cybergrid.simulation.engine.interfaces.ICollidable;

public class SpatialHashGrid {
    
    private final int cellSize;
    private final Map<String, List<ICollidable>> cells;

    public SpatialHashGrid(int cellSize) {
        this.cellSize = cellSize;
        this.cells = new HashMap<>();
    }

    public void clear() {
        for (List<ICollidable> list : cells.values()) {
            list.clear();
        }
    }

    private String getCellKey(int x, int y) {
        return x + "," + y;
    }

    public void insert(ICollidable entity) {
        CollisionComponent comp = entity.getCollisionComponent();
        if (comp == null) return;

        Rectangle bounds = comp.getBounds(entity.getPosition());

        int startX = (int) Math.floor(bounds.x / cellSize);
        int endX = (int) Math.floor((bounds.x + bounds.width) / cellSize);
        int startY = (int) Math.floor(bounds.y / cellSize);
        int endY = (int) Math.floor((bounds.y + bounds.height) / cellSize);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                String key = getCellKey(x, y);
                cells.computeIfAbsent(key, k -> new ArrayList<>()).add(entity);
            }
        }
    }

    public Set<ICollidable> getPotentialColliders(ICollidable entity) {
        Set<ICollidable> colliders = new HashSet<>();
        CollisionComponent comp = entity.getCollisionComponent();
        if (comp == null) return colliders;

        Rectangle bounds = comp.getBounds(entity.getPosition());

        int startX = (int) Math.floor(bounds.x / cellSize);
        int endX = (int) Math.floor((bounds.x + bounds.width) / cellSize);
        int startY = (int) Math.floor(bounds.y / cellSize);
        int endY = (int) Math.floor((bounds.y + bounds.height) / cellSize);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                String key = getCellKey(x, y);
                List<ICollidable> cellEntities = cells.get(key);
                
                if (cellEntities != null) {
                    for (ICollidable other : cellEntities) {
                        if (other.getID() != entity.getID()) { 
                            colliders.add(other);
                        }
                    }
                }
            }
        }
        return colliders;
    }
}