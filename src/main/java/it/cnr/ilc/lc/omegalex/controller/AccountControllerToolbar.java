package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.HibernateUtil;
import it.cnr.ilc.lc.omegalex.controller.action.AccountNewAction;
import it.cnr.ilc.lc.omegalex.controller.action.AccountRemoveAction;
import it.cnr.ilc.lc.omegalex.controller.action.Action;
import it.cnr.ilc.lc.omegalex.controller.action.ActionAbort;
import it.cnr.ilc.lc.omegalex.controller.action.ActionException;
import it.cnr.ilc.lc.omegalex.domain.AccountType;
import it.cnr.ilc.lc.omegalex.manager.AccountManager;
import java.io.Serializable;
import java.util.Stack;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;

/**
 *
 * @author oakgen
 */
@ViewScoped
@Named
public class AccountControllerToolbar extends BaseController implements Serializable {

    private static final Logger LOG = LogManager.getLogger(AccountControllerToolbar.class);

    @Inject
    private LoginController loginController;
    @Inject
    private AccountControllerTable accountControllerTable;
    @Inject
    private AccountManager accountManager;

    private transient final Stack<Action> undo = new Stack<>();
    private transient final Stack<Action> redo = new Stack<>();

    public boolean isUndoDisabled() {
        return undo.isEmpty();
    }

    public boolean isRedoDisabled() {
        return redo.isEmpty();
    }

    public boolean isNewDisabled() {
        return !accountManager.hasPermission(AccountType.Permission.WRITE_ALL, AccountManager.Access.ACCOUNT, loginController.getAccount());
    }

    public boolean isRemoveDisabled() {
        return !accountManager.hasPermission(AccountType.Permission.WRITE_ALL, AccountManager.Access.ACCOUNT, loginController.getAccount())
                || accountControllerTable.isSelectionEmpty();
    }

    public void newAction() {
        log(Level.INFO, loginController.getAccount(), "create new account");
        Action action = new AccountNewAction(accountControllerTable, accountManager);
        doAction(action);
    }

    public void removeAction() {
        log(Level.INFO, loginController.getAccount(), "remove account '" + accountControllerTable.getSelection().getUsername() + "'");
        Action action = new AccountRemoveAction(accountControllerTable, accountManager, accountControllerTable.getSelection());
        doAction(action);
    }

    public void resetAction() {
        undo.clear();
        redo.clear();
    }

    public void doAction(Action action) {
        try {
            action.doAction();
            HibernateUtil.getSession().flush();
            undo.push(action);
            redo.clear();
        } catch (ActionAbort ex) {
            //  log(Level.INFO, loginController.getAccount(), "on doAction() '" + accountControllerTable.getSelection().getUsername() + "'");
            LOG.fatal("On doAction() '" + accountControllerTable.getSelection().getUsername() + "'", ex);
        } catch (HibernateException t) {
            LOG.error("On doAction() '" + accountControllerTable.getSelection().getUsername() + "'", t);
            HibernateUtil.getSession().getTransaction().rollback();
            HibernateUtil.getSession().beginTransaction();
            throw new ActionException(t);
        }

    }

    public void undoAction() {
        try {
            log(Level.INFO, loginController.getAccount(), "undo");
            Action action = undo.peek();
            action.undoAction();
            HibernateUtil.getSession().flush();
            undo.pop();
            redo.push(action);
        } catch (ActionAbort ex) {
            LOG.fatal("On undoAction() '" + accountControllerTable.getSelection().getUsername() + "'", ex);

        } catch (HibernateException t) {
            LOG.fatal("On undoAction() '" + accountControllerTable.getSelection().getUsername() + "'", t);
            HibernateUtil.getSession().getTransaction().rollback();
            HibernateUtil.getSession().beginTransaction();
            throw new ActionException(t);
        }
    }

    public void redoAction() {
        try {
            log(Level.INFO, loginController.getAccount(), "redo");
            Action action = redo.peek();
            action.redoAction();
            HibernateUtil.getSession().flush();
            redo.pop();
            undo.push(action);
        } catch (ActionAbort ex) {
            LOG.fatal("On redoAction() '" + accountControllerTable.getSelection().getUsername() + "'", ex);
        } catch (HibernateException t) {
            LOG.fatal("On redoAction() '" + accountControllerTable.getSelection().getUsername() + "'", t);
            HibernateUtil.getSession().getTransaction().rollback();
            HibernateUtil.getSession().beginTransaction();
            throw new ActionException(t);
        }
    }

}
