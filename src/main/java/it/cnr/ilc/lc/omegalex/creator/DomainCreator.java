package it.cnr.ilc.lc.omegalex.creator;

import it.cnr.ilc.lc.omegalex.HibernateUtil;
import it.cnr.ilc.lc.omegalex.domain.Account;
import it.cnr.ilc.lc.omegalex.domain.AccountType;
import it.cnr.ilc.lc.omegalex.manager.DomainManager;
import it.cnr.ilc.lc.omegalex.manager.AccountManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.SQLQuery;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author oakgen
 */
@WebServlet(urlPatterns = "/servlet/domainCreator")
public class DomainCreator extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(DomainCreator.class);

    private final DomainManager manager = new DomainManager();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String command = request.getParameter("command");
        if ("create".equals(command)) {
            create();
        } else if ("update".equals(command)) {
            update();
        } else if ("preset".equals(command)) {
            preset();
        } else if ("extra".equals(command)) {
            extra();
        } else if ("import".equals(command)) {
            importDocuments();
        } else {
            throw new ServletException("Unknow command");
        }
        response.getOutputStream().print("OK");
    }

    public String update() {
        Configuration configuration = new Configuration().configure();
        SchemaUpdate schemaUpdate = new SchemaUpdate(configuration);
        schemaUpdate.execute(true, true);
        return null;
    }

    public String create() {
        Configuration configuration = new Configuration().configure();
        SchemaExport schemaExport = new SchemaExport(configuration);
        schemaExport.create(true, true);
        return null;
    }

    private void preset() {
        createAccountType();
        createAdminDitmao();
        createUserDitmao();
    }

    private void createAccountType() {

        AccountType accountType = new AccountType();
        accountType.setName(AccountManager.ADMINISTRATOR);
        accountType.setColor("lightsteelblue");
        manager.insert(accountType);

        accountType = new AccountType();
        accountType.setName(AccountManager.USER);
        accountType.setColor("orangered");
        manager.insert(accountType);

//        accountType = new AccountType();
//        accountType.setName(AccountManager.REVISOR);
//        accountType.setColor("yellow");
//        manager.insert(accountType);
//
        accountType = new AccountType();
        accountType.setName(AccountManager.VIEWER);
        accountType.setColor("white");
        manager.insert(accountType);
    }

    private void createAdmin(String name, String username, String password) {
        Account account = new Account();
        account.setName(name);
        account.setUsername(username);
        account.setPassword(password);
        account.setEnabled(true);
        account.setType((AccountType) HibernateUtil.getSession().createCriteria(AccountType.class).add(Restrictions.eq("name", AccountManager.ADMINISTRATOR)).uniqueResult());
        manager.insert(account);
        HibernateUtil.getSession().flush();
        String sql = "update Account set password = upper(sha1('" + password + "')) where username = '" + username + "'";
        SQLQuery query = HibernateUtil.getSession().createSQLQuery(sql);
        query.executeUpdate();
    }

    private void createUserDitmao() {
        createUser("Emiliano Giovannetti", "emiliano", "emiliano001");
        createUser("Andrea Bellandi", "andreab", "andreab001");
        createUser("Davide Albanesi", "davide", "davide001");
        createUser("Simone Marchi", "simone", "simone001");
        createUser("Veronika", "veronika", "veronika001");
        createUser("Anja", "anja", "anja001");
        createUser("Maylin", "maylin", "maylin001");
        createUser("Julia", "julia", "julia001");
        createUser("Erminio", "erminio", "erminio001");
        createUser("Felix", "felix", "felix001");
        createUser("Andrea", "andrea", "andrea001");
        createUser("Britta", "britta", "britta001");
    }

    private void createAdminDitmao() {
        createAdmin("Maria Sofia", "sofia", "sofia001");
        createAdmin("Guido Mensching", "guido", "guido001");
        createAdmin("Gerrit Bos", "gerrit", "gerrit001");
    }

    private void createUser(String name, String username, String password) {
        Account account = new Account();
        account.setName(name);
        account.setUsername(username);
        account.setPassword(password);
        account.setEnabled(true);
        account.setType((AccountType) HibernateUtil.getSession().createCriteria(AccountType.class).add(Restrictions.eq("name", AccountManager.USER)).uniqueResult());
        manager.insert(account);
        HibernateUtil.getSession().flush();
        String sql = "update Account set password = upper(sha1('" + password + "')) where username = '" + username + "'";
        SQLQuery query = HibernateUtil.getSession().createSQLQuery(sql);
        query.executeUpdate();
    }

    private void extra() throws IOException {
        BufferedReader reader = null;
        try {
            String resource = "/sql/extra.sql";
            InputStream input = getClass().getResourceAsStream(resource);
            reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("")) {
//                    HibernateUtil.getSession().createSQLQuery(builder.toString()).executeUpdate();
                    builder = new StringBuilder();
                } else {
                    builder.append(line).append(" ");
                }
            }
            if (builder.length() > 0) {
//                HibernateUtil.getSession().createSQLQuery(builder.toString()).executeUpdate();
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    private void importDocuments() throws IOException {
        try {
            new AttestationImporter().importAttestations();
        } catch (ClassNotFoundException | SQLException ex) {
            LOG.error(ex);
            throw new IOException(ex);
        }
    }

}
