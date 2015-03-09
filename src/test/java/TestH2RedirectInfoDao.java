import com.henko.server.dao.RedirectDao;
import com.henko.server.db.HikariConnPool;
import com.henko.server.dao.impl.DaoFactory;
import com.henko.server.db.DBManager;
import com.henko.server.model.Redirect;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.henko.server.dao.impl.DaoFactory.*;
import static junit.framework.Assert.assertEquals;

public class TestH2RedirectInfoDao {
    HikariConnPool pool = HikariConnPool.getConnPool();
    DaoFactory daoFactory = getDaoFactory(H2);
    RedirectDao redirectDao = daoFactory.getRedirectDao();

    @Before
    public void setUp() throws SQLException {
        DBManager dbManager = new DBManager();
        dbManager.dropTables();
        dbManager.initialiseDB();
        initialiseDBData();
    }

    private void initialiseDBData() throws SQLException {
        Connection conn = pool.getConnection();
        Statement stmt = conn.createStatement();

        String insert1DataSQL = "INSERT INTO REDIRECTS (URL, R_COUNT) VALUES('google.com', 10);";
        String insert2DataSQL = "INSERT INTO REDIRECTS (URL, R_COUNT) VALUES('vk.com', 20);";
        String insert3DataSQL = "INSERT INTO REDIRECTS (URL, R_COUNT) VALUES('facebook.com', 30);";

        stmt.executeUpdate(insert1DataSQL);
        stmt.executeUpdate(insert2DataSQL);
        stmt.executeUpdate(insert3DataSQL);
    }

    @Test
    public void testSelectByUrl() {
        Redirect expected = new Redirect(1, "google.com", 10);
        Redirect actual = redirectDao.getByUrl("google.com");

        assertEquals(expected, actual);
    }

    @Test
    public void testSelectAll() {
        List<Redirect> expected = new ArrayList<Redirect>() {{
            add(new Redirect(1, "google.com", 10));
            add(new Redirect(2, "vk.com", 20));
            add(new Redirect(3, "facebook.com", 30));
        }};

        List<Redirect> actual = redirectDao.getAll();

        assertEquals(expected, actual);
    }

}
