package medicalcenter;

import java.sql.*;
import java.util.Date;
import java.util.Formatter;

public class MedicalManagementSystem {

    public static void main(String[] args) {
        // Test the system here
    }
}

class StockLedgerEntry {
    private ResultSet rs;
    private String query;
    private Database db;

    public StockLedgerEntry() {
        rs = null;
        query = null;
        try {
            db = new Database();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet getInfo(String fld, String tbl) {
        query = "SELECT DISTINCT(" + fld + ") FROM " + tbl;
        try {
            rs = db.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public ResultSet getInfo(String fld1, String fld2, String tbl) {
        query = "SELECT " + fld1 + ", " + fld2 + " FROM " + tbl + " ORDER BY " + fld2;
        try {
            rs = db.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public ResultSet getCentralStockInfo(String viewName) {
        query = "SELECT * FROM " + viewName;
        try {
            rs = db.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public ResultSet getUserProfile(String userPk) {
        query = "SELECT full_name, employee_code, designation_name, dept_name, sex " +
                "FROM employee, employee_personal_info, employee_status, designation, department " +
                "WHERE employee_pk = employee_personal_pk_fk " +
                "AND employee_pk = employee_status_pk_fk " +
                "AND designation_pk = designation_fk " +
                "AND department_fk = dept_pk " +
                "AND employee_pk = " + userPk;
        try {
            rs = db.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public ResultSet getPrescriptionDate(String stdPk) {
        query = "SELECT std_prescription_pk, prescription_dt FROM std_prescription_info " +
                "WHERE student_pk_fk = " + stdPk + " ORDER BY prescription_dt DESC";
        try {
            rs = db.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public ResultSet getDiagnosisDetails(String presPk) {
        query = "SELECT prescription_dt, diagnosis_detail, reconsult_dt, general_advice, full_name " +
                "FROM std_prescription_info, employee_personal_info " +
                "WHERE std_prescription_pk = " + presPk + " " +
                "AND emplaoyee_pk_fk = employee_personal_pk_fk;";
        try {
            rs = db.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    public ResultSet getPatientMedicine(String presPk) {
        query = "SELECT med_type, med_com_name, med_weight, no_of_doses, day_duration, med_qty, medication_inst_text " +
                "FROM patient_med_info, medicine_gen_info, medicine_com_info, medication_inst_detail " +
                "WHERE std_prescription_fk = " + presPk + " " +
                "AND med_com_name_fk = med_com_name_pk " +
                "AND med_gen_name_fk = med_gen_name_pk " +
                "AND medication_inst_fk = medication_inst_pk " +
                "ORDER BY med_type ASC";
        try {
            rs = db.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }
}

class Database {
    private Connection con = null;
    private Statement st;
    private ResultSet rs;
    private final String dbName = "central_db";
    private final String userName = "root";
    private final String password = "admin";
    private final String host = "localhost";
    private final String url = "jdbc:mysql://" + host + "/" + dbName;

    public Database() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            con = DriverManager.getConnection(url, userName, password);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ResultSet executeQuery(String query) throws SQLException {
        st = con.createStatement();
        rs = st.executeQuery(query);
        return rs;
    }

    public void executeProcedure(String procName, int paramValue) throws SQLException {
        String query = "{ CALL " + procName + "( ? ) }";
        CallableStatement cs = con.prepareCall(query);
        cs.setInt(1, paramValue);
        cs.execute();
    }

    public void executeProcedure(String procName, int paramValue1, int paramValue2) throws SQLException {
        String query = "{ CALL " + procName + "( ? , ? ) }";
        CallableStatement cs = con.prepareCall(query);
        cs.setInt(1, paramValue1);
        cs.setInt(2, paramValue2);
        cs.execute();
    }

    public ResultSet select(String tbl) throws SQLException {
        return select(tbl, "*", "", "");
    }

    public ResultSet select(String tbl, String fld) throws SQLException {
        return select(tbl, fld, "", "");
    }

    public ResultSet select(String tbl, String fld, String cnd) throws SQLException {
        return select(tbl, fld, " WHERE " + cnd, "");
    }

    public ResultSet select(String tbl, String fld, String cnd, String ord) throws SQLException {
        if (!ord.equals("")) {
            ord = " ORDER BY " + ord;
        }
        String query = "SELECT " + fld + " FROM " + tbl + cnd + ord;
        st = con.createStatement();
        rs = st.executeQuery(query);
        return rs;
    }

    public void closeStatement() throws SQLException {
        st.close();
    }

    public void insert(String tbl, String fld, String values) throws SQLException {
        if (!fld.equals("")) {
            fld = "( " + fld + " ) ";
        }
        String query = "INSERT INTO " + tbl + fld + " VALUES (" + values + ")";
        st = con.createStatement();
        st.execute(query);
    }

    public void insert(String tbl, String[] fld, String[] values) throws SQLException {
        String temp1 = "", temp2 = "";
        temp1 += fld[0];
        temp2 += values[0];
        for (int i = 1; i < fld.length; i++) {
            temp1 = temp1 + ", " + fld[i];
            temp2 = temp2 + ", " + values[i];
        }
        String query = "INSERT INTO " + tbl + "(" + temp1 + ")" + " VALUES(" + temp2 + ")";
        st = con.createStatement();
        st.execute(query);
    }

    public void storeCache(String code, byte[] data) throws SQLException {
        String query = "INSERT INTO cache(code, data) VALUES(?, ?)";
        PreparedStatement pst = con.prepareStatement(query);
        pst.setString(1, code);
        pst.setBytes(2, data);
        pst.executeUpdate();
        con.commit();
        pst.close();
    }

    public byte[] getCache(String code) throws SQLException {
        String query = "SELECT data FROM cache WHERE code = '" + code + "'";
        st = con.createStatement();
        rs = st.executeQuery(query);
        byte[] output = null;
        if (rs.next()) {
            output = rs.getBytes("data");
        }
        return output;
    }

    public void insert(String tbl, String[] fld, String[][] rows) throws SQLException {
        String temp1 = "", temp2 = "";
        for (int i = 0; i < fld.length - 1; i++) {
            temp1 += fld[i] + ", ";
            temp2 += "?, ";
        }
        temp1 += fld[fld.length - 1];
        temp2 += "?";
        String query = "INSERT INTO " + tbl + "(" + temp1 + ")" + " VALUES(" + temp2 + ")";
        PreparedStatement pst = con.prepareStatement(query);

        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < rows[i].length; j++) {
                pst.setInt(j + 1, Integer.parseInt(rows[i][j]));
            }
            pst.addBatch();
        }
        pst.executeBatch();
        pst.close();
    }

    public void insert2(String tbl, String[] fld, String[][] rows) throws SQLException {
        String temp1 = "", temp2 = "";
        for (int i = 0; i < fld.length - 1; i++) {
            temp1 += fld[i] + ", ";
            temp2 += "?, ";
        }
        temp1 += fld[fld.length - 1];
        temp2 += "?";
        String query = "INSERT INTO " + tbl + "(" + temp1 + ")" + " VALUES(" + temp2 + ")";
        PreparedStatement pst = con.prepareStatement(query);

        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < rows[i].length; j++) {
                if (j == 1 || j == 6) {
                    pst.setString(j + 1, rows[i][j]);
                } else if (
