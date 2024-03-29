import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class SqlModelTest {
    
    @Test
    public void loginAs() {
        SqlModel n = new SqlModel();

        boolean result1 = n.loginAs("1001");
        assertEquals(result1, true);

        boolean result2 = n.loginAs("9001");
        assertEquals(result2, false);
    }

    @Test
    public void dropCourse() {
        SqlModel n = new SqlModel();
        int result1 = n.dropCourse("3001", "te901").getSuccess();
        assertEquals(1,result1);
        int result2 = n.dropCourse("3001", "cs901").getSuccess();
        assertEquals(0,result2);
    }

    @Test
    public void takenCourse() {
        SqlModel n = new SqlModel();

        // already exist in txn
        int result1 = n.addCourse("3001", "te901").getSuccess();

        assertEquals(0,result1);
        
        // already taken in hist
        int result1b = n.addCourse("3001", "cs101").getSuccess();
        
        assertEquals(0,result1b);
        
        // not already exist
        int result2 = n.addCourse("3009", "cs109").getSuccess();
        assertEquals(1,result2);

    }

    @Test
    public void checkDep() {
        SqlModel n = new SqlModel();

        // te919 requires te910, fufilled in txn
        boolean result3a = n.checkDependency("te919", "3001");
        assertEquals(true, result3a);

        // cs102 requires cs101, fufilled in history
        boolean result3a2 = n.checkDependency("te919", "3001");
        assertEquals(true, result3a2);

        // te919 requires te910, not fufilled
        boolean result3b = n.checkDependency("te919", "3009");
        assertEquals(false, result3b);

        // te910 requires nothing
        boolean result4 = n.checkDependency("te910", "3001");
        assertEquals(true, result4);
    }

    @Test
    public void creditCheck() {
        // user 3001 has selected 6 credit
        SqlModel n = new SqlModel();

        boolean result1 = n.checkIfCreditExceed("3001", "20");
        assertEquals(true, result1);

        boolean result2 = n.checkIfCreditExceed("3001", "3");
        assertEquals(false, result2);

        // 15+6 not bigger than 21
        // boolean result3 = n.checkIfCreditExceed("3001", "15");
        // assertEquals(false, result3);
    }

    @Test
    public void listMyCoursesDetails() {
        SqlModel n = new SqlModel();
        List<Courses> cList = n.listMyCoursesDetails("3001");
        System.out.println("3001");
        for (Courses co : cList) {
            System.out.println(co);
        }

        cList = n.listMyCoursesDetails("3002");
        System.out.println("3002");
        for (Courses co : cList) {
            System.out.println(co);
        }
    }

    @Test
    public void decisionTest() {
        // c, t equal start
        assertTrue(decision(1, 1, 1, 1));
        assertTrue(decision(1, 2, 1, 1));
        assertTrue(decision(1, 1, 1, 2));
        assertTrue(decision(1, 3, 1, 2));
        assertTrue(decision(1, 2, 1, 3));

        // c, t equal end
        assertTrue(decision(1, 3, 2, 3));
        assertTrue(decision(2, 3, 1, 3));

        // t first
        assertFalse(decision(5, 6, 2, 4));
        assertTrue(decision(5, 6, 2, 5));

        // c first
        assertFalse(decision(2, 4, 5, 5));
        assertTrue(decision(2, 5, 4, 7));

        // course cs102 collide cs101
        // user 1001 has cs101, try add cs102
        // pass dependency but fail collision
        SqlModel n = new SqlModel();
        int result1 = n.addCourse("1001", "cs102").getSuccess();
        assertEquals(result1, 0);
    }

    public boolean decision(int cStart, int cEnd, int tStart, int tEnd) {
        boolean collide = false;
        while (true) {

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

            break;
        }
        return collide;
    }

    @Test
    public void checkIfQuotaExceed() {
        SqlModel n = new SqlModel();

        // te901 is already selected by 1 person
        // te901 has 1 quota
        Courses test1 = new Courses();
        test1.setQta("1");
        test1.setCourseId("te901");

        boolean t = n.checkIfQuotaExceed(test1);
        assertTrue(t);

        // cs101 has 1/5 selected in txn
        Courses test2 = new Courses();
        test2.setQta("5");
        test2.setCourseId("cs101");

        boolean t2 = n.checkIfQuotaExceed(test2);
        assertFalse(t2);
    }
}