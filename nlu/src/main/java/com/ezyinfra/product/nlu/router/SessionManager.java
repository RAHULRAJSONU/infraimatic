package com.ezyinfra.product.nlu.router;

import com.ezyinfra.product.common.exception.AuthException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public UserSession getOrCreate(String phone) {
        try{
            return sessions.computeIfAbsent(phone,
                    p -> new UserSession(p));
        }catch (Exception ex){
            throw new AuthException("Could not create the userSession, exception: "+ex.getMessage());
        }
    }

    public boolean hasActiveWorkflow(String phone) {
        UserSession s = sessions.get(phone);
        return s != null && s.getState() == WorkflowState.IN_PROGRESS;
    }

    public void clear(String phone) {
        sessions.remove(phone);
    }
}
