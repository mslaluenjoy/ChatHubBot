/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.db;

import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public abstract class DBAccessWithResult<T> {

    protected abstract T operation(Session session);

    public T execute() {
        Session session = null;
        try {
            session = HibernateUtil.openSession();
            return operation(session);
        } finally {
            if (session != null && session.isOpen()) {
                session.clear();
                session.close();
            }
        }
    }
}
