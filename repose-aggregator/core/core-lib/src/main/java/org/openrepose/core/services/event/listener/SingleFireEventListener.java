package org.openrepose.core.services.event.listener;

import org.openrepose.core.services.event.common.Event;
import org.openrepose.core.services.event.common.EventListener;

public abstract class SingleFireEventListener <T extends Enum, P> implements EventListener<T, P> {

    private final Class<T> eventClass;
    private boolean fired;

    public SingleFireEventListener(Class<T> eventClass) {
        this.eventClass = eventClass;
    }
    
    @Override
    public synchronized void onEvent(Event<T, P> e) {
        if (!fired) {
            onlyOnce(e);
            
            fired = true;
        }
        
        e.eventManager().squelch(this, eventClass);
    }
    
    public abstract void onlyOnce(Event<T, P> e);
}
