package it.cnr.ilc.lc.omegalex.controller;

import it.cnr.ilc.lc.omegalex.domain.AccountType.Permission;
import it.cnr.ilc.lc.omegalex.manager.AccountManager;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import org.apache.log4j.Level;
import javax.inject.Named;
import javax.inject.Inject;

/**
 *
 * @author oakgen
 */
@SessionScoped
@Named
public class TemplateController extends BaseController implements Serializable {

    @Inject
    private LoginController loginController;
    @Inject
    private AccountManager accountManager;

    public String homeAction() {
        log(Level.INFO, loginController.getAccount(), "navigate to home");
        return "homeView?faces-redirect=true";
    }

    public boolean isEnableAccount() {
        return accountManager.hasPermission(Permission.READ_ALL, AccountManager.Access.ACCOUNT, loginController.getAccount());
    }

    public String accountAction() {
        log(Level.INFO, loginController.getAccount(), "navigate to accounts");
        return "accountView?faces-redirect=true";
    }

    public boolean isEnableLexiconEditor() {
        return accountManager.hasPermission(Permission.WRITE_ALL, AccountManager.Access.LEXICON_EDITOR, loginController.getAccount());
    }

    public String lexiconEditorAction() {
        log(Level.INFO, loginController.getAccount(), "navigate to lexicon editor");
        return "lexiconCreationView?faces-redirect=true";
    }

    public boolean isEnableLexiconView() {
        return accountManager.hasPermission(Permission.READ_ALL, AccountManager.Access.LEXICON_VIEW, loginController.getAccount());
    }

    public String lexiconViewAction() {
        log(Level.INFO, loginController.getAccount(), "navigate to lexicon view");
        return "lexiconStatisticsView?faces-redirect=true";
    }
}
