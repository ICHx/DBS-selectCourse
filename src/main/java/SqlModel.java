import java.io.File;
import java.nio.file.*;
import java.util.List;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

public class SqlModel {
    private Sql2o sqlObj = null;

    // initialize connection
    SqlModel() {
        init();
    }

    SqlModel(File db) {
        init(db);
    }

    public void init() {
        File f = new File("db/main.db");
        this.init(f);
    }

    public void init(File f) {
        if (!f.exists()) {
            System.err.println("E: Sqlite DB not found.");
            System.err.println(f.getAbsolutePath());
            System.err.println("Usage: java -jar ... [filepath to db].");
            System.err.println("Default db: ./db/main.db");
            System.exit(1);
        }

        boolean isAppEngine = System.getProperty("com.google.appengine.runtime.version") != null;

        try {
            if (isAppEngine) {
                //  ! copy db to /tmp, ammend f.
                System.err.println("I: Running on Google App Engine.");
                var dest = Paths.get("/tmp/target.db");
                Files.copy(f.toPath(), dest);
                f = dest.toFile();
                System.err.println("I: Copied File to /tmpfs");
                
                // ! debug off
                Main.DEBUG=0;
            }

            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + f.getAbsolutePath();
            this.sqlObj = new Sql2o(url, "DataStore", "");
            System.err.println("I: Connection to SQLite established. " + url);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static boolean containsCourse(final List<? extends RecordEntry> list, final String name) {
        // ?ref:
        // https://stackoverflow.com/questions/18852059/java-list-containsobject-with-field-value-equal-to-x
        return list.stream().anyMatch(o -> o.getCourseId().equals(name));
    }

    public Sql2o getSqlObj() {
        return sqlObj;
    }

    public List<Courses> listCourses() {
        // list all available courses
        try (Connection c = sqlObj.open()) {
            String cmd = "select * from Courses";
            Query q = c.createQuery(cmd);
            return q.executeAndFetch(Courses.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Courses listCourses(String cid) {
        // list all available courses
        try (Connection c = sqlObj.open()) {
            String cmd = "select * from Courses where courseid == :cid";
            Query q = c.createQuery(cmd);
            q.addParameter("cid", cid);

            return q.executeAndFetchFirst(Courses.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Txn> listMyCourses(String netID) {
        // list selected courses in txn
        try (Connection c = sqlObj.open()) {
            String cmd = "select * from Txn t where t.NETID == :netID";
            Query q = c.createQuery(cmd);
            q.addParameter("netID", netID);
            q.throwOnMappingFailure(false);
            List<Txn> t = q.executeAndFetch(Txn.class);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Courses> listMyCoursesDetails(String netID) {
        // list selected courses in txn and return course details
        try (Connection c = sqlObj.open()) {
            String cmd = "select c.* from Courses c, txn t where t.courseid == c.courseid and t.netid==:netID";
            Query q = c.createQuery(cmd);
            q.addParameter("netID", netID);
            q.throwOnMappingFailure(false);
            List<Courses> t = q.executeAndFetch(Courses.class);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<History> listMyHistory(String netID) {
        // list previously taken courses

        try (Connection c = sqlObj.open()) {
            String cmd = "select * from History h where h.NETID == :netID";
            Query q = c.createQuery(cmd);
            q.addParameter("netID", netID);
            q.throwOnMappingFailure(false);
            List<History> t = q.executeAndFetch(History.class);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean loginAs(String netID) {
        boolean result = false;
        List<Users> l = null;
        netID = netID.trim();

        try (Connection c = sqlObj.open()) {
            String cmd = "select * from Users where Users.netid == :ID";
            Query q = c.createQuery(cmd);

            q.addParameter("ID", netID);
            l = q.executeAndFetch(Users.class);
        }
        if (!l.isEmpty())
            result = true;

        return result;
    }

    public Response addCourse(String netId, String courseId) {
        int statusCode = -1;
        StringBuilder message = new StringBuilder();

        Courses target = listCourses(courseId); // supposedly only 1 result

        do { /* Do-While false loop */
            // A. search if have already taken in history or txn
            boolean taken = false;
            List<Txn> tlist = listMyCourses(netId);
            taken = containsCourse(tlist, courseId);
            List<History> hlist = listMyHistory(netId);

            taken |= containsCourse(hlist, courseId);

            // pass-fail check currently not planned
            if (taken) {
                // taken, end add
                statusCode = 0;
                message.append(courseId + " already taken by " + netId + "\n");
                break;
            } else {
                statusCode = 1;
                message.append(courseId + " not taken by " + netId + "\n");
            }

            // B. check dependency for course
            boolean fufillDep = checkDependency(courseId, netId);

            if (fufillDep) {
                statusCode = 1;
                message.append(courseId + " dependencies fufilled by " + netId + "\n");
            } else {
                statusCode = 0;
                message.append(courseId + " dependencies not fufilled by " + netId + "\n");
                break;
            }

            // C. try to add the course, checkings
            // ? over credit limit?
            boolean overlimit = checkIfCreditExceed(netId, target.getCred());

            if (overlimit) {
                statusCode = 0;
                message.append(netId + " exceed credit limit. " + "\n");
                break;
            } else {
                statusCode = 1;
                message.append(netId + " pass credit limit check " + "\n");
            }

            // ? is collide?
            boolean collide = checkCollision(target, netId);

            if (collide) {
                statusCode = 0;
                message.append(courseId + " collides with other courses selected by " + netId + "\n");
                break;
            } else {
                statusCode = 1;
                message.append(courseId + " fits with other courses selected by " + netId + "\n");
            }

            // ? is quota full? (still allow register, but inform user)
            boolean quotaFull = checkIfQuotaExceed(target);
            if (quotaFull) {
                statusCode = 2;
                message.append(courseId + " courses quota full " + netId + "\n");
                message.append("Registration may be revoked " + "\n");
            } else {
                statusCode = 1;
                message.append(courseId + " courses still have quota " + netId + "\n");
            }

            // ! real add
            try (Connection c = sqlObj.beginTransaction(1)) {
                // delete
                String cmd2 = "insert into txn (courseid, netid) values (:CID , :NETID)";
                Query q2 = c.createQuery(cmd2);
                q2.addParameter("NETID", netId);
                q2.addParameter("CID", courseId);
                q2.executeUpdate();

                message.append("Added " + courseId + " for " + netId);

                if (Main.DEBUG > 0) {
                    List<Txn> newTxns = c.createQuery("select * from txn").executeAndFetch(Txn.class);
                    System.out.println("before rollback");
                    for (Txn txn : newTxns) {
                        System.out.println(txn.toString());
                    }
                    c.rollback(); // todo : debug only
                } else {
                    c.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
                message.append(e.getMessage());
                statusCode = -2;
            } finally {
                Connection c = sqlObj.open();
                if (Main.DEBUG > 0) {
                    List<Txn> newTxns = c.createQuery("select * from txn").executeAndFetch(Txn.class);
                    System.out.println("after all (rollback)");
                    for (Txn txn : newTxns) {
                        System.out.println(txn.toString());
                    }
                    System.out.println("-----------------");
                }
            }

        } while (false);

        if (Main.DEBUG > 0) {
            System.out.println(message);
        }
        return new Response(statusCode, message.toString());
    }

    public boolean checkIfQuotaExceed(Courses target) {
        int maxQuota = Integer.parseInt(target.getQta());
        int count = 0;
        // if count +1 < maxQuota, true

        try (Connection c = sqlObj.open()) {
            String cmd = " select count(txnno) as code from txn where courseID ==:cid ";
            Query q = c.createQuery(cmd);
            q.addParameter("cid", target.getCourseId());

            // get list of selected course (credit)
            sqlFeedback t = q.executeAndFetchFirst(sqlFeedback.class);
            count = t.getCode() + 1;

            System.out.println("count +1 =" + count);
            System.out.println("maxQuota =" + maxQuota);

            return count > maxQuota;

        } catch (Exception e) {
            e.printStackTrace();
            return true;
            // just prevent add
        }

    }

    private boolean checkCollision(Courses target, String netId) {
        String targetWeekday = target.getWeekday();
        final int tStart = Integer.parseInt(targetWeekday);
        final int tEnd = Integer.parseInt(target.getEnd());

        boolean collide = false;
        List<Courses> cList = listMyCoursesDetails(netId);

        for (Courses current : cList) {
            if (!current.getWeekday().equals(targetWeekday)) {
                continue;
            }
            int cStart = Integer.parseInt(current.getStart());
            int cEnd = Integer.parseInt(current.getEnd());

            if (Main.DEBUG > 0) {
                System.out.println();
                System.out.println(tStart + " " + tEnd);
                System.out.println(cStart + " " + cEnd);
            }

            /* if start to end overlap, it is true */

            if (cStart >= tStart) {
                // case 1: t start first

                // cStart > tEnd -> no collision

                if (cStart <= tEnd) {
                    // c starts before t end
                    collide = true;
                    break;
                }

                if (cEnd <= tEnd) {
                    // c end before t
                    // c is contained in t
                    collide = true;
                    break;
                }
            } else {
                // case 2: c start first

                if (cEnd >= tStart) {
                    // c end after t start
                    // c cuts t
                    collide = true;
                    break;
                }
            }
        }

        return collide;
    }

    public boolean checkIfCreditExceed(String netid, String targetCred) {

        int sum = 0;

        try (Connection c = sqlObj.open()) {
            String cmd = "select cred from courses c inner join txn on c.courseid = txn.courseid where txn.netid== :netID";
            Query q = c.createQuery(cmd);
            q.addParameter("netID", netid);

            // get list of selected course (credit)
            List<sqlFeedback> t = q.executeAndFetch(sqlFeedback.class);

            for (sqlFeedback sqlFeedback : t) {
                sum += sqlFeedback.getCred();
            }
            sum += Integer.parseInt(targetCred);

            System.out.println("cred sum=" + sum);

            return sum > Main.MAXCREDIT;

        } catch (Exception e) {
            e.printStackTrace();
            return true;
            // just prevent add, return "exceed"
        }

    }

    public boolean checkDependency(String courseId, String netId) {
        // find preq
        try (Connection c = sqlObj.open()) {
            String cmd = "select preq from courses where courseid == :cid  and preq != ''";
            Query q1 = c.createQuery(cmd);
            q1.addParameter("cid", courseId);
            List<sqlFeedback> preq = q1.executeAndFetch(sqlFeedback.class);
            if (preq.isEmpty()) {
                // no need dep, fufilled.
                return true;
            }

            // ? check if satisfied, in either past or current records

            String cmd2 = "select * from txn where courseid==(select preq from courses where courseid == :cid) and netid == :netid";

            Query q2 = c.createQuery(cmd2);
            q2.addParameter("cid", courseId);
            q2.addParameter("netid", netId);

            List<Txn> result = q2.executeAndFetch(Txn.class);
            boolean depTxn = !result.isEmpty();

            // ----------------------------------------------
            String cmd3 = "select * from history where courseid==(select preq from courses where courseid == :cid) and netid == :netid";

            Query q3 = c.createQuery(cmd3);
            q3.addParameter("cid", courseId);
            q3.addParameter("netid", netId);

            List<History> result2 = q3.executeAndFetch(History.class);
            boolean depHist = !result2.isEmpty();

            // ----------------------------------------------

            return depTxn || depHist;
        }

    }

    public Response dropCourse(String netId, String courseId) {
        int statusCode = -1;

        StringBuilder message = new StringBuilder();

        // In order to open a transaction, we use the beginTransaction method instead of
        // the open method

        // this ensures atomicity in ACID
        try (Connection c = sqlObj.beginTransaction(1)) {

            // A. search if exist in txn
            // as xxx to match the pojo field name
            String cmd = "SELECT EXISTS( SELECT * FROM TXN WHERE (TXN.NETID == :NETID AND TXN.COURSEID == :CID)) as code";
            Query q = c.createQuery(cmd);
            q.addParameter("NETID", netId).addParameter("CID", courseId);
            // pojo class created to receive values
            sqlFeedback statusC = q.executeAndFetchFirst(sqlFeedback.class);
            statusCode = statusC.getCode();

            // B. remove
            if (statusCode == 1) {
                // exist
                String msg = courseId + " chosen by " + netId + "\n";
                System.out.println(msg);

                // delete
                String cmd2 = "delete from txn where (TXN.NETID == :NETID AND TXN.COURSEID == :CID)";
                Query q2 = c.createQuery(cmd2);
                q2.addParameter("NETID", netId).addParameter("CID", courseId);
                q2.executeUpdate();

                message.append("Dropped " + msg);
            } else {
                // not exist
                statusCode = 0;
                String msg = courseId + " not chosen by " + netId + "\n";
                System.out.println(msg);
                message.append(msg);
            }

            if (Main.DEBUG > 0) {
                List<Txn> newTxns = c.createQuery("select * from txn").executeAndFetch(Txn.class);
                System.out.println("before rollback");
                for (Txn txn : newTxns) {
                    System.out.println(txn.toString());
                }
                c.rollback(); // todo : debug only
            } else {
                c.commit();
            }

            // fail, throw exception, reason

        } catch (Exception e) {
            e.printStackTrace();
            message.append(e.getMessage());
            statusCode = -2;
        } finally {
            Connection c = sqlObj.open();
            if (Main.DEBUG > 0) {
                List<Txn> newTxns = c.createQuery("select * from txn").executeAndFetch(Txn.class);
                System.out.println("after all (rollback)");
                for (Txn txn : newTxns) {
                    System.out.println(txn.toString());
                }
                System.out.println("-----------------");
            }
        }

        return new Response(statusCode, message.toString());
    }

}
