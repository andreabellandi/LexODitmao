package it.cnr.ilc.lc.omegalex.controller.action;

import it.cnr.ilc.lc.omegalex.controller.AccountControllerTable;
import it.cnr.ilc.lc.omegalex.controller.AccountControllerTable.AccountData;
import it.cnr.ilc.lc.omegalex.domain.Account;
import it.cnr.ilc.lc.omegalex.manager.AccountManager;

/**
 *
 * @author oakgen
 */
public class AccountRemoveAction implements Action {

    private final AccountControllerTable accountControllerTable;
    private final AccountManager accountManager;
    private final AccountData selection;

    public AccountRemoveAction(AccountControllerTable accountControllerTable, AccountManager accountManager, AccountData selection) {
        this.accountControllerTable = accountControllerTable;
        this.accountManager = accountManager;
        this.selection = selection;
    }

    @Override
    public void doAction() throws ActionAbort {
        Account account = accountManager.loadAccount(selection.getId());
        accountManager.remove(account);
        accountControllerTable.remove(selection);
        accountControllerTable.warn("template.account", "account.toolbar.message.removed");
    }

    @Override
    public void undoAction() throws ActionAbort {
        accountControllerTable.warn("template.account", "template.message.unsupported");
        throw new ActionAbort();
    }

    @Override
    public void redoAction() throws ActionAbort {
        accountControllerTable.warn("template.account", "template.message.unsupported");
        throw new ActionAbort();
    }

}
