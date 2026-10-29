package io.github.cybergrid.simulation.engine.interfaces;

import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.TriggerEntity;

@FunctionalInterface
public interface TriggerCallback {
    
    //Called when a trigger is activated.
    void onTrigger(TriggerEntity trigger, Entity activator);
}