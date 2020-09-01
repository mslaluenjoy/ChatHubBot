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
public abstract class TransactionalDBAccess extends DBAccess {

    @Override
    public void execute() {
        Session session = null;
        try {
            session = HibernateUtil.openSession();
            session.beginTransaction();
            operation(session);
            if (session != null && session.isOpen() && session.getTransaction().isActive()) {
                session.getTransaction().commit();
            }
        } finally {
            if (session != null && session.isOpen()) {
                session.clear();
                session.close();
            }
        }
    }

}
