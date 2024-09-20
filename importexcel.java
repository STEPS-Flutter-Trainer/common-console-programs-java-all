package excel_operations;

import mail.Mail;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import dB.Dbb;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.util.CellRangeAddressList;

/**
 * @author Sathyajith P S
 * Student has methods to upload, download fees and students list
 */
public class Student {
	public void ImportStudent(String filepath,String user) throws Exception {
		//System.out.println("Started importing student");
		//System.out.println("Creating database connection");
		Dbb db = new Dbb();
		//System.out.println("Database connection created");
		int id =0;
		//System.out.println("Preparing query for fetching school id");
		String sql_id="SELECT Sch_id FROM project.school WHERE Sch_email=?";
		PreparedStatement get_sch_id = db.getPreparedstatement(sql_id);
		//System.out.println("Setting values for fetching school id");
		get_sch_id.setString(1, user);
		//System.out.println("Fetching resultset for school id");
		ResultSet fetched_id = get_sch_id.executeQuery();
		//System.out.println("Resultset fetched");
		while(fetched_id.next()) {
			id = fetched_id.getInt("Sch_id");
			//System.out.println("School ID saved to id");
		}
		//System.out.println("Starting Apache POI");
		FileInputStream input = new FileInputStream(filepath);
		XSSFWorkbook workbook = new XSSFWorkbook(input);
		XSSFSheet sheet = workbook.getSheetAt(0);
		Row row;
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			row = (Row) sheet.getRow(i);
			int roll = (int)row.getCell(0).getNumericCellValue();
			String name = row.getCell(1).getStringCellValue();
			String course = row.getCell(2).getStringCellValue();
			String email = row.getCell(3).getStringCellValue();

			String sql = "INSERT INTO student (Student_id,Student_name,Student_course,Student_email,Student_status,Sch_id,Par_id) VALUES(?,?,?,?,?,?,?)";
			PreparedStatement student_insert = db.getPreparedstatement(sql);
			student_insert.setInt(1, roll);
			student_insert.setString(2, name);
			student_insert.setString(3, course);
			student_insert.setString(4, email);
			student_insert.setInt(5, 1);
			student_insert.setInt(6, id);
			student_insert.setInt(7, 8);
			student_insert.execute();

			String sql_1 = "SELECT Student_pid FROM student WHERE Student_name=? and Student_Email=?";
			PreparedStatement student_id_retrieve = db.getPreparedstatement(sql_1);
			student_id_retrieve.setString(1, name);
			student_id_retrieve.setString(2, email);

			ResultSet student_id = student_id_retrieve.executeQuery();

			while (student_id.next()) {
				int retrieved_id = student_id.getInt(1);
				String sql_2 = "INSERT INTO fee(Student_id) VALUES (?)";
				PreparedStatement student_id_insert = db.getPreparedstatement(sql_2);
				student_id_insert.setInt(1, retrieved_id);
				student_id_insert.executeUpdate();
				Mail.sendParentMail(email,name,retrieved_id);
			}

		}
		/* System.out.println("Import excel to student table success"); */

	}

	public void ImportFee(String filepath) throws Exception {
		//System.out.println("Started importing fee");
		//System.out.println("Creating database connection");
		Dbb db = new Dbb();
		//System.out.println("Created database connection");
		//System.out.println("Starting Apache POI");
		FileInputStream input = new FileInputStream(filepath);
		XSSFWorkbook workbook = new XSSFWorkbook(input);
		XSSFSheet sheet = workbook.getSheetAt(0);
		Row row;
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			row = (Row) sheet.getRow(i);
			int primary_key = Integer.valueOf(row.getCell(0).getStringCellValue());
			float fee = (float) row.getCell(4).getNumericCellValue();
			Date udue = row.getCell(5).getDateCellValue();
			java.sql.Date due = new java.sql.Date(udue.getTime());

			String sql = "INSERT INTO fee (fee_id) VALUES (?) ON DUPLICATE KEY UPDATE fee_amount =?,fee_due=?";
			PreparedStatement fee_import = db.getPreparedstatement(sql);
			fee_import.setInt(1, primary_key);
			fee_import.setFloat(2, fee);
			fee_import.setDate(3, (java.sql.Date) due);
			fee_import.executeUpdate();

		}
		/* System.out.println("Import excel to fee table success"); */

	}

	public void ExportFee(String filepath,String user) throws Exception {
		//System.out.println("Started exporting fee");
		//System.out.println("Creating database connection");
		Dbb db = new Dbb();
		//System.out.println("Created database connection");
		//System.out.println("Starting Apache POI");
		String sql = "SELECT fee.fee_id,student.Student_id,student.Student_course,student.Student_name,fee.fee_amount,fee.fee_due FROM student INNER JOIN fee ON student.Student_pid=fee.Student_id RIGHT JOIN school ON student.Sch_id=school.Sch_id WHERE school.Sch_email =?";
		PreparedStatement export_fees = db.getPreparedstatement(sql);
		export_fees.setString(1, user);
		ResultSet retrieved_fee_data = export_fees.executeQuery();

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Fee Details");
		sheet.protectSheet("jLdxdJqqkZPc9DSe");
		XSSFRow rowhead = sheet.createRow((short) 0);
		rowhead.createCell((short) 0).setCellValue("Do not Edit or Delete");
		rowhead.createCell((short) 1).setCellValue("Roll No");
		rowhead.createCell((short) 2).setCellValue("Course");
		rowhead.createCell((short) 3).setCellValue("Student");
		rowhead.createCell((short) 4).setCellValue("Fee");
		rowhead.createCell((short) 5).setCellValue("Fee Due Date");

		int i = 1;

		CellStyle EditableCell = workbook.createCellStyle();
		EditableCell.setLocked(false);

		CellStyle DatecellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		short dateFormat = createHelper.createDataFormat().getFormat("yyyy-mm-dd");
		DatecellStyle.setDataFormat(dateFormat);
		DatecellStyle.setLocked(false);

		while (retrieved_fee_data.next()) {

			XSSFRow row = sheet.createRow((short) i);

			Cell FeeId = row.createCell((int) 0);
			FeeId.setCellValue(Integer.toString(retrieved_fee_data.getInt("fee_id")));

			Cell RollNo = row.createCell((int) 1);
			RollNo.setCellValue(retrieved_fee_data.getInt("Student_id"));
			RollNo.setCellStyle(EditableCell);
			Cell Course = row.createCell((short) 2);
			Course.setCellValue(retrieved_fee_data.getString("Student_course"));
			Course.setCellStyle(EditableCell);
			Cell Name = row.createCell((short) 3);
			Name.setCellValue(retrieved_fee_data.getString("Student_name"));
			Name.setCellStyle(EditableCell);
			Cell Amount = row.createCell((int) 4);
			Amount.setCellValue(retrieved_fee_data.getFloat("fee_amount"));
			Amount.setCellStyle(EditableCell);
			Cell DateCell = row.createCell(5);
			DateCell.setCellValue(retrieved_fee_data.getDate("fee_due"));
			DateCell.setCellStyle(DatecellStyle);

			i++;
		}
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		sheet.autoSizeColumn(5);
		String FileName= "Fees.xlsx";
		String SavePath = filepath+FileName;
		int lastRowNum = sheet.getLastRowNum();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

		DataValidationHelper dvHelper = sheet.getDataValidationHelper();
		DataValidationConstraint dvConstraint = dvHelper.createDateConstraint(OperatorType.BETWEEN,
				"" + DateUtil.getExcelDate(sdf.parse("1800-01-01")),
				"" + DateUtil.getExcelDate(sdf.parse("3000-12-31")), "");
		CellRangeAddressList addressList = new CellRangeAddressList(1, lastRowNum, 5, 5);
		DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);

		validation.setShowErrorBox(true);

		sheet.addValidationData(validation);
		FileOutputStream fileOut = new FileOutputStream(SavePath);
		workbook.write(fileOut);
		fileOut.close();
		//System.out.println("Ended exporting fee");
	}
}