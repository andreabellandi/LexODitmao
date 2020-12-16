package it.cnr.ilc.lc.omegalex;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.hibernate.Session;

/**
 *
 * @author oakgen
 */
@WebFilter(urlPatterns = {"/faces/*", "/servlet/*"})
public class OmegaLexFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        File logFile;
        String logFilePath = null;
//                OmegaLexProperties.getProperty("logFilePath");
        if (logFilePath != null) {
            logFile = new File(logFilePath);
        } else {
            logFile = new File(filterConfig.getServletContext().getRealPath("/"));
            logFile = new File(logFile.getParentFile().getParentFile(), "logs/omegalex.log");
        }
        PatternLayout layout = new PatternLayout();
        String conversionPattern = "%d %p %m\n";
        layout.setConversionPattern(conversionPattern);
        DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender();
        rollingAppender.setFile(logFile.getAbsolutePath());
        rollingAppender.setDatePattern("'.'yyyy-MM-dd");
        rollingAppender.setLayout(layout);
        rollingAppender.activateOptions();
        Logger logger = Logger.getLogger("omegalex");
        logger.setLevel(Level.INFO);
        logger.addAppender(rollingAppender);
        logger.info("OmegaLex start");
        System.out.println("log file " + logFile);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HibernateUtil.getSession().beginTransaction();
            enableFilters(HibernateUtil.getSession());
            chain.doFilter(request, response);
            try {
                HibernateUtil.getSession().getTransaction().commit();
            } catch (Exception e) {
            }
        } catch (Throwable ex) {
            Logger.getLogger("omegalex").error("", ex);
            try {
                HibernateUtil.getSession().getTransaction().rollback();
            } catch (Exception e) {
            }
            ex.printStackTrace(new PrintStream(response.getOutputStream()));
        } finally {
            try {
                HibernateUtil.getSession().close();
            } catch (Exception e) {
            }
        }
    }

    private void enableFilters(Session session) {
        session.enableFilter("status");
    }

    @Override
    public void destroy() {
    }
}
